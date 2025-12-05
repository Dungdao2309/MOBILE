package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class RequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : RequestRepository {

    // Collection "requests"
    private val requestsCollection = firestore.collection("requests")

    /**
     * Lắng nghe TẤT CẢ yêu cầu
     */
    override fun getAllRequests(): Flow<List<DocumentRequest>> {
        return callbackFlow {
            val listenerRegistration = requestsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING) // Mới nhất lên đầu
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val requests = snapshot.toObjects(DocumentRequest::class.java)
                        trySend(requests)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Lắng nghe CHI TIẾT 1 yêu cầu
     */
    override fun getRequestById(requestId: String): Flow<DocumentRequest?> {
        return callbackFlow {
            val listenerRegistration = requestsCollection.document(requestId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val request = snapshot.toObject(DocumentRequest::class.java)
                        trySend(request)
                    } else {
                        trySend(null) // Không tìm thấy hoặc đã bị xóa
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Lắng nghe DANH SÁCH BÌNH LUẬN (Chat)
     */
    override fun getCommentsForRequest(requestId: String): Flow<List<CommentEntity>> {
        return callbackFlow {
            val commentsRef = requestsCollection.document(requestId).collection("comments")

            val listenerRegistration = commentsRef
                .orderBy("timestamp", Query.Direction.ASCENDING) // Tin nhắn cũ ở trên
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val comments = snapshot.toObjects(CommentEntity::class.java)
                        trySend(comments)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Tạo yêu cầu mới
     */
    override suspend fun createRequest(title: String, subject: String, description: String) {
        try {
            val currentUser = firebaseAuth.currentUser
            val authorName = currentUser?.displayName ?: "Người dùng ẩn danh"
            val authorId = currentUser?.uid ?: ""
            val authorAvatar = currentUser?.photoUrl?.toString()

            val newRequest = DocumentRequest(
                title = title,
                subject = subject,
                description = description,
                authorName = authorName,
                authorId = authorId,
                authorAvatar = authorAvatar,
                isSolved = false // Mặc định là chưa giải quyết
            )

            requestsCollection.add(newRequest).await()

        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Không thể tạo yêu cầu", e)
        }
    }

    /**
     * Gửi bình luận (Chat)
     */
    override suspend fun addCommentToRequest(requestId: String, content: String) {
        try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("Chưa đăng nhập")

            val comment = CommentEntity(
                documentId = requestId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Ẩn danh",
                userAvatar = currentUser.photoUrl?.toString(),
                content = content
            )

            requestsCollection.document(requestId)
                .collection("comments")
                .add(comment)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Không thể gửi bình luận", e)
        }
    }

    /**
     * 🟢 MỚI: Cập nhật trạng thái Hoàn thành
     */
    override suspend fun updateRequestStatus(requestId: String, isSolved: Boolean): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update("isSolved", isSolved)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}