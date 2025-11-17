package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.DocumentRequest
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    fun getAllRequests(): Flow<List<DocumentRequest>>

    // ⭐️ HÀM NÀY ĐÃ BỊ XÓA ⭐️
    // suspend fun refreshRequests()
    // (Không cần nữa vì Firestore là real-time)

    suspend fun createRequest(title: String, subject: String, description: String)
}