package com.example.stushare.ui.theme

import androidx.compose.ui.graphics.Color

// --- MÀU THƯƠNG HIỆU (BRAND COLORS) ---
// Giữ nguyên màu xanh chủ đạo của bạn
val PrimaryGreen = Color(0xFF4CAF50)
val GreenPrimary = PrimaryGreen // Alias nếu cần dùng tên khác

// Màu nền phụ trợ (dùng cho các badge hoặc box nhẹ)
// Playbook khuyên dùng màu nền trung tính hoặc nhạt để làm nổi bật nội dung[cite: 1724].
val LightGreen = Color(0xFFE6F5F0)

// --- HỆ THỐNG MÀU VĂN BẢN (TEXT COLORS - QUAN TRỌNG) ---
// Thay vì dùng Color.Black (#000000), ta dùng các màu này:

// 1. Dùng cho Tiêu đề lớn (Headings) - Màu xám đen, tạo cảm giác chuyên nghiệp[cite: 2127].
val TextBlack = Color(0xFF1A1A1A)

// 2. Dùng cho Nội dung chính (Body Text) - Màu xám đậm, dễ đọc, không gắt[cite: 2135].
val TextDarkGrey = Color(0xFF4E4E4E)

// 3. Dùng cho Chú thích phụ (Caption/Placeholder) - Màu xám nhạt hơn nhưng vẫn đảm bảo độ tương phản[cite: 2211].
// Lưu ý: Không dùng màu quá nhạt (như #A9A9A9) gây khó đọc[cite: 2208].
val TextLightGrey = Color(0xFF626262)


val BackgroundWhite = Color(0xFFFFFFFF)
val BackgroundLightGrey = Color(0xFFFAFAFA) // Dùng cho nền tổng thể app nếu muốn tách biệt với các thẻ (Card)

// --- CÁC MÀU MẶC ĐỊNH CỦA COMPOSE (Giữ lại nếu chưa muốn xóa) ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)