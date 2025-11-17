// File: core/data/db/AppDatabase.kt
// (⭐️ ĐÃ CẬP NHẬT - XÓA REQUEST ⭐️)

package com.example.stushare.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stushare.core.data.models.Document
// ⭐️ XÓA: import com.example.stushare.core.data.models.DocumentRequest

@Database(
    entities = [
        Document::class
        // ⭐️ XÓA: DocumentRequest::class
    ],
    version = 2, // (Bạn có thể cần tăng version nếu Room yêu cầu)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    // ⭐️ XÓA: abstract fun requestDao(): RequestDao

    // ĐẢM BẢO KHÔNG CÓ companion object nào chứa getInstance() hay @Provides Ở ĐÂY

}