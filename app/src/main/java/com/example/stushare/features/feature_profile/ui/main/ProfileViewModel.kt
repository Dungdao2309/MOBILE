package com.example.stushare.features.feature_profile.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.features.feature_profile.ui.model.DocItem
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// 1. ĐỊNH NGHĨA UI STATE
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Unauthenticated : ProfileUiState
    data class Authenticated(val profile: UserProfile) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- Thông báo (Toast) ---
    private val _updateMessage = MutableSharedFlow<String>()
    val updateMessage = _updateMessage.asSharedFlow()

    // --- Trạng thái upload Avatar ---
    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar = _isUploadingAvatar.asStateFlow()

    // --- Luồng theo dõi User Firebase ---
    private val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }.flowOn(Dispatchers.IO)

    // 2. UI STATE CHÍNH
    val uiState: StateFlow<ProfileUiState> = authStateFlow
        .map { user ->
            if (user != null) {
                try { user.reload().await() } catch (e: Exception) { e.printStackTrace() }
                val profile = UserProfile(
                    id = user.uid,
                    fullName = user.displayName ?: user.email ?: "Sinh viên UTH",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString()
                )
                ProfileUiState.Authenticated(profile)
            } else {
                ProfileUiState.Unauthenticated
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ProfileUiState.Loading
        )

    // 3. Danh sách tài liệu ĐÃ ĐĂNG
    @OptIn(ExperimentalCoroutinesApi::class)
    val publishedDocuments: StateFlow<List<DocItem>> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                documentRepository.getDocumentsByAuthor(user.uid).map { documents ->
                    documents.map { doc ->
                        DocItem(
                            documentId = doc.id,
                            docTitle = doc.title,
                            meta = "Đã đăng • ${doc.downloads} lượt tải"
                        )
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🟢 4. Danh sách ĐÃ LƯU (BOOKMARK)
    @OptIn(ExperimentalCoroutinesApi::class)
    val savedDocuments: StateFlow<List<DocItem>> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                // Gọi Repository lấy danh sách bookmark thật
                documentRepository.getBookmarkedDocuments().map { documents ->
                    documents.map { doc ->
                        DocItem(
                            documentId = doc.id,
                            docTitle = doc.title,
                            meta = "Đã lưu • ${doc.type}"
                        )
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. Danh sách tài liệu ĐÃ TẢI (Offline - Giả lập lấy tất cả, bạn có thể lọc lại sau)
    val downloadedDocuments: StateFlow<List<DocItem>> = documentRepository.getAllDocuments()
        .map { documents ->
            documents.map { doc ->
                DocItem(
                    documentId = doc.id,
                    docTitle = doc.title,
                    meta = "Đã tải về • ${doc.type.uppercase()}"
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // =========================================================================
    // ACTIONS - CÁC HÀM XỬ LÝ SỰ KIỆN
    // =========================================================================

    // Cập nhật Avatar
    fun uploadAvatar(uri: Uri) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                val storageRef = storage.reference.child("avatars/${user.uid}.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUrl)
                    .build()

                user.updateProfile(profileUpdates).await()
                user.reload().await()

                _updateMessage.emit("Đã cập nhật ảnh đại diện!")
            } catch (e: Exception) {
                e.printStackTrace()
                _updateMessage.emit("Lỗi tải ảnh: ${e.message}")
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    // Cập nhật Tên hiển thị
    fun updateUserName(newName: String) {
        val user = auth.currentUser ?: return
        if (newName.isBlank()) {
            viewModelScope.launch { _updateMessage.emit("Tên không được để trống!") }
            return
        }
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            viewModelScope.launch {
                if (task.isSuccessful) {
                    _updateMessage.emit("Cập nhật tên thành công!")
                    user.reload()
                } else {
                    _updateMessage.emit("Lỗi: ${task.exception?.message}")
                }
            }
        }
    }

    // Đổi mật khẩu
    fun changePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser ?: return
        if (user.email == null) return

        // Cần xác thực lại trước khi đổi mật khẩu
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    viewModelScope.launch {
                        if (updateTask.isSuccessful) {
                            _updateMessage.emit("Đổi mật khẩu thành công!")
                        } else {
                            _updateMessage.emit("Lỗi: ${updateTask.exception?.message}")
                        }
                    }
                }
            } else {
                viewModelScope.launch { _updateMessage.emit("Mật khẩu hiện tại không đúng!") }
            }
        }
    }

    // Đổi Email
    fun updateEmail(currentPass: String, newEmail: String) {
        val user = auth.currentUser ?: return
        if (user.email == null) return

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                    viewModelScope.launch {
                        if (updateTask.isSuccessful) {
                            _updateMessage.emit("Đổi email thành công!")
                        } else {
                            _updateMessage.emit("Lỗi: ${updateTask.exception?.message}")
                        }
                    }
                }
            } else {
                viewModelScope.launch { _updateMessage.emit("Mật khẩu không đúng!") }
            }
        }
    }

    // Xóa tài liệu đã đăng
    fun deletePublishedDocument(docId: String) {
        viewModelScope.launch {
            try {
                val result = documentRepository.deleteDocument(docId)
                if (result.isSuccess) {
                    _updateMessage.emit("Đã xóa tài liệu")
                } else {
                    _updateMessage.emit("Xóa thất bại")
                }
            } catch (e: Exception) {
                _updateMessage.emit("Lỗi khi xóa: ${e.message}")
            }
        }
    }

    // Đăng xuất
    fun signOut() {
        auth.signOut()
    }
}