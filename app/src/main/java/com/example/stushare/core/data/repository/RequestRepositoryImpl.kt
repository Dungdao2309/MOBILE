package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.RequestDao
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.RequestDto
import com.example.stushare.core.data.network.models.toRequestEntity
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class RequestRepositoryImpl @Inject constructor(
    private val requestDao: RequestDao,
    private val apiService: ApiService
) : RequestRepository {

    override fun getAllRequests(): Flow<List<DocumentRequest>> {
        // Luôn lắng nghe Database
        return requestDao.getAllRequests()
    }

    override suspend fun refreshRequests() {
        // Lấy từ API và lưu vào DB
        try {
            val networkRequests = apiService.getAllRequests()
            requestDao.insertAllRequests(networkRequests.map { it.toRequestEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Tạo một yêu cầu mới.
     * (ĐÃ CẢI TIẾN ĐỂ BỎ QUA API VÌ my-json-server KHÔNG HỖ TRỢ POST)
     */
    override suspend fun createRequest(title: String, subject: String, description: String) {

        // 1. Tạo một Entity (Database object) MỚI
        val newLocalRequest = DocumentRequest(
            id = UUID.randomUUID().toString(), // Tạo ID ngẫu nhiên
            title = "$title ($subject)", // Ghép title và subject
            authorName = "Người Dùng Hiện Tại" // (Giả)
        )

        // 2. LƯU THẲNG VÀO DATABASE (Bỏ qua API)
        try {
            // Logic này sẽ khiến Flow ở RequestListViewModel tự động cập nhật!
            requestDao.insertRequest(newLocalRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /* // ----- KHỐI CODE GỌI API (TẠM THỜI TẮT) -----
        // (Khi có API thật, bạn hãy mở khối này ra và xóa code "LƯU THẲNG" ở trên)

        val newRequestDto = RequestDto(
            id = UUID.randomUUID().toString(),
            title = "$title ($subject)",
            authorName = "Người Dùng Hiện Tại"
        )

        try {
            // 1. Gửi (POST) lên API
            val createdDto = apiService.createRequest(newRequestDto)
            // 2. Lưu kết quả trả về vào Database
            requestDao.insertRequest(createdDto.toRequestEntity())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        */
    }
}