package com.example.stushare.core.data.repository

import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.network.models.toDocumentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val CACHE_DURATION_MS = 15 * 60 * 1000 // 15 phút

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()

    override fun getDocumentById(documentId: String): Flow<Document> = documentDao.getDocumentById(documentId)

    override suspend fun searchDocuments(query: String): List<Document> = documentDao.searchDocuments(query)

    override fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)

    override suspend fun refreshDocumentsIfStale() {
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        val isStale = (currentTime - lastRefresh) > CACHE_DURATION_MS

        if (isStale || lastRefresh == 0L) {
            try {
                refreshDocuments()
            } catch (e: Exception) {
                e.printStackTrace()
                // Không throw lỗi ở đây để UI vẫn hiện dữ liệu cũ (cache) nếu có
            }
        }
    }

    override suspend fun refreshDocuments() {
        try {
            // 1. Gọi API
            val networkDocuments = apiService.getAllDocuments()

            // 2. Chuyển đổi
            val databaseDocuments = networkDocuments.map { it.toDocumentEntity() }

            // 3. CẬP NHẬT DB (Dùng Transaction để tránh màn hình trắng xóa)
            // Hàm này sẽ xóa cũ và chèn mới trong cùng 1 giao dịch
            documentDao.replaceAllDocuments(databaseDocuments)

            // 4. Lưu thời gian
            settingsRepository.updateLastRefreshTimestamp()

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