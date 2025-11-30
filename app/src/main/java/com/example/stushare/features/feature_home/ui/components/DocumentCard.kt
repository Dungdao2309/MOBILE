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
            .width(160.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // ✅ FIX 1: Dùng màu theo Theme thay vì Color.White
            // Light Mode: Nó sẽ lấy màu Surface (thường là trắng hoặc xám rất nhạt)
            // Dark Mode: Nó sẽ lấy màu xám đậm (VD: #1E1E1E) giúp mắt dễ chịu
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 1. Ảnh bìa
            AsyncImage(
                model = document.imageUrl,
                contentDescription = document.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tiêu đề
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 48.dp),
                // ✅ Thêm dòng này để chắc chắn chữ màu trắng khi ở Dark Mode
                // (Mặc định nó sẽ tự lấy onSurface, nhưng khai báo rõ càng tốt)
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Loại tài liệu
            Text(
                text = document.type,
                style = MaterialTheme.typography.bodyMedium,
                // Lưu ý: PrimaryGreen cần đảm bảo đủ sáng để nhìn thấy trên nền đen.
                // Nếu quá tối, hãy dùng MaterialTheme.colorScheme.primary
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
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(document.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface // ✅ Đồng bộ màu chữ
                    )
                }

                // Lượt tải
                Text(
                    text = "${document.downloads} tải",
                    style = MaterialTheme.typography.labelSmall,
                    // ✅ FIX 2: Thay Color.Gray bằng onSurfaceVariant
                    // Đây là màu ngữ nghĩa dành cho text phụ (secondary text)
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
