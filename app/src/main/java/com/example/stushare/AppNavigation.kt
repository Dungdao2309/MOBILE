package com.example.stushare

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

// Import NavRoute
import com.example.stushare.core.navigation.NavRoute

// Import các màn hình chức năng chính (Home, Search, Request...)
import com.example.stushare.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.feature_search.ui.search.SearchResultScreen

// --- QUAN TRỌNG: Import các màn hình Auth & Profile mới thêm ---
import com.example.stushare.features.auth.ui.* @Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    val duration = 300
    // Các biến Animation
    val slideIn = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { it }) + fadeIn(animationSpec = tween(duration))
    val slideOut = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(duration))
    val popSlideIn = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(duration))
    val popSlideOut = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { it }) + fadeOut(animationSpec = tween(duration))

    NavHost(
        navController = navController,
        startDestination = NavRoute.Intro, // <--- ĐỔI TỪ HOME SANG INTRO ĐỂ CHẠY LUỒNG ĐĂNG NHẬP TRƯỚC
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {

        // ==========================================
        // KHU VỰC 1: XÁC THỰC (AUTHENTICATION)
        // ==========================================

        // 1. Màn hình Chào (Splash)
        composable<NavRoute.Intro> {
            ManHinhChao(navController)
        }

        // 2. Màn hình Giới thiệu (Onboarding)
        composable<NavRoute.Onboarding> {
            ManHinhGioiThieu(navController)
        }

        // 3. Màn hình Đăng nhập
        composable<NavRoute.Login> {
            ManHinhDangNhap(navController)
        }

        // 4. Màn hình Đăng ký
        composable<NavRoute.Register> {
            ManHinhDangKy(navController)
        }

        // 5. Màn hình Quên mật khẩu
        composable<NavRoute.ForgotPassword> {
            ManHinhQuenMatKhau(navController)
        }

        // 6. Màn hình Đăng nhập SĐT
        composable<NavRoute.LoginSMS> {
            ManHinhDangNhapSDT(navController)
        }

        // 7. Màn hình Xác thực OTP (Có nhận tham số verificationId)
        composable<NavRoute.VerifyOTP> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoute.VerifyOTP>()
            ManHinhXacThucOTP(navController, args.verificationId)
        }


        // ==========================================
        // KHU VỰC 2: CHỨC NĂNG CHÍNH (MAIN APP)
        // ==========================================

        // 8. Màn hình Home
        composable<NavRoute.Home> {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = { navController.navigate(NavRoute.Search) },
                onViewAllClick = { category ->
                    navController.navigate(NavRoute.ViewAll(category))
                },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail(documentId))
                },
                onCreateRequestClick = { navController.navigate(NavRoute.CreateRequest) }
            )
        }

        // 9. Màn hình Search
        composable<NavRoute.Search>(
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSearchSubmit = { query ->
                    navController.navigate(NavRoute.SearchResult(query))
                }
            )
        }

        // 10. Màn hình Kết quả Tìm kiếm
        composable<NavRoute.SearchResult>(
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.SearchResult>()
            SearchResultScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail(documentId.toString()))
                }
            )
        }

        // 11. Màn hình Chi tiết Tài liệu
        composable<NavRoute.DocumentDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.DocumentDetail>()
            DocumentDetailScreen(
                documentId = route.documentId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 12. Màn hình Xem tất cả
        composable<NavRoute.ViewAll> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.ViewAll>()
            ViewAllScreen(
                category = route.category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail(documentId))
                }
            )
        }

        // 13. Màn hình Danh sách Yêu cầu
        composable<NavRoute.RequestList> {
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = { navController.navigate(NavRoute.CreateRequest) }
            )
        }

        // 14. Màn hình Tạo Yêu cầu
        composable<NavRoute.CreateRequest> {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.popBackStack() }
            )
        }

        // 15. Màn hình Cá nhân (Profile) - MỚI
        composable<NavRoute.Profile> {
            ManHinhCaNhan(navController)
        }
    }
}