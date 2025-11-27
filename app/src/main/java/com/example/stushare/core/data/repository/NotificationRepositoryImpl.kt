package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.NotificationDao
import com.example.stushare.core.data.models.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications()

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

    override suspend fun addNotification(title: String, message: String, type: String) {
        withContext(Dispatchers.IO) {
            val notification = NotificationEntity(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type
            )
            notificationDao.insertNotification(notification)
        }
    }

    override suspend fun markAsRead(id: Long) {
        withContext(Dispatchers.IO) {
            notificationDao.markAsRead(id)
        }
    }

    override suspend fun deleteNotification(id: Long) {
        withContext(Dispatchers.IO) {
            notificationDao.deleteNotification(id)
        }
    }
}