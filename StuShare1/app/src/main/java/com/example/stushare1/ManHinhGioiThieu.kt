package com.example.stushare1

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// Màu xanh chủ đạo của App (lấy từ file xml cũ)
val MauXanhChuDao = Color(0xFF2ecc71)

data class DuLieuGioiThieu(val tieuDe: String, val moTa: String, val hinhAnh: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManHinhGioiThieu(boDieuHuong: NavController) {
    val danhSachTrang = listOf(
        DuLieuGioiThieu("Tìm kiếm thông minh", "Truy cập kho tài liệu khổng lồ từ mọi khoa.", R.drawable.intro1),
        DuLieuGioiThieu("Chia sẻ tri thức", "Đăng tải tài liệu và tích lũy điểm đóng góp.", R.drawable.intro2),
        DuLieuGioiThieu("Ôn tập hiệu quả", "Học tập thông minh hơn cho các kỳ thi.", R.drawable.intro3)
    )

    val trangThaiPager = rememberPagerState(pageCount = { danhSachTrang.size })
    val phamViCoroutine = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phần Slider
        HorizontalPager(
            state = trangThaiPager,
            modifier = Modifier.weight(1f)
        ) { viTri ->
            NoiDungTrang(danhSachTrang[viTri])
        }

        // Dấu chấm chỉ dẫn (Indicator)
        Row(
            Modifier.height(50.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(danhSachTrang.size) { iteration ->
                val mauSac = if (trangThaiPager.currentPage == iteration) MauXanhChuDao else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(mauSac)
                        .size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút Tiếp tục / Bắt đầu
        Button(
            onClick = {
                if (trangThaiPager.currentPage < danhSachTrang.size - 1) {
                    phamViCoroutine.launch {
                        trangThaiPager.animateScrollToPage(trangThaiPager.currentPage + 1)
                    }
                } else {
                    boDieuHuong.navigate(ManHinh.DANG_NHAP) {
                        popUpTo(ManHinh.GIOI_THIEU) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = if (trangThaiPager.currentPage == 2) "Bắt đầu" else "Tiếp Tục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Nút Bỏ qua
        TextButton(onClick = {
            boDieuHuong.navigate(ManHinh.DANG_NHAP) {
                popUpTo(ManHinh.GIOI_THIEU) { inclusive = true }
            }
        }) {
            Text("Bỏ qua", color = Color.Black)
        }
    }
}

@Composable
fun NoiDungTrang(duLieu: DuLieuGioiThieu) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = duLieu.hinhAnh),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = duLieu.tieuDe,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = duLieu.moTa,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}