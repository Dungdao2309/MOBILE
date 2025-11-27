package com.example.stushare.features.feature_notification.ui

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Định nghĩa Model cho UI (để hiển thị thời gian đẹp hơn)
data class NotificationUiModel(
    val id: Long,
    val title: String,
    val message: String,
    val timeDisplay: String, // Chuỗi hiển thị thời gian (vd: "5 phút trước")
    val type: String,
    val isRead: Boolean
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    // 2. Lấy dữ liệu từ Repository và chuyển đổi sang UI Model
    val notifications: StateFlow<List<NotificationUiModel>> = repository.getNotifications()
        .map { entities ->
            entities.map { entity ->
                NotificationUiModel(
                    id = entity.id,
                    title = entity.title,
                    message = entity.message,
                    // Logic chuyển timestamp -> "vừa xong", "1 giờ trước"
                    timeDisplay = convertTimestampToRelativeTime(entity.timestamp),
                    type = entity.type,
                    isRead = entity.isRead
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Hàm đánh dấu đã đọc
    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    // Hàm xóa thông báo
    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Logic định dạng thời gian (giữ lại từ code của bạn)
    private fun convertTimestampToRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        return try {
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                now,
                DateUtils.SECOND_IN_MILLIS
            ).toString()
        } catch (e: Exception) {
            "Vừa xong"
        }
    }
}