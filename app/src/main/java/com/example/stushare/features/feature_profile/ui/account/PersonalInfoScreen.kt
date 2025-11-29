package com.example.stushare.features.feature_profile.ui.account

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt // 👈 Icon máy ảnh
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage // 👈 Import Coil
import coil.request.ImageRequest
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 1. Lấy dữ liệu User & Trạng thái upload
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()

    // 2. State tên hiển thị
    var name by remember { mutableStateOf("") }

    // 3. Cập nhật dữ liệu vào ô nhập khi load xong
    LaunchedEffect(userProfile) {
        userProfile?.let { user ->
            name = user.fullName
        }
    }

    // 4. Lắng nghe thông báo kết quả (Toast)
    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 5. Trình chọn ảnh từ thư viện
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Gọi ViewModel để upload ngay khi chọn xong
            viewModel.uploadAvatar(it)
        }
    }

    // Màu sắc từ Theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.p_info_title), // "Thông tin cá nhân"
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- KHU VỰC AVATAR ---
                    Box(contentAlignment = Alignment.Center) {
                        // 1. Ảnh đại diện (Dùng Coil)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfile?.avatarUrl) // URL từ Firebase
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                            // Placeholder nếu chưa có ảnh hoặc lỗi
                            error = null, // Có thể để icon mặc định nếu muốn
                            fallback = null
                        )

                        // Nếu chưa có Avatar thì hiện Icon mặc định đè lên
                        if (userProfile?.avatarUrl.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = onSurfaceColor.copy(alpha = 0.4f)
                            )
                        }

                        // 2. Loading Indicator (Khi đang upload)
                        if (isUploadingAvatar) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(100.dp),
                                color = PrimaryGreen,
                                strokeWidth = 4.dp
                            )
                        }

                        // 3. Nút Máy ảnh nhỏ (Nút bấm để đổi ảnh)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd) // Góc dưới phải
                                .offset(x = 4.dp, y = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen)
                                .clickable(enabled = !isUploadingAvatar) {
                                    // Mở thư viện ảnh
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- EMAIL (Read-only) ---
                    OutlinedTextField(
                        value = userProfile?.email ?: "Loading...",
                        onValueChange = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.acc_sec_email)) }, // "Email"
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = onSurfaceColor.copy(alpha = 0.7f),
                            disabledBorderColor = Color.LightGray,
                            disabledLabelColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TÊN HIỂN THỊ (Editable) ---
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.p_info_name_hint)) }, // "Tên hiển thị"
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedLabelColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen,
                            focusedLeadingIconColor = PrimaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- NÚT LƯU ---
                    Button(
                        onClick = { viewModel.updateUserName(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.p_info_save), // "Lưu thay đổi"
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}