package com.example.stushare.features.feature_home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stushare.core.data.models.Document
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp) // Tăng nhẹ chiều rộng để cân đối với padding mới
            .wrapContentHeight(), // Chiều cao linh hoạt theo nội dung
        shape = RoundedCornerShape(16.dp), // Bo góc lớn hơn (Modern UI)
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Nền trắng sạch sẽ
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Shadow nhẹ tạo độ nổi
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp) // Tăng padding (Whitespace) để tạo cảm giác thoáng đãng
        ) {
            // 1. Ảnh bìa (Vuông vức & Bo góc)
            AsyncImage(
                model = document.imageUrl,
                contentDescription = document.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Tỷ lệ 1:1
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tiêu đề (Sử dụng Style mới: titleMedium)
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 48.dp) // Cố định chiều cao 2 dòng để các thẻ thẳng hàng
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Loại tài liệu (Màu xanh chủ đạo, bỏ chữ "Loại:" để đơn giản hóa)
            Text(
                text = document.type,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryGreen,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Footer (Rating & Lượt tải)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107) // Màu vàng ngôi sao
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(document.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lượt tải (Dùng label nhỏ, màu xám nhạt)
                Text(
                    text = "${document.downloads} tải",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}