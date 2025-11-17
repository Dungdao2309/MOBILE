// File: SearchViewModel.kt (Đã cải tiến - Tách biệt trách nhiệm)

package com.example.stushare.features.feature_search.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ⭐️ XÓA: import com.example.stushare.core.data.models.Document (Không cần nữa)
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ⭐️ THÊM IMPORT: Giờ đây chúng ta "nhập" SearchUiState từ file riêng
import com.example.stushare.feature_search.ui.search.SearchUiState


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    // ⭐️ XÓA: Khối "sealed interface SearchUiState" đã bị xóa khỏi đây.

    // ⭐️ THAY ĐỔI: _uiState không còn cần thiết ở đây,
    // thay bằng một "sự kiện điều hướng".
    // (Bạn có thể giữ lại _uiState nếu muốn hiển thị vòng xoay trên màn hình *này*)
    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    // (Code này giữ nguyên)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    init {
        _searchQuery
            .debounce(500L)
            .filter { it.length > 2 } // Chỉ tìm khi > 2 ký tự
            .distinctUntilChanged()
            .onEach { query ->
                // ⭐️ THAY ĐỔI: Gọi hàm mới
                onSearchTriggered(query)
            }
            .launchIn(viewModelScope)
    }

    // --------------------
    // ⭐️ HÀM CHÍNH ĐÃ THAY ĐỔI:
    // Giờ đây chỉ "làm nóng" cache và "bắn" sự kiện điều hướng
    // --------------------
    fun onSearchTriggered(query: String) {
        viewModelScope.launch {
            try {
                // 1. "Làm nóng" cache: Vẫn gọi API (nếu cache cũ)
                // Điều này giúp màn hình kết quả tải tức thì
                try {
                    repository.refreshDocumentsIfStale()
                } catch (e: Exception) {
                    // Bỏ qua lỗi mạng, màn hình tiếp theo sẽ xử lý
                    e.printStackTrace()
                }

                // 2. Bắn sự kiện để UI điều hướng
                _navigationEvent.emit(query)

            } catch (e: Exception) {
                // (Lỗi nghiêm trọng nếu không thể emit)
                e.printStackTrace()
            }
        }
    }

    // ⭐️ HÀM performSearch(), navigationHandled(),
    // và removeAccents() đã bị XÓA khỏi file này.
    // Chúng sẽ được chuyển sang SearchResultViewModel.
}