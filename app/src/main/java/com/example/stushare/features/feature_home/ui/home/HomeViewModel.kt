// File: HomeViewModel.kt (Đã cập nhật logic Caching ⭐️)

package com.example.stushare.features.feature_home.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.domain.usecase.GetExamDocumentsUseCase
import com.example.stushare.core.domain.usecase.GetNewDocumentsUseCase
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val avatarUrl: String = "https://i.imgur.com/4z3316U.png",
    val newDocuments: List<Document> = emptyList(),
    val examDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    // ⭐️ LƯU Ý: Đảm bảo đây là interface "DocumentRepository"
    private val repository: DocumentRepository,
    private val getNewDocumentsUseCase: GetNewDocumentsUseCase,
    private val getExamDocumentsUseCase: GetExamDocumentsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // (Tất cả code từ 1 đến 2 và hàm init/collectDocumentFlows giữ nguyên)
    // 1. TÀI SẢN HỖ TRỢ CHÍNH (UI State)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 2. STATEFLOW CHO USER NAME (Từ DataStore)
    val userNameState: StateFlow<String> = settingsRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Tên Sinh Viên"
        )

    init {
        collectDocumentFlows()
        refreshData(isInitialLoad = true)
    }

    /**
     * Lắng nghe thay đổi từ các Use Case (Database)
     */
    private fun collectDocumentFlows() {
        viewModelScope.launch {
            getNewDocumentsUseCase().collect { newDocs ->
                _uiState.value = _uiState.value.copy(
                    newDocuments = newDocs
                )
            }
        }

        viewModelScope.launch {
            getExamDocumentsUseCase().collect { examDocs ->
                _uiState.value = _uiState.value.copy(
                    examDocuments = examDocs
                )
            }
        }
    }

    /**
     * ⭐️ HÀM ĐÃ ĐƯỢC CẬP NHẬT ⭐️
     * Gọi Repository để làm mới dữ liệu (API -> ROOM)
     */
    fun refreshData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isInitialLoad) {
                    _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null, isLoading = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }

                // ⭐️ THAY ĐỔI DUY NHẤT:
                // Đã thay thế hàm refresh() cũ bằng hàm refreshIfStale() mới.
                // Giờ đây, API sẽ CHỈ được gọi nếu đã quá 15 phút.
                repository.refreshDocumentsIfStale()

            } catch (e: Exception) {
                e.printStackTrace()

                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> e.message ?: "Kiểm tra kết nối mạng."
                    is DataFailureException.ApiError -> "Lỗi máy chủ (${e.code}). Vui lòng thử lại sau."
                    else -> "Tải dữ liệu thất bại: Lỗi không xác định."
                }

                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            } finally {
                // (Phần finally giữ nguyên)
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Xóa thông báo lỗi (Side effect)
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}