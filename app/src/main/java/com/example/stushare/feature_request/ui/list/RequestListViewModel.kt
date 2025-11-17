package com.example.stushare.features.feature_request.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
// ⭐️ IMPORT THÊM:
import kotlinx.coroutines.flow.catch

// ⭐️ BƯỚC 1: ĐỊNH NGHĨA UI STATE RÕ RÀNG HƠN ⭐️
data class RequestListUiState(
    val requests: List<DocumentRequest> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null // Thêm trạng thái lỗi
)

@HiltViewModel
class RequestListViewModel @Inject constructor(
    private val repository: RequestRepository
) : ViewModel() {

    // ⭐️ BƯỚC 2: SỬ DỤNG UI STATE MỚI ⭐️
    private val _uiState = MutableStateFlow(RequestListUiState()) // Bắt đầu bằng Loading
    val uiState: StateFlow<RequestListUiState> = _uiState.asStateFlow()

    init {
        // ⭐️ BƯỚC 3: CHỈ CẦN LẮNG NGHE FLOW TỪ FIRESTORE ⭐️
        viewModelScope.launch {
            repository.getAllRequests() // Flow này là real-time
                .catch { e ->
                    // Xử lý lỗi nếu không thể lắng nghe
                    _uiState.value = RequestListUiState(
                        isLoading = false,
                        errorMessage = "Không thể tải yêu cầu: ${e.message}"
                    )
                }
                .collect { requestsFromFirestore ->
                    // Cập nhật UI khi có dữ liệu mới
                    _uiState.value = RequestListUiState(
                        requests = requestsFromFirestore,
                        isLoading = false
                    )
                }
        }

        // ⭐️ BƯỚC 4: XÓA BỎ LỆNH REFRESH THỦ CÔNG ⭐️
        // viewModelScope.launch { repository.refreshRequests() }
        // (Không còn cần thiết nữa!)
    }
}