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
import com.google.firebase.storage.FirebaseStorage // 👈 Import Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val storage: FirebaseStorage // 👈 Inject thêm Storage để up ảnh
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- 1. Sự kiện thông báo (Toast) ---
    private val _updateMessage = MutableSharedFlow<String>()
    val updateMessage = _updateMessage.asSharedFlow()

    // --- 2. Trạng thái Loading khi upload ảnh ---
    // Để hiển thị vòng quay loading trên Avatar khi đang tải
    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar = _isUploadingAvatar.asStateFlow()

    // --- 3. Lắng nghe trạng thái Auth (Realtime) ---
    private val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    // --- 4. Dữ liệu User Profile ---
    val userProfile: StateFlow<UserProfile?> = authStateFlow
        .map { user ->
            if (user != null) {
                user.reload().await() // Đảm bảo lấy dữ liệu mới nhất (Avatar, Name)
                UserProfile(
                    id = user.uid,
                    fullName = user.displayName ?: user.email ?: "Sinh viên UTH",
                    email = user.email ?: "",
                    // Lấy URL ảnh avatar (nếu có)
                    avatarUrl = user.photoUrl?.toString()
                )
            } else {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- 5. Danh sách tài liệu đã đăng ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val publishedDocuments: StateFlow<List<DocItem>> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                documentRepository.getDocumentsByAuthor(user.uid)
                    .map { documents ->
                        documents.map { doc ->
                            DocItem(
                                documentId = doc.id.toString(),
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

    val savedDocuments: StateFlow<List<DocItem>> = MutableStateFlow(emptyList())
    val downloadedDocuments: StateFlow<List<DocItem>> = MutableStateFlow(emptyList())

    // =========================================================================
    // CÁC HÀM XỬ LÝ LOGIC (ACTIONS)
    // =========================================================================

    // ✅ 1. Upload Avatar (MỚI THÊM)
    fun uploadAvatar(uri: Uri) {
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            _isUploadingAvatar.value = true // Bật loading
            try {
                // 1. Tạo đường dẫn file: avatars/user_id.jpg
                val storageRef = storage.reference.child("avatars/${user.uid}.jpg")

                // 2. Upload file lên Firebase Storage
                storageRef.putFile(uri).await()

                // 3. Lấy link tải xuống (Download URL)
                val downloadUrl = storageRef.downloadUrl.await()

                // 4. Cập nhật URL vào Firebase Auth Profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUrl)
                    .build()

                user.updateProfile(profileUpdates).await()

                // 5. Reload lại user để UI cập nhật ảnh mới ngay lập tức
                user.reload().await()

                _updateMessage.emit("Đã cập nhật ảnh đại diện!")
            } catch (e: Exception) {
                e.printStackTrace()
                _updateMessage.emit("Lỗi tải ảnh: ${e.message}")
            } finally {
                _isUploadingAvatar.value = false // Tắt loading
            }
        }
    }

    // ✅ 2. Cập nhật Tên hiển thị
    fun updateUserName(newName: String) {
        val user = auth.currentUser
        if (user != null) {
            if (newName.isBlank()) {
                viewModelScope.launch { _updateMessage.emit("Tên không được để trống!") }
                return
            }
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
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
    }

    // ✅ 3. Đổi Mật Khẩu
    fun changePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            viewModelScope.launch { _updateMessage.emit("Lỗi: Không tìm thấy thông tin người dùng") }
            return
        }
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    user.updatePassword(newPass)
                        .addOnCompleteListener { updateTask ->
                            viewModelScope.launch {
                                if (updateTask.isSuccessful) {
                                    _updateMessage.emit("Đổi mật khẩu thành công!")
                                } else {
                                    _updateMessage.emit("Lỗi đổi pass: ${updateTask.exception?.message}")
                                }
                            }
                        }
                } else {
                    viewModelScope.launch { _updateMessage.emit("Mật khẩu hiện tại không đúng!") }
                }
            }
    }

    // ✅ 4. Cập nhật Email (Mới thêm cho đủ bộ)
    fun updateEmail(currentPass: String, newEmail: String) {
        val user = auth.currentUser
        if (user == null || user.email == null) return

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                    viewModelScope.launch {
                        if (updateTask.isSuccessful) {
                            _updateMessage.emit("Đổi email thành công!")
                            user.reload()
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

    // ✅ 5. Xóa tài liệu
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

    // ✅ 6. Đăng xuất
    fun signOut() {
        auth.signOut()
    }
}