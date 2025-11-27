package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

/**
 * Interface (Giao diện) cho Kho chứa Tài liệu.
 * ViewModel sẽ "nói chuyện" với cái này,
 * mà không cần biết dữ liệu đến từ DAO hay API.
 */
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
        fileUri: android.net.Uri
    ): Result<String>
}