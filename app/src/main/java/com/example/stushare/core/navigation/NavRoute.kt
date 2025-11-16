package com.example.stushare.core.navigation

// File: core/navigation/NavRoute.kt
sealed class NavRoute(val route: String) {

    // 1. Các Routes không có tham số
    data object Home : NavRoute("home_route")
    data object Search : NavRoute("search_route")
    data object RequestList : NavRoute("request_list_route")
    data object CreateRequest : NavRoute("create_request_route")

    // 2. Các Routes CÓ tham số

    // Route Chi tiết Tài liệu (documentId)
    data object DocumentDetail : NavRoute("document_detail_route/{documentId}") {
        // Hàm này được dùng để tạo route thực tế khi điều hướng
        fun createRoute(documentId: String) = "document_detail_route/$documentId"
    }

    // Route Xem Tất cả (category)
    data object ViewAll : NavRoute("view_all_route/{category}") {
        // Hàm này được dùng để tạo route thực tế khi điều hướng
        fun createRoute(category: String) = "view_all_route/$category"
    }

    // Route Kết quả Tìm kiếm (query)
    data object SearchResult : NavRoute("search_result_route/{query}") {
        // Hàm này được dùng để tạo route thực tế khi điều hướng
        fun createRoute(query: String) = "search_result_route/$query"
    }
}