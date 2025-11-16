// File: HomeViewModel.kt (Đã sửa lỗi logic isLoading)

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
    // SỬA LỖI: Đặt isLoading = true làm giá trị mặc định
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val getNewDocumentsUseCase: GetNewDocumentsUseCase,
    private val getExamDocumentsUseCase: GetExamDocumentsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // 1. TÀI SẢN HỖ TRỢ CHÍNH (UI State)
    // Giờ đây nó sẽ tự động bắt đầu với isLoading = true
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
        // Gọi refreshData với isInitialLoad = true
        // Hàm này giờ sẽ chịu trách nhiệm set isLoading = false
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
                    // ⭐️ SỬA LỖI: Đã xóa "isLoading = false" khỏi đây
                )
            }
        }

        viewModelScope.launch {
            getExamDocumentsUseCase().collect { examDocs ->
                _uiState.value = _uiState.value.copy(
                    examDocuments = examDocs
                    // ⭐️ SỬA LỖI: Đã xóa "isLoading = false" khỏi đây
                )
            }
        }
    }

    /**
     * Gọi Repository để làm mới dữ liệu (API -> ROOM)
     */
    fun refreshData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isInitialLoad) {
                    _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null, isLoading = true)
                } else {
                    // Đảm bảo cờ isLoading là true khi bắt đầu tải lần đầu
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }

                // (Gợi ý Cải tiến 1: nên gọi hàm refreshDocumentsIfStale() ở đây)
                repository.refreshDocuments()

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
                // ⭐️ SỬA LỖI QUAN TRỌNG:
                // Chỉ sau khi refresh (thành công hoặc thất bại)
                // chúng ta mới được phép tắt các cờ loading.
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    isLoading = false // <-- Dòng này sẽ tắt Skeleton UI
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