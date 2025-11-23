package com.example.stushare1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@Composable
fun ManHinhQuenMatKhau(boDieuHuong: NavController) {
    var email by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) }

    // 1. Biến trạng thái lưu thông báo lỗi
    var thongBaoLoi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun guiEmailKhoiPhuc() {
        // Reset lỗi cũ
        thongBaoLoi = ""

        if (email.isEmpty()) {
            thongBaoLoi = "Vui lòng nhập email" // Gán lỗi trực tiếp
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            thongBaoLoi = "Định dạng email không hợp lệ"
            return
        }

        dangXuLy = true
        firebaseAuth.sendPasswordResetEmail(email.trim()) // trim() để xóa khoảng trắng thừa
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư (cả mục Spam).", Toast.LENGTH_LONG).show()
                    boDieuHuong.popBackStack()
                } else {
                    // 2. Xử lý lỗi từ Firebase
                    val ngoaiLe = tacVu.exception
                    thongBaoLoi = when (ngoaiLe) {
                        is FirebaseAuthInvalidUserException -> "Email này chưa được đăng ký tài khoản."
                        else -> "Lỗi: ${ngoaiLe?.message}"
                    }
                }
            }
    }

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Quên Mật Khẩu",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhChuDao
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nhập email của bạn để nhận đường dẫn đặt lại mật khẩu.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = email,
                onValueChange = {
                    email = it
                    thongBaoLoi = ""
                },
                placeholder = { Text("Email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = thongBaoLoi.isNotEmpty() // Hiện viền đỏ nếu có lỗi
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Hiển thị dòng chữ báo lỗi
            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { guiEmailKhoiPhuc() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
                shape = RoundedCornerShape(25.dp),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Gửi Yêu Cầu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Quay lại
            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}