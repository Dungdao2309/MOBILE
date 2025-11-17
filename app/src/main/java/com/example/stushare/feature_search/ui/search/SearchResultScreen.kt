// File: SearchResultScreen.kt (Đã cải tiến - Tách biệt trách nhiệm)

package com.example.stushare.feature_search.ui.search

// Imports cơ bản

// Imports rõ ràng cho Material 3
// ⭐️ XÓA: import androidx.compose.material3.TextButton (không cần nữa)

// Imports của dự án
import com.example.stushare.feature_search.ui.search.SearchUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    // ⭐️ XÓA: query: String,
    onBackClick: () -> Unit,
    onDocumentClick: (Long) -> Unit, // Mong đợi kiểu Long
    // ⭐️ THAY ĐỔI: Sử dụng ViewModel mới, được Hilt tự động cung cấp
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    // ⭐️ Lấy state và query từ ViewModel MỚI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query = viewModel.query // Lấy query từ ViewModel

    Scaffold(
        topBar = {
            SearchResultTopBar(
                query = query, // Truyền query vào TopBar
                onBackClick = {
                    // ⭐️ ĐƠN GIẢN HÓA: Chỉ cần gọi onBackClick
                    // Không cần reset state thủ công nữa.
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
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is SearchUiState.Success -> {
                    SearchResultList(
                        documents = state.results,
                        onDocumentClick = onDocumentClick
                    )
                }
                is SearchUiState.Empty -> {
                    EmptyResult(query = query)
                }
                is SearchUiState.Error -> {
                    ErrorMessage(message = state.message)
                }
                is SearchUiState.Initial -> {
                    // Trạng thái này không nên xảy ra, nhưng nếu có,
                    // cứ hiển thị Loading.
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

// --- Component TopBar (Giữ nguyên) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultTopBar(query: String, onBackClick: () -> Unit) {
    // (Hàm này giữ nguyên, không cần thay đổi)
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}

// --- Component Hiển thị Danh sách Kết quả (Giữ nguyên) ---

@Composable
private fun SearchResultList(
    documents: List<Document>,
    onDocumentClick: (Long) -> Unit
) {
    // (Hàm này giữ nguyên, không cần thay đổi)
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
    // (Hàm này giữ nguyên, không cần thay đổi)
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
    // (Hàm này giữ nguyên, không cần thay đổi)
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