package com.example.stushare.features.feature_request.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.RequestRepository // <-- IMPORT MỚI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestListUiState(
    val requests: List<DocumentRequest> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RequestListViewModel @Inject constructor(
    private val repository: RequestRepository // <-- INJECT REPO
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestListUiState())
    val uiState: StateFlow<RequestListUiState> = _uiState.asStateFlow()

    init {
        // Lắng nghe thay đổi từ DB
        viewModelScope.launch {
            repository.getAllRequests().collect { requestsFromDb ->
                _uiState.value = RequestListUiState(requests = requestsFromDb, isLoading = false)
            }
        }
        // Làm mới từ API
        viewModelScope.launch {
            repository.refreshRequests()
        }
    }
}