package com.example.stushare1

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone


@Composable
fun ManHinhDangNhapSDT(boDieuHuong: NavController) {
    var soDienThoai by remember { mutableStateOf("") }
    var loiSdt by remember { mutableStateOf(false) } // Trạng thái lỗi
    var dangXuLy by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // Hàm kiểm tra định dạng số điện thoại
    fun kiemTraDinhDangSDT(sdt: String): Boolean {
        // Bắt đầu bằng số 0, theo sau là 9 chữ số bất kỳ (Tổng 10 số)
        val pattern = Regex("^0[0-9]{9}$")
        return pattern.matches(sdt)
    }

    // Hàm xử lý gửi mã
    fun guiMaOTP() {
        // 1. Reset lỗi
        loiSdt = false

        // 2. Kiểm tra rỗng
        if (soDienThoai.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Kiểm tra định dạng bằng Regex
        if (!kiemTraDinhDangSDT(soDienThoai)) {
            loiSdt = true // Kích hoạt trạng thái lỗi để hiện viền đỏ
            return
        }

        dangXuLy = true

        // Chuyển đổi số điện thoại về định dạng quốc tế +84
        // Vì đã validate regex bắt đầu bằng 0 nên substring(1) luôn an toàn
        val soDienThoaiChuan = "+84${soDienThoai.substring(1)}"

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                dangXuLy = false
            }

            override fun onVerificationFailed(e: FirebaseException) {
                dangXuLy = false
                // Hiển thị lỗi chi tiết từ Firebase (ví dụ: chặn spam, hạn ngạch)
                if (e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                    loiSdt = true
                    Toast.makeText(context, "Số điện thoại không hợp lệ.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                dangXuLy = false
                Toast.makeText(context, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show()
                // Chuyển sang màn hình nhập OTP, truyền theo verificationId
                boDieuHuong.navigate("man_hinh_xac_thuc_otp/$verificationId")
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(soDienThoaiChuan)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

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

            Spacer(modifier = Modifier.height(30.dp))

            // Ô nhập số điện thoại
            TextField(
                value = soDienThoai,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        soDienThoai = it
                        loiSdt = false // Xóa lỗi khi người dùng nhập lại
                    }
                },
                placeholder = { Text("Số điện thoại (10 số)", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    // Nếu có lỗi thì viền đỏ, không thì trong suốt
                    .border(
                        width = 1.dp,
                        color = if (loiSdt) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Color.Transparent),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = loiSdt
            )

            // Thông báo lỗi hiển thị bên dưới
            if (loiSdt) {
                Text(
                    text = "Số điện thoại không đúng định dạng (cần 10 số, bắt đầu là 0)",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { guiMaOTP() },
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
                    Text("Gửi mã OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}