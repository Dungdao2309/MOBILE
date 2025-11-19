package com.bancu.ungdungcuatoi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManHinhQuenMatKhauActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var truongEmail: EditText
    private lateinit var nutGuiYeuCau: Button
    private lateinit var nutTroVe: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_quen_mat_khau)

        // Khởi tạo Firebase
        firebaseAuth = Firebase.auth

        // Ánh xạ View
        truongEmail = findViewById(R.id.truongEmailQuenMatKhau)
        nutGuiYeuCau = findViewById(R.id.nutGuiYeuCau)
        nutTroVe = findViewById(R.id.nutTroVe)

        // Xử lý sự kiện click
        nutTroVe.setOnClickListener {
            finish() // Đóng Activity hiện tại và quay lại màn hình trước
        }

        nutGuiYeuCau.setOnClickListener {
            thucHienGuiEmailDatLai()
        }
    }

    // Hàm thực hiện gửi email (Theo Lựa chọn 1)
    private fun thucHienGuiEmailDatLai() {
        val email = truongEmail.text.toString().trim()

        if (email.isEmpty()) {
            truongEmail.error = "Vui lòng nhập email"
            truongEmail.requestFocus()
            return
        }

        // Bắt đầu hiển thị loading (nếu có)
        // ...

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { tacVu ->
                // Tắt loading
                // ...
                if (tacVu.isSuccessful) {
                    Toast.makeText(baseContext, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show()
                    // Gửi thành công, đóng màn hình này
                    finish()
                } else {
                    Toast.makeText(baseContext, "Lỗi: ${tacVu.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}