package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    fun getAllDocuments(): Flow<List<Document>>

    fun getDocumentById(documentId: String): Flow<Document>

    suspend fun searchDocuments(query: String): List<Document>

    fun getDocumentsByType(type: String): Flow<List<Document>>

    suspend fun insertDocument(document: Document)

    suspend fun refreshDocuments()
    suspend fun refreshDocumentsIfStale()

    suspend fun uploadDocument(
        title: String,
        description: String,
        fileUri: android.net.Uri,
        mimeType: String // <--- Thêm cái này vào
    ): Result<String>

    // 👇 THÊM 2 HÀM NÀY ĐỂ PROFILE VIEWMODEL KHÔNG BỊ LỖI
    fun getDocumentsByAuthor(authorId: String): Flow<List<Document>>

    suspend fun deleteDocument(documentId: String): Result<Unit>
}