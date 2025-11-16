package com.example.stushare.features.feature_search.ui.search

// Imports cơ bản
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Imports rõ ràng cho Material 3
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton

// Imports của dự án
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    query: String,
    onBackClick: () -> Unit,
    onDocumentClick: (Long) -> Unit, // Mong đợi kiểu Long
    // SỬA LỖI: Nhận ViewModel từ AppNavigation
    viewModel: SearchViewModel
) {
    // Lắng nghe trạng thái từ ViewModel DÙNG CHUNG
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // XÓA BỎ LỖI LOGIC: Khối LaunchedEffect(query) đã bị xóa.
    // Màn hình này không nên tự ý tìm kiếm lại.

    Scaffold(
        topBar = {
            SearchResultTopBar(
                query = query,
                onBackClick = {
                    // ⭐️ THÊM LOGIC VÀO ĐÂY ⭐️
                    // 1. Reset trạng thái ViewModel TRƯỚC KHI quay lại
                    viewModel.navigationHandled()
                    // 2. Gọi hành động quay lại (để NavController xử lý)
                    onBackClick()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SỬA LỖI LOGIC: Tách biệt Initial và Loading
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    // Chỉ hiển thị Loading khi ViewModel thực sự đang tải
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is SearchUiState.Success -> {
                    // Hiển thị kết quả
                    SearchResultList(
                        documents = state.results,
                        onDocumentClick = onDocumentClick
                    )
                }

                is SearchUiState.Empty -> {
                    // Hiển thị không có kết quả
                    EmptyResult(query = query)
                }

                is SearchUiState.Error -> {
                    // Hiển thị lỗi
                    ErrorMessage(message = state.message)
                }

                is SearchUiState.Initial -> {
                    // Nếu trạng thái bị reset (do nhấn Back), hiển thị Empty
                    EmptyResult(query = query)
                }
            }
        }
    }
}

// --- Component TopBar (SỬA LỖI BIÊN DỊCH) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultTopBar(query: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Kết quả cho \"$query\"",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        // SỬA LỖI BIÊN DỊCH: Dùng TopAppBarDefaults.topAppBarColors()
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}

// --- Component Hiển thị Danh sách Kết quả ---

@Composable
private fun SearchResultList(
    documents: List<Document>,
    onDocumentClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Tìm thấy ${documents.size} tài liệu",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(documents) { document ->
            // Giả định document.id là Long (theo lỗi trước đó)
            DocumentCard(
                document = document,
                onClick = { onDocumentClick(document.id) }
            )
        }
    }
}

// --- Component Không có Kết quả (Giữ nguyên) ---

@Composable
private fun EmptyResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Không tìm thấy",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy tài liệu nào",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chúng tôi không tìm thấy kết quả cho từ khóa \"$query\". Vui lòng thử từ khóa chung hơn.",
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// --- Component Lỗi (Giữ nguyên) ---

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đã xảy ra lỗi: $message",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}