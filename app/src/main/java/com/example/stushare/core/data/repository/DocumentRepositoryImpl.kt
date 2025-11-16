package com.example.stushare.core.data.repository

import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.Document
// import com.example.stushare.core.data.network.models.toDocumentEntity (Không cần import này ở hàm search nữa)
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.network.models.toDocumentEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val apiService: ApiService
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments()
    }

    override fun getDocumentById(documentId: String): Flow<Document> {
        return documentDao.getDocumentById(documentId)
    }

    // --- [SỬA ĐỔI QUAN TRỌNG TẠI ĐÂY] ---
    override suspend fun searchDocuments(query: String): List<Document> {
        // Thay vì gọi API, chúng ta tìm trong Database cục bộ (nơi dữ liệu vừa được tải về)
        // Điều này giúp tận dụng câu lệnh "LIKE" bạn đã viết trong DAO
        return documentDao.searchDocuments(query)
    }
    // ------------------------------------

    override fun getDocumentsByType(type: String): Flow<List<Document>> {
        return documentDao.getDocumentsByType(type)
    }

    // Hàm này giữ nguyên: Lấy từ API -> Lưu vào DB
    override suspend fun refreshDocuments() {
        try {
            // 1. Gọi API lấy danh sách (DTO)
            val networkDocuments = apiService.getAllDocuments()

            // 2. Chuyển đổi DTO -> Entity
            // LƯU Ý: Bạn cần kiểm tra file chứa hàm toDocumentEntity() (Xem Bước 2 bên dưới)
            val databaseDocuments = networkDocuments.map { it.toDocumentEntity() }

            // 3. Lưu vào DB
            documentDao.insertAllDocuments(databaseDocuments)
        } catch (e: Exception) {
            throw when (e) {
                is IOException -> DataFailureException.NetworkError
                is HttpException -> DataFailureException.ApiError(e.code())
                else -> DataFailureException.UnknownError(e.message)
            }
        }
    }

    override suspend fun insertDocument(document: Document) {
        documentDao.insertDocument(document)
    }
}