package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.DocumentRequest
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
    private val firestore: FirebaseFirestore
    // ⭐️ XÓA: requestDao: RequestDao
    // ⭐️ XÓA: apiService: ApiService
) : RequestRepository {

    // Định nghĩa tên bộ sưu tập (collection)
    private val requestsCollection = firestore.collection("requests")

    /**
     * Lắng nghe TẤT CẢ yêu cầu từ Firestore TRONG THỜI GIAN THỰC.
     */
    override fun getAllRequests(): Flow<List<DocumentRequest>> {
        // ⭐️ THAY ĐỔI: Dùng callbackFlow để lắng nghe snapshot
        return callbackFlow {
            // Sắp xếp theo ngày tạo mới nhất (dựa trên trường @ServerTimestamp)
            val listenerRegistration = requestsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error) // Đóng Flow nếu có lỗi
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        // "Dịch" các tài liệu Firestore sang List<DocumentRequest>
                        val requests = snapshot.toObjects(DocumentRequest::class.java)
                        trySend(requests) // Gửi danh sách mới
                    }
                }
            // Khi Flow bị hủy (ví dụ: ViewModel bị hủy), gỡ bỏ listener
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * ⭐️ XÓA: Hàm refreshRequests() đã bị xóa (vì không cần nữa)
     */

    /**
     * Tạo một yêu cầu mới trên Firestore.
     */
    override suspend fun createRequest(title: String, subject: String, description: String) {
        try {
            // 1. Tạo đối tượng model MỚI
            val newRequest = DocumentRequest(
                title = title,
                subject = subject,
                description = description,
                authorName = "Người Dùng (Auth)" // Tạm thời hardcode, sẽ thay bằng user
                // createdAt và id sẽ được Firestore tự động thêm
            )

            // 2. Thêm vào bộ sưu tập "requests"
            // .await() sẽ tạm ngưng coroutine cho đến khi Firebase xác nhận
            requestsCollection.add(newRequest).await()

        } catch (e: Exception) {
            e.printStackTrace()
            // (Bạn có thể ném một lỗi tùy chỉnh để ViewModel bắt)
            throw IOException("Không thể tạo yêu cầu", e)
        }
    }
}