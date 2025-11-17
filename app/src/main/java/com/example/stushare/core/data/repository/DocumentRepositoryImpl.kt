// File: core/data/repository/DocumentRepositoryImpl.kt
// (Đã cập nhật logic Caching)

package com.example.stushare.core.data.repository

import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.network.models.toDocumentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first // ⭐️ IMPORT MỚI
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// ⭐️ ĐỊNH NGHĨA THỜI GIAN CACHE (ví dụ: 15 phút)
// 15 (phút) * 60 (giây) * 1000 (mili giây)
private const val CACHE_DURATION_MS = 15 * 60 * 1000

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository // ⭐️ 1. THÊM VÀO CONSTRUCTOR
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments()
    }

    override fun getDocumentById(documentId: String): Flow<Document> {
        return documentDao.getDocumentById(documentId)
    }

    override suspend fun searchDocuments(query: String): List<Document> {
        return documentDao.searchDocuments(query)
    }

    override fun getDocumentsByType(type: String): Flow<List<Document>> {
        return documentDao.getDocumentsByType(type)
    }

    /**
     * ⭐️ 2. HÀM MỚI (ViewModels sẽ gọi hàm này)
     * Chỉ gọi API nếu dữ liệu đã cũ (stale)
     */
    override suspend fun refreshDocumentsIfStale() {
        // Lấy mốc thời gian từ DataStore
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()

        // Kiểm tra xem đã quá 15 phút chưa
        val isStale = (System.currentTimeMillis() - lastRefresh) > CACHE_DURATION_MS

        // Nếu dữ liệu đã cũ (hoặc là lần đầu, lastRefresh = 0L), thì gọi API
        if (isStale || lastRefresh == 0L) {
            try {
                refreshDocuments() // Gọi hàm refresh thật
            } catch (e: Exception) {
                // Nếu refresh thất bại (ví dụ: mất mạng),
                // chúng ta không làm gì cả. Ứng dụng sẽ tự động
                // dùng dữ liệu cũ trong Room (hỗ trợ offline).
                e.printStackTrace()
                // Ném lại lỗi để ViewModel (ví dụ HomeViewModel) có thể
                // tắt vòng xoay "isRefreshing"
                throw e
            }
        }
        // else: Dữ liệu còn mới (< 15 phút), không cần làm gì. Ứng dụng sẽ đọc từ Room.
    }

    /**
     * ⭐️ 3. HÀM CŨ ĐƯỢC CẬP NHẬT
     * Hàm này giờ sẽ LUÔN gọi API và cập nhật mốc thời gian
     */
    override suspend fun refreshDocuments() {
        try {
            // 1. Gọi API
            val networkDocuments = apiService.getAllDocuments()

            // 2. Chuyển đổi
            val databaseDocuments = networkDocuments.map { it.toDocumentEntity() }

            // 3. Xóa dữ liệu cũ và chèn dữ liệu mới (để đảm bảo sạch)
            // (Nếu DAO của bạn đã có onConflict = REPLACE, bạn có thể bỏ qua deleteAll)
            documentDao.deleteAllDocuments() // <-- Thêm dòng này
            documentDao.insertAllDocuments(databaseDocuments)

            // 4. CẬP NHẬT MỐC THỜI GIAN (Rất quan trọng)
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