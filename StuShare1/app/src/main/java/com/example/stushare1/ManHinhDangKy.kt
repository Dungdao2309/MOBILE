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
fun ManHinhDangKy(boDieuHuong: NavController) {
    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var xacNhanMatKhau by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun thucHienDangKy() {
        if (email.isEmpty() || matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (matKhau != xacNhanMatKhau) {
            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }
        if (matKhau.length < 6) {
            Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
            return
        }

        dangXuLy = true
        firebaseAuth.createUserWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                        popUpTo(ManHinh.DANG_KY) { inclusive = true }
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
        Text(text = "Đăng Ký", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MauXanhChuDao)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = matKhau,
            onValueChange = { matKhau = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = xacNhanMatKhau,
            onValueChange = { xacNhanMatKhau = it },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { thucHienDangKy() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
            enabled = !dangXuLy
        ) {
            if (dangXuLy) CircularProgressIndicator(color = Color.White)
            else Text("Đăng Ký Ngay", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Đã có tài khoản? ")
            Text(
                text = "Đăng nhập",
                color = MauXanhChuDao,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { boDieuHuong.popBackStack() }
            )
        }
    }
}