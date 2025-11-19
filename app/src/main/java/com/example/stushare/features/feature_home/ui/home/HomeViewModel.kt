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
import kotlinx.coroutines.flow.update // ⭐️ QUAN TRỌNG: Import update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth

data class HomeUiState(
    val userName: String = "Tên Sinh Viên",
    val avatarUrl: String? = null,
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
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        collectDocumentFlows()
        refreshData(isInitialLoad = true)
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // ⭐️ CẬP NHẬT AN TOÀN
            _uiState.update { currentState ->
                currentState.copy(
                    userName = currentUser.displayName ?: "Tên Sinh Viên",
                    avatarUrl = currentUser.photoUrl?.toString()
                )
            }
        }
    }

    private fun collectDocumentFlows() {
        viewModelScope.launch {
            getNewDocumentsUseCase().collect { newDocs ->
                // ⭐️ CẬP NHẬT AN TOÀN
                _uiState.update { it.copy(newDocuments = newDocs) }
            }
        }

        viewModelScope.launch {
            getExamDocumentsUseCase().collect { examDocs ->
                // ⭐️ CẬP NHẬT AN TOÀN
                _uiState.update { it.copy(examDocuments = examDocs) }
            }
        }
    }

    fun refreshData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            try {
                // ⭐️ CẬP NHẬT AN TOÀN: BẮT ĐẦU TẢI
                _uiState.update { currentState ->
                    currentState.copy(
                        isRefreshing = !isInitialLoad,
                        isLoading = isInitialLoad,
                        errorMessage = null
                    )
                }

                repository.refreshDocumentsIfStale()

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> e.message ?: "Kiểm tra kết nối mạng."
                    is DataFailureException.ApiError -> "Lỗi máy chủ (${e.code}). Vui lòng thử lại sau."
                    else -> "Tải dữ liệu thất bại: Lỗi không xác định."
                }
                // ⭐️ CẬP NHẬT AN TOÀN: LỖI
                _uiState.update { it.copy(errorMessage = errorMessage) }
            } finally {
                // ⭐️ CẬP NHẬT AN TOÀN: KẾT THÚC
                _uiState.update {
                    it.copy(isRefreshing = false, isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        // ⭐️ CẬP NHẬT AN TOÀN
        _uiState.update { it.copy(errorMessage = null) }
    }
}