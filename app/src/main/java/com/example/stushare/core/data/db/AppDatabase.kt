// File: core/data/db/AppDatabase.kt
// (PHIÊN BẢN ĐÚNG - ĐÃ XÓA HILT)

package com.example.stushare.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.DocumentRequest

@Database(
    entities = [
        Document::class,
        DocumentRequest::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun requestDao(): RequestDao

    // ĐẢM BẢO KHÔNG CÓ companion object nào chứa getInstance() hay @Provides Ở ĐÂY

}