package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.NotificationEntity
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.utils.AppConstants
import com.example.stushare.core.utils.removeAccents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

private const val CACHE_DURATION_MS = 15 * 60 * 1000 // 15 phút

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : DocumentRepository {

    // ==========================================
    // 1. TRUY VẤN (READ)
    // ==========================================
    override fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()
    override fun getDocumentById(documentId: String): Flow<Document> = documentDao.getDocumentById(documentId)

    override fun searchDocuments(query: String): Flow<List<Document>> {
        val normalizedQuery = query.removeAccents()
        return documentDao.searchDocuments(normalizedQuery)
    }

    override fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)


    // ==========================================
    // 2. ĐỒNG BỘ DỮ LIỆU (SYNC) - 🟢 CẬP NHẬT MẠNH MẼ
    // ==========================================

    override suspend fun refreshDocuments(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    // 1. Mới nhất
                    val recentDeferred = async {
                        firestore.collection("documents")
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .limit(50)
                            .get().await()
                    }

                    // 2. Ôn thi
                    val examDeferred = async {
                        firestore.collection("documents")
                            .whereEqualTo("type", AppConstants.TYPE_EXAM_PREP)
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .limit(20)
                            .get().await()
                    }

                    // 3. 🟢 THÊM: Sách (book)
                    val bookDeferred = async {
                        firestore.collection("documents")
                            .whereEqualTo("type", "book")
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .limit(20)
                            .get().await()
                    }

                    // 4. 🟢 THÊM: Bài giảng (lecture)
                    val lectureDeferred = async {
                        firestore.collection("documents")
                            .whereEqualTo("type", "lecture")
                            .orderBy("uploadedAt", Query.Direction.DESCENDING)
                            .limit(20)
                            .get().await()
                    }

                    val recentSnapshot = recentDeferred.await()
                    val examSnapshot = examDeferred.await()
                    val bookSnapshot = bookDeferred.await()       // Chờ tải xong
                    val lectureSnapshot = lectureDeferred.await() // Chờ tải xong

                    val allDocs = mutableSetOf<Document>()

                    recentSnapshot.documents.forEach { doc -> mapFirestoreToDocument(doc)?.let { allDocs.add(it) } }
                    examSnapshot.documents.forEach { doc -> mapFirestoreToDocument(doc)?.let { allDocs.add(it) } }
                    bookSnapshot.documents.forEach { doc -> mapFirestoreToDocument(doc)?.let { allDocs.add(it) } }    // Gộp vào
                    lectureSnapshot.documents.forEach { doc -> mapFirestoreToDocument(doc)?.let { allDocs.add(it) } } // Gộp vào

                    documentDao.insertAllDocuments(allDocs.toList())
                    settingsRepository.updateLastRefreshTimestamp()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshDocumentsIfStale() {
        val lastRefresh = settingsRepository.lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastRefresh) > CACHE_DURATION_MS || lastRefresh == 0L) {
            refreshDocuments()
        }
    }

    /**
     * Hàm tiện ích: Chuyển đổi dữ liệu từ Firestore sang Model Document
     * 🟢 ĐÃ FIX: Lấy thêm uploadedAt gán vào createdAt để sắp xếp đúng
     */
    private fun mapFirestoreToDocument(doc: DocumentSnapshot): Document? {
        val data = doc.data ?: return null
        val title = data["title"] as? String ?: ""

        // Lấy thời gian upload từ Firebase
        val uploadedAt = (data["uploadedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        return Document(
            id = doc.getString("id") ?: doc.id,
            title = title,
            normalizedTitle = title.removeAccents(),
            description = data["description"] as? String ?: "",
            type = data["type"] as? String ?: "Sách",
            imageUrl = data["imageUrl"] as? String ?: "",
            fileUrl = data["fileUrl"] as? String ?: "",
            downloads = (data["downloads"] as? Number)?.toInt() ?: 0,
            rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
            author = data["author"] as? String ?: data["authorName"] as? String ?: "Ẩn danh",
            courseCode = data["courseCode"] as? String ?: "GEN",
            authorId = data["authorId"] as? String,

            // 🟢 QUAN TRỌNG: Gán thời gian để sắp xếp mục "Mới tải lên"
            createdAt = uploadedAt
        )
    }

    // ==========================================
    // 3. UPLOAD & WRITE
    // ==========================================

    override suspend fun uploadDocument(
        title: String, description: String, fileUri: Uri, mimeType: String, coverUri: Uri?, author: String, type: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Bạn chưa đăng nhập!"))
                val uploaderName = auth.currentUser?.displayName ?: "Ẩn danh"

                // 1. Upload File
                val docExtension = if (mimeType.contains("pdf")) "pdf" else "docx"
                val docFileName = "documents/${UUID.randomUUID()}.$docExtension"
                val docRef = storage.reference.child(docFileName)
                docRef.putFile(fileUri).await()
                val fileDownloadUrl = docRef.downloadUrl.await().toString()

                // 2. Upload Cover Image
                val imageDownloadUrl = if (coverUri != null) {
                    val imageFileName = "covers/${UUID.randomUUID()}.jpg"
                    val imageRef = storage.reference.child(imageFileName)
                    imageRef.putFile(coverUri).await()
                    imageRef.downloadUrl.await().toString()
                } else "https://picsum.photos/seed/${System.currentTimeMillis()}/200/300"

                // 3. Lưu Metadata lên Firestore
                val newId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis() // Thời điểm upload

                val documentMap = hashMapOf(
                    "id" to newId,
                    "title" to title,
                    "description" to description,
                    "fileUrl" to fileDownloadUrl,
                    "imageUrl" to imageDownloadUrl,
                    "type" to type,
                    "uploadedAt" to timestamp,
                    "downloads" to 0,
                    "rating" to 0.0,
                    "ratingCount" to 0,
                    "author" to author,
                    "authorName" to uploaderName,
                    "authorId" to currentUserId,
                    "courseCode" to "GEN"
                )
                firestore.collection("documents").document(newId).set(documentMap).await()

                // 4. Lưu vào Local DB
                val newLocalDocument = Document(
                    id = newId, title = title, normalizedTitle = title.removeAccents(), type = type, description = description,
                    imageUrl = imageDownloadUrl, fileUrl = fileDownloadUrl, downloads = 0, rating = 0.0,
                    author = author, courseCode = "GEN", authorId = currentUserId,
                    createdAt = timestamp // 🟢 Lưu luôn thời gian vào Local
                )
                documentDao.insertDocument(newLocalDocument)

                // 5. Tạo thông báo
                notificationRepository.createNotification(
                    targetUserId = currentUserId,
                    title = "Đăng tải thành công ✅",
                    message = "Tài liệu '$title' của bạn đã được chia sẻ.",
                    type = NotificationEntity.TYPE_UPLOAD,
                    relatedId = newId
                )
                Result.success(newId)
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    override suspend fun incrementDownloadCount(documentId: String, authorId: String?, docTitle: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("documents").document(documentId).update("downloads", FieldValue.increment(1))
                val localDoc = documentDao.getDocumentById(documentId).first()
                if (localDoc != null) {
                    documentDao.insertDocument(localDoc.copy(downloads = localDoc.downloads + 1))
                }
                val currentUserId = auth.currentUser?.uid
                if (authorId != null && authorId != currentUserId) {
                    notificationRepository.createNotification(
                        targetUserId = authorId,
                        title = "Tài liệu được tải xuống 📥",
                        message = "Ai đó vừa tải tài liệu '$docTitle' của bạn!",
                        type = NotificationEntity.TYPE_DOWNLOAD,
                        relatedId = documentId
                    )
                }
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    // Các hàm khác giữ nguyên
    override suspend fun insertDocument(document: Document) { withContext(Dispatchers.IO) { documentDao.insertDocument(document) } }
    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("documents").document(documentId).delete().await()
                documentDao.deleteDocumentById(documentId)
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    override suspend fun isDocumentBookmarked(documentId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.success(false)
                val doc = firestore.collection("users").document(userId).collection("bookmarks").document(documentId).get().await()
                Result.success(doc.exists())
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    override suspend fun toggleBookmark(documentId: String, isBookmarked: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
                val ref = firestore.collection("users").document(userId).collection("bookmarks").document(documentId)
                if (isBookmarked) ref.set(mapOf("documentId" to documentId, "savedAt" to System.currentTimeMillis())).await()
                else ref.delete().await()
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    override fun getBookmarkedDocuments(): Flow<List<Document>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) { trySend(emptyList()); close(); return@callbackFlow }
        val subscription = firestore.collection("users").document(userId).collection("bookmarks").orderBy("savedAt", Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val ids = snapshot.documents.mapNotNull { it.getString("documentId") }
                if (ids.isEmpty()) trySend(emptyList())
                else {
                    firestore.collection("documents").whereIn("id", ids.take(10)).get().addOnSuccessListener { docsSnapshot ->
                        val docs = docsSnapshot.documents.mapNotNull { mapFirestoreToDocument(it) }
                        trySend(docs)
                    }
                }
            }
        }
        awaitClose { subscription.remove() }
    }
    override suspend fun sendComment(documentId: String, content: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
                val docSnapshot = firestore.collection("documents").document(documentId).get().await()
                val authorId = docSnapshot.getString("authorId")
                val docTitle = docSnapshot.getString("title") ?: "Tài liệu"
                val commentData = hashMapOf("documentId" to documentId, "userId" to user.uid, "userName" to (user.displayName ?: "Sinh viên"), "userAvatar" to user.photoUrl?.toString(), "content" to content, "timestamp" to FieldValue.serverTimestamp())
                firestore.collection("documents").document(documentId).collection("comments").add(commentData).await()
                if (authorId != null && authorId != user.uid) {
                    notificationRepository.createNotification(targetUserId = authorId, title = "Bình luận mới 💬", message = "${user.displayName ?: "Ai đó"} đã bình luận vào tài liệu '$docTitle': \"$content\"", type = NotificationEntity.TYPE_COMMENT, relatedId = documentId)
                }
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    override fun getComments(documentId: String): Flow<List<CommentEntity>> = callbackFlow {
        val subscription = firestore.collection("documents").document(documentId).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val comments = snapshot.documents.mapNotNull { doc ->
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()
                    CommentEntity(id = doc.id, documentId = doc.getString("documentId") ?: "", userId = doc.getString("userId") ?: "", userName = doc.getString("userName") ?: "Ẩn danh", userAvatar = doc.getString("userAvatar"), content = doc.getString("content") ?: "", timestamp = timestamp)
                }
                trySend(comments)
            }
        }
        awaitClose { subscription.remove() }
    }
    override suspend fun deleteComment(documentId: String, commentId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("documents").document(documentId).collection("comments").document(commentId).delete().await()
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    override suspend fun rateDocument(documentId: String, rating: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
                val docRef = firestore.collection("documents").document(documentId)
                val userRatingRef = docRef.collection("ratings").document(userId)

                var finalNewAverage = 0.0
                var docTitle = ""
                var docAuthorId = ""

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    docTitle = snapshot.getString("title") ?: "Tài liệu"
                    docAuthorId = snapshot.getString("authorId") ?: ""

                    val currentRating = snapshot.getDouble("rating") ?: 0.0
                    val ratingCount = snapshot.getLong("ratingCount") ?: 0

                    val userRatingSnapshot = transaction.get(userRatingRef)
                    val oldRating = if (userRatingSnapshot.exists()) userRatingSnapshot.getLong("value")?.toInt() ?: 0 else 0

                    var newCount = ratingCount
                    if (oldRating == 0) newCount = ratingCount + 1
                    if (newCount == 0L) newCount = 1

                    val totalOldScore = currentRating * ratingCount
                    val newTotalScore = totalOldScore - oldRating + rating
                    finalNewAverage = newTotalScore / newCount.toDouble()

                    transaction.set(userRatingRef, mapOf("value" to rating, "userId" to userId))
                    transaction.update(docRef, "rating", finalNewAverage)
                    transaction.update(docRef, "ratingCount", newCount)
                }.await()

                // Cập nhật Local DB
                val currentLocalDoc = documentDao.getDocumentById(documentId).first()
                if (currentLocalDoc != null) {
                    // 🟢 SỬA LỖI: Dùng finalNewAverage thay vì 0.0
                    val updatedDoc = currentLocalDoc.copy(rating = finalNewAverage)
                    documentDao.insertDocument(updatedDoc)
                }

                // Gửi thông báo
                if (docAuthorId.isNotEmpty() && docAuthorId != userId) {
                    notificationRepository.createNotification(
                        targetUserId = docAuthorId,
                        title = "Đánh giá mới ⭐️",
                        message = "Tài liệu '$docTitle' vừa nhận được $rating sao.",
                        type = NotificationEntity.TYPE_RATING,
                        relatedId = documentId
                    )
                }

                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }
    }    override fun getDocumentsByAuthor(authorId: String): Flow<List<Document>> = callbackFlow {
        val query = firestore.collection("documents").whereEqualTo("authorId", authorId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val docs = snapshot.documents.mapNotNull { mapFirestoreToDocument(it) }
                trySend(docs)
            }
        }
        awaitClose { listener.remove() }
    }
}