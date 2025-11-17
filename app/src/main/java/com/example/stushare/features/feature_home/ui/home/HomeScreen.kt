// File: HomeScreen.kt (Đã cập nhật Skeleton UI)
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.LightGreen

// ----- IMPORT CÁC SKELETON MỚI -----
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

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(swipeRefreshState)
            ) {

                // ----- NÂNG CẤP LOGIC HIỂN THỊ -----

                // 1. Nếu đang tải LẦN ĐẦU (chưa có dữ liệu), hiển thị SKELETON
                if (uiState.isLoading) { // <-- CHỈ KIỂM TRA CÁI NÀY
                    HomeScreenSkeleton(columns = columns)
                }
                // 2. Nếu CSDL trống (sau khi tải xong)
                else if (uiState.newDocuments.isEmpty() && uiState.examDocuments.isEmpty()) {
                    EmptyState()
                }
                // 3. Hiển thị dữ liệu
                else {

                    // 1. DÙNG LAZYCOLUMN CHO MÀN HÌNH HẸP (columns = 1)
                    if (columns == 1) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
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
                                        title = "Mới được tải lên",
                                        onViewAllClick = { onViewAllClick("new_uploads") }
                                    )
                                }
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp)
                                    ) {
                                        items(
                                            items = uiState.newDocuments,
                                            key = { it.id }
                                        ) { document ->
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
                                        title = "Tài liệu ôn thi",
                                        onViewAllClick = { onViewAllClick("exam_prep") }
                                    )
                                }
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp)
                                    ) {
                                        items(
                                            items = uiState.examDocuments,
                                            key = { it.id }
                                        ) { document ->
                                            DocumentCard(
                                                document = document,
                                                onClick = { onDocumentClick(document.id.toString()) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 2. DÙNG LAZYVERTICALGRID CHO MÀN HÌNH RỘNG (columns > 1)
                    else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(span = { GridItemSpan(columns) }) {
                                HomeHeaderSection(
                                    userName = uiState.userName,
                                    avatarUrl = uiState.avatarUrl,
                                    onSearchClick = onSearchClick
                                )
                            }
                            item(span = { GridItemSpan(columns) }) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (uiState.newDocuments.isNotEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    DocumentSectionHeader(
                                        title = "Mới được tải lên",
                                        onViewAllClick = { onViewAllClick("new_uploads") },
                                        modifier = Modifier.padding(horizontal = 0.dp)
                                    )
                                }
                                items(
                                    items = uiState.newDocuments,
                                    key = { it.id }
                                ) { document ->
                                    DocumentCard(
                                        document = document,
                                        onClick = { onDocumentClick(document.id.toString()) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            if (uiState.examDocuments.isNotEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    DocumentSectionHeader(
                                        title = "Tài liệu ôn thi",
                                        onViewAllClick = { onViewAllClick("exam_prep") },
                                        modifier = Modifier.padding(horizontal = 0.dp)
                                    )
                                }
                                items(
                                    items = uiState.examDocuments,
                                    key = { it.id }
                                ) { document ->
                                    DocumentCard(
                                        document = document,
                                        onClick = { onDocumentClick(document.id.toString()) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // PullRefreshIndicator (Giữ nguyên)
                // Nó sẽ hiển thị *trên* nội dung (kể cả skeleton) khi kéo
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
}

// --- CÁC HÀM COMPOSABLE KHÁC (Giữ nguyên) ---
// (EmptyState, HomeHeaderSection, DocumentSectionHeader)

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Không có dữ liệu",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Không có dữ liệu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "Vui lòng kéo xuống để thử tải lại.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HomeHeaderSection(
    userName: String,
    avatarUrl: String?,
    onSearchClick: () -> Unit
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
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, fontSize = 16.sp)
                Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(12.dp),
            color = LightGreen
        ) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Tìm kiếm tài liệu", color = Color.Gray, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Search, "Tìm kiếm", tint = Color.Gray)
            }
        }
    }
}

// Đừng quên file DocumentSectionHeader.kt mà chúng ta đã tạo trước đó
/*
@Composable
fun DocumentSectionHeader(
    title: String,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) { ... }
*/