// File: core/domain/usecase/GetNewDocumentsUseCase.kt
package com.example.stushare.core.domain.usecase

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // Cần import map
import javax.inject.Inject

class GetNewDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    /**
     * Hàm invoke giúp gọi Use Case như một hàm (getNewDocumentsUseCase()).
     */
    operator fun invoke(): Flow<List<Document>> {
        // LỌC LOGIC ĐÃ ĐƯỢC CHUYỂN TỪ VIEWMODEL XUỐNG ĐÂY
        return repository.getAllDocuments().map { allDocuments ->
            allDocuments.filter { it.type == "Sách" }
        }
    }
}