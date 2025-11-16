package com.example.stushare.features.feature_home.ui.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.features.feature_home.ui.components.GridDocumentCard // <-- DÙNG CARD MỚI
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun ViewAllScreen(
    category: String, // Nhận category
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    viewModel: ViewAllViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dịch category sang Tiếng Việt để làm tiêu đề
    val screenTitle = when(category) {
        "new_uploads" -> "Mới được tải lên"
        "exam_prep" -> "Tài liệu ôn thi"
        else -> "Xem tất cả"
    }

    LaunchedEffect(key1 = category) {
        viewModel.loadCategory(category)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // PHẦN 1: HEADER
        ViewAllHeader(title = screenTitle, onBackClick = onBackClick)

        // PHẦN 2: NỘI DUNG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ViewAllUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ViewAllUiState.Success -> {
                    Column {
                        // Hàng nút "Sắp xếp" và "Bộ lọc"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                                Text("Sắp xếp: Mới nhất")
                            }
                            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PrimaryGreen)) {
                                Text("Bộ lọc")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Lưới kết quả (giống Figma)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2), // 2 cột
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.documents) { document ->
                                // DÙNG CARD MỚI
                                GridDocumentCard(
                                    document = document,
                                    onClick = { onDocumentClick(document.id.toString()) }                                )
                            }
                        }
                    }
                }
                is ViewAllUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Đã xảy ra lỗi: ${state.message}")
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewAllHeader(title: String, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay về",
                    tint = Color.White
                )
            }
            Text(
                text = title, // Tiêu đề động
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}