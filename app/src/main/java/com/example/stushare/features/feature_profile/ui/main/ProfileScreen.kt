package com.example.stushare.features.feature_profile.ui.main

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onNavigateToRegister: () -> Unit
) {
    // Lấy dữ liệu từ ViewModel
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val publishedDocs by viewModel.publishedDocuments.collectAsStateWithLifecycle()
    val savedDocs by viewModel.savedDocuments.collectAsStateWithLifecycle()
    val downloadedDocs by viewModel.downloadedDocuments.collectAsStateWithLifecycle()

    // Màu nền chính của màn hình (Trắng hoặc Đen)
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        if (userProfile == null) {
            // Màn hình chưa đăng nhập
            UnauthenticatedProfileContent(
                onLoginClick = onNavigateToLogin,
                onRegisterClick = onNavigateToRegister
            )
        } else {
            // Màn hình đã đăng nhập
            AuthenticatedProfileContent(
                userProfile = userProfile!!,
                publishedDocs = publishedDocs,
                savedDocs = savedDocs,
                downloadedDocs = downloadedDocs,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onDeleteDoc = { docId -> viewModel.deletePublishedDocument(docId) }
            )
        }
    }
}

// =================================================================
// 1. GIAO DIỆN KHI CHƯA ĐĂNG NHẬP
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
            // ⭐️ FIX: Màu icon mờ theo theme
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bạn chưa đăng nhập",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            // ⭐️ FIX: Màu chữ theo theme
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

// =================================================================
// 2. GIAO DIỆN KHI ĐÃ ĐĂNG NHẬP
// =================================================================
@Composable
fun AuthenticatedProfileContent(
    userProfile: UserProfile,
    publishedDocs: List<DocItem>,
    savedDocs: List<DocItem>,
    downloadedDocs: List<DocItem>,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onDeleteDoc: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        stringResource(R.string.profile_tab_posted),
        stringResource(R.string.profile_tab_saved),
        stringResource(R.string.profile_tab_downloaded)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        val userName = stringResource(R.string.profile_hello, userProfile.fullName)

        // Header Profile
        ProfileHeader(
            userName = userName,
            subText = stringResource(R.string.profile_dept),
            onSettingsClick = onNavigateToSettings,
            onLeaderboardClick = onNavigateToLeaderboard
        )

        // ⭐️ FIX: TabRow dùng màu Surface (tự động tối khi Dark Mode)
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
                            // ⭐️ FIX: Logic màu chữ Tab
                            // Chọn -> Xanh. Không chọn -> Màu chữ theme (Trắng/Đen)
                            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        val currentList = when (selectedTabIndex) {
            0 -> publishedDocs
            1 -> savedDocs
            2 -> downloadedDocs
            else -> emptyList()
        }

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.profile_empty_list),
                    // ⭐️ FIX: Màu chữ thông báo trống
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(currentList) { doc ->
                    DocItemRow(
                        item = doc,
                        isDeletable = selectedTabIndex == 0,
                        onDelete = { onDeleteDoc(doc.documentId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// =================================================================
// 3. CÁC COMPONENT PHỤ
// =================================================================

@Composable
fun ProfileHeader(
    userName: String,
    subText: String,
    onSettingsClick: () -> Unit,
    onLeaderboardClick: () -> Unit
) {
    Card(
        // ⭐️ FIX: Màu nền Card theo theme
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.padding(bottom = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                // ⭐️ FIX: Màu icon avatar
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
                modifier = Modifier
                    .size(64.dp)
                    // ⭐️ FIX: Nền tròn sau avatar
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    // ⭐️ FIX: Màu tên user
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Nút "Xem bảng xếp hạng" (Giữ màu cam làm điểm nhấn)
                Surface(
                    onClick = onLeaderboardClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0), // Màu nền cam nhạt
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = stringResource(R.string.profile_view_leaderboard),
                        color = Color(0xFFFF9800), // Màu chữ cam đậm
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
                    // ⭐️ FIX: Màu nút cài đặt
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun DocItemRow(item: DocItem, isDeletable: Boolean, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Lấy màu từ theme
    val cardColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        // ⭐️ FIX: Màu nền Card tài liệu
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon tài liệu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    // ⭐️ FIX: Màu nền ô vuông icon
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Thêm icon tài liệu vào đây cho đẹp
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
                    // ⭐️ FIX: Màu tên tài liệu
                    color = contentColor
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
                    modifier = Modifier.background(cardColor) // Đảm bảo menu cùng màu card
                ) {
                    if (isDeletable) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete), color = contentColor) },
                            onClick = {
                                onDelete()
                                showMenu = false
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
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