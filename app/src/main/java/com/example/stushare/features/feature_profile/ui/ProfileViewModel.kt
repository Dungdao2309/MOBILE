package com.example.stushare.features.feature_profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.SettingsRepository
// 👇 THÊM DÒNG QUAN TRỌNG NÀY
import com.example.stushare.core.data.repository.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- 1. Thông tin người dùng ---
    val userProfile: StateFlow<UserProfile?> = settingsRepository.userPreferencesFlow
        .map { prefs ->
            UserProfile(
                id = "user_001",
                fullName = prefs.userName.ifEmpty { "Sinh viên UTH" },
                email = "sinhvien@uth.edu.vn",
                avatarUrl = null
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- 2. Danh sách tài liệu (Giả lập) ---
    private val _publishedDocuments = MutableStateFlow<List<DocItem>>(
        listOf(
            DocItem("1", "Đề thi Giải tích 1 - HK1 2024", "12/10/2024 • 150 lượt xem"),
            DocItem("2", "Giáo trình Triết học Mác - Lênin", "05/09/2024 • 320 lượt xem")
        )
    )
    val publishedDocuments: StateFlow<List<DocItem>> = _publishedDocuments

    // Tài liệu đã lưu
    private val _savedDocuments = MutableStateFlow<List<DocItem>>(
        listOf(
            DocItem("3", "Tổng hợp công thức Vật lý đại cương", "Đã lưu 2 ngày trước"),
            DocItem("4", "Slide bài giảng Lập trình di động", "Đã lưu 1 tuần trước")
        )
    )
    val savedDocuments: StateFlow<List<DocItem>> = _savedDocuments

    // Tài liệu đã tải về
    private val _downloadedDocuments = MutableStateFlow<List<DocItem>>(emptyList())
    val downloadedDocuments: StateFlow<List<DocItem>> = _downloadedDocuments

    // --- 3. Các hành động ---
    fun deletePublishedDocument(docId: String) {
        viewModelScope.launch {
            val currentList = _publishedDocuments.value.toMutableList()
            currentList.removeIf { it.documentId == docId }
            _publishedDocuments.value = currentList
        }
    }

    fun refreshData() {
        // Logic refresh
    }
}