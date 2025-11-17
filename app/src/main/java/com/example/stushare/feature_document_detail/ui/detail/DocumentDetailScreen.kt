package com.example.stushare.features.feature_document_detail.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.stushare.ui.theme.PrimaryGreen
// ⭐️ IMPORT MỚI ⭐️
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ⭐️ BƯỚC 1: THIẾT LẬP SNACKBARHOSTSTATE ⭐️
    val snackbarHostState = remember { SnackbarHostState() }

    // Gọi hàm getDocumentById MỘT LẦN khi màn hình được tạo
    LaunchedEffect(key1 = documentId) {
        viewModel.getDocumentById(documentId)
    }

    // ⭐️ BƯỚC 2: LẮNG NGHE SỰ KIỆN TỪ VIEWMODEL ⭐️
    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    // ⭐️ KẾT THÚC BƯỚC 2 ⭐️

    Scaffold(
        // ⭐️ BƯỚC 3: THÊM SNACKBARHOST VÀO SCAFFOLD ⭐️
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // ⭐️ KẾT THÚC BƯỚC 3 ⭐️

        // Phần footer "Viết bình luận" (giống Figma)
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Viết bình luận của bạn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues) // Padding cho bottomBar
                .verticalScroll(rememberScrollState()) // Cho phép cuộn
        ) {
            // PHẦN 1: HEADER MÀU XANH
            DetailHeader(onBackClick = onBackClick)

            // PHẦN 2: NỘI DUNG (Loading, Error, Success)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Dùng offset y âm để kéo nội dung đè lên header
                    .offset(y = (-60).dp)
            ) {
                when (val state = uiState) {
                    is DetailUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is DetailUiState.Error -> {
                        Text(state.message, modifier = Modifier.align(Alignment.Center))
                    }
                    is DetailUiState.Success -> {
                        // HIỂN THỊ NỘI DUNG VÀ TRUYỀN VIEWMODEL VÀO
                        DetailContent(document = state.document, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// Header màu xanh
@Composable
private fun DetailHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Tăng chiều cao
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay về", tint = Color.White)
        }
        Text(
            "Chi tiết tài liệu",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        )
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.MoreVert, "Tùy chọn", tint = Color.White)
        }
    }
}

// Nội dung chính (Ảnh bìa, info,...) - (Không thay đổi)
@Composable
private fun DetailContent(document: Document, viewModel: DocumentDetailViewModel) { // <-- NHẬN VIEWMODEL
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ảnh bìa
        AsyncImage(
            model = document.imageUrl,
            contentDescription = document.title,
            modifier = Modifier
                .width(180.dp)
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tên tài liệu
        Text(
            text = document.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Người đăng
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) // Placeholder cho Avatar
            Spacer(modifier = Modifier.width(8.dp))
            Text("Người đăng", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thanh thông số (Rating, Tải, Xem)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChip(icon = Icons.Filled.Star, text = document.rating.toString(), tint = Color(0xFFFFC107))
            InfoChip(icon = Icons.Filled.Download, text = document.downloads.toString())
            InfoChip(icon = Icons.Default.MoreVert, text = "3000") // (Xem)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút Tải Về (ĐÃ CẬP NHẬT)
        Button(
            onClick = {
                // GỌI HÀM TẢI VỀ
                viewModel.startDownload(
                    url = document.imageUrl,
                    title = document.title
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tải Về", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mô tả
        DetailInfoSection("Mô tả", "Nội dung mô tả chi tiết về tài liệu...")
        DetailInfoSection("Thông tin thêm", "Mã môn: HH50\nLoại: ${document.type}")
        DetailInfoSection("Bình luận (42)", "Người A: Tài liệu rất hay!\n...")
    }
}

// Component nhỏ cho "Mô tả", "Thông tin thêm"... (Không thay đổi)
@Composable
private fun DetailInfoSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Component nhỏ cho chip thông số (Rating, Download) (Không thay đổi)
@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color = LocalContentColor.current) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}