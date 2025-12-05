package com.example.stushare.features.feature_admin.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.* // Import quan trọng cho mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

// 👇 Import Dialog bạn vừa tạo ở bước trước
import com.example.stushare.features.feature_admin.ui.components.SendNotificationDialog

// Định nghĩa màu cục bộ
val AdminPrimaryColor = Color(0xFF4CAF50)
val AdminBgColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToUsers: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoadingDashboard.collectAsState()
    val context = LocalContext.current

    // 🟢 1. State quản lý việc hiện Dialog
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Lắng nghe thông báo kết quả gửi
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 🟢 2. Hiển thị Dialog nếu biến showNotificationDialog = true
    if (showNotificationDialog) {
        SendNotificationDialog(
            onDismiss = { showNotificationDialog = false },
            onSend = { title, content ->
                // Gọi ViewModel để gửi thông báo
                viewModel.sendSystemNotification(title, content)
                showNotificationDialog = false // Đóng dialog sau khi bấm gửi
            }
        )
    }

    Scaffold(
        containerColor = AdminBgColor,
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Xin chào,", fontSize = 14.sp, color = Color.Gray)
                    Text("Quản trị viên", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tổng quan hệ thống", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), color = AdminPrimaryColor)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(count = uiState.userCount, label = "Người dùng", color = Color(0xFF2196F3))
                            StatItem(count = uiState.documentCount, label = "Tài liệu", color = Color(0xFFFF9800))
                            StatItem(count = uiState.requestCount, label = "Yêu cầu", color = Color(0xFFF44336))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Chức năng quản lý", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

            // --- DANH SÁCH CHỨC NĂNG ---

            DashboardActionItem(
                icon = Icons.Default.Group,
                title = "Quản lý người dùng",
                color = Color(0xFF2196F3),
                onClick = onNavigateToUsers
            )

            DashboardActionItem(
                icon = Icons.Default.Warning,
                title = "Duyệt tài liệu / Báo cáo vi phạm",
                color = Color(0xFFF44336),
                onClick = onNavigateToReports
            )

            // 🟢 3. Cập nhật nút Gửi thông báo
            DashboardActionItem(
                icon = Icons.Default.Notifications,
                title = "Gửi thông báo hệ thống",
                color = AdminPrimaryColor,
                onClick = { showNotificationDialog = true } // Bấm vào thì hiện Dialog
            )
        }
    }
}

@Composable
fun StatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
fun DashboardActionItem(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}