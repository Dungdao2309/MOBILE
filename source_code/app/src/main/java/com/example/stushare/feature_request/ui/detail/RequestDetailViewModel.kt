package com.example.stushare.feature_request.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.navigation.NavRoute
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import javax.inject.Inject

data class RequestDetailUiState(
    val request: DocumentRequest? = null,
    val comments: List<CommentEntity> = emptyList(),
    val isLoadingRequest: Boolean = true,
    val isSending: Boolean = false,
    val currentUserId: String = ""
)

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val args = savedStateHandle.toRoute<NavRoute.RequestDetail>()
    private val requestId = args.requestId
    private val currentUserId = firebaseAuth.currentUser?.uid ?: ""

    private val _commentText = MutableStateFlow("")
    val commentText = _commentText.asStateFlow()

    private val _isSending = MutableStateFlow(false)

    val uiState: StateFlow<RequestDetailUiState> = combine(
        requestRepository.getRequestById(requestId),
        requestRepository.getCommentsForRequest(requestId),
        _isSending
    ) { request, comments, isSending ->
        RequestDetailUiState(
            request = request,
            comments = comments,
            isLoadingRequest = request == null,
            isSending = isSending,
            currentUserId = currentUserId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestDetailUiState(currentUserId = currentUserId)
    )

    fun onCommentChange(text: String) {
        _commentText.value = text
    }

    fun sendComment() {
        val content = _commentText.value.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                requestRepository.addCommentToRequest(requestId, content)
                _commentText.value = ""
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    // 🟢 CẬP NHẬT: Hàm markAsSolved có thêm callbacks để báo kết quả cho UI
    fun markAsSolved(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            // Gọi repository update
            val result = requestRepository.updateRequestStatus(requestId, true)

            if (result.isSuccess) {
                onSuccess()
                // UI tự động cập nhật nhờ Flow từ Repository
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                onError(errorMsg)
            }
        }
    }
}