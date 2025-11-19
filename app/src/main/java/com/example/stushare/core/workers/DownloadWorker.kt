package com.example.stushare.core.workers

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Worker chịu trách nhiệm tải file.
 * Kế thừa CoroutineWorker để hỗ trợ Coroutines (theo kiến trúc hiện đại của sách).
 */
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Lấy dữ liệu đầu vào (URL và Tên file) được gửi từ DownloadHelper
        val fileUrl = inputData.getString(KEY_FILE_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "document"

        return try {
            // 2. Sử dụng DownloadManager (Logic cũ nhưng được bọc trong WorkManager)
            // Lý do: DownloadManager của Android xử lý thông báo và progress bar rất tốt.
            val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(fileUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("Đang tải: $fileName")
                setDescription("Vui lòng đợi...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "${fileName}_${System.currentTimeMillis()}.pdf"
                )
                // ⭐️ CẢI TIẾN: Chỉ cho phép tải qua Wifi/Mobile (tùy chỉnh)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadId = downloadManager.enqueue(request)

            // 3. Trả về kết quả thành công kèm ID download (để theo dõi nếu cần)
            val outputData = workDataOf("DOWNLOAD_ID" to downloadId)
            Result.success(outputData)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_URL = "key_file_url"
        const val KEY_FILE_NAME = "key_file_name"
    }
}