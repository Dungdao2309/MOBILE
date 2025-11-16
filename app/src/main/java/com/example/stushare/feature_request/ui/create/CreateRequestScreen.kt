// ĐƯỜNG DẪN: .../feature_request/ui/create/CreateRequestScreen.kt

package com.example.stushare.features.feature_request.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    viewModel: CreateRequestViewModel = hiltViewModel() // <-- LẤY VIEWMODEL
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // PHẦN 1: HEADER
        CreateRequestHeader(onBackClick = onBackClick)

        // PHẦN 2: FORM NỘI DUNG
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Tiêu đề yêu cầu", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FormTextField(value = title, onValueChange = { title = it }, "Cần tìm đề cuối kỳ...")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Môn học (*)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FormTextField(value = subject, onValueChange = { subject = it }, "Tư tưởng Hồ Chí Minh")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Mô tả", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FormTextField(value = description, onValueChange = { description = it }, "Dành cho không chuyên...", minLines = 4)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red)
                ) {
                    Text("Hủy", fontSize = 16.sp)
                }
                Button(
                    onClick = {
                        // GỌI HÀM CỦA VIEWMODEL ĐỂ LƯU VÀO DB
                        viewModel.submitRequest(title, subject, description)
                        // GỌI HÀM CALLBACK (để quay về)
                        onSubmitClick()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Gửi yêu cầu", fontSize = 16.sp)
                }
            }
        }
    }
}

// Header
@Composable
private fun CreateRequestHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay về", tint = Color.White)
        }
        Text(
            "Tạo yêu cầu mới",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

// Component Ô nhập liệu
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightGreen.copy(alpha = 0.5f),
            unfocusedContainerColor = LightGreen.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        minLines = minLines
    )
}