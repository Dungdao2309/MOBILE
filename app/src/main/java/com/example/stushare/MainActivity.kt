package com.example.stushare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
// 1. IMPORT CÁC THƯ VIỆN WINDOW SIZE CLASSES
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.ui.theme.StuShareTheme
import com.example.stushare.core.navigation.NavRoute // <-- Import NavRoute đã tạo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 2. Thêm OptIn cho API thử nghiệm
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 3. TÍNH TOÁN KÍCH THƯỚC CỬA SỔ
            val windowSizeClass = calculateWindowSizeClass(this)

            StuShareTheme {
                // 4. Truyền WindowSizeClass vào App chính
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    // 5. Nhận WindowSizeClass
    windowSizeClass: WindowSizeClass
) {
    // Sử dụng NavRoute đã cải tiến để định nghĩa startDestination
    val startRoute = NavRoute.Home.route

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Kiểm tra currentRoute dựa trên NavRoute (an toàn hơn)
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Hiển thị BottomBar chỉ khi ở các màn hình chính (có thể mở rộng thêm)
            if (currentRoute == NavRoute.Home.route ||
                currentRoute == NavRoute.Search.route ||
                currentRoute == NavRoute.RequestList.route) {

                NavigationBar {
                    // Nút HOME
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Trang chủ") },
                        selected = currentRoute == NavRoute.Home.route,
                        onClick = {
                            navController.navigate(NavRoute.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )

                    // Nút SEARCH
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, "Search") },
                        label = { Text("Tìm kiếm") },
                        selected = currentRoute == NavRoute.Search.route,
                        onClick = {
                            navController.navigate(NavRoute.Search.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )

                    // Nút YÊU CẦU
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ListAlt, "Request") },
                        label = { Text("Yêu cầu") },
                        selected = currentRoute == NavRoute.RequestList.route,
                        onClick = {
                            navController.navigate(NavRoute.RequestList.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 6. Truyền WindowSizeClass xuống AppNavigation
        AppNavigation(
            navController = navController,
            windowSizeClass = windowSizeClass, // <-- THÊM THAM SỐ NÀY
            modifier = Modifier.padding(innerPadding)
        )
    }
}