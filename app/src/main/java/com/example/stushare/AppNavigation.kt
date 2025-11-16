package com.example.stushare

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // <-- THÊM IMPORT
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation // <-- THÊM IMPORT

// Imports cho Animation
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween

// Imports Màn hình
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.features.feature_search.ui.search.SearchResultScreen
import com.example.stushare.features.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_request.ui.list.RequestListScreen
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen

// Imports cho Shared ViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import com.example.stushare.features.feature_search.ui.search.SearchViewModel


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
        startDestination = NavRoute.Home.route, // Màn hình chính vẫn là Home
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {

        // Luồng 1: Yêu cầu
        composable(
            route = NavRoute.RequestList.route,
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) {
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = {
                    navController.navigate(NavRoute.CreateRequest.route)
                }
            )
        }

        // Luồng 2: Trang chủ
        composable(route = NavRoute.Home.route) {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = {
                    // SỬA: Điều hướng đến graph lồng nhau "search_flow"
                    navController.navigate("search_flow")
                },
                onViewAllClick = { category ->
                    navController.navigate(NavRoute.ViewAll.createRoute(category))
                },
                onDocumentClick = { documentId -> // Nhận documentId (Long)
                    // Chuyển Long thành String để điều hướng
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                },
                onCreateRequestClick = {
                    navController.navigate(NavRoute.CreateRequest.route)
                }
            )
        }

        // *** SỬA LỖI LOGIC: TẠO GRAPH LỒNG NHAU CHO TÌM KIẾM ***
        searchNavGraph(navController, windowSizeClass, slideIn, slideOut, popSlideIn, popSlideOut)


        // Luồng 5: Chi tiết Tài liệu (Giữ nguyên)
        composable(
            route = NavRoute.DocumentDetail.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
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
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ViewAllScreen(
                category = category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    // Chuyển Long thành String
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                }
            )
        }

        // Luồng 7: Tạo Yêu cầu (Giữ nguyên)
        composable(
            route = NavRoute.CreateRequest.route,
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
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

// HÀM MỚI: Định nghĩa graph lồng nhau cho Tìm kiếm
private fun NavGraphBuilder.searchNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    // (Truyền các hiệu ứng vào)
    slideIn: EnterTransition,
    slideOut: ExitTransition,
    popSlideIn: EnterTransition,
    popSlideOut: ExitTransition
) {
    navigation(
        startDestination = NavRoute.Search.route, // Màn hình bắt đầu của luồng này
        route = "search_flow" // Tên của graph lồng nhau
    ) {
        // Luồng 3: Tìm kiếm (Màn hình 1 của search_flow)
        composable(
            route = NavRoute.Search.route,
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) { navBackStackEntry ->
            // Lấy NavBackStackEntry của graph lồng nhau ("search_flow")
            val searchNavGraphEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry("search_flow")
            }
            // Tạo ViewModel duy nhất cho graph này
            val searchViewModel: SearchViewModel = hiltViewModel(searchNavGraphEntry)

            SearchScreen(
                viewModel = searchViewModel, // Truyền VM
                onBackClick = { navController.popBackStack() },
                onSearchSubmit = { query ->
                    // Đã có kết quả trong VM, chỉ cần điều hướng
                    navController.navigate(NavRoute.SearchResult.createRoute(query))
                }
            )
        }

        // Luồng 4: Kết quả Tìm kiếm (Màn hình 2 của search_flow)
        composable(
            route = NavRoute.SearchResult.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popSlideIn },
            popExitTransition = { popSlideOut }
        ) { backStackEntry ->
            // Lấy NavBackStackEntry của graph lồng nhau ("search_flow")
            val searchNavGraphEntry = remember(backStackEntry) {
                navController.getBackStackEntry("search_flow")
            }
            // Lấy lại ViewModel đã được tạo ở Luồng 3
            val searchViewModel: SearchViewModel = hiltViewModel(searchNavGraphEntry)

            val query = backStackEntry.arguments?.getString("query") ?: ""

            SearchResultScreen(
                viewModel = searchViewModel, // Truyền VM
                query = query,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    // Chuyển Long thành String để điều hướng
                    navController.navigate(NavRoute.DocumentDetail.createRoute(documentId.toString()))
                }
            )
        }
    }
}