package com.example.stushare.feature_search.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
// ⭐️ BƯỚC 1: IMPORT SETTINGS REPOSITORY ⭐️
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.stushare.feature_search.ui.search.SearchUiState

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val repository: DocumentRepository,
    // ⭐️ BƯỚC 2: INJECT SETTINGS REPOSITORY ⭐️
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val query: String = savedStateHandle.get<String>("query") ?: ""

    init {
        if (query.isNotBlank()) {
            performSearch(query)

            // ⭐️ BƯỚC 3: LƯU TỪ KHÓA KHI VIEWMODEL KHỞI CHẠY ⭐️
            viewModelScope.launch {
                settingsRepository.addRecentSearch(query)
            }

        } else {
            _uiState.value = SearchUiState.Error("Không nhận được từ khóa tìm kiếm.")
        }
    }

    // (Hàm performSearch giữ nguyên, không thay đổi)
    fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                try {
                    repository.refreshDocumentsIfStale()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val results = repository.searchDocuments(query.trim())

                if (results.isEmpty()) {
                    _uiState.value = SearchUiState.Empty
                } else {
                    _uiState.value = SearchUiState.Success(results, results.size)
                }

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> e.message ?: "Mất kết nối mạng"
                    is DataFailureException.ApiError -> "Lỗi máy chủ (${e.code}). Thử lại sau."
                    else -> "Lỗi tìm kiếm không xác định"
                }
                _uiState.value = SearchUiState.Error(errorMessage)
            }
        }
    }
}