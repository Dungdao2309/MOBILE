// File: features/feature_search/ui/search/SearchResultViewModel.kt
// (⭐️ ĐÃ CẬP NHẬT VỚI LOGIC TÌM KIẾM TỐI ƯU ⭐️)

package com.example.stushare.feature_search.ui.search // Sửa package nếu cần

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
// ⭐️ XÓA: import com.example.stushare.core.data.models.Document (Không cần)
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// ⭐️ XÓA: import java.text.Normalizer (Không còn dùng removeAccents)
import javax.inject.Inject

// ⭐️ THÊM IMPORT: Giờ đây chúng ta "nhập" SearchUiState từ file riêng
import com.example.stushare.feature_search.ui.search.SearchUiState

// ⭐️ XÓA: Khối "sealed interface SearchUiState" đã bị xóa khỏi đây.

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val repository: DocumentRepository,
    savedStateHandle: SavedStateHandle // Dùng để lấy query từ Nav
) : ViewModel() {

    // 1. Quản lý trạng thái UI của riêng mình
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // 2. Lấy query từ NavArgument (được truyền từ NavHost)
    val query: String = savedStateHandle.get<String>("query") ?: ""

    init {
        // 3. Tải kết quả ngay khi ViewModel được tạo
        if (query.isNotBlank()) {
            performSearch(query)
        } else {
            // Trường hợp lỗi (không nhận được query)
            _uiState.value = SearchUiState.Error("Không nhận được từ khóa tìm kiếm.")
        }
    }

    // --------------------
    // HÀM CHÍNH: Xử lý tìm kiếm (⭐️ ĐÃ CẬP NHẬT ⭐️)
    // --------------------
    fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                // 1. Luôn kiểm tra cache (logic từ Cải tiến 1)
                try {
                    repository.refreshDocumentsIfStale()
                } catch (e: Exception) {
                    e.printStackTrace() // Bỏ qua lỗi mạng, tìm trên cache
                }

                // 2. ⭐️ THAY ĐỔI: GỌI TRỰC TIẾP VÀO DATABASE
                // Không cần lọc thủ công hay removeAccents nữa.
                val results = repository.searchDocuments(query.trim())

                // 3. Cập nhật UI
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

    // --------------------
    // ⭐️ HÀM removeAccents() ĐÃ BỊ XÓA (Không còn cần thiết)
    // --------------------
}