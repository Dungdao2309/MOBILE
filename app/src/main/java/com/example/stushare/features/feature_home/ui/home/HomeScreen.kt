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
import androidx.compose.material.icons.filled.CloudUpload // ⭐️ IMPORT MỚI
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
    onCreateRequestClick: () -> Unit,
    onUploadClick: () -> Unit // ⭐️ THÊM THAM SỐ NÀY
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                uiState.isLoading && uiState.newDocuments.isEmpty() -> {
                    HomeScreenSkeleton(columns = columns)
                }
                uiState.errorMessage != null && uiState.newDocuments.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Đã xảy ra lỗi",
                        onRetry = { viewModel.refreshData() }
                    )
                }
                uiState.newDocuments.isEmpty() && uiState.examDocuments.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    HomeContent(
                        columns = columns,
                        uiState = uiState,
                        onSearchClick = onSearchClick,
                        onViewAllClick = onViewAllClick,
                        onDocumentClick = onDocumentClick,
                        onUploadClick = onUploadClick // ⭐️ TRUYỀN XUỐNG
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
fun EmptyState() {
    TODO("Not yet implemented")
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
private fun HomeContent(
    columns: Int,
    uiState: HomeUiState,
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onUploadClick: () -> Unit // ⭐️ NHẬN THAM SỐ
) {
    if (columns == 1) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                HomeHeaderSection(
                    userName = uiState.userName,
                    avatarUrl = uiState.avatarUrl,
                    onSearchClick = onSearchClick,
                    onUploadClick = onUploadClick // ⭐️ TRUYỀN XUỐNG HEADER
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
                    onSearchClick = onSearchClick,
                    onUploadClick = onUploadClick
                )
            }
            // Logic grid items...
            if (uiState.newDocuments.isNotEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    DocumentSectionHeader(
                        title = stringResource(R.string.section_new_uploads),
                        onViewAllClick = { onViewAllClick("new_uploads") }
                    )
                }
                items(items = uiState.newDocuments, key = { it.id }) { document ->
                    DocumentCard(
                        document = document,
                        onClick = { onDocumentClick(document.id.toString()) }
                    )
                }
            }
        }
    }
}

// ... (EmptyState, ErrorState giữ nguyên) ...

@Composable
fun HomeHeaderSection(
    userName: String,
    avatarUrl: String?,
    onSearchClick: () -> Unit,
    onUploadClick: () -> Unit // ⭐️ NHẬN THAM SỐ
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
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(0.3f)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.home_greeting), color = Color.White, fontSize = 16.sp)
                Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // ⭐️ NÚT UPLOAD Ở GÓC PHẢI
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onUploadClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = "Tải tài liệu lên",
                    modifier = Modifier.size(28.dp)
                )
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