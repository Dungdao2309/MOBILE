package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity // 🟢 Import UserEntity
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ==========================================
// 1. DATA MODELS
// ==========================================

data class AdminStats(
    val userCount: String = "0", // Đổi sang String cho dễ hiển thị UI
    val documentCount: String = "0",
    val requestCount: String = "0"
)

// ==========================================
// 2. INTERFACE
// ==========================================

interface AdminRepository {
    // Thống kê
    suspend fun getSystemStats(): AdminStats

    // Quản lý Báo cáo
    suspend fun getPendingReports(): Result<List<Report>>
    suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit>
    suspend fun dismissReport(reportId: String): Result<Unit>

    // 🟢 MỚI: Quản lý Người dùng
    suspend fun getAllUsers(): Result<List<UserEntity>>
    suspend fun setUserLockStatus(userId: String, isLocked: Boolean): Result<Unit>
}

// ==========================================
// 3. IMPLEMENTATION
// ==========================================

class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AdminRepository {

    // --- THỐNG KÊ ---
    override suspend fun getSystemStats(): AdminStats {
        return try {
            val usersQuery = firestore.collection("users").count().get(AggregateSource.SERVER)
            val docsQuery = firestore.collection("documents").count().get(AggregateSource.SERVER)
            val requestsQuery = firestore.collection("requests").count().get(AggregateSource.SERVER)

            val userSnapshot = usersQuery.await()
            val docSnapshot = docsQuery.await()
            val reqSnapshot = requestsQuery.await()

            AdminStats(
                userCount = userSnapshot.count.toString(),
                documentCount = docSnapshot.count.toString(),
                requestCount = reqSnapshot.count.toString()
            )
        } catch (e: Exception) {
            AdminStats("0", "0", "0")
        }
    }

    // --- QUẢN LÝ BÁO CÁO (Code cũ giữ nguyên) ---
    override suspend fun getPendingReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp")
                .get()
                .await()

            val reports = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Report::class.java)?.copy(id = doc.id)
            }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit> {
        return try {
            val docSnapshot = firestore.collection("documents").document(documentId).get().await()

            if (docSnapshot.exists()) {
                val fileUrl = docSnapshot.getString("fileUrl")
                val imageUrl = docSnapshot.getString("imageUrl")

                if (!fileUrl.isNullOrBlank() && fileUrl.startsWith("http")) {
                    try { storage.getReferenceFromUrl(fileUrl).delete().await() } catch (_: Exception) {}
                }
                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http") && !imageUrl.contains("picsum")) {
                    try { storage.getReferenceFromUrl(imageUrl).delete().await() } catch (_: Exception) {}
                }
            }

            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("documents").document(documentId)
                val reportRef = firestore.collection("reports").document(reportId)
                transaction.delete(docRef)
                transaction.update(reportRef, "status", "resolved")
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dismissReport(reportId: String): Result<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update("status", "dismissed")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 MỚI: QUẢN LÝ NGƯỜI DÙNG
    override suspend fun getAllUsers(): Result<List<UserEntity>> {
        return try {
            // Lấy danh sách user, sắp xếp theo tên
            val snapshot = firestore.collection("users")
                .orderBy("fullName", Query.Direction.ASCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toObject(UserEntity::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setUserLockStatus(userId: String, isLocked: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("isLocked", isLocked)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}