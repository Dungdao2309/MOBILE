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

val MauXanhChuDao = Color(0xFF2ecc71)

data class DuLieuGioiThieu(val tieuDe: String, val moTa: String, val hinhAnh: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManHinhGioiThieu(boDieuHuong: NavController) {
    val danhSachTrang = listOf(
        DuLieuGioiThieu("Tìm kiếm thông minh, đúng chuyên ngành", "Truy cập kho tài liệu khổng lồ từ mọi khoa và môn học tại UTH. Tìm kiếm đề thi, slide bài giảng, và ghi chú chỉ trong vài giây.", R.drawable.intro1),
        DuLieuGioiThieu("Chia sẻ tri thức, xây dựng cộng đồng", "Trở thành một phần của cộng đồng sinh viên UTH. Đăng tải tài liệu của bạn, giúp đỡ bạn bè và tích lũy điểm đóng góp.", R.drawable.intro2),
        DuLieuGioiThieu("Ôn tập hiệu quả, chinh phục điểm cao", "Tất cả tài liệu bạn cần cho các kỳ thi đều ở đây. Hãy học tập thông minh hơn và sẵn sàng cho mọi thử thách.", R.drawable.intro3)
    )

    val trangThaiPager = rememberPagerState(pageCount = { danhSachTrang.size })
    val phamViCoroutine = rememberCoroutineScope()

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Thêm padding trên và dưới để nội dung không bị sóng che mất
                .padding(top = 120.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phần Slider
            HorizontalPager(
                state = trangThaiPager,
                modifier = Modifier.weight(1f)
            ) { viTri ->
                NoiDungTrang(danhSachTrang[viTri])
            }

            // Dấu chấm chỉ dẫn
            Row(
                Modifier
                    .height(30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(danhSachTrang.size) { iteration ->
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhSong),
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
            modifier = Modifier.size(280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = duLieu.tieuDe,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = duLieu.moTa,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}