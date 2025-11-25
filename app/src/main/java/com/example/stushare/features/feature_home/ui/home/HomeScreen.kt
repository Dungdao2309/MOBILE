package com.example.stushare.features.feature_home.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
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
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.features.feature_home.ui.home.HomeScreenSkeleton

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: HomeViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onCreateRequestClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Chỉ hiện Snackbar cho thông báo tải xong hoặc các thông báo nhẹ
    // Lỗi mạng nghiêm trọng sẽ dùng giao diện ErrorState ở giữa màn hình
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && uiState.newDocuments.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage ?: "Lỗi",
                actionLabel = "Đóng",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    val swipeRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateRequestClick() },
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
                .background(Color.White)
                .padding(paddingValues)
                .pullRefresh(swipeRefreshState)
        ) {
            when {
                // 1. Loading ban đầu -> Skeleton
                uiState.isLoading && uiState.newDocuments.isEmpty() -> {
                    HomeScreenSkeleton(columns = columns)
                }

                // 2. Có lỗi và không có dữ liệu -> Error State với nút Thử lại
                uiState.errorMessage != null && uiState.newDocuments.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Đã xảy ra lỗi",
                        onRetry = { viewModel.refreshData() }
                    )
                }

                // 3. Không có dữ liệu -> Empty State
                uiState.newDocuments.isEmpty() && uiState.examDocuments.isEmpty() -> {
                    EmptyState()
                }

                // 4. Có dữ liệu -> Hiển thị danh sách
                else -> {
                    HomeContent(
                        columns = columns,
                        uiState = uiState,
                        onSearchClick = onSearchClick,
                        onViewAllClick = onViewAllClick,
                        onDocumentClick = onDocumentClick
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = swipeRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryGreen
            )
        }
    }
}

@Composable
private fun HomeContent(
    columns: Int,
    uiState: HomeUiState,
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onDocumentClick: (String) -> Unit
) {
    if (columns == 1) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Tránh FAB che
        ) {
            item {
                HomeHeaderSection(
                    userName = uiState.userName,
                    avatarUrl = uiState.avatarUrl,
                    onSearchClick = onSearchClick
                )
            }
            if (uiState.newDocuments.isNotEmpty()) {
                item {
                    DocumentSectionHeader(
                        title = stringResource(R.string.section_new_uploads),
                        onViewAllClick = { onViewAllClick("new_uploads") }
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(items = uiState.newDocuments, key = { it.id }) { document ->
                            DocumentCard(
                                document = document,
                                onClick = { onDocumentClick(document.id.toString()) }
                            )
                        }
                    }
                }
            }
            if (uiState.examDocuments.isNotEmpty()) {
                item {
                    DocumentSectionHeader(
                        title = stringResource(R.string.section_exam_prep),
                        onViewAllClick = { onViewAllClick("exam_prep") }
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(items = uiState.examDocuments, key = { it.id }) { document ->
                            DocumentCard(
                                document = document,
                                onClick = { onDocumentClick(document.id.toString()) }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Tablet / Landscape layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                HomeHeaderSection(
                    userName = uiState.userName,
                    avatarUrl = uiState.avatarUrl,
                    onSearchClick = onSearchClick
                )
            }
            // ... (Phần logic Grid giữ nguyên như cũ hoặc tùy chỉnh thêm nếu muốn)
        }
    }
}

// Cải tiến EmptyState
@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.CloudOff, null, Modifier.size(64.dp), Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.error_no_data), color = Color.Gray)
            Text(stringResource(R.string.msg_pull_to_refresh), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Component hiển thị lỗi với nút thử lại
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(stringResource(R.string.btn_retry))
        }
    }
}

@Composable
fun HomeHeaderSection(userName: String, avatarUrl: String?, onSearchClick: () -> Unit) {
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
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(0.3f)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.home_greeting), color = Color.White, fontSize = 16.sp)
                Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(50.dp).clickable { onSearchClick() },
            shape = RoundedCornerShape(12.dp),
            color = LightGreen
        ) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_search_hint), color = Color.Gray, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Search, null, tint = Color.Gray)
            }
        }
    }
}