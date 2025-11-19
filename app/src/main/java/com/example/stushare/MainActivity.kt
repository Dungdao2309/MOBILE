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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.ui.theme.StuShareTheme
import com.example.stushare.core.navigation.NavRoute
import dagger.hilt.android.AndroidEntryPoint

// 👇👇👇 PHẦN QUAN TRỌNG BẠN ĐANG THIẾU 👇👇👇
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Tính toán kích thước màn hình
            val windowSizeClass = calculateWindowSizeClass(this)

            StuShareTheme {
                // Gọi hàm giao diện chính
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}
// 👆👆👆 HẾT PHẦN THIẾU 👆👆👆

@Composable
fun MainAppScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // Kiểm tra xem đang ở màn hình nào bằng hasRoute (Type-Safe)
            val isHome = currentDestination?.hasRoute<NavRoute.Home>() == true
            val isSearch = currentDestination?.hasRoute<NavRoute.Search>() == true
            val isRequest = currentDestination?.hasRoute<NavRoute.RequestList>() == true

            // Chỉ hiện BottomBar ở 3 màn hình chính
            if (isHome || isSearch || isRequest) {
                NavigationBar {
                    // Nút HOME
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Trang chủ") },
                        selected = isHome,
                        onClick = {
                            navController.navigate(NavRoute.Home) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Nút SEARCH
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, "Search") },
                        label = { Text("Tìm kiếm") },
                        selected = isSearch,
                        onClick = {
                            navController.navigate(NavRoute.Search) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Nút YÊU CẦU
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ListAlt, "Request") },
                        label = { Text("Yêu cầu") },
                        selected = isRequest,
                        onClick = {
                            navController.navigate(NavRoute.RequestList) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            windowSizeClass = windowSizeClass,
            modifier = Modifier.padding(innerPadding)
        )
    }
}