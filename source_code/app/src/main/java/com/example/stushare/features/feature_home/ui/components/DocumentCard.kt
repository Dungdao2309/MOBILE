package com.example.stushare.features.feature_home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star // Dùng icon bo tròn (Rounded) cho mềm mại
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stushare.core.data.models.Document
import com.example.stushare.ui.theme.* // Import các màu và font mới

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Logic dịch tên loại tài liệu (Giữ nguyên)
    val displayType = remember(document.type) {
        when (document.type) {
            "exam_review" -> "Ôn thi"
            "book", "Sách" -> "Sách"
            "lecture", "slide" -> "Bài giảng"
            else -> document.type
        }
    }

    val safeRating = document.rating ?: 0.0
    val hasRating = safeRating > 0.0

    // --- CẤU TRÚC CARD MỚI ---
    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp) // Giữ độ rộng cố định
            .shadow(
                elevation = 8.dp, // Độ cao bóng đổ
                shape = RoundedCornerShape(16.dp), // Bo góc mềm mại
                spotColor = Color(0xFF000000).copy(alpha = 0.1f), // UXPeak: Bóng màu nhạt (10%), tránh đen kịt
                ambientColor = Color(0xFF000000).copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Nền trắng sạch sẽ
        elevation = CardDefaults.cardElevation(0.dp) // Tắt elevation mặc định để dùng shadow custom ở trên
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp) // UXPeak: Tăng khoảng trắng (Whitespace) để nội dung dễ thở
        ) {
            // 1. ẢNH BÌA (Cover Image)
            AsyncImage(
                model = document.imageUrl,
                contentDescription = document.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Tỉ lệ 1:1 vuông vắn
                    .clip(RoundedCornerShape(12.dp)) // Bo góc ảnh khớp với card
                    .background(Color.LightGray.copy(alpha = 0.2f)), // Màu nền chờ khi load ảnh
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. BADGE LOẠI TÀI LIỆU (Category Badge)
            // Thay vì chỉ để text, ta đặt nó trong một Surface nhỏ (Chip)
            Surface(
                color = PrimaryGreen.copy(alpha = 0.1f), // Nền xanh rất nhạt
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = displayType.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = PrimaryGreen, // Chữ xanh đậm
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. TIÊU ĐỀ (Title)
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium, // Dùng style SemiBold đã định nghĩa
                color = TextBlack, // Màu xám đen chuyên nghiệp
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 44.dp) // Cố định chiều cao tối thiểu để các card đều nhau
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. FOOTER (Rating & Lượt tải)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hiển thị Rating hoặc nhãn "Mới"
                if (hasRating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(bottom = 2.dp),
                            tint = Color(0xFFFFC107) // Màu vàng ngôi sao
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(safeRating),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                    }
                } else {
                    Text(
                        text = "Mới",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightGrey
                    )
                }

                // Hiển thị lượt tải (Thông tin phụ -> Màu nhạt)
                Text(
                    text = "${document.downloads} tải",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGrey // Màu xám nhạt (Visual Hierarchy)
                )
            }
        }
    }
}