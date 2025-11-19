package com.example.stushare1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@Composable
fun ManHinhQuenMatKhau(boDieuHuong: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun guiEmailKhoiPhuc() {
        if (email.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { tacVu ->
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show()
                    boDieuHuong.popBackStack()
                } else {
                    Toast.makeText(context, "Lỗi: ${tacVu.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { boDieuHuong.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quên Mật Khẩu",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MauXanhChuDao,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nhập email của bạn để nhận đường dẫn đặt lại mật khẩu.",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { guiEmailKhoiPhuc() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao)
        ) {
            Text("Gửi Yêu Cầu", fontSize = 18.sp)
        }
    }
}