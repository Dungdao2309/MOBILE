// File: app/src/main/java/com/example/stushare/AppNavigation.kt
package com.example.stushare

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth

// Import NavRoute
import com.example.stushare.core.navigation.NavRoute

// Import Screens (Giữ nguyên các import cũ)
import com.example.stushare.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.feature_search.ui.search.SearchResultScreen
import com.example.stushare.features.auth.ui.*
import com.example.stushare.features.feature_upload.ui.UploadScreen
import com.example.stushare.features.feature_upload.ui.UploadViewModel
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardViewModel
import com.example.stushare.features.feature_notification.ui.NotificationScreen

// Import Profile & Settings Components
import com.example.stushare.features.feature_profile.ui.ProfileScreen
import com.example.stushare.features.feature_profile.ui.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.SettingsScreen
import com.example.stushare.features.feature_profile.ui.AccountSecurityScreen
import com.example.stushare.features.feature_profile.ui.NotificationSettingsScreen
import com.example.stushare.features.feature_profile.ui.AppearanceSettingsScreen
import com.example.stushare.features.feature_profile.ui.AboutAppScreen
import com.example.stushare.features.feature_profile.ui.ContactSupportScreen
import com.example.stushare.features.feature_profile.ui.ReportViolationScreen
import com.example.stushare.features.feature_profile.ui.SwitchAccountScreen
import com.example.stushare.features.feature_profile.ui.ChangePasswordScreen
import com.example.stushare.features.feature_profile.ui.AppearanceViewModel
import com.example.stushare.features.feature_profile.ui.PersonalInfoScreen // ⭐️ Import PersonalInfoScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    val duration = 300
    val slideIn = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { it }) + fadeIn(animationSpec = tween(duration))
    val slideOut = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(duration))
    val popSlideIn = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(duration))
    val popSlideOut = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { it }) + fadeOut(animationSpec = tween(duration))

    NavHost(
        navController = navController,
        startDestination = NavRoute.Intro,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {
        // ... (Phần Auth, Home, Profile, Settings chính giữ nguyên như cũ) ...
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

        composable<NavRoute.Profile> {
            val viewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(NavRoute.Settings) },
                onNavigateToLeaderboard = { navController.navigate(NavRoute.Leaderboard) }
            )
        }

        composable<NavRoute.Settings>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            val context = LocalContext.current
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
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // --- 🔴 KHỐI CẦN SỬA: ACCOUNT SECURITY ---
        composable<NavRoute.AccountSecurity>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            val context = LocalContext.current
            AccountSecurityScreen(
                // 1. Nút Back
                onBackClick = { navController.popBackStack() },

                // 2. Thông tin cá nhân (Điều hướng đến màn hình PersonalInfo)
                onPersonalInfoClick = { navController.navigate(NavRoute.PersonalInfo) },

                // 3. Số điện thoại (Tạm thời hiện Toast nếu chưa có màn hình sửa)
                onPhoneClick = { Toast.makeText(context, "Tính năng cập nhật SĐT đang phát triển", Toast.LENGTH_SHORT).show() },

                // 4. Email (Tạm thời hiện Toast)
                onEmailClick = { Toast.makeText(context, "Tính năng cập nhật Email đang phát triển", Toast.LENGTH_SHORT).show() },

                // 5. Mật khẩu (Điều hướng đến ChangePassword - Đổi tên tham số cho khớp với file bạn gửi)
                onPasswordClick = { navController.navigate(NavRoute.ChangePassword) },

                // 6. Xóa tài khoản
                onDeleteAccountClick = { Toast.makeText(context, "Chức năng xóa tài khoản cần xác thực lại", Toast.LENGTH_SHORT).show() }
            )
        }

        // --- ⭐️ THÊM MÀN HÌNH THÔNG TIN CÁ NHÂN ---
        composable<NavRoute.PersonalInfo>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            val viewModel = hiltViewModel<ProfileViewModel>() // Hoặc ViewModel riêng
            PersonalInfoScreen(
                onBackClick = { navController.popBackStack() },
                // Giả sử PersonalInfoScreen cần viewModel, truyền vào đây
            )
        }

        // --- Các màn hình con khác của Settings (Giữ nguyên) ---
        composable<NavRoute.ChangePassword>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            ChangePasswordScreen(onBackClick = { navController.popBackStack() })
        }
        composable<NavRoute.NotificationSettings>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }
        composable<NavRoute.AppearanceSettings>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            val viewModel = hiltViewModel<AppearanceViewModel>()
            AppearanceSettingsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }
        composable<NavRoute.AboutApp>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            // Create a context to show Toasts or launch intents
            val context = LocalContext.current

            AboutAppScreen(
                onBackClick = { navController.popBackStack() },
                // Add the missing parameters below:
                onPrivacyClick = {
                    // TODO: Replace with actual navigation or Intent to open URL
                    Toast.makeText(context, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show()
                },
                onTermsClick = {
                    // TODO: Replace with actual navigation or Intent to open URL
                    Toast.makeText(context, "Opening Terms of Use...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable<NavRoute.ContactSupport>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            ContactSupportScreen(onBackClick = { navController.popBackStack() })
        }
        composable<NavRoute.ReportViolation>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            ReportViolationScreen(onBackClick = { navController.popBackStack() })
        }
        composable<NavRoute.SwitchAccount>(
            enterTransition = { slideIn }, exitTransition = { slideOut },
            popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }
        ) {
            SwitchAccountScreen(onBackClick = { navController.popBackStack() })
        }

        // ... (Phần Main App: Home, Search, Upload... giữ nguyên) ...
        composable<NavRoute.Home> {
            val context = LocalContext.current
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = { navController.navigate(NavRoute.Search) },
                onViewAllClick = { category -> navController.navigate(NavRoute.ViewAll(category)) },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else { Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show(); navController.navigate(NavRoute.Login) }
                },
                onUploadClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.Upload)
                    else { Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show(); navController.navigate(NavRoute.Login) }
                },
                onLeaderboardClick = { navController.navigate(NavRoute.Leaderboard) },
                onNotificationClick = { navController.navigate(NavRoute.Notification) }
            )
        }
        composable<NavRoute.Search>(enterTransition = { slideIn }, exitTransition = { slideOut }, popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }) {
            SearchScreen(onBackClick = { navController.popBackStack() }, onSearchSubmit = { query -> navController.navigate(NavRoute.SearchResult(query)) })
        }
        composable<NavRoute.SearchResult>(enterTransition = { slideIn }, exitTransition = { slideOut }, popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }) { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.SearchResult>()
            SearchResultScreen(onBackClick = { navController.popBackStack() }, onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId.toString())) })
        }
        composable<NavRoute.DocumentDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.DocumentDetail>()
            val context = LocalContext.current
            DocumentDetailScreen(documentId = route.documentId, onBackClick = { navController.popBackStack() }, onLoginRequired = { Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show(); navController.navigate(NavRoute.Login) })
        }
        composable<NavRoute.ViewAll> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.ViewAll>()
            ViewAllScreen(category = route.category, onBackClick = { navController.popBackStack() }, onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) })
        }
        composable<NavRoute.RequestList> {
            val context = LocalContext.current
            RequestListScreen(onBackClick = { navController.popBackStack() }, onCreateRequestClick = { if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest) else { Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show(); navController.navigate(NavRoute.Login) } })
        }
        composable<NavRoute.CreateRequest> {
            CreateRequestScreen(onBackClick = { navController.popBackStack() }, onSubmitClick = { navController.popBackStack() })
        }
        composable<NavRoute.Upload>(enterTransition = { slideIn }, exitTransition = { slideOut }, popEnterTransition = { popSlideIn }, popExitTransition = { popSlideOut }) {
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
    }
}