package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.UserDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val documentDao: DocumentDao
) : LeaderboardRepository {

    override fun getTopUsers(): Flow<List<UserEntity>> {
        return userDao.getTopUsers()
    }

    // Bạn cần đảm bảo DocumentDao có hàm getTopDocuments() nhé (xem lưu ý bên dưới)
    override fun getTopDocuments(): Flow<List<Document>> {
        return documentDao.getTopDocuments()
    }

    override suspend fun updateUserContribution(userId: String, points: Int) {
        // Logic cập nhật điểm: Lấy user hiện tại -> cộng điểm -> lưu lại
        val user = userDao.getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(contributionPoints = user.contributionPoints + points)
            userDao.insertUser(updatedUser)
        }
    }
}