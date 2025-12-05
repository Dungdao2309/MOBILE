package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- 1. CÁC DATA MODEL CẦN THIẾT (Nếu chưa có file riêng thì dùng tạm ở đây) ---

// Model cho Dashboard
data class AdminUiState(
    val userCount: String = "0",
    val documentCount: String = "0",
    val requestCount: String = "0"
)

// Model cho Report (Thêm cái này để AdminReportScreen hết lỗi)
data class Report(
    val id: String,
    val documentId: String,
    val documentTitle: String,
    val reason: String,
    val reporterEmail: String,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {

    // ==========================================================
    // PHẦN 1: DASHBOARD
    // ==========================================================
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // ==========================================================
    // PHẦN 2: USER MANAGEMENT (QUẢN LÝ USER)
    // ==========================================================
    private val _rawUsersList = MutableStateFlow<List<UserEntity>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoadingUsers = MutableStateFlow(false)

    val searchQuery = _searchQuery.asStateFlow()
    val isLoadingUsers = _isLoadingUsers.asStateFlow()

    // Logic tìm kiếm User
    val usersList: StateFlow<List<UserEntity>> = combine(_rawUsersList, _searchQuery) { users, query ->
        if (query.isBlank()) users
        else users.filter {
            it.fullName.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================================
    // PHẦN 3: REPORT MANAGEMENT (QUẢN LÝ BÁO CÁO VI PHẠM) - MỚI THÊM
    // ==========================================================

    // List báo cáo
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    // Trạng thái xử lý (loading) chung cho màn Report
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Kênh thông báo (Toast)
    private val _toastMessage = Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

    init {
        loadDashboardStats()
        loadReports() // Tải dữ liệu báo cáo giả lập
    }

    // --- LOGIC DASHBOARD ---
    private fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(userCount = "120", documentCount = "45", requestCount = "12") }
        }
    }

    // --- LOGIC USER ---
    fun loadUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            delay(1000)
            _rawUsersList.value = listOf(
                UserEntity("1", "Nguyễn Văn A", "a@gmail.com", isLocked = false),
                UserEntity("2", "Trần Thị B", "b@gmail.com", isLocked = true),
                UserEntity("3", "Lê C", "c@gmail.com", isLocked = false)
            )
            _isLoadingUsers.value = false
        }
    }

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    fun toggleUserLock(user: UserEntity) {
        viewModelScope.launch {
            val updatedList = _rawUsersList.value.map {
                if (it.id == user.id) it.copy(isLocked = !it.isLocked) else it
            }
            _rawUsersList.value = updatedList
        }
    }

    // --- LOGIC REPORTS (FIX LỖI CHO ADMIN REPORT SCREEN) ---

    private fun loadReports() {
        // Giả lập dữ liệu báo cáo
        _reports.value = listOf(
            Report("r1", "doc1", "Đề thi Toán HK1", "Nội dung sai lệch/Spam", "user1@gmail.com"),
            Report("r2", "doc2", "Giải tích 2", "Vi phạm bản quyền", "user2@gmail.com"),
            Report("r3", "doc3", "Tài liệu lạ", "Chứa mã độc", "user3@gmail.com")
        )
    }

    // Xóa tài liệu bị báo cáo
    fun deleteDocument(docId: String, reportId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            delay(1500) // Giả lập gọi API xóa

            // Xóa xong thì xóa report khỏi list
            _reports.update { currentList ->
                currentList.filter { it.id != reportId }
            }

            _toastMessage.send("Đã xóa tài liệu $docId thành công!")
            _isProcessing.value = false
        }
    }

    // Bỏ qua báo cáo (không xóa tài liệu)
    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            _reports.update { currentList ->
                currentList.filter { it.id != reportId }
            }
            _toastMessage.send("Đã bỏ qua báo cáo.")
        }
    }
}