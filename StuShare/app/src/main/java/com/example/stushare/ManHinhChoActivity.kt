package com.example.stushare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class ManHinhChoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_cho)

        // Chuyển sang màn hình giới thiệu sau 2 giây
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ManHinhGioiThieuActivity::class.java)
            startActivity(intent)
            finish() // Đóng màn hình chờ để user không back lại được
        }, 2000)
    }
}