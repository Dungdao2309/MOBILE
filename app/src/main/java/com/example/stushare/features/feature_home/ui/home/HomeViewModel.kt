package com.example.stushare.features.feature_home.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.domain.usecase.GetExamDocumentsUseCase
import com.example.stushare.core.domain.usecase.GetNewDocumentsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Sinh Viên",
    val avatarUrl: String? = null,
    val newDocuments: List<Document> = emptyList(),
    val examDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = false,
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

    // State riêng cho loading/error để dễ quản lý
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // ⭐️ LOGIC MỚI: Dùng combine để gộp các luồng dữ liệu lại
    // Khi Database thay đổi (Repository phát tín hiệu) -> UseCase phát -> combine nhận được -> UI update
    val uiState: StateFlow<HomeUiState> = combine(
        getNewDocumentsUseCase(),   // Luồng 1: Tài liệu mới
        getExamDocumentsUseCase(),  // Luồng 2: Đề thi
        _isLoading,                 // Luồng 3: Trạng thái loading
        _errorMessage               // Luồng 4: Lỗi
    ) { newDocs, examDocs, isLoading, error ->

        // Lấy thông tin user (đơn giản hóa, có thể chuyển sang Flow nếu muốn realtime profile)
        val currentUser = firebaseAuth.currentUser
        val name = currentUser?.displayName ?: "Sinh Viên"
        val avatar = currentUser?.photoUrl?.toString()

        HomeUiState(
            userName = name,
            avatarUrl = avatar,
            newDocuments = newDocs,
            examDocuments = examDocs,
            isLoading = isLoading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Chỉ chạy khi UI hiển thị, tiết kiệm pin
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        // Tải dữ liệu ban đầu
        refreshData(isInitialLoad = true)
    }

    fun refreshData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            _errorMessage.value = null
            if (isInitialLoad) _isLoading.value = true

            try {
                // Gọi Repository để sync dữ liệu từ Cloud/API về Local DB
                // Khi Local DB có dữ liệu mới, Flow ở trên (combine) sẽ tự chạy lại
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = when (e) {
                    is DataFailureException.NetworkError -> "Vui lòng kiểm tra kết nối mạng."
                    else -> "Không thể cập nhật dữ liệu mới nhất."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}