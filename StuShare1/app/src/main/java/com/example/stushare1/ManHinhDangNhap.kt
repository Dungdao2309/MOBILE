package com.example.stushare1

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ManHinhDangNhap(boDieuHuong: NavController) {
    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) } // Loading state
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // Kiểm tra nếu đã đăng nhập thì vào thẳng trang chủ
    LaunchedEffect(Unit) {
        if (firebaseAuth.currentUser != null) {
            boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
            }
        }
    }

    fun thucHienDangNhap() {
        if (email.isEmpty() || matKhau.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        dangXuLy = true
        firebaseAuth.signInWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                        popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Lỗi: ${tacVu.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Đăng Nhập", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MauXanhChuDao)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = matKhau,
            onValueChange = { matKhau = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = "Quên mật khẩu?",
                color = MauXanhChuDao,
                modifier = Modifier.clickable { boDieuHuong.navigate(ManHinh.QUEN_MAT_KHAU) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { thucHienDangNhap() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
            enabled = !dangXuLy
        ) {
            if (dangXuLy) CircularProgressIndicator(color = Color.White)
            else Text("Đăng Nhập", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Chưa có tài khoản? ")
            Text(
                text = "Đăng ký",
                color = MauXanhChuDao,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { boDieuHuong.navigate(ManHinh.DANG_KY) }
            )
        }
    }
}