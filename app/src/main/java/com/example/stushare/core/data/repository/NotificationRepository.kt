package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // Lấy danh sách thông báo
    fun getNotifications(): Flow<List<NotificationEntity>>

    // Lấy số lượng tin chưa đọc (để hiện chấm đỏ)
    fun getUnreadCount(): Flow<Int>

    // Thêm thông báo mới
    suspend fun addNotification(title: String, message: String, type: String)

    // Đánh dấu đã đọc
    suspend fun markAsRead(id: Long)

    // Xóa thông báo
    suspend fun deleteNotification(id: Long)
}