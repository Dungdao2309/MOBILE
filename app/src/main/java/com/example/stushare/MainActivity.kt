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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.StuShareTheme
import dagger.hilt.android.AndroidEntryPoint

// Cấu hình Dagger Hilt cho Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Tính toán kích thước màn hình (để hỗ trợ responsive nếu cần)
            val windowSizeClass = calculateWindowSizeClass(this)

            StuShareTheme {
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}

// 1. Cấu trúc dữ liệu cho một mục trên thanh điều hướng
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,   // Icon khi được chọn (Đậm)
    val unselectedIcon: ImageVector, // Icon khi không chọn (Viền)
    val route: NavRoute              // Đường dẫn màn hình
)

@Composable
fun MainAppScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()

    // Lấy màn hình hiện tại để biết nút nào đang active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 2. Danh sách các màn hình trên BottomBar
    val bottomNavItems = remember {
        listOf(
            BottomNavItem(
                title = "Trang chủ",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                route = NavRoute.Home
            ),
            BottomNavItem(
                title = "Tìm kiếm",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                route = NavRoute.Search
            ),
            BottomNavItem(
                title = "Yêu cầu",
                selectedIcon = Icons.Filled.ListAlt,
                unselectedIcon = Icons.Outlined.ListAlt,
                route = NavRoute.RequestList
            )
        )
    }

    // 3. Logic kiểm tra: Chỉ hiện BottomBar nếu màn hình hiện tại nằm trong danh sách trên
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White, // Nền trắng sạch sẽ
                    contentColor = PrimaryGreen
                ) {
                    bottomNavItems.forEach { item ->
                        // Kiểm tra nút này có đang được chọn không
                        val isSelected = currentDestination?.hasRoute(item.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Quay về màn hình chính (Home) để tránh chồng chất stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Tránh mở lại cùng một màn hình nhiều lần
                                    launchSingleTop = true
                                    // Khôi phục trạng thái (vị trí cuộn...) khi quay lại
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    // Đổi icon dựa trên trạng thái chọn
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                indicatorColor = PrimaryGreen.copy(alpha = 0.1f), // Nền tròn mờ quanh icon khi chọn
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Truyền padding vào AppNavigation để nội dung không bị che bởi BottomBar
        AppNavigation(
            navController = navController,
            windowSizeClass = windowSizeClass,
            modifier = Modifier.padding(innerPadding)
        )
    }
}