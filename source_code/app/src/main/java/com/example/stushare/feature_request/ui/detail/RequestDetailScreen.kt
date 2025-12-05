package com.example.stushare.feature_request.ui.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    onBackClick: () -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val commentText by viewModel.commentText.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Kiểm tra trạng thái hoàn thành
    val isSolved = uiState.request?.isSolved == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết & Thảo luận", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            // Thanh nhập chat
            Surface(
                shadowElevation = 8.dp,
                color = if (isSolved) Color(0xFFEEEEEE) else Color.White // Xám nếu đã khóa
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSolved) {
                        // 🟢 HIỂN THỊ KHI ĐÃ KHÓA
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đã hoàn thành • Chức năng chat bị khóa", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        // 🟢 HIỂN THỊ KHI CÒN MỞ
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = viewModel::onCommentChange,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("Nhập bình luận hoặc link tài liệu...") },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                viewModel.sendComment()
                                keyboardController?.hide()
                            })
                        )

                        IconButton(
                            onClick = {
                                viewModel.sendComment()
                                keyboardController?.hide()
                            },
                            enabled = commentText.isNotBlank() && !uiState.isSending
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = if (commentText.isNotBlank()) PrimaryGreen else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingRequest) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGreen
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        uiState.request?.let { request ->
                            RequestContentHeader(
                                request = request,
                                isCurrentUserOwner = uiState.currentUserId == request.authorId,
                                onMarkSolved = {
                                    // 🟢 GỌI HÀM VIEWMODEL MỚI VỚI CALLBACK
                                    viewModel.markAsSolved(
                                        onSuccess = {
                                            Toast.makeText(context, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { msg ->
                                            Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Bình luận (${uiState.comments.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    if (uiState.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có bình luận nào.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                isCurrentUser = comment.userId == uiState.currentUserId
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
fun RequestContentHeader(
    request: DocumentRequest,
    isCurrentUserOwner: Boolean,
    onMarkSolved: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Môn học + Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.subject,
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 🟢 Trạng thái ĐÃ HOÀN THÀNH (Hiện chữ ở tiêu đề)
                if (request.isSolved) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hoàn thành",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng bởi ${request.authorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // 🟢 NÚT HOÀN THÀNH (Chỉ hiện nếu chưa xong)
            if (isCurrentUserOwner && !request.isSolved) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onMarkSolved,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hoàn thành", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ... CommentItem giữ nguyên ...
@Composable
fun CommentItem(comment: CommentEntity, isCurrentUser: Boolean) {
    val dateFormatter = SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault())
    val timeString = comment.timestamp?.let { dateFormatter.format(it) } ?: "Vừa xong"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }

        Surface(
            shape = if (isCurrentUser)
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            else
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color = if (isCurrentUser) PrimaryGreen else Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = comment.content,
                modifier = Modifier.padding(12.dp),
                color = if (isCurrentUser) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
        )
    }
}