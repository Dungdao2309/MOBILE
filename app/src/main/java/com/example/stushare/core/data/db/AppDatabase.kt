package com.example.stushare.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.NotificationEntity
import com.example.stushare.core.data.models.UserEntity // ⭐️ Import bảng User

@Database(
    entities = [
        Document::class,           // Bảng Tài liệu
        NotificationEntity::class, // Bảng Thông báo
        UserEntity::class          // ⭐️ Bảng Người dùng (cho Bảng xếp hạng)
    ],
    version = 3, // ⭐️ QUAN TRỌNG: Tăng version lên 3 để tránh lỗi crash
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Các hàm lấy DAO để Repository sử dụng
    abstract fun documentDao(): DocumentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao // ⭐️ DAO cho User

}