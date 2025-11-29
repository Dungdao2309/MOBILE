package com.example.stushare.features.feature_upload.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _uploadEvent = MutableSharedFlow<UploadResult>()
    val uploadEvent = _uploadEvent.asSharedFlow()

    sealed class UploadResult {
        data class Success(val message: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }

    // ⭐️ CẬP NHẬT: Thêm tham số mimeType
    fun handleUploadClick(title: String, description: String, fileUri: Uri?, mimeType: String) {
        if (fileUri == null) {
            viewModelScope.launch {
                _uploadEvent.emit(UploadResult.Error("Vui lòng chọn file tài liệu!"))
            }
            return
        }

        if (title.isBlank()) {
            viewModelScope.launch {
                _uploadEvent.emit(UploadResult.Error("Vui lòng nhập tiêu đề!"))
            }
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            try {
                // Truyền mimeType xuống Repository
                val result = documentRepository.uploadDocument(title, description, fileUri, mimeType)

                if (result.isSuccess) {
                    _uploadEvent.emit(UploadResult.Success("Upload thành công!"))
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    _uploadEvent.emit(UploadResult.Error("Lỗi: $errorMsg"))
                }

            } catch (e: Exception) {
                _uploadEvent.emit(UploadResult.Error(e.message ?: "Đã xảy ra lỗi khi upload"))
            } finally {
                _isUploading.value = false
            }
        }
    }
}