package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

/**
 * Interface định nghĩa các hành vi thao tác dữ liệu.
 * Đã được phân nhóm rõ ràng để dễ implement.
 */
interface DocumentRepository {

    // ==========================================
    // 1. TRUY VẤN DỮ LIỆU (READ)
    // ==========================================

    fun getAllDocuments(): Flow<List<Document>>

    fun getDocumentById(documentId: String): Flow<Document>

    fun searchDocuments(query: String): Flow<List<Document>>

    /**
     * Lấy tài liệu theo phân loại.
     * Dùng cho tính năng: "Tài liệu ôn thi" (exam_review), "Bài giảng", v.v.
     */
    fun getDocumentsByType(type: String): Flow<List<Document>>

    fun getDocumentsByAuthor(authorId: String): Flow<List<Document>>

    fun getBookmarkedDocuments(): Flow<List<Document>>


    // ==========================================
    // 2. ĐỒNG BỘ DỮ LIỆU (SYNC)
    // ==========================================

    /**
     * Force refresh: Bắt buộc tải lại từ Server.
     * Trả về Result để ViewModel biết thành công hay thất bại.
     */
    suspend fun refreshDocuments(): Result<Unit>

    /**
     * Smart refresh: Chỉ tải lại nếu dữ liệu đã cũ (hết hạn cache).
     */
    suspend fun refreshDocumentsIfStale()


    // ==========================================
    // 3. TÁC VỤ DỮ LIỆU (WRITE/UPDATE)
    // ==========================================

    suspend fun insertDocument(document: Document)

    suspend fun uploadDocument(
        title: String,
        description: String,
        fileUri: Uri,
        mimeType: String,
        coverUri: Uri?,
        author: String,
        type: String // 🟢 THÊM THAM SỐ NÀY
    ): Result<String>

    suspend fun deleteDocument(documentId: String): Result<Unit>

    // Cập nhật: Thêm Result để xử lý trường hợp mất mạng khi đếm lượt tải
    suspend fun incrementDownloadCount(documentId: String, authorId: String?, docTitle: String): Result<Unit>


    // ==========================================
    // 4. TƯƠNG TÁC NGƯỜI DÙNG (USER ACTIONS)
    // ==========================================

    // --- Bookmark (Lưu trữ) ---
    suspend fun isDocumentBookmarked(documentId: String): Result<Boolean>

    suspend fun toggleBookmark(documentId: String, isBookmarked: Boolean): Result<Unit>

    // --- Comment (Bình luận) ---
    fun getComments(documentId: String): Flow<List<CommentEntity>>

    suspend fun sendComment(documentId: String, content: String): Result<Unit>

    suspend fun deleteComment(documentId: String, commentId: String): Result<Unit>

    // --- Rating (Đánh giá) ---
    suspend fun rateDocument(documentId: String, rating: Int): Result<Unit>
}