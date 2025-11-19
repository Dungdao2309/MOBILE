package com.bancu.ungdungcuatoi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManHinhDangNhapActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var truongEmail: EditText
    private lateinit var truongMatKhau: EditText
    private lateinit var nutDangNhap: Button
    private lateinit var lienKetDangKy: TextView
    private lateinit var lienKetQuenMatKhau: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_dang_nhap)

        firebaseAuth = Firebase.auth

        // Ánh xạ View
        truongEmail = findViewById(R.id.truongEmailDangNhap)
        truongMatKhau = findViewById(R.id.truongMatKhauDangNhap)
        nutDangNhap = findViewById(R.id.nutDangNhap)
        lienKetDangKy = findViewById(R.id.lienKetDangKy)
        lienKetQuenMatKhau = findViewById(R.id.lienKetQuenMatKhau)

        // Xử lý sự kiện
        nutDangNhap.setOnClickListener {
            thucHienDangNhap()
        }

        lienKetDangKy.setOnClickListener {
            val yDinh = Intent(this, ManHinhDangKyActivity::class.java)
            startActivity(yDinh)
        }

        lienKetQuenMatKhau.setOnClickListener {
            val yDinh = Intent(this, ManHinhQuenMatKhauActivity::class.java)
            startActivity(yDinh)
        }
    }

    // Hàm kiểm tra xem người dùng đã đăng nhập chưa
    override fun onStart() {
        super.onStart()
        val nguoiDungHienTai = firebaseAuth.currentUser
        if (nguoiDungHienTai != null) {
            // Nếu đã đăng nhập, chuyển thẳng vào Màn hình Chính
            chuyenToiManHinhChinh()
        }
    }

    // Hàm thực hiện đăng nhập
    private fun thucHienDangNhap() {
        val email = truongEmail.text.toString().trim()
        val matKhau = truongMatKhau.text.toString().trim()

        if (email.isEmpty()) {
            truongEmail.error = "Vui lòng nhập email"
            truongEmail.requestFocus()
            return
        }

        if (matKhau.isEmpty()) {
            truongMatKhau.error = "Vui lòng nhập mật khẩu"
            truongMatKhau.requestFocus()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener(this) { tacVu ->
                if (tacVu.isSuccessful) {
                    // Đăng nhập thành công
                    Toast.makeText(baseContext, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show()
                    chuyenToiManHinhChinh()
                } else {
                    // Đăng nhập thất bại
                    Toast.makeText(baseContext, "Đăng nhập thất bại: ${tacVu.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Hàm chuyển tới Màn hình Chính
    private fun chuyenToiManHinhChinh() {
        val yDinh = Intent(this, ManHinhChinhActivity::class.java)
        yDinh.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(yDinh)
        finish()
    }
}
