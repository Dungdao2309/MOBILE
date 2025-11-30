package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.toDocumentEntity
import com.example.stushare.core.utils.AppConstants
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

    // =========================================================================
    // 1. DATA STREAMING
    // =========================================================================
    override fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()

    override fun getDocumentById(documentId: String): Flow<Document> = documentDao.getDocumentById(documentId)

    override fun searchDocuments(query: String): Flow<List<Document>> = documentDao.searchDocuments(query)

    override fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)

    override suspend fun insertDocument(document: Document) {
        documentDao.insertDocument(document)
    }

    // =========================================================================
    // 2. LOGIC UPLOAD (CẢI TIẾN: Ảnh bìa + Tác giả)
    // =========================================================================
    override suspend fun uploadDocument(
        title: String,
        description: String,
        fileUri: Uri,
        mimeType: String,
        coverUri: Uri?, // 📸 Ảnh bìa
        author: String  // ✍️ Tên tác giả sách
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserId = currentUser?.uid ?: return@withContext Result.failure(Exception("Bạn chưa đăng nhập!"))
                // Người upload (Uploader)
                val uploaderName = currentUser.displayName ?: "Ẩn danh"

                // --- A. Upload File Tài Liệu ---
                val docExtension = when (mimeType) {
                    "application/pdf" -> "pdf"
                    "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                    else -> "bin"
                }
                val docFileName = "documents/${UUID.randomUUID()}.$docExtension"
                val docRef = storage.reference.child(docFileName)
                docRef.putFile(fileUri).await()
                val fileDownloadUrl = docRef.downloadUrl.await().toString()

                // --- B. Upload Ảnh Bìa (Nếu có) ---
                var imageDownloadUrl = ""
                if (coverUri != null) {
                    val imageFileName = "covers/${UUID.randomUUID()}.jpg"
                    val imageRef = storage.reference.child(imageFileName)
                    imageRef.putFile(coverUri).await()
                    imageDownloadUrl = imageRef.downloadUrl.await().toString()
                } else {
                    // Nếu không chọn ảnh, dùng ảnh random đẹp mắt
                    imageDownloadUrl = "https://picsum.photos/seed/${System.currentTimeMillis()}/200/300"
                }

                // --- C. Chuẩn bị dữ liệu ---
                val newId = UUID.randomUUID().toString()
                val fixedType = AppConstants.TYPE_BOOK

                // 1. Map lưu lên Cloud (Firestore)
                val documentMap = hashMapOf(
                    "id" to newId,
                    "title" to title,
                    "description" to description,
                    "fileUrl" to fileDownloadUrl,
                    "imageUrl" to imageDownloadUrl,
                    "type" to fixedType,
                    "uploadedAt" to System.currentTimeMillis(),
                    "downloads" to 0,
                    "rating" to 5.0,

                    // ⭐️ QUAN TRỌNG: Phân biệt "Tác giả sách" và "Người upload"
                    "author" to author,             // Tên tác giả sách (User nhập)
                    "authorName" to uploaderName,   // Tên người upload (Lấy từ nick)
                    "authorId" to currentUserId,    // ID người upload

                    "courseCode" to "GEN"
                )

                // 2. Lưu lên Firestore
                firestore.collection("documents").document(newId).set(documentMap).await()

                // 3. Lưu xuống Local (Room)
                val newLocalDocument = Document(
                    id = newId,
                    title = title,
                    type = fixedType,
                    imageUrl = imageDownloadUrl,
                    fileUrl = fileDownloadUrl,
                    downloads = 0,
                    rating = 5.0,

                    // Lưu ý: Trường 'author' trong Local DB sẽ hiển thị ở UI
                    // Ta lưu "Tên tác giả sách" để hiển thị đúng ý người dùng
                    author = author,

                    courseCode = "GEN",
                    authorId = currentUserId
                )
                documentDao.insertDocument(newLocalDocument)

                Result.success("Đăng thành công!")
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // =========================================================================
    // 3. LOGIC LẤY BÀI ĐĂNG CỦA TÔI
    // =========================================================================
    override fun getDocumentsByAuthor(authorId: String): Flow<List<Document>> = callbackFlow {
        val query = firestore.collection("documents").whereEqualTo("authorId", authorId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val docs = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    // Ưu tiên lấy field "author" (Tác giả sách).
                    // Nếu là bài cũ chưa có field này thì fallback về "authorName" (Người up)
                    val bookAuthor = data["author"] as? String ?: data["authorName"] as? String ?: "Unknown"

                    Document(
                        id = doc.getString("id") ?: doc.id,
                        title = data["title"] as? String ?: "No Title",
                        type = data["type"] as? String ?: "Sách",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        fileUrl = data["fileUrl"] as? String ?: "",
                        downloads = (data["downloads"] as? Number)?.toInt() ?: 0,
                        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,

                        author = bookAuthor, // Hiển thị tác giả sách

                        courseCode = data["courseCode"] as? String ?: "",
                        authorId = data["authorId"] as? String
                    )
                }
                trySend(docs)
            }
        }
        awaitClose { listener.remove() }
    }

    // =========================================================================
    // 4. LOGIC XÓA BÀI ĐĂNG
    // =========================================================================
    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("documents").document(documentId).delete().await()
                documentDao.deleteDocumentById(documentId)
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // =========================================================================
    // 5. SYNC DATA
    // =========================================================================
    override suspend fun refreshDocuments() {
        withContext(Dispatchers.IO) {
            try {
                // A. Mock API
                val mockDocs = try {
                    apiService.getAllDocuments().map { it.toDocumentEntity() }
                } catch (e: Exception) { emptyList() }

                // B. Firestore
                val firestoreSnapshot = firestore.collection("documents")
                    .orderBy("uploadedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get().await()

                val firestoreDocs = firestoreSnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    // Lấy Tác giả sách (ưu tiên) hoặc Người up
                    val bookAuthor = data["author"] as? String ?: data["authorName"] as? String ?: ""

                    Document(
                        id = doc.getString("id") ?: doc.id,
                        title = data["title"] as? String ?: "",
                        type = data["type"] as? String ?: "Sách",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        fileUrl = data["fileUrl"] as? String ?: "",
                        downloads = (data["downloads"] as? Number)?.toInt() ?: 0,
                        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,

                        author = bookAuthor,

                        courseCode = "KHTN",
                        authorId = data["authorId"] as? String
                    )
                }

                documentDao.insertAllDocuments(mockDocs)
                documentDao.insertAllDocuments(firestoreDocs)

                settingsRepository.updateLastRefreshTimestamp()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun refreshDocumentsIfStale() {
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastRefresh) > CACHE_DURATION_MS || lastRefresh == 0L) {
            refreshDocuments()
        }
    }
}