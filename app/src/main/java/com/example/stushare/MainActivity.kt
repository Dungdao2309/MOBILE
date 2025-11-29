package com.example.stushare

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density // Import Density để dùng constructor bên dưới
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.StuShareTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            // --- 1. Lắng nghe các cài đặt từ DataStore ---
            val isDarkTheme by settingsRepository.isDarkTheme
                .collectAsState(initial = isSystemInDarkTheme())

            val fontScale by settingsRepository.fontScale
                .collectAsState(initial = 1.0f)

            val languageCode by settingsRepository.languageCode
                .collectAsState(initial = "vi")

            // --- 2. Đồng bộ Ngôn ngữ hệ thống ---
            LaunchedEffect(languageCode) {
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                val newLocale = LocaleListCompat.forLanguageTags(languageCode)

                // Chỉ set lại nếu ngôn ngữ thực sự thay đổi để tránh vòng lặp
                if (currentLocales.toLanguageTags() != languageCode) {
                    AppCompatDelegate.setApplicationLocales(newLocale)
                }
            }

            // --- 3. Áp dụng Theme ---
            StuShareTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
fun MainAppScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Danh sách các màn hình sẽ hiển thị BottomBar
    val showBottomBar = listOf(
        NavRoute.Home,
        NavRoute.Search,
        NavRoute.Notification,
        NavRoute.Profile,
        NavRoute.RequestList,
        NavRoute.Upload
    ).any { route ->
        currentDestination?.hasRoute(route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // ⭐️ KỸ THUẬT: Cố định fontScale = 1.0 cho BottomBar
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density = currentDensity.density, fontScale = 1.0f)
                ) {
                    BottomNavBar(navController = navController)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavigation(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}

// ==========================================
// PHẦN CUSTOM BOTTOM NAVIGATION BAR
// ==========================================

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Sử dụng stringResource cho Đa ngôn ngữ
    val leftItems = listOf(
        NavigationItem(stringResource(R.string.nav_home), Icons.Filled.Home, Icons.Outlined.Home, NavRoute.Home),
        NavigationItem(stringResource(R.string.nav_search), Icons.Filled.Search, Icons.Outlined.Search, NavRoute.Search)
    )

    val rightItems = listOf(
        NavigationItem(stringResource(R.string.notifications), Icons.Filled.Notifications, Icons.Outlined.Notifications, NavRoute.Notification),
        NavigationItem(stringResource(R.string.nav_profile), Icons.Filled.Person, Icons.Outlined.Person, NavRoute.Profile)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nền thanh Bar
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
                // Trái
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    leftItems.forEach { item ->
                        BottomNavItem(
                            item = item,
                            isSelected = currentDestination?.hasRoute(item.route::class) == true,
                            onClick = { navigateSafe(navController, item.route) }
                        )
                    }
                }

                // Khoảng trống giữa (cho nút Upload)
                Spacer(modifier = Modifier.size(60.dp))

                // Phải
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rightItems.forEach { item ->
                        BottomNavItem(
                            item = item,
                            isSelected = currentDestination?.hasRoute(item.route::class) == true,
                            onClick = { navigateSafe(navController, item.route) }
                        )
                    }
                }
            }
        }

        // Nút Upload Nổi
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { navigateSafe(navController, NavRoute.Upload) }
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.upload_header),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(item: NavigationItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Text(
                text = item.title,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

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