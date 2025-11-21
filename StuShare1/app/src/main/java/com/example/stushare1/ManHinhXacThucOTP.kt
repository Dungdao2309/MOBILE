package com.example.stushare1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay

@Composable
fun ManHinhXacThucOTP(boDieuHuong: NavController, verificationId: String) {
    var maOTP by remember { mutableStateOf("") }
    var thoiGianDemNguoc by remember { mutableStateOf(59) }
    var dangXuLy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // Logic đếm ngược thời gian
    LaunchedEffect(Unit) {
        while (thoiGianDemNguoc > 0) {
            delay(1000)
            thoiGianDemNguoc--
        }
    }

    fun xacNhanMaOTP() {
        if (maOTP.length != 6) {
            Toast.makeText(context, "Mã OTP phải đủ 6 số", Toast.LENGTH_SHORT).show()
            return
        }
        dangXuLy = true

        val credential = PhoneAuthProvider.getCredential(verificationId, maOTP)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(ManHinh.TRANG_CHU) {
                        popUpTo(ManHinh.DANG_NHAP) { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Mã OTP không đúng", Toast.LENGTH_SHORT).show()
                }
            }
    }

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Căn giữa form theo chiều dọc
        ) {
            Text(
                text = "Xác thực OTP",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhChuDao,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Giao diện nhập OTP 6 số
            BasicTextField(
                value = maOTP,
                onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) maOTP = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = if (index < maOTP.length) maOTP[index].toString() else ""

                            if (index == 3) {
                                Text(
                                    text = "-",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(55.dp)
                                    .padding(4.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (index < maOTP.length) MauXanhChuDao else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dòng gửi lại mã
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Gửi lại mã ", color = MauXanhChuDao, fontSize = 14.sp)
                Text(
                    text = "(${String.format("00:%02d", thoiGianDemNguoc)})",
                    color = MauXanhChuDao,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { xacNhanMaOTP() },
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
                    Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Quay lại
            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}