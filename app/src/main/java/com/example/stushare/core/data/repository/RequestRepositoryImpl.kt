package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.DocumentRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
// ⭐️ IMPORT THÊM:
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class RequestRepositoryImpl @Inject constructor(
    // ⭐️ THAY ĐỔI: Inject Firestore
    private val firestore: FirebaseFirestore,
    // ⭐️ BƯỚC 1: INJECT FIREBASE AUTH ⭐️
    private val firebaseAuth: FirebaseAuth //
    // ⭐️ XÓA: requestDao: RequestDao
    // ⭐️ XÓA: apiService: ApiService
) : RequestRepository {

    // Định nghĩa tên bộ sưu tập (collection)
    private val requestsCollection = firestore.collection("requests")

    /**
     * Lắng nghe TẤT CẢ yêu cầu từ Firestore TRONG THỜI GIAN THỰC.
     */
    override fun getAllRequests(): Flow<List<DocumentRequest>> {
        // (Hàm này giữ nguyên, không thay đổi)
        return callbackFlow {
            val listenerRegistration = requestsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
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
     * Tạo một yêu cầu mới trên Firestore.
     */
    override suspend fun createRequest(title: String, subject: String, description: String) {
        try {
            // ⭐️ BƯỚC 2: LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI ⭐️
            val currentUser = firebaseAuth.currentUser
            val authorName = currentUser?.displayName ?: "Người dùng ẩn danh"

            // 1. Tạo đối tượng model MỚI
            val newRequest = DocumentRequest(
                title = title,
                subject = subject,
                description = description,
                // ⭐️ BƯỚC 3: SỬ DỤNG TÊN THẬT (THAY VÌ HARDCODE) ⭐️
                authorName = authorName
            )

            // 2. Thêm vào bộ sưu tập "requests"
            requestsCollection.add(newRequest).await()

        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Không thể tạo yêu cầu", e)
        }
    }
}