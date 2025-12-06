package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.AdminStats
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ✅ FILE IMPLEMENTATION: Đã cập nhật logic xóa local database
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val documentDao: DocumentDao // 🔥 1. Inject thêm DAO để xử lý dữ liệu local
) : AdminRepository {

    // --- THỐNG KÊ ---
    override suspend fun getSystemStats(): AdminStats = coroutineScope {
        try {
            val usersDeferred = async { firestore.collection("users").count().get(AggregateSource.SERVER).await() }
            val docsDeferred = async { firestore.collection("documents").count().get(AggregateSource.SERVER).await() }
            val requestsDeferred = async { firestore.collection("requests").count().get(AggregateSource.SERVER).await() }

            val userSnapshot = usersDeferred.await()
            val docSnapshot = docsDeferred.await()
            val reqSnapshot = requestsDeferred.await()

            AdminStats(
                userCount = userSnapshot.count,
                documentCount = docSnapshot.count,
                requestCount = reqSnapshot.count
            )
        } catch (e: Exception) {
            AdminStats(0, 0, 0)
        }
    }

    // --- QUẢN LÝ BÁO CÁO ---
    override suspend fun getPendingReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp")
                .get().await()

            val reports = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Report::class.java)?.copy(id = doc.id)
            }
            Result.success(reports)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit> {
        return try {
            // 1. Xóa file gốc (PDF/Word) và ảnh bìa trên Firebase Storage
            val docSnapshot = firestore.collection("documents").document(documentId).get().await()
            if (docSnapshot.exists()) {
                val fileUrl = docSnapshot.getString("fileUrl")
                val imageUrl = docSnapshot.getString("imageUrl")

                // Xóa file tài liệu
                if (!fileUrl.isNullOrBlank() && fileUrl.startsWith("http")) {
                    try { storage.getReferenceFromUrl(fileUrl).delete().await() } catch (_: Exception) {}
                }

                // Xóa ảnh bìa (trừ ảnh mặc định picsum)
                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http") && !imageUrl.contains("picsum")) {
                    try { storage.getReferenceFromUrl(imageUrl).delete().await() } catch (_: Exception) {}
                }
            }

            // 2. Xóa dữ liệu trên Firestore (Server) & Cập nhật trạng thái báo cáo
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("documents").document(documentId)
                val reportRef = firestore.collection("reports").document(reportId)

                if (transaction.get(docRef).exists()) {
                    transaction.delete(docRef)
                }
                transaction.update(reportRef, "status", "resolved")
            }.await()

            // 3. 🔥 QUAN TRỌNG: Xóa dữ liệu trong Local Database (Máy người dùng)
            // Bước này giúp app cập nhật ngay lập tức mà không cần reload lại
            try {
                documentDao.deleteDocumentById(documentId)
            } catch (e: Exception) {
                // Nếu lỗi xóa local thì bỏ qua, vì server đã xóa rồi, lần sync sau sẽ tự mất
            }

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun dismissReport(reportId: String): Result<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update("status", "dismissed")
                .await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- QUẢN LÝ NGƯỜI DÙNG ---
    override suspend fun getAllUsers(): Result<List<UserEntity>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("fullName", Query.Direction.ASCENDING)
                .get()
                .await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserEntity::class.java)?.copy(id = doc.id)
            }
            Result.success(users)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun setUserLockStatus(userId: String, isLocked: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("isLocked", isLocked)
                .await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- GỬI THÔNG BÁO HỆ THỐNG ---
    override suspend fun sendSystemNotification(title: String, content: String): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "title" to title,
                "message" to content,
                "type" to "system",       // Loại thông báo
                "userId" to "ALL",        // Gửi cho tất cả mọi người
                "isRead" to false,
                "timestamp" to System.currentTimeMillis()
            )

            // Lưu vào collection "notifications" trên Firebase
            firestore.collection("notifications").add(notification).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}