package com.example.stushare1

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun ManHinhChao(boDieuHuong: NavController) {
    val context = LocalContext.current

    // Tự động chuyển màn hình sau 2 giây
    LaunchedEffect(Unit) {
        delay(2000)

        // 1. Truy cập SharedPreferences (Đặt tên file lưu trữ là "CauHinhApp")
        val sharedPreferences = context.getSharedPreferences("CauHinhApp", Context.MODE_PRIVATE)

        // 2. Lấy giá trị "lanDauMoApp", mặc định là true (nếu chưa có thì coi là lần đầu)
        val laLanDau = sharedPreferences.getBoolean("lanDauMoApp", true)

        if (laLanDau) {
            // TRƯỜNG HỢP CHẠY LẦN ĐẦU

            // Lưu lại trạng thái là "đã mở rồi" (false) ngay lập tức
            sharedPreferences.edit().putBoolean("lanDauMoApp", false).apply()

            // Điều hướng sang Màn hình Giới thiệu
            boDieuHuong.navigate(ManHinh.GIOI_THIEU) {
                popUpTo(ManHinh.CHAO) { inclusive = true }
            }
        } else {
            // TRƯỜNG HỢP NHỮNG LẦN SAU

            // Điều hướng thẳng sang Màn hình Đăng nhập
            boDieuHuong.navigate(ManHinh.DANG_NHAP) {
                popUpTo(ManHinh.CHAO) { inclusive = true }
            }
        }
    }

    NenHinhSong {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Ứng dụng",
                modifier = Modifier.size(400.dp)
            )
        }
    }
}