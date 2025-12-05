package com.example.stushare.features.feature_admin.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.Report
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit, // Callback mở xem tài liệu
    viewModel: AdminViewModel = hiltViewModel()
) {
    // Collect State
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // State cho Dialog xác nhận xóa
    var reportToDelete by remember { mutableStateOf<Report?>(null) }

    // Lắng nghe Toast thông báo kết quả
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // HỘP THOẠI XÁC NHẬN XÓA
    if (reportToDelete != null) {
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red) },
            title = { Text("Xóa tài liệu bị báo cáo?") },
            text = {
                Text("Hành động này sẽ xóa vĩnh viễn tài liệu \"${reportToDelete?.documentTitle}\" khỏi hệ thống và đánh dấu báo cáo là đã giải quyết.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        reportToDelete?.let { report ->
                            viewModel.deleteDocument(report.documentId, report.id)
                        }
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) { Text("Hủy bỏ") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý vi phạm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Red
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isProcessing) {
                // Loading Overlay
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Red)
                }
            } else if (reports.isEmpty()) {
                // Empty State đẹp hơn
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tuyệt vời!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Hiện không có báo cáo vi phạm nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportItem(
                            report = report,
                            onDeleteClick = { reportToDelete = report }, // Hiện dialog thay vì xóa ngay
                            onDismissClick = { viewModel.dismissReport(report.id) },
                            onViewDocumentClick = { onDocumentClick(report.documentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    report: Report,
    onDeleteClick: () -> Unit,
    onDismissClick: () -> Unit,
    onViewDocumentClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Lý do (Màu đỏ để gây chú ý)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = report.reason,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Content: Thông tin tài liệu
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = report.documentTitle.ifBlank { "ID: ${report.documentId}" },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Người báo cáo: ${report.reporterEmail}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 24.dp) // Thụt vào cho thẳng hàng
            )

            // Format ngày tháng an toàn
            val date = try {
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(report.timestamp)
            } catch (e: Exception) { "" }

            if (date.isNotEmpty()) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp).fillMaxWidth(), color = Color(0xFFEEEEEE))

            // Buttons Actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Xem
                TextButton(onClick = onViewDocumentClick) {
                    Icon(Icons.Default.Visibility, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xem tài liệu")
                }

                Row {
                    // Nút Bỏ qua
                    OutlinedButton(
                        onClick = onDismissClick,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        border = null // Style nhẹ nhàng hơn
                    ) {
                        Text("Bỏ qua", color = Color.Gray)
                    }

                    Spacer(Modifier.width(8.dp))

                    // Nút Xóa
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Đỏ đậm hơn chút
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        Text("Xóa", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}