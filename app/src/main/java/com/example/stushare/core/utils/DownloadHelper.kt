package com.example.stushare.core.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DownloadHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun downloadFile(url: String, title: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val uri = Uri.parse(url)

        val request = DownloadManager.Request(uri)
            // 1. Tùy chọn: Đặt tiêu đề cho thông báo
            .setTitle("Tải về: $title")
            .setDescription("Đang tải tài liệu...")
            // 2. Cho phép hiển thị trên thanh thông báo
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // 3. Đặt đường dẫn lưu file (ví dụ: thư mục Downloads)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                title + "_" + System.currentTimeMillis() + ".pdf" // Đặt tên file
            )

        // 4. Bắt đầu tải và trả về ID
        return downloadManager.enqueue(request)
    }
}