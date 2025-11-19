package com.example.stushare.feature_document_detail.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_document_detail.ui.detail.DetailUiState
import com.example.stushare.features.feature_document_detail.ui.detail.DocumentDetailViewModel
import com.example.stushare.ui.theme.PrimaryGreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = documentId) {
        viewModel.getDocumentById(documentId)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        // 1. Header: Đơn giản, nền trắng để tập trung vào nội dung
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chi tiết tài liệu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay về")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },

        // 2. Thumb Zone: Nút hành động chính nằm dưới cùng, dễ bấm
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                val document = (uiState as DetailUiState.Success).document
                BottomActionSection(
                    onDownloadClick = {
                        viewModel.startDownload(document.imageUrl, document.title)
                    },
                    onCommentClick = { /* TODO: Mở bình luận */ }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is DetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is DetailUiState.Success -> {
                    DocumentDetailContent(document = state.document)
                }
            }
        }
    }
}

@Composable
fun DocumentDetailContent(document: Document) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp) // Tăng padding lên 20dp cho thoáng (Whitespace)
    ) {
        // 1. Ảnh bìa lớn (Tiêu điểm thị giác)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = document.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(160.dp) // Kích thước ảnh bìa chuẩn
                    .aspectRatio(0.7f) // Tỉ lệ ~3:4 (giống sách thật)
                    .clip(RoundedCornerShape(16.dp)) // Bo góc mềm mại
                    .background(Color.LightGray.copy(alpha = 0.2f)), // Placeholder nhẹ
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Thông tin chính (Title & Author) - Visual Hierarchy
        Text(
            text = document.title,
            style = MaterialTheme.typography.titleLarge, // Font to, đậm
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tác giả: ${document.author}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Màu nhạt hơn tiêu đề
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Các chỉ số (Rating, Downloads) - Dùng Badge gọn gàng
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BadgeInfo(
                icon = Icons.Filled.Star,
                text = "%.1f".format(document.rating),
                color = Color(0xFFFFC107) // Vàng
            )
            BadgeInfo(
                icon = Icons.Filled.Download,
                text = "${document.downloads} lượt tải",
                color = PrimaryGreen // Xanh chủ đạo
            )
            BadgeInfo(
                icon = Icons.Default.ChatBubbleOutline, // Icon phụ
                text = "${document.downloads / 10} bình luận", // Giả lập số liệu
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Chi tiết mô tả (Simplicity - Chia nhỏ nội dung)
        DetailSection(
            title = "Mô tả tài liệu",
            content = "Đây là tài liệu tham khảo chất lượng cao dành cho sinh viên, bao gồm các kiến thức từ cơ bản đến nâng cao. Tài liệu được biên soạn kỹ lưỡng, dễ hiểu và có nhiều ví dụ minh họa thực tế."
        )

        DetailSection(
            title = "Thông tin thêm",
            content = "• Mã môn học: ${document.courseCode}\n• Loại tài liệu: ${document.type}\n• Định dạng: PDF"
        )

        // Khoảng trống để nội dung không bị che bởi BottomBar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Component hiển thị thông tin dạng Badge (Huy hiệu)
@Composable
fun BadgeInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

// Component hiển thị từng phần nội dung có tiêu đề
@Composable
fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp // Tăng line-height để dễ đọc
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Component Bottom Bar chứa nút hành động chính
@Composable
fun BottomActionSection(onDownloadClick: () -> Unit, onCommentClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp, // Tạo bóng đổ nổi lên
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp) // Padding an toàn
                .navigationBarsPadding(), // Tránh thanh điều hướng ảo
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nút phụ: Bình luận
            OutlinedButton(
                onClick = onCommentClick,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray)
            }

            // Nút chính: Tải về (Nổi bật hơn)
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .weight(2f) // Chiếm 2/3 chiều rộng
                    .height(54.dp), // Nút cao, dễ bấm
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tải về ngay",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}