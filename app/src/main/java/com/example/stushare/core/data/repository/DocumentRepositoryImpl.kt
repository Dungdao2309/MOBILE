package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.toDocumentEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    // ⭐️ MỚI: Inject Firebase vào đây
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()

    override fun getDocumentById(documentId: String): Flow<Document> = documentDao.getDocumentById(documentId)

    override suspend fun searchDocuments(query: String): List<Document> = documentDao.searchDocuments(query)

    override fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)

    // --- LOGIC UPLOAD MỚI (QUAN TRỌNG) ---
    override suspend fun uploadDocument(title: String, description: String, fileUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Tạo tên file duy nhất (vd: documents/abc-xyz.pdf)
                val fileName = "documents/${UUID.randomUUID()}.pdf"
                val storageRef = storage.reference.child(fileName)

                // 2. Upload file lên Firebase Storage
                storageRef.putFile(fileUri).await()

                // 3. Lấy link tải xuống (Download URL)
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 4. Tạo dữ liệu metadata để lưu Firestore
                val documentMap = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "fileUrl" to downloadUrl,
                    "type" to "pdf", // Mặc định là PDF
                    "uploadedAt" to System.currentTimeMillis(),
                    "downloads" to 0,
                    "authorName" to "User", // TODO: Lấy tên thật từ Auth
                    // "authorId" to FirebaseAuth.getInstance().currentUser?.uid
                )

                // 5. Gửi lên Firestore (Collection "documents")
                firestore.collection("documents").add(documentMap).await()

                // (Tùy chọn) 6. Lưu tạm vào Room để UI cập nhật ngay lập tức mà không cần tải lại từ mạng
                // Lưu ý: ID ở đây là random Long tạm thời vì Room dùng Long, còn Firestore dùng String
                val localDoc = Document(
                    id = System.currentTimeMillis(),
                    title = title,
                    type = "pdf",
                    imageUrl = "", // Icon mặc định
                    downloads = 0,
                    rating = 0.0,
                    author = "Me",
                    courseCode = "NEW"
                )
                documentDao.insertDocument(localDoc)

                Result.success("Upload thành công!")
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshDocumentsIfStale() {
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        val isStale = (currentTime - lastRefresh) > CACHE_DURATION_MS

        if (isStale || lastRefresh == 0L) {
            try {
                refreshDocuments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun refreshDocuments() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Gọi API (Cũ)
                val networkDocuments = apiService.getAllDocuments()
                val databaseDocuments = networkDocuments.map { it.toDocumentEntity() }

                // 2. Cập nhật DB
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