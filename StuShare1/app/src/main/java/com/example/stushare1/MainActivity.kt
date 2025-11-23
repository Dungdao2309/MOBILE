package com.example.stushare1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.stushare1.ui.theme.StuShare1Theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StuShare1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Khởi tạo NavController
                    val boDieuHuong = rememberNavController()

                    // Thiết lập sơ đồ điều hướng
                    NavHost(navController = boDieuHuong, startDestination = ManHinh.CHAO) {
                        composable(ManHinh.CHAO) { ManHinhChao(boDieuHuong) }
                        composable(ManHinh.GIOI_THIEU) { ManHinhGioiThieu(boDieuHuong) }
                        composable(ManHinh.DANG_NHAP) { ManHinhDangNhap(boDieuHuong) }
                        composable(ManHinh.DANG_KY) { ManHinhDangKy(boDieuHuong) }
                        composable(ManHinh.QUEN_MAT_KHAU) { ManHinhQuenMatKhau(boDieuHuong) }
                        composable(ManHinh.TRANG_CHU) { ManHinhChinh(boDieuHuong) }
                        composable(ManHinh.DANG_NHAP_SDT) { ManHinhDangNhapSDT(boDieuHuong) }
                        composable(
                            route = "man_hinh_xac_thuc_otp/{verificationId}"
                        ) { backStackEntry ->
                            // Lấy ID xác thực được truyền sang
                            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
                            ManHinhXacThucOTP(boDieuHuong, verificationId)
                        }
                    }
                }
            }
        }
    }
}