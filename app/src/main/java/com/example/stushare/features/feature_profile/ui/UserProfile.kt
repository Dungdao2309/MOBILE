package com.example.stushare.features.feature_profile.ui

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? // This needs to be nullable because you passed 'null' in the screenshot
)