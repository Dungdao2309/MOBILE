package com.bancu.ungdungcuatoi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManHinhChinhActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var nutDangXuat: Button
    private lateinit var nutManHinhChinh: Button
    private lateinit var vanBanChaoMung: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_chinh)

        // Khởi tạo
        firebaseAuth = Firebase.auth

        // Ánh xạ
        nutDangXuat = findViewById(R.id.nutDangXuat)
        nutManHinhChinh = findViewById(R.id.nutManHinhChinh)
        vanBanChaoMung = findViewById(R.id.vanBanChaoMung)

        // Lấy thông tin người dùng hiện tại (nếu cần)
        val nguoiDungHienTai = firebaseAuth.currentUser
        if (nguoiDungHienTai != null) {
            // Hiển thị email chào mừng
            vanBanChaoMung.text = "Chào mừng,\n${nguoiDungHienTai.email}"
        }

        // Xử lý sự kiện click
        nutDangXuat.setOnClickListener {
            thucHienDangXuat()
        }

        nutManHinhChinh.setOnClickListener {
            Toast.makeText(this, "Bạn đã ở Màn hình chính!", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm thực hiện đăng xuất
    private fun thucHienDangXuat() {
        firebaseAuth.signOut() // Đăng xuất khỏi Firebase

        // Chuyển về Màn hình Đăng nhập
        val yDinh = Intent(this, ManHinhDangNhapActivity::class.java)
        // Xóa tất cả các Activity trước đó khỏi stack
        yDinh.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(yDinh)
        finish() // Đóng Activity màn hình chính
    }
}