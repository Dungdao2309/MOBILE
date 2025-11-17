// File: SearchScreen.kt (Đã cải tiến - Tách biệt trách nhiệm)

package com.example.stushare.features.feature_search.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.stushare.features.feature_search.ui.search.SearchViewModel
// ⭐️ XÓA: import com.example.stushare.features.feature_search.ui.search.SearchUiState

// Import các component và Theme
import com.example.stushare.features.feature_search.ui.components.SearchTagChip
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    // ⭐️ THAY ĐỔI: ViewModel giờ được Hilt tự động cung cấp
    viewModel: SearchViewModel = hiltViewModel()
) {
    val currentQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    // ⭐️ XÓA: val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val recentSearches = remember { listOf("Lập trình mobile", "Triết", "Kỹ thuật lập trình", "Hệ điều hành", "Mạng máy tính") }
    val suggestions = remember { listOf("Pháp luật đại cương", "Công nghệ phần mềm", "Khoa CNTT", "#dethi") }

    // ⭐️ THAY ĐỔI: Lắng nghe sự kiện điều hướng (navigationEvent)
    LaunchedEffect(Unit) { // Chỉ chạy một lần
        viewModel.navigationEvent.collect { query ->
            // 1. Điều hướng sang màn hình kết quả
            onSearchSubmit(query)
            // 2. (Không cần reset state nữa)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // PHẦN 1: HEADER MÀU XANH
        SearchHeader(
            onBackClick = onBackClick,
            query = currentQuery,
            onQueryChange = { viewModel.onQueryChanged(it) },
            onSearchClick = {
                if (currentQuery.isNotBlank()) {
                    // ⭐️ THAY ĐỔI: Gọi hàm "trigger" mới
                    viewModel.onSearchTriggered(currentQuery)
                }
            }
        )

        // PHẦN 2: NỘI DUNG MÀU TRẮNG
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {

            // ⭐️ THAY ĐỔI: Đã xóa toàn bộ khối `when (uiState)`
            // Màn hình này giờ chỉ hiển thị Lịch sử và Gợi ý

            SearchHistorySection(
                searches = recentSearches,
                onChipClick = { tag -> viewModel.onQueryChanged(tag) },
                onClearClick = { /* Xử lý xóa lịch sử */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SuggestionSection(
                suggestions = suggestions,
                onChipClick = { tag -> viewModel.onQueryChanged(tag) }
            )
        }
    }
}

// --- Các Component con của SearchScreen (Giữ nguyên) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHeader(
    onBackClick: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    // (Hàm này giữ nguyên, không cần thay đổi gì)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
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
                text = "Tìm Kiếm",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Tìm kiếm tài liệu", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightGreen,
                unfocusedContainerColor = LightGreen,
                disabledContainerColor = LightGreen,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen
            ),
            trailingIcon = {
                // ⭐️ LƯU Ý: onSearchClick đã được cập nhật ở Composable cha
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, "Tìm kiếm", tint = Color.Gray)
                }
            },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistorySection(
    searches: List<String>,
    onChipClick: (String) -> Unit,
    onClearClick: () -> Unit
) {
    // (Hàm này giữ nguyên)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tìm kiếm gần đây", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClearClick) {
                Text("Xóa", color = Color.Red)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            searches.forEach { search ->
                SearchTagChip(text = search, onClick = { onChipClick(search) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionSection(
    suggestions: List<String>,
    onChipClick: (String) -> Unit
) {
    // (Hàm này giữ nguyên)
    Column {
        Text("Gợi ý cho bạn", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                SearchTagChip(text = suggestion, onClick = { onChipClick(suggestion) })
            }
        }
    }
}