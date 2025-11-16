package com.example.stushare.features.feature_document_detail.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.utils.DownloadHelper // <-- 1. IMPORT DOWNLOAD HELPER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Trạng thái UiState (Không đổi)
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val document: Document) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val repository: DocumentRepository, // <-- 2. INJECT REPOSITORY
    private val downloadHelper: DownloadHelper // <-- 3. INJECT DOWNLOAD HELPER VÀO CONSTRUCTOR
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Lắng nghe tài liệu dựa trên ID (được gọi từ Screen)
    fun getDocumentById(documentId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading

            repository.getDocumentById(documentId).collect { documentFromDb ->
                if (documentFromDb == null) {
                    _uiState.value = DetailUiState.Error("Không tìm thấy tài liệu")
                } else {
                    _uiState.value = DetailUiState.Success(documentFromDb)
                }
            }
        }
    }

    // 4. HÀM MỚI: BẮT ĐẦU TẢI VỀ
    fun startDownload(url: String, title: String) {
        // Gọi DownloadHelper để bắt đầu tải file
        downloadHelper.downloadFile(url, title)
        // (Bạn có thể thêm logic báo thành công/thất bại ở đây)
    }
}