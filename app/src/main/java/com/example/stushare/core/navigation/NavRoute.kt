package com.example.stushare.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {

    // --- NHÓM AUTH ---
    @Serializable data object Intro : NavRoute
    @Serializable data object Onboarding : NavRoute
    @Serializable data object Login : NavRoute
    @Serializable data object Register : NavRoute
    @Serializable data object ForgotPassword : NavRoute
    @Serializable data object LoginSMS : NavRoute
    @Serializable data class VerifyOTP(val verificationId: String) : NavRoute
    @Serializable data object Profile : NavRoute

    // --- NHÓM MAIN APP ---
    @Serializable data object Home : NavRoute
    @Serializable data object Search : NavRoute
    @Serializable data object RequestList : NavRoute
    @Serializable data object CreateRequest : NavRoute
    @Serializable data class DocumentDetail(val documentId: String) : NavRoute
    @Serializable data class ViewAll(val category: String) : NavRoute
    @Serializable data class SearchResult(val query: String) : NavRoute

    // ⭐️ PHẦN QUAN TRỌNG BẠN ĐANG THIẾU 👇
    @Serializable data object Upload : NavRoute
    @Serializable data object Notification : NavRoute
    @Serializable data object Leaderboard : NavRoute
}