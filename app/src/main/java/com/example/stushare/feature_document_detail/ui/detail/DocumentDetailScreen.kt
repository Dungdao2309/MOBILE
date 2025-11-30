package com.example.stushare.feature_document_detail.ui.detail

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ⭐️ Lấy Context để dùng cho DownloadManager
    val context = LocalContext.current

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val isLoggedIn = currentUser != null

    LaunchedEffect(key1 = documentId) {
        viewModel.getDocumentById(documentId)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
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
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                val document = (uiState as DetailUiState.Success).document

                BottomActionSection(
                    onDownloadClick = {
                        if (isLoggedIn) {
                            // ⭐️ SỬA LOGIC TẠI ĐÂY:
                            // 1. Gọi hàm downloadDocument trực tiếp
                            // 2. Truyền document.fileUrl (thay vì imageUrl)
                            downloadDocument(context, document.fileUrl, document.title)
                        } else {
                            onLoginRequired()
                        }
                    },
                    onCommentClick = {
                        if (isLoggedIn) {
                            Toast.makeText(context, "Tính năng bình luận đang phát triển", Toast.LENGTH_SHORT).show()
                        } else {
                            onLoginRequired()
                        }
                    }
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

// ⭐️ HÀM XỬ LÝ TẢI FILE (Logic cốt lõi)
fun downloadDocument(context: Context, url: String, title: String) {
    if (url.isBlank()) {
        Toast.makeText(context, "Lỗi: Tài liệu này chưa có link tải!", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)

        // Tạo tên file an toàn (thay thế khoảng trắng và ký tự lạ)
        val safeFileName = "${title.replace("[^a-zA-Z0-9]".toRegex(), "_")}.pdf" // Mặc định đuôi .pdf hoặc lấy từ url

        val request = DownloadManager.Request(uri)
            .setTitle(title)
            .setDescription("Đang tải tài liệu về máy...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)
        Toast.makeText(context, "Đang bắt đầu tải: $title", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Không thể tải file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun DocumentDetailContent(document: Document) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // 1. Ảnh bìa
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = document.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(160.dp)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Tiêu đề
        Text(
            text = document.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tác giả
        Text(
            text = "Tác giả: ${document.author}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Badge chỉ số
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BadgeInfo(
                icon = Icons.Filled.Star,
                text = "%.1f".format(document.rating),
                color = Color(0xFFFFC107)
            )
            BadgeInfo(
                icon = Icons.Filled.Download,
                text = "${document.downloads} lượt tải",
                color = PrimaryGreen
            )
            BadgeInfo(
                icon = Icons.Default.ChatBubbleOutline,
                text = "${document.downloads / 10} bình luận",
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Nội dung chi tiết
        DetailSection(
            title = "Mô tả tài liệu",
            content = "Đây là tài liệu tham khảo chất lượng cao dành cho sinh viên, bao gồm các kiến thức từ cơ bản đến nâng cao. Tài liệu được biên soạn kỹ lưỡng, dễ hiểu và có nhiều ví dụ minh họa thực tế."
        )

        DetailSection(
            title = "Thông tin thêm",
            content = "• Mã môn học: ${document.courseCode}\n• Loại tài liệu: ${document.type}\n• Định dạng: PDF"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BottomActionSection(onDownloadClick: () -> Unit, onCommentClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nút Bình luận
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

            // Nút Tải về
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .weight(2f)
                    .height(54.dp),
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