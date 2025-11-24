package com.example.stushare.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {

    // ==============================
    // 1. NHÓM AUTH (MỚI THÊM)
    // ==============================
    @Serializable
    data object Intro : NavRoute          // Màn hình Chào

    @Serializable
    data object Onboarding : NavRoute     // Màn hình Giới thiệu

    @Serializable
    data object Login : NavRoute          // Đăng nhập

    @Serializable
    data object Register : NavRoute       // Đăng ký

    @Serializable
    data object ForgotPassword : NavRoute // Quên mật khẩu

    @Serializable
    data object LoginSMS : NavRoute       // Đăng nhập SĐT

    @Serializable
    data class VerifyOTP(val verificationId: String) : NavRoute // Xác thực OTP

    @Serializable
    data object Profile : NavRoute        // Màn hình Cá nhân


    // ==============================
    // 2. NHÓM MAIN APP (CŨ)
    // ==============================
    @Serializable
    data object Home : NavRoute

    @Serializable
    data object Search : NavRoute

    @Serializable
    data object RequestList : NavRoute

    @Serializable
    data object CreateRequest : NavRoute

    @Serializable
    data class DocumentDetail(val documentId: String) : NavRoute

    @Serializable
    data class ViewAll(val category: String) : NavRoute

    @Serializable
    data class SearchResult(val query: String) : NavRoute
}