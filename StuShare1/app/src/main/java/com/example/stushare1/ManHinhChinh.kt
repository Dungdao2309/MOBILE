package com.example.stushare1

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ManHinhChinh(boDieuHuong: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val nguoiDung = firebaseAuth.currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Chào mừng!", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = nguoiDung?.email ?: "Khách", fontSize = 18.sp, color = MauXanhChuDao)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: Logic upload file */ },
            colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao)
        ) {
            Text("Màn hình Upload Tài Liệu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                firebaseAuth.signOut()
                boDieuHuong.navigate(ManHinh.DANG_NHAP) {
                    popUpTo(ManHinh.TRANG_CHU) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Đăng Xuất")
        }
    }
}