package com.example.stushare.feature_search.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.R
import com.example.stushare.core.data.models.Document
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit, // Đã đồng nhất kiểu dữ liệu là String (id của Document)
    onRequestClick: () -> Unit,
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query = viewModel.query

    Scaffold(
        topBar = {
            SearchResultTopBar(
                query = query,
                onBackClick = onBackClick
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
                    if (state.results.isEmpty()) {
                        EmptyResult(query = query, onRequestClick = onRequestClick)
                    } else {
                        SearchResultList(
                            documents = state.results,
                            onDocumentClick = onDocumentClick
                        )
                    }
                }
                is SearchUiState.Empty -> {
                    EmptyResult(query = query, onRequestClick = onRequestClick)
                }
                is SearchUiState.Error -> {
                    ErrorMessage(message = state.message)
                }
                is SearchUiState.Initial -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

// --- Component TopBar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultTopBar(query: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.search_result_title, query),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}

// --- Component Hiển thị Danh sách Kết quả (Dạng Lưới) ---
@Composable
private fun SearchResultList(
    documents: List<Document>,
    onDocumentClick: (String) -> Unit
) {
    // Sử dụng LazyVerticalGrid thay vì LazyColumn
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Chia thành 2 cột
        contentPadding = PaddingValues(16.dp), // Padding viền ngoài
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Khoảng cách giữa các cột
        verticalArrangement = Arrangement.spacedBy(12.dp),   // Khoảng cách giữa các hàng
        modifier = Modifier.fillMaxSize()
    ) {
        // Item hiển thị số lượng kết quả (Chiếm trọn bề ngang - span = 2)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.search_result_count, documents.size),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Danh sách tài liệu
        items(documents) { document ->
            SearchResultCard(
                document = document,
                onClick = { onDocumentClick(document.id) }
            )
        }
    }
}

// --- Component Không có Kết quả ---
@Composable
private fun EmptyResult(
    query: String,
    onRequestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = stringResource(R.string.content_desc_not_found),
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.search_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.search_empty_desc, query),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_request_help),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Component Lỗi ---
@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.content_desc_error),
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_message, message),
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}