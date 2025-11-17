package com.example.stushare.features.feature_home.ui.viewall

// IMPORT THÊM ĐỂ DEBUG VÀ XỬ LÝ LỖI
import android.util.Log
import com.example.stushare.core.data.models.DataFailureException
// -------------------------------------

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.models.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ⭐️ IMPORT HẰNG SỐ MỚI ⭐️
import com.example.stushare.core.utils.AppConstants

// UiState (Định nghĩa bên ngoài Class)
sealed interface ViewAllUiState {
    data object Loading : ViewAllUiState
    data class Success(val documents: List<Document>) : ViewAllUiState
    data class Error(val message: String) : ViewAllUiState
}

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    // 1. TÀI SẢN HỖ TRỢ (Backing Property)
    private val _uiState: MutableStateFlow<ViewAllUiState> =
        MutableStateFlow(ViewAllUiState.Loading)

    // 2. TÀI SẢN CÔNG KHAI (Public Getter)
    val uiState: StateFlow<ViewAllUiState> = _uiState.asStateFlow()

    /**
     * Tải tài liệu dựa trên một DANH MỤC CỤ THỂ (ví dụ: "Sách", "Tài Liệu").
     */
    fun loadCategory(category: String) {
        // [CẢI TIẾN] Thêm Log để chẩn đoán lỗi
        Log.e("VIEWMODEL_TEST", "--- ĐANG CHẠY HÀM loadCategory VỚI: $category ---")

        viewModelScope.launch {
            _uiState.value = ViewAllUiState.Loading

            // ⭐️ THAY ĐỔI: Ánh xạ (Map) category từ UI sang 'type' trong DB ⭐️
            val databaseType = when (category) {
                AppConstants.CATEGORY_NEW_UPLOADS -> AppConstants.TYPE_BOOK
                AppConstants.CATEGORY_EXAM_PREP -> AppConstants.TYPE_EXAM_PREP
                else -> "" // Các loại không xác định sẽ trả về rỗng
            }
            // ⭐️ KẾT THÚC THAY ĐỔI ⭐️

            try {
                // 1. ⭐️ THAY ĐỔI: Chỉ làm mới nếu cache đã cũ ⭐️
                repository.refreshDocumentsIfStale() //

                // 2. Sau đó đọc dữ liệu từ DB (Single Source of Truth)
                repository.getDocumentsByType(databaseType)
                    .catch { e ->
                        // Lỗi khi đọc DB
                        _uiState.value = ViewAllUiState.Error(e.message ?: "Đã xảy ra lỗi đọc dữ liệu")
                    }
                    .collect { documentsFromDb ->
                        // Trả về Success(danh sách)
                        _uiState.value = ViewAllUiState.Success(documentsFromDb)
                    }
            } catch (e: Exception) {
                // [CẢI TIẾN] Xử lý lỗi chi tiết hơn
                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> "Lỗi kết nối mạng. Vui lòng thử lại."
                    is DataFailureException.ApiError -> "Lỗi từ máy chủ (Code: ${e.code})."
                    is DataFailureException.UnknownError -> e.message ?: "Lỗi không xác định."
                    else -> e.message ?: "Không thể tải dữ liệu."
                }
                _uiState.value = ViewAllUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Tìm kiếm tài liệu dựa trên một TỪ KHÓA (query).
     * Hàm này sẽ gọi đúng hàm searchDocuments trong Repository.
     */
    fun search(query: String) {
        // [CẢI TIẾN] Thêm Log để chẩn đoán lỗi
        Log.e("VIEWMODEL_TEST", "--- ĐANG CHẠY HÀM search VỚI: $query ---")
        viewModelScope.launch {
            _uiState.value = ViewAllUiState.Loading
            try {
                // 1. ⭐️ THAY ĐỔI: Chỉ làm mới nếu cache đã cũ ⭐️
                repository.refreshDocumentsIfStale() //

                // 2. Gọi đúng hàm search (suspend fun)
                val searchResults = repository.searchDocuments(query)

                // 3. Cập nhật UI với kết quả tìm kiếm
                _uiState.value = ViewAllUiState.Success(searchResults)

            } catch (e: Exception) {
                // [CẢI TIẾN] Xử lý lỗi chi tiết hơn
                val errorMessage = when (e) {
                    is DataFailureException.NetworkError -> "Lỗi kết nối mạng. Vui lòng thử lại."
                    is DataFailureException.ApiError -> "Lỗi từ máy chủ (Code: ${e.code})."
                    is DataFailureException.UnknownError -> e.message ?: "Lỗi không xác định."
                    else -> e.message ?: "Lỗi tìm kiếm."
                }
                _uiState.value = ViewAllUiState.Error(errorMessage)
            }
        }
    }
}