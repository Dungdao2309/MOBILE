package com.example.stushare.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {

    // ==========================================
    // 1. Các màn hình chính (Bottom Navigation)
    // ==========================================

    @Serializable
    data object Home : NavRoute

    @Serializable
    data object Search : NavRoute

    @Serializable
    data object RequestList : NavRoute

    @Serializable
    data object Profile : NavRoute // <-- Đã thêm màn hình Cá nhân

    // ==========================================
    // 2. Các màn hình chức năng (Không tham số)
    // ==========================================

    @Serializable
    data object CreateRequest : NavRoute

    // ==========================================
    // 3. Các màn hình chi tiết (Có tham số)
    // ==========================================

    @Serializable
    data class DocumentDetail(val documentId: String) : NavRoute

    @Serializable
    data class ViewAll(val category: String) : NavRoute

    @Serializable
    data class SearchResult(val query: String) : NavRoute
}