package com.example.stushare // (Hoặc package của bạn)

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    // Hilt sẽ tự động xử lý
}