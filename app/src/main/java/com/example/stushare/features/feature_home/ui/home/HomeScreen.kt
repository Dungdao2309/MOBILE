package com.example.stushare.features.feature_home.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import com.example.stushare.core.data.models.Document
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

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && uiState.newDocuments.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = uiState.errorMessage ?: "Error")
            viewModel.clearError()
        }
    }

    val swipeRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo yêu cầu mới")
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
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

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = swipeRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = PrimaryGreen
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
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Section
        item {
            HomeHeaderSection(
                userName = uiState.userName,
                avatarUrl = uiState.avatarUrl,
                unreadCount = uiState.unreadNotificationCount, // 🟢 TRUYỀN SỐ LƯỢNG VÀO ĐÂY
                onSearchClick = onSearchClick,
                onUploadClick = onUploadClick,
                onLeaderboardClick = onLeaderboardClick,
                onNotificationClick = onNotificationClick
            )
        }

        // 2. Section: Mới được tải lên
        item {
            DocumentSection(
                title = stringResource(R.string.section_new_uploads),
                documents = uiState.newDocuments,
                onViewAllClick = { onViewAllClick("new_uploads") },
                onDocumentClick = onDocumentClick
            )
        }

        // 3. Section: Tài liệu ôn thi
        item {
            DocumentSection(
                title = stringResource(R.string.section_exam_review),
                documents = uiState.examDocuments,
                onViewAllClick = { onViewAllClick("exam_review") },
                onDocumentClick = onDocumentClick
            )
        }

        // 4. Section: Sách / Giáo trình
        item {
            DocumentSection(
                title = "Sách / Giáo trình",
                documents = uiState.bookDocuments,
                onViewAllClick = { onViewAllClick("book") },
                onDocumentClick = onDocumentClick
            )
        }

        // 5. Section: Bài giảng / Slide
        item {
            DocumentSection(
                title = "Bài giảng / Slide",
                documents = uiState.lectureDocuments,
                onViewAllClick = { onViewAllClick("lecture") },
                onDocumentClick = onDocumentClick
            )
        }
    }
}

@Composable
private fun DocumentSection(
    title: String,
    documents: List<Document>,
    onViewAllClick: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    if (documents.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents) { doc ->
                    DocumentCard(
                        document = doc,
                        onClick = { onDocumentClick(doc.id.toString()) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeaderSection(
    userName: String,
    avatarUrl: String?,
    unreadCount: Int = 0, // 🟢 NHẬN SỐ LƯỢNG
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
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.2f)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_greeting),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = userName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }

            // Các nút hành động
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLeaderboardClick) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = Color.White)
                }

                // 🟢 ICON THÔNG BÁO VỚI CHẤM ĐỎ
                Box {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = Color.White)
                    }
                    if (unreadCount > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(10.dp) // Kích thước chấm đỏ
                        )
                    }
                }

                IconButton(onClick = onUploadClick) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Search Bar
        Surface(
            onClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_search_hint),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            }
        }
    }
}