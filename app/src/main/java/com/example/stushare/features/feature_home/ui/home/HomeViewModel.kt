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
// ⭐️ XÓA: import kotlinx.coroutines.flow.SharingStarted
// ⭐️ XÓA: import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
// ⭐️ BƯỚC 1: IMPORT FIREBASE AUTH ⭐️
import com.google.firebase.auth.FirebaseAuth

data class HomeUiState(
    // ⭐️ BƯỚC 2: THÊM USERNAME VÀO STATE, XÓA AVATAR HARDCODE ⭐️
    val userName: String = "Tên Sinh Viên",
    val avatarUrl: String? = null, // Cho phép null
    val newDocuments: List<Document> = emptyList(),
    val examDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val getNewDocumentsUseCase: GetNewDocumentsUseCase,
    private val getExamDocumentsUseCase: GetExamDocumentsUseCase,
    private val settingsRepository: SettingsRepository,
    // ⭐️ BƯỚC 3: INJECT FIREBASE AUTH ⭐️
    private val firebaseAuth: FirebaseAuth //
) : ViewModel() {

    // 1. TÀI SẢN HỖ TRỢ CHÍNH (UI State)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ⭐️ BƯỚC 4: XÓA BỎ STATEFLOW CỦA USERNAME (VÌ ĐÃ ĐƯA VÀO UI STATE) ⭐️
    /*
    val userNameState: StateFlow<String> = settingsRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Tên Sinh Viên"
        )
    */

    init {
        // ⭐️ BƯỚC 5: GỌI HÀM LẤY THÔNG TIN NGƯỜI DÙNG ⭐️
        loadUserProfile()
        collectDocumentFlows()
        refreshData(isInitialLoad = true)
    }

    /**
     * ⭐️ HÀM MỚI: Lấy thông tin người dùng từ FirebaseAuth
     */
    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                userName = currentUser.displayName ?: "Tên Sinh Viên",
                avatarUrl = currentUser.photoUrl?.toString()
            )
        }
    }

    /**
     * Lắng nghe thay đổi từ các Use Case (Database)
     */
    private fun collectDocumentFlows() {
        // (Hàm này giữ nguyên)
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
        // (Hàm này giữ nguyên)
        viewModelScope.launch {
            try {
                if (!isInitialLoad) {
                    _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null, isLoading = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }

                repository.refreshDocumentsIfStale()

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> e.message ?: "Kiểm tra kết nối mạng."
                    is DataFailureException.ApiError -> "Lỗi máy chủ (${e.code}). Vui lòng thử lại sau."
                    else -> "Tải dữ liệu thất bại: Lỗi không xác định."
                }
                _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            } finally {
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
        // (Hàm này giữ nguyên)
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}