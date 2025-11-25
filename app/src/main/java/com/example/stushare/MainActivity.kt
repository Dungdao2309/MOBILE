package com.example.stushare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.StuShareTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            StuShareTheme {
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}

// 1. Cấu trúc dữ liệu cho mục BottomBar
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: NavRoute
)

@Composable
fun MainAppScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()

    // Theo dõi màn hình hiện tại để đổi màu icon
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 2. Danh sách các Tab
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
            ),
            BottomNavItem(
                title = "Cá nhân",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                route = NavRoute.Profile
            )
        )
    }

    // 3. Chỉ hiện BottomBar khi ở các màn hình chính
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        // --- CẢI TIẾN 1: Bo tròn 2 góc trên (30dp) ---
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),

                    // --- CẢI TIẾN 2: Đổi nền sang màu Xanh ---
                    containerColor = PrimaryGreen,
                    contentColor = Color.White, // Màu mặc định của nội dung là trắng
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        // Kiểm tra tab nào đang được chọn
                        val isSelected = currentDestination?.hasRoute(item.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
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
                                // --- CẢI TIẾN 3: Phối màu cho nền xanh ---

                                // Khi ĐƯỢC CHỌN: Icon màu xanh (để nổi trên nền trắng), Chữ màu trắng
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = Color.White,

                                // Indicator (cái bầu dục bao quanh icon) chuyển thành màu Trắng
                                indicatorColor = Color.White,

                                // Khi CHƯA CHỌN: Icon và Chữ màu trắng mờ (0.6f) để chìm xuống nền xanh
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
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