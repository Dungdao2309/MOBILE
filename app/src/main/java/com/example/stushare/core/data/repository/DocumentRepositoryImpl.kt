package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.toDocumentEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val CACHE_DURATION_MS = 15 * 60 * 1000 // 15 phút

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()

    override fun getDocumentById(documentId: String): Flow<Document> = documentDao.getDocumentById(documentId)

    override suspend fun searchDocuments(query: String): List<Document> = documentDao.searchDocuments(query)

    override fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)

    // =========================================================================
    // 1. LOGIC UPLOAD CẢI TIẾN (Đã sửa authorId)
    // =========================================================================
    // Lưu ý: Hãy đảm bảo Interface của bạn cũng có tham số 'mimeType' nhé!
    override suspend fun uploadDocument(title: String, description: String, fileUri: Uri, mimeType: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Lấy ID người dùng hiện tại (Quan trọng để sau này lọc bài đăng)
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
                val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

                // 1. Xác định đuôi file
                val extension = when (mimeType) {
                    "application/pdf" -> "pdf"
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                    else -> "bin"
                }

                // 2. Upload file
                val fileName = "documents/${UUID.randomUUID()}.$extension"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(fileUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 3. Lưu Metadata vào Firestore
                val documentMap = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "fileUrl" to downloadUrl,
                    "type" to extension,
                    "uploadedAt" to System.currentTimeMillis(),
                    "downloads" to 0,
                    "authorName" to currentUserName,
                    "authorId" to currentUserId // ✅ Đã mở khóa dòng này
                )

                // Lưu vào Firestore và lấy ID
                firestore.collection("documents").add(documentMap).await()

                Result.success("Upload thành công!")
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // =========================================================================
    // 2. LOGIC LẤY BÀI ĐĂNG CỦA TÔI (Mới thêm)
    // =========================================================================
    override fun getDocumentsByAuthor(authorId: String): Flow<List<Document>> = callbackFlow {
        val query = firestore.collection("documents")
            .whereEqualTo("authorId", authorId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val docs = snapshot.documents.mapNotNull { doc ->
                    // Map dữ liệu Firestore sang Document Model của bạn
                    // Lưu ý: Firestore ID là String, còn Document Model ID của bạn có thể là Long.
                    // Ở đây mình tạm dùng hashCode() cho ID Long, nhưng quan trọng là hiển thị đúng Title/Type
                    val data = doc.data
                    if (data != null) {
                        Document(
                            id = (data["uploadedAt"] as? Long) ?: System.currentTimeMillis(), // Dùng timestamp làm ID tạm
                            title = data["title"] as? String ?: "No Title",
                            type = data["type"] as? String ?: "pdf",
                            imageUrl = "",
                            downloads = (data["downloads"] as? Long)?.toInt() ?: 0,
                            rating = 0.0,
                            author = data["authorName"] as? String ?: "Me",
                            courseCode = "",
                            // ⚠️ QUAN TRỌNG: Bạn nên thêm field 'firestoreId' (String) vào Model Document
                            // để sau này xóa cho dễ. Tạm thời mình map thế này để App chạy được đã.
                        ).apply {
                            // Nếu class Document của bạn là Data Class, bạn không thể gán id string vào đây
                            // Logic xóa sẽ cần xử lý khéo ở ViewModel hoặc sửa lại Model sau.
                        }
                    } else null
                }
                trySend(docs)
            }
        }
        awaitClose { listener.remove() }
    }

    // =========================================================================
    // 3. LOGIC XÓA BÀI ĐĂNG (Mới thêm)
    // =========================================================================
    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        return try {
            // Xóa document trên Firestore dựa vào ID (String)
            // Lưu ý: ID truyền vào đây phải là ID chuỗi của Firestore (Vd: "ABCxyz...")
            // chứ không phải ID Long (12345...)
            firestore.collection("documents").document(documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // =========================================================================
    // 4. CÁC HÀM KHÁC GIỮ NGUYÊN
    // =========================================================================
    override suspend fun refreshDocumentsIfStale() {
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastRefresh) > CACHE_DURATION_MS || lastRefresh == 0L) {
            try { refreshDocuments() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override suspend fun refreshDocuments() {
        withContext(Dispatchers.IO) {
            try {
                val networkDocuments = apiService.getAllDocuments()
                val databaseDocuments = networkDocuments.map { it.toDocumentEntity() }
                documentDao.replaceAllDocuments(databaseDocuments)
                settingsRepository.updateLastRefreshTimestamp()
            } catch (e: Exception) {
                throw when (e) {
                    is IOException -> DataFailureException.NetworkError
                    is HttpException -> DataFailureException.ApiError(e.code())
                    else -> DataFailureException.UnknownError(e.message)
                }
            }
        }
    }

    override suspend fun insertDocument(document: Document) {
        withContext(Dispatchers.IO) {
            documentDao.insertDocument(document)
        }
    }
}