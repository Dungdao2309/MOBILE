package com.example.stushare1

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

@Composable
fun ManHinhDangKy(boDieuHuong: NavController) {
    // Các biến trạng thái lưu trữ dữ liệu nhập vào
    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var xacNhanMatKhau by remember { mutableStateOf("") }

    var dangXuLy by remember { mutableStateOf(false) }

    // Biến trạng thái hiển thị lỗi
    var thongBaoLoi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun thucHienDangKy() {
        // Reset lỗi
        thongBaoLoi = ""

        if (email.isEmpty() || matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
            thongBaoLoi = "Vui lòng điền đầy đủ thông tin"
            return
        }

        // Kiểm tra định dạng email cơ bản
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            thongBaoLoi = "Định dạng email không hợp lệ"
            return
        }

        if (matKhau != xacNhanMatKhau) {
            thongBaoLoi = "Mật khẩu xác nhận không khớp"
            return
        }

        if (matKhau.length < 6) {
            thongBaoLoi = "Mật khẩu phải từ 6 ký tự trở lên"
            return
        }

        dangXuLy = true
        firebaseAuth.createUserWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                        popUpTo(ManHinh.DANG_KY) { inclusive = true }
                        popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
                    }
                } else {
                    // Xử lý các lỗi phổ biến khi Đăng ký
                    val ngoaiLe = tacVu.exception
                    thongBaoLoi = when (ngoaiLe) {
                        is FirebaseAuthUserCollisionException -> "Email này đã được sử dụng bởi tài khoản khác."
                        is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu (cần ít nhất 6 ký tự)."
                        is FirebaseAuthInvalidCredentialsException -> "Định dạng email không hợp lệ."
                        else -> "Lỗi: ${ngoaiLe?.message}"
                    }
                }
            }
    }

    // GIAO DIỆN
    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Đăng Ký",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhChuDao
            )

            Spacer(modifier = Modifier.height(40.dp))

            ONhapLieuTuyChinh(
                giaTri = email,
                khiThayDoi = {
                    email = it
                    thongBaoLoi = "" // Xóa lỗi khi nhập lại
                },
                nhanDan = "Email",
                icon = Icons.Default.Email,
                loaiBanPhim = KeyboardType.Email,
                coLoi = thongBaoLoi.contains("email") || thongBaoLoi.contains("Email") || thongBaoLoi.contains("đầy đủ")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mật khẩu
            ONhapLieuTuyChinh(
                giaTri = matKhau,
                khiThayDoi = {
                    matKhau = it
                    thongBaoLoi = ""
                },
                nhanDan = "Mật khẩu",
                icon = Icons.Default.Lock,
                laMatKhau = true,
                loaiBanPhim = KeyboardType.Password,
                coLoi = thongBaoLoi.contains("Mật khẩu") || thongBaoLoi.contains("đầy đủ")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Xác nhận mật khẩu
            ONhapLieuTuyChinh(
                giaTri = xacNhanMatKhau,
                khiThayDoi = {
                    xacNhanMatKhau = it
                    thongBaoLoi = ""
                },
                nhanDan = "Xác nhận Mật khẩu",
                icon = Icons.Default.Lock,
                laMatKhau = true,
                loaiBanPhim = KeyboardType.Password,
                coLoi = thongBaoLoi.contains("khớp") || thongBaoLoi.contains("đầy đủ")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PHẦN HIỂN THỊ LỖI
            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { thucHienDangKy() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
                shape = RoundedCornerShape(25.dp),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Đăng Ký Ngay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Đã có tài khoản? ", color = Color.Black)
                Text(
                    text = "Đăng nhập",
                    color = MauXanhChuDao,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { boDieuHuong.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ONhapLieuTuyChinh(
    giaTri: String,
    khiThayDoi: (String) -> Unit,
    nhanDan: String,
    icon: ImageVector,
    laMatKhau: Boolean = false,
    loaiBanPhim: KeyboardType = KeyboardType.Text,
    coLoi: Boolean = false
) {
    var hienThi by remember { mutableStateOf(false) }

    TextField(
        value = giaTri,
        onValueChange = khiThayDoi,
        placeholder = { Text(text = nhanDan, color = Color.Gray) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        trailingIcon = if (laMatKhau) {
            {
                val iconMat = if (hienThi) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { hienThi = !hienThi }) {
                    Icon(imageVector = iconMat, contentDescription = "Hiện/Ẩn mật khẩu", tint = Color.Gray)
                }
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            // Logic viền đỏ khi có lỗi
            .border(
                width = 1.dp,
                color = if (coLoi) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        isError = coLoi,
        visualTransformation = if (laMatKhau && !hienThi) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = loaiBanPhim),
        singleLine = true
    )
}