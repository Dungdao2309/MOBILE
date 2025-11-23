package com.example.stushare1

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.FirebaseTooManyRequestsException

@Composable
fun ManHinhDangNhap(boDieuHuong: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("DangNhapPrefs", Context.MODE_PRIVATE)

    // Lấy dữ liệu đã lưu (nếu có)
    var email by remember { mutableStateOf(sharedPreferences.getString("email", "") ?: "") }
    var matKhau by remember { mutableStateOf(sharedPreferences.getString("matKhau", "") ?: "") }
    var nhoMatKhau by remember { mutableStateOf(sharedPreferences.getBoolean("nhoMatKhau", false)) }

    // Biến trạng thái cho việc hiện/ẩn mật khẩu
    var hienThiMatKhau by remember { mutableStateOf(false) }

    var loiEmail by remember { mutableStateOf(false) }
    var dangXuLy by remember { mutableStateOf(false) }

    // Biến trạng thái để hiển thị lỗi lên màn hình
    var thongBaoLoi by remember { mutableStateOf("") }

    val firebaseAuth = FirebaseAuth.getInstance()

    // CẤU HÌNH GOOGLE SIGN IN
    val tuyChonGoogle = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, tuyChonGoogle)

    val trinhKhoiChayGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { ketQua ->
        val tacVu = GoogleSignIn.getSignedInAccountFromIntent(ketQua.data)
        try {
            val taiKhoan = tacVu.getResult(ApiException::class.java)
            if (taiKhoan != null) {
                dangXuLy = true
                val chungChi = GoogleAuthProvider.getCredential(taiKhoan.idToken, null)
                firebaseAuth.signInWithCredential(chungChi)
                    .addOnCompleteListener { nhiemVu ->
                        dangXuLy = false
                        if (nhiemVu.isSuccessful) {
                            Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                            boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                                popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
                            }
                        } else {
                            thongBaoLoi = "Lỗi Firebase: ${nhiemVu.exception?.message}"
                        }
                    }
            }
        } catch (e: ApiException) {
            thongBaoLoi = "Lỗi Google: ${e.message}"
        }
    }

    // Hàm kiểm tra Email hợp lệ
    fun kiemTraEmailHopLe(emailCheck: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(emailCheck).matches()
    }

    fun thucHienDangNhap() {
        // Reset trạng thái lỗi
        thongBaoLoi = ""
        loiEmail = false

        if (email.isEmpty() || matKhau.isEmpty()) {
            thongBaoLoi = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        if (!kiemTraEmailHopLe(email)) {
            loiEmail = true
            thongBaoLoi = "Định dạng email không đúng!"
            return
        }

        val editor = sharedPreferences.edit()
        if (nhoMatKhau) {
            editor.putString("email", email)
            editor.putString("matKhau", matKhau)
            editor.putBoolean("nhoMatKhau", true)
        } else {
            editor.clear()
        }
        editor.apply()

        dangXuLy = true
        firebaseAuth.signInWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                        popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
                    }
                } else {
                    // Phân loại lỗi từ Firebase
                    val ngoaiLe = tacVu.exception
                    thongBaoLoi = when (ngoaiLe) {
                        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại hoặc đã bị xóa."
                        is FirebaseAuthInvalidCredentialsException -> "Sai email hoặc mật khẩu."
                        is FirebaseTooManyRequestsException -> "Quá nhiều lần thử sai. Vui lòng thử lại sau."
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
                text = "Đăng Nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhChuDao
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ô nhập Email
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    loiEmail = false
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (loiEmail) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    disabledContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                isError = loiEmail,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // Chỉ hiện thông báo lỗi cụ thể cho Email nếu cần, hoặc dùng thongBaoLoi chung bên dưới
            if (loiEmail) {
                Text(
                    text = "Email không hợp lệ",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ô nhập Mật khẩu
            TextField(
                value = matKhau,
                onValueChange = { matKhau = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    val icon = if (hienThiMatKhau) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (hienThiMatKhau) "Ẩn mật khẩu" else "Hiện mật khẩu"

                    IconButton(onClick = { hienThiMatKhau = !hienThiMatKhau }) {
                        Icon(imageVector = icon, contentDescription = description, tint = Color.Gray)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                visualTransformation = if (hienThiMatKhau) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = nhoMatKhau,
                        onCheckedChange = { nhoMatKhau = it },
                        colors = CheckboxDefaults.colors(checkedColor = MauXanhChuDao)
                    )
                    Text(text = "Nhớ mật khẩu", fontSize = 14.sp)
                }
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { boDieuHuong.navigate(ManHinh.QUEN_MAT_KHAU) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PHẦN HIỂN THỊ LỖI
            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { thucHienDangNhap() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhChuDao),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Đăng Nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Chưa có tài khoản, ")
                Text(
                    text = "Đăng ký",
                    color = MauXanhChuDao,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { boDieuHuong.navigate(ManHinh.DANG_KY) }
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
                Text(
                    text = "hoặc",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color.Gray
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                NutIconTronAnh(
                    anh = painterResource(id = R.drawable.ic_dienthoai1),
                    mauVien = Color.Black,
                    onClick = { boDieuHuong.navigate(ManHinh.DANG_NHAP_SDT) }
                )

                Spacer(modifier = Modifier.width(24.dp))

                NutIconTronAnh(
                    anh = painterResource(id = R.drawable.ic_google),
                    mauVien = Color.Transparent,
                    onClick = { trinhKhoiChayGoogle.launch(googleSignInClient.signInIntent) }
                )
            }
        }
    }
}

@Composable
fun NutIconTronAnh(
    anh: androidx.compose.ui.graphics.painter.Painter,
    mauVien: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 4.dp,
        border = if (mauVien != Color.Transparent) BorderStroke(2.dp, mauVien) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = anh,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}