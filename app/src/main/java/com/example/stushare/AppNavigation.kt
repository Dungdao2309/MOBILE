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

// Import Screens
import com.example.stushare.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.feature_search.ui.search.SearchResultScreen
import com.example.stushare.features.auth.ui.*

// Import Tính năng Mới (Upload, Leaderboard, Notification)
import com.example.stushare.features.feature_upload.ui.UploadScreen
import com.example.stushare.features.feature_upload.ui.UploadViewModel
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardViewModel
import com.example.stushare.features.feature_notification.ui.NotificationScreen

// ⭐️ IMPORT PROFILE MỚI
import com.example.stushare.features.feature_profile.ui.ProfileScreen
import com.example.stushare.features.feature_profile.ui.ProfileViewModel

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
        // --- NHÓM AUTH (Giữ nguyên) ---
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

        // ⭐️ CẬP NHẬT: Dùng ProfileScreen xịn thay vì ManHinhCaNhan
        composable<NavRoute.Profile> {
            val viewModel = hiltViewModel<ProfileViewModel>()
            val context = LocalContext.current
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
                },
                onNavigateToLeaderboard = {
                    navController.navigate(NavRoute.Leaderboard)
                }
            )
        }

        // --- MAIN APP ---
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

        // --- CÁC MÀN HÌNH CHI TIẾT ---
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

        // --- TÍNH NĂNG MỚI ---
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