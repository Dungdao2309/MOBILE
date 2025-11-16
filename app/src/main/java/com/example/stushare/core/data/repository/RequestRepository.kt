package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.DocumentRequest
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    fun getAllRequests(): Flow<List<DocumentRequest>>
    suspend fun refreshRequests()
    suspend fun createRequest(title: String, subject: String, description: String)
}