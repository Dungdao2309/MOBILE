package com.example.stushare.features.feature_upload.ui

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ⚠️ 2. IMPORT VIEWMODEL: Đảm bảo đúng đường dẫn package bạn đã tạo cho ViewModel
import com.example.stushare.features.feature_upload.ui.UploadViewModel

// ⚠️ 3. IMPORT RESOURCE & THEME: Sử dụng R và Theme của Project A (App chính)
import com.example.stushare.R
// Nếu Project A chưa có màu GreenPrimary, hãy định nghĩa nó trong Color.kt của Project A
// Hoặc tạm thời dùng mã màu cứng: val GreenPrimary = Color(0xFF4CAF50)
import com.example.stushare.ui.theme.GreenPrimary

@Composable
fun UploadScreen(
    // Sử dụng viewModel() để Hilt hoặc Factory tự inject nếu cần,
    // hoặc truyền từ AppNavigation như hiện tại là OK.
    viewModel: UploadViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    // ... (Phần logic state giữ nguyên) ...
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val noFileStr = stringResource(R.string.upload_no_file)
    if (selectedFileName.isEmpty()) selectedFileName = noFileStr

    // ... (Phần Launcher giữ nguyên) ...
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            // Cấp quyền đọc URI lâu dài (quan trọng khi upload background hoặc xử lý sau này)
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Bỏ qua nếu không thể lấy quyền persist
            }

            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) selectedFileName = c.getString(nameIndex)
                }
            }
            if (title.isEmpty()) {
                title = selectedFileName.substringBeforeLast(".")
            }
        }
    }

    // ... (Phần xử lý sự kiện UploadEvent giữ nguyên) ...
    LaunchedEffect(Unit) {
        viewModel.uploadEvent.collect { result ->
            when (result) {
                is UploadViewModel.UploadResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    title = ""
                    description = ""
                    selectedFileName = noFileStr
                    selectedUri = null
                }
                is UploadViewModel.UploadResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // ... (Phần UI giữ nguyên) ...
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(GreenPrimary, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.padding(top = 32.dp, start = 16.dp)) {
                    Text("🔙", fontSize = 24.sp, color = Color.White)
                }
            }

            // Form Card
            Card(
                modifier = Modifier.padding(16.dp).offset(y = (-40).dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.upload_header),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // ... (Các UI component khác giữ nguyên) ...

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(stringResource(R.string.upload_selected_file), color = onSurfaceColor.copy(alpha = 0.6f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(selectedFileName, color = onSurfaceColor)
                    }

                    Button(
                        // Thêm type pdf và word
                        onClick = { filePickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.upload_choose_btn))
                    }

                    // ... (TextFields Title & Description giữ nguyên) ...
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.upload_title_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedLabelColor = GreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.upload_desc_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedLabelColor = GreenPrimary
                        )
                    )


                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        // Nút Hủy
                        OutlinedButton(
                            onClick = {
                                title = ""; description = ""; selectedUri = null; selectedFileName = noFileStr
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        // Nút Upload
                        val errorMsg = stringResource(R.string.upload_error_msg)
                        Button(
                            onClick = {
                                if (selectedUri != null && title.isNotEmpty()) {
                                    // ⚠️ Lưu ý: ViewModel cần xử lý URI này để lấy InputStream
                                    viewModel.handleUploadClick(title, description, selectedUri)
                                } else {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isUploading && selectedUri != null,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text(if (isUploading) stringResource(R.string.upload_btn_loading) else stringResource(R.string.upload_btn))
                        }
                    }
                }
            }
        }

        // Loading Indicator
        if (isUploading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
    }
}