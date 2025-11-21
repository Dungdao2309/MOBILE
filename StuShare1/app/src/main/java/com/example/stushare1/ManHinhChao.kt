package com.example.stushare1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun ManHinhChao(boDieuHuong: NavController) {
    // Tự động chuyển màn hình sau 2 giây
    LaunchedEffect(Unit) {
        delay(2000)
        boDieuHuong.navigate(ManHinh.GIOI_THIEU) {
            popUpTo(ManHinh.CHAO) { inclusive = true }
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