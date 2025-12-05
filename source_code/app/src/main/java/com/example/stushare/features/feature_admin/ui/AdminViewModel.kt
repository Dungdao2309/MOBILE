package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.core.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Model UI State (Cần String để hiển thị Text)
data class AdminUiState(
    val userCount: String = "0",
    val documentCount: String = "0",
    val requestCount: String = "0"
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    // --- DASHBOARD STATE ---
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _isLoadingDashboard = MutableStateFlow(false)
    val isLoadingDashboard = _isLoadingDashboard.asStateFlow()

    // --- USER MANAGEMENT STATE ---
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

    // --- REPORT STATE ---
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _isProcessing = MutableStateFlow(false) // Dùng chung cho loading khi xóa/gửi
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Kênh thông báo (Toast)
    private val _toastMessage = Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

    init {
        loadDashboardStats()
        loadUsers()
        loadReports()
    }

    // ==========================================
    // 1. LOGIC DASHBOARD
    // ==========================================
    private fun loadDashboardStats() {
        viewModelScope.launch {
            _isLoadingDashboard.value = true
            try {
                val stats = repository.getSystemStats()
                _uiState.update {
                    AdminUiState(
                        userCount = stats.userCount.toString(),
                        documentCount = stats.documentCount.toString(),
                        requestCount = stats.requestCount.toString()
                    )
                }
            } catch (e: Exception) {
                _toastMessage.send("Lỗi tải thống kê: ${e.message}")
            } finally {
                _isLoadingDashboard.value = false
            }
        }
    }

    // ==========================================
    // 2. LOGIC QUẢN LÝ USER
    // ==========================================
    fun loadUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            repository.getAllUsers()
                .onSuccess { users -> _rawUsersList.value = users }
                .onFailure { e -> _toastMessage.send("Lỗi tải User: ${e.message}") }
            _isLoadingUsers.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleUserLock(user: UserEntity) {
        viewModelScope.launch {
            // Optimistic update (Cập nhật UI trước cho mượt)
            val oldList = _rawUsersList.value
            val newList = oldList.map {
                if (it.id == user.id) it.copy(isLocked = !it.isLocked) else it
            }
            _rawUsersList.value = newList

            repository.setUserLockStatus(user.id, !user.isLocked)
                .onSuccess {
                    val status = if (!user.isLocked) "Đã khóa" else "Đã mở khóa"
                    _toastMessage.send("$status tài khoản ${user.fullName}")
                }
                .onFailure {
                    _rawUsersList.value = oldList // Hoàn tác nếu lỗi
                    _toastMessage.send("Lỗi cập nhật trạng thái: ${it.message}")
                }
        }
    }

    // ==========================================
    // 3. LOGIC QUẢN LÝ BÁO CÁO
    // ==========================================
    private fun loadReports() {
        viewModelScope.launch {
            repository.getPendingReports()
                .onSuccess { list -> _reports.value = list }
                .onFailure { _toastMessage.send("Lỗi tải báo cáo: ${it.message}") }
        }
    }

    fun deleteDocument(docId: String, reportId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.deleteDocumentAndResolveReport(docId, reportId)
                .onSuccess {
                    _reports.update { list -> list.filter { it.id != reportId } }
                    _toastMessage.send("Đã xóa tài liệu và giải quyết báo cáo!")
                    loadDashboardStats() // Cập nhật lại số liệu dashboard
                }
                .onFailure { _toastMessage.send("Lỗi xóa tài liệu: ${it.message}") }
            _isProcessing.value = false
        }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            repository.dismissReport(reportId)
                .onSuccess {
                    _reports.update { list -> list.filter { it.id != reportId } }
                    _toastMessage.send("Đã bỏ qua báo cáo.")
                }
                .onFailure { _toastMessage.send("Lỗi: ${it.message}") }
        }
    }

    // ==========================================
    // 4. 🟢 MỚI: GỬI THÔNG BÁO HỆ THỐNG
    // ==========================================
    fun sendSystemNotification(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            // trySend dùng cho Channel khi không trong coroutine (hoặc dùng launch cũng được)
            viewModelScope.launch { _toastMessage.send("Vui lòng nhập đủ tiêu đề và nội dung!") }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            repository.sendSystemNotification(title, content)
                .onSuccess {
                    _toastMessage.send("✅ Đã gửi thông báo đến toàn hệ thống!")
                }
                .onFailure {
                    _toastMessage.send("❌ Lỗi gửi: ${it.message}")
                }
            _isProcessing.value = false
        }
    }
}