package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    fun getAllDocuments(): Flow<List<Document>>

    fun getDocumentById(documentId: String): Flow<Document>

    fun searchDocuments(query: String): Flow<List<Document>>

    fun getDocumentsByType(type: String): Flow<List<Document>>

    suspend fun insertDocument(document: Document)

    suspend fun refreshDocuments()

    suspend fun refreshDocumentsIfStale()

    // 🔴 CẬP NHẬT HÀM UPLOAD: Thêm tham số ảnh bìa và tên tác giả
    suspend fun uploadDocument(
        title: String,
        description: String,
        fileUri: Uri,
        mimeType: String,
        // 👇 THÊM 2 THAM SỐ MỚI
        coverUri: Uri?, // Cho phép null (dấu ?)
        author: String
    ): Result<String>

    fun getDocumentsByAuthor(authorId: String): Flow<List<Document>>

    suspend fun deleteDocument(documentId: String): Result<Unit>
}