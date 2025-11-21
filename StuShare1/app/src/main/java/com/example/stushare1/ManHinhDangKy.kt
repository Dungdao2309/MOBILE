package com.example.stushare1

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ManHinhDangKy(boDieuHuong: NavController) {
    // Các biến trạng thái lưu trữ dữ liệu nhập vào
    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var xacNhanMatKhau by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun thucHienDangKy() {
        if (email.isEmpty() || matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (matKhau != xacNhanMatKhau) {
            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (matKhau.length < 6) {
            Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Lỗi: ${tacVu.exception?.message}", Toast.LENGTH_SHORT).show()
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
                khiThayDoi = { email = it },
                nhanDan = "Email",
                icon = Icons.Default.Email,
                loaiBanPhim = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mật khẩu
            ONhapLieuTuyChinh(
                giaTri = matKhau,
                khiThayDoi = { matKhau = it },
                nhanDan = "Mật khẩu",
                icon = Icons.Default.Lock,
                laMatKhau = true,
                loaiBanPhim = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Xác nhận mật khẩu
            ONhapLieuTuyChinh(
                giaTri = xacNhanMatKhau,
                khiThayDoi = { xacNhanMatKhau = it },
                nhanDan = "Xác nhận Mật khẩu",
                icon = Icons.Default.Lock,
                laMatKhau = true,
                loaiBanPhim = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(32.dp))

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

// --- COMPONENT CON: Ô NHẬP LIỆU TÙY CHỈNH (ĐÃ CẬP NHẬT NÚT MẮT) ---
@Composable
fun ONhapLieuTuyChinh(
    giaTri: String,
    khiThayDoi: (String) -> Unit,
    nhanDan: String,
    icon: ImageVector,
    laMatKhau: Boolean = false,
    loaiBanPhim: KeyboardType = KeyboardType.Text
) {
    // Biến trạng thái riêng cho từng ô nhập liệu (để mỗi ô có thể ẩn/hiện độc lập)
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
        // Chỉ hiện nút mắt nếu ô này là ô nhập mật khẩu
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
            .clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        // Logic hiển thị: Nếu không phải mật khẩu -> Hiện luôn.
        // Nếu là mật khẩu -> Phụ thuộc vào biến hienThi
        visualTransformation = if (laMatKhau && !hienThi) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = loaiBanPhim),
        singleLine = true
    )
}