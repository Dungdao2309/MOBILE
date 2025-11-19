package com.example.stushare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    // 1. TIÊU ĐỀ LỚN (Dùng cho Header màn hình)
    // Nguyên lý: Dùng Font đậm (Bold) và màu tối nhất để tạo điểm nhấn [cite: 2142, 2104]
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp, // Line-height thấp (~1.2x) cho tiêu đề lớn [cite: 2316]
        color = TextBlack
    ),

    // 2. TIÊU ĐỀ TRUNG BÌNH (Dùng cho tên tài liệu trong Card)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextBlack
    ),

    // 3. TIÊU ĐỀ NHỎ (Dùng cho các mục con)
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextBlack
    ),

    // 4. NỘI DUNG CHÍNH (Body text)
    // Nguyên lý: Dùng màu xám dịu mắt và Line-height rộng (~1.5x) để dễ đọc [cite: 2104, 2307]
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp, // 14px * 1.6 ≈ 22px (Tăng khả năng đọc)
        color = TextGrey
    ),

    // 5. CHÚ THÍCH NHỎ (Caption, Label)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextLightGrey
    )
)