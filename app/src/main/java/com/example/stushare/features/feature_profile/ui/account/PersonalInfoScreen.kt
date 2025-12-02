package com.example.stushare.features.feature_profile.ui.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.main.ProfileUiState
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel() // Sử dụng chung ViewModel với màn Profile
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 1. 🟢 LẤNG NGHE UI STATE (Thay vì userProfile cũ)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()

    // 2. State tên hiển thị (Local state để edit)
    var name by remember { mutableStateOf("") }

    // 3. Đồng bộ dữ liệu từ ViewModel vào ô nhập khi load xong
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Authenticated) {
            name = (uiState as ProfileUiState.Authenticated).profile.fullName
        }
    }

    // 4. Lắng nghe thông báo (Toast)
    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 5. Trình chọn ảnh (Đồng bộ với ProfileScreen)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadAvatar(uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.p_info_title), // "Thông tin cá nhân"
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 🟢 XỬ LÝ HIỂN THỊ THEO TRẠNG THÁI
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is ProfileUiState.Unauthenticated -> {
                    // Trường hợp hiếm khi vào màn này mà chưa login (xử lý an toàn)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Vui lòng đăng nhập lại")
                    }
                }

                is ProfileUiState.Authenticated -> {
                    // Hiển thị Form chỉnh sửa
                    PersonalInfoContent(
                        userProfile = state.profile,
                        nameState = name,
                        onNameChange = { name = it },
                        isUploadingAvatar = isUploadingAvatar,
                        onAvatarClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSaveClick = {
                            if (name.isNotBlank()) {
                                focusManager.clearFocus() // Ẩn bàn phím
                                viewModel.updateUserName(name)
                            } else {
                                Toast.makeText(context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalInfoContent(
    userProfile: UserProfile,
    nameState: String,
    onNameChange: (String) -> Unit,
    isUploadingAvatar: Boolean,
    onAvatarClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- CARD CHỨA THÔNG TIN ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. AVATAR AREA
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = userProfile.avatarUrl ?: "https://ui-avatars.com/api/?name=${userProfile.fullName}&background=random",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable(enabled = !isUploadingAvatar) { onAvatarClick() },
                        contentScale = ContentScale.Crop
                    )

                    // Loading indicator overlay
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = PrimaryGreen,
                            strokeWidth = 3.dp
                        )
                    }

                    // Icon Camera nhỏ
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                            .clickable { onAvatarClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. EMAIL (READ-ONLY)
                OutlinedTextField(
                    value = userProfile.email,
                    onValueChange = {},
                    enabled = false, // Không cho sửa email ở đây
                    label = { Text(stringResource(R.string.acc_sec_email)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray,
                        disabledLeadingIconColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. TÊN HIỂN THỊ (EDITABLE)
                OutlinedTextField(
                    value = nameState,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.p_info_name_hint)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedLeadingIconColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. BUTTON SAVE
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.p_info_save),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}