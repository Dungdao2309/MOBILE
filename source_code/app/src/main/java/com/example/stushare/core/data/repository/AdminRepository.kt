package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.AdminStats
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity

// ✅ FILE INTERFACE: Đã thêm hàm gửi thông báo
interface AdminRepository {
    // Thống kê
    suspend fun getSystemStats(): AdminStats

    // Quản lý Báo cáo
    suspend fun getPendingReports(): Result<List<Report>>
    suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit>
    suspend fun dismissReport(reportId: String): Result<Unit>

    // Quản lý Người dùng
    suspend fun getAllUsers(): Result<List<UserEntity>>
    suspend fun setUserLockStatus(userId: String, isLocked: Boolean): Result<Unit>

    // 🟢 MỚI: Gửi thông báo hệ thống
    suspend fun sendSystemNotification(title: String, content: String): Result<Unit>
}