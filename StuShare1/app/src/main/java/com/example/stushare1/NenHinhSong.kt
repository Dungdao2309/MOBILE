package com.example.stushare1

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

val MauXanhSong = Color(0xFF2ecc71)
val MauXanhDam = Color(0xFF27ae60)

@Composable
fun NenHinhSong(
    noiDung: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Vẽ nền sóng bằng Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chieuRong = size.width
            val chieuCao = size.height

            // TOP WAVE
            val duongDanTren = Path().apply {
                // Bắt đầu từ góc trái trên
                moveTo(0f, 0f)
                // Vẽ sang góc phải trên
                lineTo(chieuRong, 0f)
                // Vẽ xuống một đoạn khoảng 20% chiều cao màn hình
                lineTo(chieuRong, chieuCao * 0.1f)
                // Vẽ đường cong uốn lượn về phía bên trái
                cubicTo(
                    chieuRong * 0.5f, chieuCao * 0.2f, // Điểm điều khiển 1 (kéo xuống)
                    chieuRong * 0.2f, chieuCao * 0.05f, // Điểm điều khiển 2 (kéo lên)
                    0f, chieuCao * 0.15f                // Điểm kết thúc bên trái
                )
                close()
            }

            // Tô màu gradient cho sóng trên để đẹp hơn
            drawPath(
                path = duongDanTren,
                brush = Brush.verticalGradient(
                    colors = listOf(MauXanhDam, MauXanhSong),
                    startY = 0f,
                    endY = chieuCao * 0.25f
                )
            )

            // BOTTOM WAVE
            val duongDanDuoi = Path().apply {
                // 1. Bắt đầu từ góc dưới cùng bên trái
                moveTo(0f, chieuCao)

                // 2. Vẽ sang góc dưới cùng bên phải
                lineTo(chieuRong, chieuCao)

                // 3. Vẽ lên điểm bắt đầu của sóng ở mép phải
                val chieuCaoSong = chieuCao * 0.9f
                lineTo(chieuRong, chieuCaoSong)

                // 4. Vẽ đường cong Quadratic (1 điểm điều khiển) về phía mép trái
                quadraticBezierTo(
                    chieuRong * 0.5f, chieuCao * 1f, // Điểm giữa kéo võng xuống quá đáy màn hình một chút
                    0f, chieuCaoSong                 // Điểm kết thúc bên trái (ngang hàng với bên phải)
                )

                close()
            }

            drawPath(
                path = duongDanDuoi,
                color = MauXanhSong
            )
        }

        noiDung()
    }
}