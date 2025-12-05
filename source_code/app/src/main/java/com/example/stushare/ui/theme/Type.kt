package com.example.stushare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    // 1. Dùng cho Tiêu đề lớn (Ví dụ: Tên màn hình, Tên tài liệu chi tiết)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Playbook: Dùng Bold cho tiêu đề để tạo điểm nhấn
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TextBlack // Sử dụng màu Off-Black đã định nghĩa
    ),

    // 2. Dùng cho Tiêu đề thẻ (Ví dụ: Tên tài liệu trong danh sách)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // SemiBold: Rõ ràng nhưng nhẹ hơn tiêu đề chính
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = TextBlack
    ),

    // 3. Dùng cho Nội dung chính (Ví dụ: Mô tả tài liệu, nội dung bài đăng)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Playbook: Dùng nét thường cho văn bản dài
        fontSize = 14.sp,
        lineHeight = 22.sp, // Playbook: Tăng line-height (~157%) để văn bản "dễ thở" hơn
        letterSpacing = 0.25.sp,
        color = TextDarkGrey // Màu xám đậm dịu mắt
    ),

    // 4. Dùng cho Chú thích nhỏ (Ví dụ: Ngày đăng, số lượt tải, tên tác giả phụ)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = TextLightGrey // Màu xám nhạt để giảm sự chú ý
    ),

    // 5. Dùng cho Chữ trên nút bấm (Button Text)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Chữ trên nút cần đậm để dễ nhận biết hành động
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
        // Màu sắc sẽ tự động theo ButtonColors, hoặc bạn có thể set cứng nếu muốn
    )
)