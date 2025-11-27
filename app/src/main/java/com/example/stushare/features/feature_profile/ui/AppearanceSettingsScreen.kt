package com.example.stushare.features.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// ⭐️ Import đúng Resource và Theme của dự án
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: AppearanceViewModel = viewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val currentFontScale by viewModel.fontScale.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    // Lấy màu động từ Theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance_language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                // ⭐️ SỬA: Dùng PrimaryGreen
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Giao diện Sáng/Tối
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        // ⭐️ SỬA: Dùng PrimaryGreen
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.appearance_dark_mode),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme(it) },
                        colors = SwitchDefaults.colors(
                            // ⭐️ SỬA: Dùng PrimaryGreen
                            checkedTrackColor = PrimaryGreen,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                HorizontalDivider(color = dividerColor)

                // 2. Đổi cỡ chữ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .clickable { showFontDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.appearance_font_size),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )

                    val fontSizeLabel = when(currentFontScale) {
                        0.85f -> stringResource(R.string.font_small)
                        1.15f -> stringResource(R.string.font_large)
                        else -> stringResource(R.string.font_medium)
                    }

                    Text(
                        text = fontSizeLabel,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f)
                    )
                }

                // 3. Header Ngôn ngữ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.appearance_language_header),
                        // ⭐️ SỬA: Dùng PrimaryGreen
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Item Thay đổi ngôn ngữ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .clickable { showLanguageDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.appearance_change_language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val currentLangLabel = if (currentLang == "vi") stringResource(R.string.current_language) else "English"
                    Text(
                        text = currentLangLabel,
                        fontSize = 15.sp,
                        color = onSurfaceColor
                    )
                }
            }

            // Bottom Curve
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(120.dp)
                    .offset(y = 60.dp)
                    .background(
                        // ⭐️ SỬA: Dùng PrimaryGreen
                        color = PrimaryGreen,
                        shape = RoundedCornerShape(topStart = 1000.dp, topEnd = 1000.dp)
                    )
            )
        }
    }

    // --- Dialog Chọn Ngôn Ngữ ---
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.appearance_change_language)) },
            text = {
                Column {
                    LanguageOption(stringResource(R.string.current_language), selected = currentLang == "vi") {
                        viewModel.setLanguage("vi")
                        showLanguageDialog = false
                    }
                    LanguageOption("English", selected = currentLang == "en") {
                        viewModel.setLanguage("en")
                        showLanguageDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }

    // --- Dialog Chọn Cỡ Chữ ---
    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text(stringResource(R.string.appearance_font_size)) },
            text = {
                Column {
                    FontOption(stringResource(R.string.font_small), selected = currentFontScale == 0.85f) {
                        viewModel.setFontScale(0.85f)
                        showFontDialog = false
                    }
                    FontOption(stringResource(R.string.font_medium), selected = currentFontScale == 1.0f) {
                        viewModel.setFontScale(1.0f)
                        showFontDialog = false
                    }
                    FontOption(stringResource(R.string.font_large), selected = currentFontScale == 1.15f) {
                        viewModel.setFontScale(1.15f)
                        showFontDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }
}

@Composable
fun LanguageOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
        )
        Text(
            text,
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FontOption(text: String, selected: Boolean, onClick: () -> Unit) {
    LanguageOption(text, selected, onClick)
}