package com.example.stushare.features.feature_home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Cấu hình danh sách các màn hình trong thanh điều hướng
    // Lưu ý: Upload nằm riêng ở giữa, không nằm trong list này để dễ xử lý layout
    val leftItems = listOf(
        NavigationItem("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home, NavRoute.Home),
        NavigationItem("Tìm kiếm", Icons.Filled.Search, Icons.Outlined.Search, NavRoute.Search)
    )

    val rightItems = listOf(
        // Bạn có thể thay Notification bằng RequestList nếu muốn giống dự án cũ hoàn toàn
        NavigationItem("Thông báo", Icons.Filled.Notifications, Icons.Outlined.Notifications, NavRoute.Notification),
        NavigationItem("Cá nhân", Icons.Filled.Person, Icons.Outlined.Person, NavRoute.Profile)
    )

    // Tổng chiều cao bao gồm cả phần nút nổi
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // Đủ cao để chứa nút nổi
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Phần nền thanh điều hướng (Màu xanh, bo góc trên)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = PrimaryGreen,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nhóm icon bên trái
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    leftItems.forEach { item ->
                        BottomNavItem(item, currentDestination?.hasRoute(item.route::class) == true) {
                            navigateSafe(navController, item.route)
                        }
                    }
                }

                // Khoảng trống ở giữa cho nút Upload (56dp + margin)
                Spacer(modifier = Modifier.size(60.dp))

                // Nhóm icon bên phải
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rightItems.forEach { item ->
                        BottomNavItem(item, currentDestination?.hasRoute(item.route::class) == true) {
                            navigateSafe(navController, item.route)
                        }
                    }
                }
            }
        }

        // 2. Nút Upload nổi (Nằm đè lên trên ở vị trí giữa)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter) // Căn lên đỉnh của Box cha 100dp
                .offset(y = 10.dp) // Đẩy xuống một chút để khớp vị trí đẹp
                .size(64.dp) // Kích thước vòng ngoài (viền trắng)
                .clip(CircleShape)
                .background(Color.White) // Viền trắng bao quanh nút xanh
                .clickable { navigateSafe(navController, NavRoute.Upload) }
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Vòng tròn xanh bên trong
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Hàm hỗ trợ item con
@Composable
fun BottomNavItem(item: NavigationItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Tắt hiệu ứng ripple mặc định để đẹp hơn
            ) { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            // Nếu chọn: Màu trắng. Không chọn: Màu trắng mờ
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Text(
                text = item.title,
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

// Hàm điều hướng an toàn
fun navigateSafe(navController: NavController, route: NavRoute) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: NavRoute
)