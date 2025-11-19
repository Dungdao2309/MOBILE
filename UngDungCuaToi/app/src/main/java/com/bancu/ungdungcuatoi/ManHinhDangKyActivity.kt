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

class ManHinhDangKyActivity : AppCompatActivity() {

    // Khai báo biến (tên tiếng Việt theo yêu cầu)
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var truongEmail: EditText
    private lateinit var truongMatKhau: EditText
    private lateinit var truongXacNhanMatKhau: EditText
    private lateinit var nutDangKy: Button
    private lateinit var lienKetDangNhap: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_dang_ky)

        // Khởi tạo Firebase Auth
        firebaseAuth = Firebase.auth

        // Ánh xạ View từ XML
        truongEmail = findViewById(R.id.truongEmailDangKy)
        truongMatKhau = findViewById(R.id.truongMatKhauDangKy)
        truongXacNhanMatKhau = findViewById(R.id.truongXacNhanMatKhau)
        nutDangKy = findViewById(R.id.nutDangKy)
        lienKetDangNhap = findViewById(R.id.lienKetDangNhap)

        // Xử lý sự kiện click cho nút Đăng ký
        nutDangKy.setOnClickListener {
            thucHienDangKy()
        }

        // Xử lý sự kiện click cho liên kết Đăng nhập
        lienKetDangNhap.setOnClickListener {
            // Chuyển sang Màn hình Đăng nhập
            val yDinh = Intent(this, ManHinhDangNhapActivity::class.java)
            startActivity(yDinh)
        }
    }

    // Hàm thực hiện đăng ký
    private fun thucHienDangKy() {
        // Lấy giá trị từ các trường
        val email = truongEmail.text.toString().trim()
        val matKhau = truongMatKhau.text.toString().trim()
        val xacNhanMatKhau = truongXacNhanMatKhau.text.toString().trim()

        // Kiểm tra dữ liệu đầu vào
        if (email.isEmpty()) {
            truongEmail.error = "Vui lòng nhập email"
            truongEmail.requestFocus()
            return
        }

        if (matKhau.isEmpty() || matKhau.length < 6) {
            truongMatKhau.error = "Mật khẩu phải có ít nhất 6 ký tự"
            truongMatKhau.requestFocus()
            return
        }

        if (matKhau != xacNhanMatKhau) {
            truongXacNhanMatKhau.error = "Mật khẩu không khớp"
            truongXacNhanMatKhau.requestFocus()
            return
        }

        // (Bạn cũng nên kiểm tra Checkbox điều khoản ở đây)

        // Tạo người dùng mới với Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener(this) { tacVu ->
                if (tacVu.isSuccessful) {
                    // Đăng ký thành công
                    Toast.makeText(baseContext, "Đăng ký thành công.", Toast.LENGTH_SHORT).show()

                    // Chuyển sang Màn hình Chính (ví dụ: ManHinhChinhActivity)
                    val yDinh = Intent(this, ManHinhChinhActivity::class.java)
                    // Xóa các Activity trước đó khỏi stack
                    yDinh.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(yDinh)
                    finish()

                } else {
                    // Đăng ký thất bại
                    Toast.makeText(baseContext, "Đăng ký thất bại: ${tacVu.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}