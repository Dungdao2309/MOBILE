package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Query("SELECT * FROM documents")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun getDocumentById(documentId: String): Flow<Document>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Query(
        "SELECT * FROM documents " +
                "WHERE (title LIKE '%' || :query || '%' COLLATE NOCASE COLLATE NOACCENT) " +
                "OR (type LIKE '%' || :query || '%' COLLATE NOCASE COLLATE NOACCENT)"
    )
    suspend fun searchDocuments(query: String): List<Document>

    @Query("SELECT * FROM documents WHERE type = :type")
    fun getDocumentsByType(type: String): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDocuments(documents: List<Document>)

    @Transaction
    suspend fun replaceAllDocuments(documents: List<Document>) {
        deleteAllDocuments()
        insertAllDocuments(documents)
    }

    // ⭐️ THÊM HÀM NÀY CHO LEADERBOARD
    // Lấy 10 tài liệu có lượt tải cao nhất
    @Query("SELECT * FROM documents ORDER BY downloads DESC LIMIT 10")
    fun getTopDocuments(): Flow<List<Document>>
}