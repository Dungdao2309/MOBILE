package com.example.stushare.features.feature_profile.ui.main

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.model.DocItem
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    // 🟢 THÊM MỚI: Callback để chuyển sang màn hình chi tiết khi click vào tài liệu
    onDocumentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // 1. Lắng nghe UI State
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Lắng nghe dữ liệu từ ViewModel
    val publishedDocs by viewModel.publishedDocuments.collectAsStateWithLifecycle()
    val savedDocs by viewModel.savedDocuments.collectAsStateWithLifecycle()
    val downloadedDocs by viewModel.downloadedDocuments.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()

    // 3. Lắng nghe thông báo Toast
    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // 4. Bộ chọn ảnh
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.uploadAvatar(uri)
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            is ProfileUiState.Unauthenticated -> {
                UnauthenticatedProfileContent(
                    onLoginClick = onNavigateToLogin,
                    onRegisterClick = onNavigateToRegister
                )
            }

            is ProfileUiState.Authenticated -> {
                AuthenticatedProfileContent(
                    userProfile = state.profile,
                    publishedDocs = publishedDocs,
                    savedDocs = savedDocs,
                    downloadedDocs = downloadedDocs,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
                    onDeleteDoc = { docId -> viewModel.deletePublishedDocument(docId) },
                    onAvatarClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    // 🟢 Truyền callback click xuống dưới
                    onDocumentClick = onDocumentClick
                )
            }
        }

        if (isUploadingAvatar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
    }
}

// =================================================================
// CÁC COMPONENT CON (GIAO DIỆN CHI TIẾT)
// =================================================================

@Composable
fun UnauthenticatedProfileContent(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bạn chưa đăng nhập",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Đăng nhập để quản lý tài liệu, xem lịch sử tải xuống và tham gia bảng xếp hạng.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Đăng Nhập Ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, PrimaryGreen)
        ) {
            Text("Tạo tài khoản mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        }
    }
}

@Composable
fun AuthenticatedProfileContent(
    userProfile: UserProfile,
    publishedDocs: List<DocItem>,
    savedDocs: List<DocItem>,
    downloadedDocs: List<DocItem>,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onDeleteDoc: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onDocumentClick: (String) -> Unit // 🟢 Nhận callback
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        stringResource(R.string.profile_tab_posted),
        stringResource(R.string.profile_tab_saved),
        stringResource(R.string.profile_tab_downloaded)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        val userName = stringResource(R.string.profile_hello, userProfile.fullName)

        ProfileHeader(
            userName = userName,
            subText = stringResource(R.string.profile_dept),
            avatarUrl = userProfile.avatarUrl,
            onSettingsClick = onNavigateToSettings,
            onLeaderboardClick = onNavigateToLeaderboard,
            onAvatarClick = onAvatarClick
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryGreen
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        // Chọn danh sách hiển thị dựa theo Tab
        val currentList = when (selectedTabIndex) {
            0 -> publishedDocs
            1 -> savedDocs // 🟢 Tab 1 là danh sách đã lưu
            2 -> downloadedDocs
            else -> emptyList()
        }

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.profile_empty_list),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentList) { doc ->
                    DocItemRow(
                        item = doc,
                        isDeletable = selectedTabIndex == 0,
                        onDelete = { onDeleteDoc(doc.documentId) },
                        // 🟢 Sự kiện click vào item -> Chuyển màn hình
                        onClick = { onDocumentClick(doc.documentId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    subText: String,
    avatarUrl: String?,
    onSettingsClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.padding(bottom = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() }
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = onLeaderboardClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = stringResource(R.string.profile_view_leaderboard),
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun DocItemRow(
    item: DocItem,
    isDeletable: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit // 🟢 Thêm tham số sự kiện Click
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }, // 🟢 Kích hoạt sự kiện Click cho toàn bộ Card
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.docTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    text = item.meta,
                    color = contentColor.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.6f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(cardColor)
                ) {
                    if (isDeletable) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete), color = contentColor) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = contentColor) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.feature_not_supported), color = contentColor) },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }
    }
}