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


@Composable
fun ManHinhQuenMatKhau(boDieuHuong: NavController) {
    var email by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun guiEmailKhoiPhuc() {
        if (email.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }
        dangXuLy = true
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show()
                    boDieuHuong.popBackStack()
                } else {
                    Toast.makeText(context, "Lỗi: ${tacVu.exception?.message}", Toast.LENGTH_SHORT).show()
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
                onValueChange = { email = it },
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
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            // Nút Quay lại (TextButton) ở dưới cùng
            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}