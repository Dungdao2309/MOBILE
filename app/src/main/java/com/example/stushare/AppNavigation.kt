package com.example.stushare

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth

// Import NavRoute
import com.example.stushare.core.navigation.NavRoute

// Import Screens (Auth)
import com.example.stushare.features.auth.ui.*

// Import Screens (Main)
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.feature_search.ui.search.SearchResultScreen
import com.example.stushare.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_upload.ui.UploadScreen
import com.example.stushare.features.feature_upload.ui.UploadViewModel
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardViewModel
import com.example.stushare.features.feature_notification.ui.NotificationScreen

// Import Screens (Profile & Settings)
import com.example.stushare.features.feature_profile.ui.main.ProfileScreen
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.settings.SettingsScreen
import com.example.stushare.features.feature_profile.ui.account.AccountSecurityScreen
import com.example.stushare.features.feature_profile.ui.account.PersonalInfoScreen
import com.example.stushare.features.feature_profile.ui.account.ChangePasswordScreen
import com.example.stushare.features.feature_profile.ui.account.SwitchAccountScreen
import com.example.stushare.features.feature_profile.ui.settings.notification.NotificationSettingsScreen
import com.example.stushare.features.feature_profile.ui.settings.appearance.AppearanceSettingsScreen
import com.example.stushare.features.feature_profile.ui.settings.appearance.AppearanceViewModel
import com.example.stushare.features.feature_profile.ui.legal.AboutAppScreen
import com.example.stushare.features.feature_profile.ui.legal.ContactSupportScreen
import com.example.stushare.features.feature_profile.ui.legal.ReportViolationScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    // --- CẤU HÌNH ANIMATION CHUYỂN CẢNH ---
    val duration = 300
    val enterTransition = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { it }) + fadeIn(animationSpec = tween(duration))
    val exitTransition = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(duration))
    val popEnterTransition = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(duration))
    val popExitTransition = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { it }) + fadeOut(animationSpec = tween(duration))

    NavHost(
        navController = navController,
        startDestination = NavRoute.Intro, // Hoặc màn hình Splash nếu có
        modifier = modifier,
        // Hiệu ứng mặc định (Fade) cho các màn hình không cấu hình riêng
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {
        // ==========================================
        // 1. AUTHENTICATION (Đăng nhập/Đăng ký)
        // ==========================================
        composable<NavRoute.Intro> { ManHinhChao(navController) }
        composable<NavRoute.Onboarding> { ManHinhGioiThieu(navController) }
        composable<NavRoute.Login> { ManHinhDangNhap(navController) }
        composable<NavRoute.Register> { ManHinhDangKy(navController) }
        composable<NavRoute.ForgotPassword> { ManHinhQuenMatKhau(navController) }
        composable<NavRoute.LoginSMS> { ManHinhDangNhapSDT(navController) }
        composable<NavRoute.VerifyOTP> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoute.VerifyOTP>()
            ManHinhXacThucOTP(navController, args.verificationId)
        }

        // ==========================================
        // 2. MAIN FEATURES (Home, Search, Upload...)
        // ==========================================
        composable<NavRoute.Home> {
            val context = LocalContext.current
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = { navController.navigate(NavRoute.Search) },
                onViewAllClick = { category -> navController.navigate(NavRoute.ViewAll(category)) },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
                    }
                },
                onUploadClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.Upload)
                    else {
                        Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
                    }
                },
                onLeaderboardClick = { navController.navigate(NavRoute.Leaderboard) },
                onNotificationClick = { navController.navigate(NavRoute.Notification) }
            )
        }

        composable<NavRoute.Search>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSearchSubmit = { query -> navController.navigate(NavRoute.SearchResult(query)) }
            )
        }

        composable<NavRoute.SearchResult>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            SearchResultScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId.toString())) }
            )
        }

        composable<NavRoute.DocumentDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.DocumentDetail>()
            val context = LocalContext.current
            DocumentDetailScreen(
                documentId = route.documentId,
                onBackClick = { navController.popBackStack() },
                onLoginRequired = {
                    Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login)
                }
            )
        }

        composable<NavRoute.ViewAll> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.ViewAll>()
            ViewAllScreen(
                category = route.category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) }
            )
        }

        composable<NavRoute.RequestList> {
            val context = LocalContext.current
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
                    }
                }
            )
        }

        composable<NavRoute.CreateRequest> {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.Upload>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val viewModel = hiltViewModel<UploadViewModel>()
            UploadScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.Leaderboard> {
            val viewModel = hiltViewModel<LeaderboardViewModel>()
            LeaderboardScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.Notification> {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }

        // ==========================================
        // 3. PROFILE & SETTINGS (Tài khoản & Cài đặt)
        // ==========================================
        composable<NavRoute.Profile> {
            val viewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(NavRoute.Settings) },
                onNavigateToLeaderboard = { navController.navigate(NavRoute.Leaderboard) },
                onNavigateToLogin = { navController.navigate(NavRoute.Login) },
                onNavigateToRegister = { navController.navigate(NavRoute.Register) }
            )
        }

        composable<NavRoute.Settings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>() // Lấy ViewModel để gọi signout nếu cần thiết

            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onAccountSecurityClick = { navController.navigate(NavRoute.AccountSecurity) },
                onNotificationSettingsClick = { navController.navigate(NavRoute.NotificationSettings) },
                onAppearanceSettingsClick = { navController.navigate(NavRoute.AppearanceSettings) },
                onAboutAppClick = { navController.navigate(NavRoute.AboutApp) },
                onContactSupportClick = { navController.navigate(NavRoute.ContactSupport) },
                onReportViolationClick = { navController.navigate(NavRoute.ReportViolation) },
                onSwitchAccountClick = { navController.navigate(NavRoute.SwitchAccount) },
                onLogoutClick = {
                    // 1. Xử lý đăng xuất Firebase
                    FirebaseAuth.getInstance().signOut()

                    // 2. Thông báo
                    Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

                    // 3. Điều hướng về Login và XÓA SẠCH Back Stack để không back lại được
                    navController.navigate(NavRoute.Login) {
                        popUpTo(0) { inclusive = true } // Xóa hết lịch sử
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- CÁC MÀN HÌNH CON CỦA SETTINGS ---

        // 3.1 Account & Security
        composable<NavRoute.AccountSecurity>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            AccountSecurityScreen(
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate(NavRoute.PersonalInfo) },
                onPhoneClick = { Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show() },
                onEmailClick = { Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show() },
                onPasswordClick = { navController.navigate(NavRoute.ChangePassword) },
                onDeleteAccountClick = { Toast.makeText(context, "Chức năng cần xác thực lại", Toast.LENGTH_SHORT).show() }
            )
        }

        composable<NavRoute.PersonalInfo>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            PersonalInfoScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.ChangePassword>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ChangePasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.SwitchAccount>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            SwitchAccountScreen(onBackClick = { navController.popBackStack() })
        }

        // 3.2 Notification & Appearance
        composable<NavRoute.NotificationSettings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.AppearanceSettings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val viewModel = hiltViewModel<AppearanceViewModel>()
            AppearanceSettingsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // 3.3 Legal & Support
        composable<NavRoute.AboutApp>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            AboutAppScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyClick = { Toast.makeText(context, "Đang mở chính sách...", Toast.LENGTH_SHORT).show() },
                onTermsClick = { Toast.makeText(context, "Đang mở điều khoản...", Toast.LENGTH_SHORT).show() }
            )
        }

        composable<NavRoute.ContactSupport>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ContactSupportScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.ReportViolation>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ReportViolationScreen(onBackClick = { navController.popBackStack() })
        }
    }
}