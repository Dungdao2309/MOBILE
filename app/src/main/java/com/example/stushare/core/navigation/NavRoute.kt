package com.example.stushare.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {

    // 1. Các màn hình không có tham số
    @Serializable
    data object Home : NavRoute

    @Serializable
    data object Search : NavRoute

    @Serializable
    data object RequestList : NavRoute

    @Serializable
    data object CreateRequest : NavRoute

    // 2. Các màn hình CÓ tham số (Dùng biến trực tiếp!)

    @Serializable
    data class DocumentDetail(val documentId: String) : NavRoute

    @Serializable
    data class ViewAll(val category: String) : NavRoute

    @Serializable
    data class SearchResult(val query: String) : NavRoute
}