package com.example.stushare.features.feature_search.ui.search

import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.features.feature_search.ui.components.SearchTagChip
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    // Lấy state từ ViewModel
    val currentQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearchesState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val suggestions = remember { listOf("Pháp luật đại cương", "Công nghệ phần mềm", "Khoa CNTT", "#dethi") }

    // Lắng nghe sự kiện điều hướng khi nhấn Enter hoặc chọn từ lịch sử
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { query ->
            onSearchSubmit(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // 1. Header chứa thanh tìm kiếm
        SearchHeader(
            query = currentQuery,
            onQueryChange = viewModel::onQueryChanged,
            onBackClick = onBackClick,
            onClearClick = { viewModel.onQueryChanged("") }, // Xóa text
            onSearch = {
                keyboardController?.hide()
                if (currentQuery.isNotBlank()) {
                    viewModel.onSearchTriggered(currentQuery)
                }
            }
        )

        // 2. Nội dung chính (Lịch sử & Gợi ý)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp) // Padding thoáng đãng
            ) {
                // Section: Lịch sử tìm kiếm
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tìm kiếm gần đây",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                Text("Xóa tất cả", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Danh sách lịch sử (Vertical List)
                    items(recentSearches) { search ->
                        RecentSearchItem(
                            text = search,
                            onClick = {
                                viewModel.onQueryChanged(search)
                                viewModel.onSearchTriggered(search)
                            }
                        )
                    }

                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }

                // Section: Gợi ý (Chips)
                item {
                    Text(
                        text = "Gợi ý cho bạn",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // FlowRow tự động xuống dòng khi hết chỗ
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            SearchTagChip(
                                text = suggestion,
                                onClick = {
                                    viewModel.onQueryChanged(suggestion)
                                    viewModel.onSearchTriggered(suggestion)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
    ) {
        // Hàng tiêu đề
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay về",
                    tint = Color.White
                )
            }
            Text(
                text = "Tìm kiếm",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thanh tìm kiếm (Pill Shape)
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text("Nhập tên tài liệu, môn học...", color = Color.Gray, fontSize = 14.sp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(CircleShape), // Bo tròn hoàn toàn (Pill shape)
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightGreen,     // Nền nhạt khi focus
                unfocusedContainerColor = LightGreen,   // Nền nhạt khi không focus
                disabledContainerColor = LightGreen,
                focusedIndicatorColor = Color.Transparent, // Ẩn gạch chân
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen
            ),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen)
            },
            trailingIcon = {
                // Chỉ hiện nút Xóa khi có text
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}

@Composable
private fun RecentSearchItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp), // Tăng vùng chạm (Thumb Zone friendly)
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon đồng hồ biểu thị lịch sử
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )

        // Icon mũi tên chéo để gợi ý điền nhanh (Visual Cue)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier
                .size(18.dp)
                .rotate(-45f)
        )
    }
}