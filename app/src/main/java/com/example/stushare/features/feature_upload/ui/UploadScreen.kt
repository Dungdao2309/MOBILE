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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
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
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val noFileStr = stringResource(R.string.upload_no_file)
    if (selectedFileName.isEmpty()) selectedFileName = noFileStr

    // Launcher chọn file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            // Cấp quyền đọc URI lâu dài
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Log lỗi nếu cần, nhưng thường thì vẫn đọc được trong session hiện tại
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

    // Lắng nghe sự kiện upload
    LaunchedEffect(Unit) {
        viewModel.uploadEvent.collect { result ->
            when (result) {
                is UploadViewModel.UploadResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
                is UploadViewModel.UploadResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(PrimaryGreen, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(top = 40.dp, start = 16.dp).align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Tải tài liệu lên",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center).padding(top = 20.dp)
                )
            }

            // --- FORM CARD ---
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .offset(y = (-20).dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text("Tệp đính kèm", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedFileName,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            filePickerLauncher.launch(arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.upload_choose_btn))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Input Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.upload_title_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.upload_desc_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Button Upload
                    Button(
                        onClick = {
                            if (selectedUri != null && title.isNotEmpty()) {
                                // ⭐️ CẬP NHẬT: Lấy mimeType thực tế và truyền vào ViewModel
                                val mimeType = context.contentResolver.getType(selectedUri!!) ?: "application/octet-stream"
                                viewModel.handleUploadClick(title, description, selectedUri, mimeType)
                            } else {
                                Toast.makeText(context, "Vui lòng chọn file và nhập tiêu đề", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Đăng tài liệu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}