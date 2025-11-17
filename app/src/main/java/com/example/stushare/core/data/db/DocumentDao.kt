package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

@Dao // Đánh dấu đây là Data Access Object
interface DocumentDao {
    @Query("DELETE FROM documents") // <-- Tên bảng của bạn
    suspend fun deleteAllDocuments()
    /**
     * Lấy TẤT CẢ tài liệu từ database.
     * Trả về một Flow, tự động cập nhật UI khi data thay đổi.
     */
    @Query("SELECT * FROM documents")
    fun getAllDocuments(): Flow<List<Document>>

    /**
     * Lấy MỘT tài liệu bằng ID.
     * Trả về một Flow để tự động cập nhật nếu chi tiết tài liệu thay đổi.
     */
    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun getDocumentById(documentId: String): Flow<Document>

    /**
     * Thêm hoặc Cập nhật (ghi đè) một tài liệu vào database.
     * Đây là một suspend function (hàm tạm ngưng) để chạy bất đồng bộ.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    /**
     * Tìm kiếm tài liệu dựa trên title (tiêu đề) hoặc type (loại).
     * Đây là thao tác 1 lần (không cần Flow) nên dùng suspend fun.
     */
    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%'")
    suspend fun searchDocuments(query: String): List<Document>
    @Query("SELECT * FROM documents WHERE type = :type")
    fun getDocumentsByType(type: String): Flow<List<Document>>
    // (Bạn có thể thêm các hàm @Update và @Delete ở đây nếu cần)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDocuments(documents: List<Document>)
}