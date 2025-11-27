package com.example.stushare.features.feature_home.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.R
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: HomeViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onCreateRequestClick: () -> Unit,
    onUploadClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Xử lý thông báo lỗi
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && uiState.newDocuments.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = uiState.errorMessage ?: "Đã xảy ra lỗi")
            viewModel.clearError()
        }
    }

    // Xác định số cột dựa trên kích thước màn hình
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }

    val swipeRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo yêu cầu")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(swipeRefreshState)
        ) {
            if (uiState.isLoading && uiState.newDocuments.isEmpty()) {
                // Hiển thị khung xương khi đang tải lần đầu
                // Lưu ý: Đảm bảo bạn đã có Composable HomeScreenSkeleton trong project
                HomeScreenSkeleton(columns)
            } else {
                HomeContent(
                    uiState = uiState,
                    onSearchClick = onSearchClick,
                    onViewAllClick = onViewAllClick,
                    onDocumentClick = onDocumentClick,
                    onUploadClick = onUploadClick,
                    onLeaderboardClick = onLeaderboardClick,
                    onNotificationClick = onNotificationClick
                )
            }
            // Chỉ báo loading khi kéo xuống
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = swipeRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onUploadClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        item {
            HomeHeaderSection(
                userName = uiState.userName,
                avatarUrl = uiState.avatarUrl,
                onSearchClick = onSearchClick,
                onUploadClick = onUploadClick,
                onLeaderboardClick = onLeaderboardClick,
                onNotificationClick = onNotificationClick
            )
        }

        // Phần: Tài liệu mới tải lên
        if (uiState.newDocuments.isNotEmpty()) {
            item {
                // Lưu ý: Đảm bảo bạn đã có Composable DocumentSectionHeader
                DocumentSectionHeader(
                    title = stringResource(R.string.section_new_uploads),
                    onViewAllClick = { onViewAllClick("new_uploads") }
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.newDocuments) { doc ->
                        // --- ĐÃ SỬA LỖI TẠI ĐÂY ---
                        DocumentCard(
                            document = doc,
                            onClick = { onDocumentClick(doc.id.toString()) }
                        )
                    }
                }
            }
        }

        // Bạn có thể thêm các section khác ở đây (ví dụ: Tài liệu phổ biến...)
    }
}

@Composable
fun HomeHeaderSection(
    userName: String,
    avatarUrl: String?,
    onSearchClick: () -> Unit,
    onUploadClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.3f)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, fontSize = 14.sp)
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))

            // 3 Nút chức năng trên Header
            IconButton(onClick = onLeaderboardClick) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Bảng xếp hạng", tint = Color.White)
            }
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = Color.White)
            }
            IconButton(onClick = onUploadClick) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Tải lên", tint = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Thanh tìm kiếm
        Surface(
            onClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = LightGreen
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tìm kiếm tài liệu...",
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}