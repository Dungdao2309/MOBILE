// File: SearchViewModel.kt (Đã cải tiến và hoàn chỉnh)

package com.example.stushare.features.feature_search.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

// --------------------
// UI State cho màn hình tìm kiếm
// --------------------
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val results: List<Document>, val resultCount: Int) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState
    data object Initial : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    init {
        // Tự động tìm kiếm sau khi user dừng gõ 0.5s
        _searchQuery
            .debounce(500L) // Chờ 500ms
            .filter { it.length > 2 || it.isEmpty() } // Chỉ tìm khi > 2 ký tự
            .distinctUntilChanged() // Chỉ tìm khi query thực sự thay đổi
            .onEach { query ->
                if (query.length > 2) {
                    performSearch(query)
                } else if (query.isEmpty()) {
                    // Xóa kết quả nếu người dùng xóa hết text
                    _uiState.value = SearchUiState.Initial
                }
            }
            .launchIn(viewModelScope)
    }

    // --------------------
    // HÀM CHÍNH: Xử lý tìm kiếm tài liệu
    // --------------------
    fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            try {
                // ⭐️ CẢI TIẾN QUAN TRỌNG:
                // Luôn làm mới dữ liệu từ API trước khi tìm kiếm.
                // Điều này khắc phục lỗi "timing" (race condition) khi
                // CSDL local chưa kịp đồng bộ lúc người dùng tìm kiếm.
                try {
                    repository.refreshDocuments()
                } catch (e: Exception) {
                    // Nếu làm mới thất bại (ví dụ: mất mạng),
                    // chúng ta vẫn tiếp tục và tìm kiếm trên CSDL cũ (nếu có).
                    // Không ném lỗi ở đây để tính năng tìm kiếm offline vẫn hoạt động.
                    e.printStackTrace() // Log lỗi để debug
                }

                // Lấy toàn bộ danh sách tài liệu (BÂY GIỜ ĐÃ MỚI)
                val allDocuments = repository.getAllDocuments().first()

                // Chuẩn hóa chuỗi tìm kiếm (xóa dấu, chữ thường)
                val normalizedQuery = removeAccents(query.trim().lowercase())

                // Lọc cục bộ trên client
                val results = allDocuments.filter { document ->
                    // Chuẩn hóa tiêu đề tài liệu để so sánh
                    val normalizedTitle = removeAccents(document.title.lowercase())
                    normalizedTitle.contains(normalizedQuery)
                }

                // Cập nhật trạng thái UI
                if (results.isEmpty()) {
                    _uiState.value = SearchUiState.Empty
                } else {
                    _uiState.value = SearchUiState.Success(results, results.size)
                }

            } catch (e: Exception) {
                // Xử lý các lỗi nghiêm trọng (ví dụ: không thể đọc CSDL)
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
    // Hàm reset UI khi điều hướng
    // --------------------
    fun navigationHandled() {
        _uiState.value = SearchUiState.Initial
    }

    // --------------------
    // Hàm tiện ích: Xóa dấu tiếng Việt
    // --------------------
    private fun removeAccents(str: String): String {
        val temp = Normalizer.normalize(str, Normalizer.Form.NFD)
        // Regex này sẽ xóa tất cả các dấu (dấu huyền, sắc, hỏi, ngã, nặng, và cả dấu mũ)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(temp, "")
    }
}