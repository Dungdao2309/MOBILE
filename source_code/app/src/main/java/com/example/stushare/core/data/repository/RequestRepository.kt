package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    // Lấy danh sách yêu cầu (Real-time)
    fun getAllRequests(): Flow<List<DocumentRequest>>

    // Tạo yêu cầu mới
    suspend fun createRequest(title: String, subject: String, description: String)

    // Lấy chi tiết 1 yêu cầu
    fun getRequestById(requestId: String): Flow<DocumentRequest?>

    // Lấy danh sách bình luận (Chat) của yêu cầu
    fun getCommentsForRequest(requestId: String): Flow<List<CommentEntity>>

    // Gửi bình luận/trả lời
    suspend fun addCommentToRequest(requestId: String, content: String)

    // 🟢 MỚI: Cập nhật trạng thái Hoàn thành
    suspend fun updateRequestStatus(requestId: String, isSolved: Boolean): Result<Unit>
}