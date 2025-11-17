// File: AppNavigation.kt (Đã cải tiến - Tách biệt trách nhiệm)

package com.example.stushare

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
// ⭐️ XÓA: import androidx.compose.runtime.remember (Không cần nữa)
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// ⭐️ XÓA: import androidx.navigation.navigation (Không cần graph lồng nhau nữa)

// Imports cho Animation
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween

// Imports Màn hình
import com.example.stushare.feature_search.ui.search.SearchResultScreen
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.features.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen

// ⭐️ XÓA: Imports cho Shared ViewModel (Không cần nữa)
// import androidx.hilt.navigation.compose.hiltViewModel
// import androidx.navigation.NavGraphBuilder
// import com.example.stushare.features.feature_search.ui.search.SearchViewModel


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
        startDestination = NavRoute.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {

        // Luồng 1: Yêu cầu (Giữ nguyên)
        composable(
            route = NavRoute.RequestList.route,
            // ... (animations giữ nguyên)
        ) {
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = {
                    navController.navigate(NavRoute.CreateRequest.route)
                }
            )
        }

        // Luồng 2: Trang chủ (Cập nhật onSearchClick)
        composable(route = NavRoute.Home.route) {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = {
                    // ⭐️ THAY ĐỔI: Điều hướng đến MÀN HÌNH tìm kiếm
                    navController.navigate(NavRoute.Search.route)
                },
                onViewAllClick = { category ->
                    navController.navigate(NavRoute.ViewAll.createRoute(category))
                },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                },
                onCreateRequestClick = {
                    navController.navigate(NavRoute.CreateRequest.route)
                }
            )
        }

        // ⭐️ XÓA BỎ HOÀN TOÀN: searchNavGraph(...)

        // ⭐️ THÊM MỚI: Luồng 3: Tìm kiếm (Giờ là màn hình độc lập)
        composable(
            route = NavRoute.Search.route,
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) {
            // SearchScreen (đã cập nhật) sẽ tự động lấy SearchViewModel
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSearchSubmit = { query ->
                    // Điều hướng đến màn hình kết quả với từ khóa
                    navController.navigate(NavRoute.SearchResult.createRoute(query))
                }
            )
        }

        // ⭐️ THÊM MỚI: Luồng 4: Kết quả Tìm kiếm (Giờ là màn hình độc lập)
        composable(
            route = NavRoute.SearchResult.route, // Giả sử route này là "search_result/{query}"
            arguments = listOf(navArgument("query") { type = NavType.StringType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) {
            // SearchResultScreen (đã cập nhật) sẽ tự động
            // lấy SearchResultViewModel (mới) bằng Hilt
            SearchResultScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                }
            )
        }


        // Luồng 5: Chi tiết Tài liệu (Giữ nguyên)
        composable(
            route = NavRoute.DocumentDetail.route,
            // ... (code giữ nguyên)
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            DocumentDetailScreen(
                documentId = documentId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Luồng 6: Xem tất cả (Giữ nguyên)
        composable(
            route = NavRoute.ViewAll.route,
            // ... (code giữ nguyên)
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ViewAllScreen(
                category = category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                }
            )
        }

        // Luồng 7: Tạo Yêu cầu (Giữ nguyên)
        composable(
            route = NavRoute.CreateRequest.route,
            // ... (code giữ nguyên)
        ) {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// ⭐️ XÓA BỎ HOÀN TOÀN: Hàm "private fun NavGraphBuilder.searchNavGraph(...)"
// (Toàn bộ hàm này đã bị xóa)