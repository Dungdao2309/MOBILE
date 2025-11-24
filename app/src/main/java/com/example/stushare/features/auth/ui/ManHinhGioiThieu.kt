package com.example.stushare.features.auth.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.stushare.R  // Quan trọng: Để lấy ảnh intro1, intro2...
import com.example.stushare.core.navigation.NavRoute // Import hệ thống điều hướng mới

// Dữ liệu cho từng trang slide
data class DuLieuGioiThieu(val tieuDe: String, val moTa: String, val hinhAnh: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManHinhGioiThieu(boDieuHuong: NavController) {
    // Đảm bảo bạn đã copy ảnh intro1, intro2, intro3 vào thư mục res/drawable
    val danhSachTrang = listOf(
        DuLieuGioiThieu(
            "Tìm kiếm thông minh",
            "Truy cập kho tài liệu khổng lồ từ mọi khoa và môn học tại UTH. Tìm kiếm đề thi, slide bài giảng nhanh chóng.",
            R.drawable.intro1
        ),
        DuLieuGioiThieu(
            "Chia sẻ tri thức",
            "Trở thành một phần của cộng đồng sinh viên UTH. Đăng tải tài liệu, giúp đỡ bạn bè và tích lũy điểm.",
            R.drawable.intro2
        ),
        DuLieuGioiThieu(
            "Ôn tập hiệu quả",
            "Tất cả tài liệu bạn cần cho các kỳ thi đều ở đây. Học tập thông minh hơn và sẵn sàng cho mọi thử thách.",
            R.drawable.intro3
        )
    )

    val trangThaiPager = rememberPagerState(pageCount = { danhSachTrang.size })
    val phamViCoroutine = rememberCoroutineScope()

    // Hàm chuyển sang màn hình Đăng nhập
    fun denManHinhDangNhap() {
        boDieuHuong.navigate(NavRoute.Login) {
            // Xóa màn hình Giới thiệu khỏi lịch sử để back không quay lại đây nữa
            popUpTo(NavRoute.Onboarding) { inclusive = true }
            popUpTo(NavRoute.Intro) { inclusive = true }
        }
    }

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 50.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Slider hình ảnh
            HorizontalPager(
                state = trangThaiPager,
                modifier = Modifier.weight(1f)
            ) { viTri ->
                NoiDungTrang(danhSachTrang[viTri])
            }

            // 2. Dấu chấm chỉ dẫn (Indicator)
            Row(
                Modifier
                    .height(30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(danhSachTrang.size) { iteration ->
                    // MauXanhSong được lấy từ file NenHinhSong.kt (cùng package nên không cần import)
                    val mauSac = if (trangThaiPager.currentPage == iteration) MauXanhSong else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(mauSac)
                            .size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Nút Tiếp tục / Bắt đầu
            Button(
                onClick = {
                    if (trangThaiPager.currentPage < danhSachTrang.size - 1) {
                        // Nếu chưa đến trang cuối -> Lướt tiếp
                        phamViCoroutine.launch {
                            trangThaiPager.animateScrollToPage(trangThaiPager.currentPage + 1)
                        }
                    } else {
                        // Nếu là trang cuối -> Sang Đăng nhập
                        denManHinhDangNhap()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhSong),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (trangThaiPager.currentPage == danhSachTrang.size - 1) "Bắt đầu ngay" else "Tiếp Tục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. Nút Bỏ qua
            TextButton(onClick = { denManHinhDangNhap() }) {
                Text("Bỏ qua", color = Color.Gray)
            }
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
            modifier = Modifier.size(280.dp) // Điều chỉnh kích thước ảnh cho phù hợp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = duLieu.tieuDe,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = duLieu.moTa,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 24.sp
        )
    }
}