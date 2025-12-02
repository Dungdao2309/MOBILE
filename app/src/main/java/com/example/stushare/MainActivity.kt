package com.example.stushare

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.features.feature_home.ui.components.BottomNavBar // 🟢 QUAN TRỌNG: Import cái này
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

    // 🟢 MỚI: Lấy MainViewModel để đếm tin nhắn chưa đọc
    // (Đảm bảo bạn đã tạo file MainViewModel.kt như hướng dẫn trước)
    val mainViewModel: MainViewModel = hiltViewModel()
    val unreadCount by mainViewModel.unreadCount.collectAsState(initial = 0)

    // Danh sách các màn hình sẽ hiển thị BottomBar
    val showBottomBar = listOf(
        NavRoute.Home,
        NavRoute.Search,
        NavRoute.Notification,
        NavRoute.Profile,
        NavRoute.RequestList,
        // NavRoute.Upload (Thường thì màn hình Upload nên ẩn BottomBar để tập trung)
    ).any { route ->
        currentDestination?.hasRoute(route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Cố định fontScale = 1.0 cho BottomBar
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density = currentDensity.density, fontScale = 1.0f)
                ) {
                    // 🟢 GỌI BottomNavBar XỊN VÀ TRUYỀN SỐ LƯỢNG
                    BottomNavBar(
                        navController = navController,
                        unreadNotificationCount = unreadCount // Truyền biến này vào
                    )
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

// ❌ ĐÃ XÓA TOÀN BỘ CODE BottomNavBar CŨ Ở ĐÂY
// Vì chúng ta đã import BottomNavBar từ file 'features/feature_home/ui/components/BottomNavBar.kt'