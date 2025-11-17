// File: core/domain/usecase/GetExamDocumentsUseCase.kt
package com.example.stushare.core.domain.usecase

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // Cần import map
import javax.inject.Inject

// ⭐️ IMPORT HẰNG SỐ MỚI ⭐️
import com.example.stushare.core.utils.AppConstants

class GetExamDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Document>> {
        // LỌC LOGIC ĐÃ ĐƯỢC CHUYỂN TỪ VIEWMODEL XUỐNG ĐÂY
        return repository.getAllDocuments().map { allDocuments ->
            // ⭐️ THAY ĐỔI: Dùng hằng số thay vì chuỗi "Tài Liệu" ⭐️
            allDocuments.filter { it.type == AppConstants.TYPE_EXAM_PREP }
        }
    }
}