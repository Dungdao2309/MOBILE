package com.example.stushare.features.feature_profile.ui.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null
)