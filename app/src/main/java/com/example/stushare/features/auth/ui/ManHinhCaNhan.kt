package com.example.stushare.features.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stushare.core.navigation.NavRoute
import com.google.firebase.auth.FirebaseAuth

// Dùng lại màu xanh chủ đạo đã định nghĩa ở các file trước
val MauXanhProfile = Color(0xFF4CAF50)

@Composable
fun ManHinhCaNhan(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val user = firebaseAuth.currentUser

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Avatar
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MauXanhProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Xin chào,",
                fontSize = 20.sp,
                color = Color.Gray
            )

            // Hiển thị Email
            Text(
                text = user?.email ?: "Khách",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Nút Đăng Xuất
            Button(
                onClick = {
                    firebaseAuth.signOut()
                    // Điều hướng về màn hình Đăng nhập và xóa hết lịch sử
                    navController.navigate(NavRoute.Login) {
                        popUpTo(NavRoute.Home) { inclusive = true }
                        popUpTo(NavRoute.Profile) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Đăng Xuất", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}