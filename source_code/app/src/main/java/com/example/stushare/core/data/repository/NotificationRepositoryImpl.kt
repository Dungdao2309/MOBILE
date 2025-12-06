package com.example.stushare.core.data.repository

import android.util.Log
import com.example.stushare.core.data.db.NotificationDao
import com.example.stushare.core.data.models.NotificationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    private var isListening = false
    private var currentUserIdListening: String? = null

    // Quản lý listener riêng biệt để tránh rò rỉ bộ nhớ
    private var userListenerRegistration: ListenerRegistration? = null
    private var systemListenerRegistration: ListenerRegistration? = null

    // =========================================================================
    // 1. LẤY DANH SÁCH THÔNG BÁO (REALTIME)
    // =========================================================================
    override fun getNotifications(): Flow<List<NotificationEntity>> {
        val currentUser = auth.currentUser
        if (currentUser == null) return emptyFlow()

        // Nếu đổi User, reset toàn bộ listener cũ
        if (currentUserIdListening != currentUser.uid) {
            Log.d("NOTIF_DEBUG", "🔄 Phát hiện đổi User. Reset Listener.")
            stopListening()
            currentUserIdListening = currentUser.uid
        }

        // Kích hoạt lắng nghe Realtime (Cả Private và System)
        startRealtimeSync(currentUser.uid)

        // Trả về dữ liệu từ Local Room (Single Source of Truth)
        return notificationDao.getNotifications(currentUser.uid)
    }

    override fun getUnreadCount(): Flow<Int> {
        val userId = auth.currentUser?.uid ?: return emptyFlow()
        return notificationDao.getUnreadCount(userId)
    }

    // =========================================================================
    // 2. TẠO THÔNG BÁO MỚI (GỬI ĐI)
    // =========================================================================
    override suspend fun createNotification(
        targetUserId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String?
    ) {
        withContext(Dispatchers.IO) {
            try {
                val newId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()

                // Entity lưu Local (Nếu gửi cho chính mình)
                val notificationEntity = NotificationEntity(
                    id = newId,
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    userId = targetUserId,
                    type = type,
                    isRead = false,
                    relatedId = relatedId
                )

                // Data lưu Firestore
                val firestoreData = hashMapOf(
                    "id" to newId,
                    "title" to title,
                    "message" to message,
                    "timestamp" to timestamp,
                    "userId" to targetUserId,
                    "type" to type,
                    "isRead" to false,
                    "relatedId" to relatedId
                )

                // A. Lưu lên Cloud
                firestore.collection("users")
                    .document(targetUserId)
                    .collection("notifications")
                    .document(newId)
                    .set(firestoreData)
                    .await()

                // B. Nếu gửi cho chính mình -> Lưu luôn vào Local để UI cập nhật ngay
                if (targetUserId == auth.currentUser?.uid) {
                    notificationDao.insertNotification(notificationEntity)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NOTIF_DEBUG", "❌ Lỗi tạo thông báo: ${e.message}")
            }
        }
    }

    // =========================================================================
    // 3. CÁC HÀM THAO TÁC KHÁC
    // =========================================================================
    override suspend fun markAsRead(id: String) {
        withContext(Dispatchers.IO) {
            notificationDao.markAsRead(id)
            updateReadStatusOnCloud(id, true)
        }
    }

    override suspend fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        withContext(Dispatchers.IO) {
            notificationDao.markAllAsRead(userId)
            // Lưu ý: Việc markAllAsRead trên Cloud cho system notification ("ALL")
            // là rất phức tạp nên ở đây ta chỉ ưu tiên cập nhật Local.
        }
    }

    override suspend fun deleteNotification(id: String) {
        withContext(Dispatchers.IO) {
            notificationDao.deleteNotification(id)
            updateReadStatusOnCloud(id, null) // Null = Xóa
        }
    }

    // =========================================================================
    // 4. PRIVATE HELPERS (Xử lý đồng bộ)
    // =========================================================================

    private fun startRealtimeSync(userId: String) {
        if (isListening) return
        isListening = true
        Log.d("NOTIF_DEBUG", "🎧 Bắt đầu lắng nghe thông báo cho User: $userId")

        // --- 1. Lắng nghe thông báo CÁ NHÂN (users/{uid}/notifications) ---
        userListenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                processSnapshot(snapshot, e, userId, source = "PRIVATE")
            }

        // --- 2. Lắng nghe thông báo HỆ THỐNG (notifications where userId == 'ALL') ---
        systemListenerRegistration = firestore.collection("notifications")
            .whereEqualTo("userId", "ALL")
            // .orderBy("timestamp", Query.Direction.DESCENDING) // Cần tạo Composite Index nếu dùng orderBy với whereEqualTo
            .addSnapshotListener { snapshot, e ->
                processSnapshot(snapshot, e, userId, source = "SYSTEM")
            }
    }

    private fun stopListening() {
        userListenerRegistration?.remove()
        systemListenerRegistration?.remove()
        isListening = false
    }

    /**
     * Hàm xử lý chung cho dữ liệu trả về từ cả 2 luồng
     */
    private fun processSnapshot(
        snapshot: QuerySnapshot?,
        e: Exception?,
        currentUserId: String,
        source: String
    ) {
        if (e != null) {
            Log.e("NOTIF_DEBUG", "❌ Lỗi lắng nghe ($source): ${e.message}")
            return
        }

        if (snapshot != null && !snapshot.isEmpty) {
            Log.d("NOTIF_DEBUG", "📥 Nhận được ${snapshot.size()} thông báo từ nguồn: $source")

            CoroutineScope(Dispatchers.IO).launch {
                val notifications = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    val type = doc.getString("type") ?: "SYSTEM"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    // Với thông báo hệ thống, cloud thường không lưu trạng thái đã đọc của từng user
                    // Nên ta lấy giá trị mặc định false nếu không có field này.
                    val isRead = doc.getBoolean("isRead") ?: false
                    val relatedId = doc.getString("relatedId")

                    // QUAN TRỌNG: Dù trên cloud userId là "ALL", khi lưu vào Local
                    // ta phải gán userId = currentUserId thì DAO mới query ra được.
                    NotificationEntity(
                        id = id,
                        title = title,
                        message = message,
                        timestamp = timestamp,
                        userId = currentUserId, // <-- Luôn gán cho user hiện tại
                        type = type,
                        isRead = isRead,
                        relatedId = relatedId
                    )
                }

                // Lưu vào Room (Dùng Insert với onConflict = REPLACE trong DAO để cập nhật nội dung mới nhất)
                notifications.forEach {
                    notificationDao.insertNotification(it)
                }
            }
        }
    }

    private fun updateReadStatusOnCloud(notifId: String, isRead: Boolean?) {
        val userId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chỉ cập nhật trạng thái trên Cloud đối với thông báo CÁ NHÂN
                // (Thông báo hệ thống nằm ở collection chung, user không có quyền sửa/xóa trực tiếp file gốc)
                val ref = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notifId)

                // Kiểm tra xem doc có tồn tại trong collection cá nhân không trước khi update
                // Nếu không (tức là thông báo hệ thống), ta chỉ update ở Local (đã làm ở trên)
                val docCheck = ref.get().await()
                if (docCheck.exists()) {
                    if (isRead == null) {
                        ref.delete()
                    } else {
                        ref.update("isRead", isRead)
                    }
                }
            } catch (e: Exception) {
                // Log lỗi nhẹ, không crash
                Log.w("NOTIF_DEBUG", "Không thể cập nhật trạng thái Cloud (có thể là System Notif): ${e.message}")
            }
        }
    }
}