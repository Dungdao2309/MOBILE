package com.example.stushare.core.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.stushare.core.workers.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DownloadHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun downloadFile(url: String, title: String) {
        // 1. Tạo dữ liệu đầu vào để gửi cho Worker
        val inputData = workDataOf(
            DownloadWorker.KEY_FILE_URL to url,
            DownloadWorker.KEY_FILE_NAME to title
        )

        // 2. ⭐️ CẢI TIẾN QUAN TRỌNG: Thêm Ràng buộc (Constraints)
        // Theo sách Head First Android (Phụ lục): Chỉ chạy khi có mạng (CONNECTED).
        // Bạn có thể đổi thành UNMETERED nếu chỉ muốn tải qua WiFi.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Yêu cầu phải có mạng
            .setRequiresBatteryNotLow(true) // Không tải nếu pin yếu
            .build()

        // 3. Tạo yêu cầu công việc (WorkRequest)
        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        // 4. Gửi yêu cầu cho WorkManager
        WorkManager.getInstance(context).enqueue(downloadWorkRequest)
    }
}