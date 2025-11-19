package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction // ⭐️ Import Transaction
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("DELETE FROM documents")
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
     */
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

    /**
     * ⭐️ CẢI TIẾN: Dùng @Transaction để đảm bảo an toàn dữ liệu.
     * Hàm này sẽ chạy trong một giao dịch duy nhất: Xóa cũ -> Thêm mới.
     * Nếu có lỗi xảy ra, nó sẽ rollback (hoàn tác) để không bị mất dữ liệu.
     */
    @Transaction
    suspend fun replaceAllDocuments(documents: List<Document>) {
        deleteAllDocuments()
        insertAllDocuments(documents)
    }
}