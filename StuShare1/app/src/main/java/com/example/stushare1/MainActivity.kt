package com.example.stushare1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.stushare1.ui.theme.StuShare1Theme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stushare1.ui.theme.StuShare1Theme

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
                    }
                }
            }
        }
    }
}