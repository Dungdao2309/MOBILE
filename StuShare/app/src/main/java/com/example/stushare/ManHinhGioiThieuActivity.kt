package com.example.stushare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.stushare.databinding.ActivityManHinhGioiThieuBinding
import com.google.android.material.tabs.TabLayoutMediator

class ManHinhGioiThieuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManHinhGioiThieuBinding
    private lateinit var boChuyenDoi: BoChuyenDoiTrang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManHinhGioiThieuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        khoiTaoDuLieuVaGiaoDien()
        xuLySuKien()
    }

    private fun khoiTaoDuLieuVaGiaoDien() {
        // 1. Chuẩn bị dữ liệu (Phần quan trọng nhất của OOP: Khởi tạo Object)
        val danhSach = listOf(
            DuLieuTrang(
                tieuDe = "Tìm kiếm thông minh, đúng chuyên ngành",
                moTa = "Truy cập kho tài liệu khổng lồ từ mọi khoa và môn học tại UTH. Tìm kiếm đề thi, slide bài giảng , và ghi chú chỉ trong vai giây",
                hinhAnh = R.drawable.intro1 // Ảnh 2
            ),
            DuLieuTrang(
                tieuDe = "Chia sẻ tri thức, xây dựng cộng đồng",
                moTa = "Trở thành một phần của cộng đồng sinh viên UTH. Đăng tải tài liệu của bạn, giúp đỡ bạn bè và tích lũy điểm đóng góp.",
                hinhAnh = R.drawable.intro2 // Ảnh 3
            ),
            DuLieuTrang(
                tieuDe = "Ôn tập hiệu quả, chinh phục điểm cao",
                moTa = "Tất cả tài liệu bạn cần cho các kỳ thi đều ở đây. Hãy học tập thông minh hơn và sẵn sàng cho mọi thử thách.",
                hinhAnh = R.drawable.intro3 // Ảnh 4
            )
        )

        // 2. Gán Adapter
        boChuyenDoi = BoChuyenDoiTrang(danhSach)
        binding.vungHienThiTrang.adapter = boChuyenDoi

        // 3. Liên kết TabLayout với ViewPager2 (Để tạo dấu chấm tròn)
        TabLayoutMediator(binding.tabLayoutChamTron, binding.vungHienThiTrang) { tab, position ->
            tab.setCustomView(R.layout.tab_dot)
        }.attach()
    }

    private fun xuLySuKien() {
        // Lắng nghe sự kiện vuốt trang để đổi text nút bấm
        binding.vungHienThiTrang.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) { // Trang cuối cùng (index bắt đầu từ 0)
                    binding.nutHanhDongChinh.text = "Bắt đầu"
                } else {
                    binding.nutHanhDongChinh.text = "Tiếp Tục"
                }
            }
        })

        // Xử lý click nút Xanh
        binding.nutHanhDongChinh.setOnClickListener {
            val trangHienTai = binding.vungHienThiTrang.currentItem
            if (trangHienTai < 2) {
                // Chưa phải trang cuối -> Next trang
                binding.vungHienThiTrang.currentItem = trangHienTai + 1
            } else {
                // Đã là trang cuối -> Vào màn hình chính
                chuyenDenManHinhChinh()
            }
        }

        // Xử lý click nút Bỏ qua
        binding.nutBoQua.setOnClickListener {
            chuyenDenManHinhChinh()
        }
    }

    private fun chuyenDenManHinhChinh() {
        // TODO: Tạo Activity Màn Hình Chính (ví dụ Login hoặc Home)
        // val intent = Intent(this, ManHinhChinhActivity::class.java)
        // startActivity(intent)
        // finish()
    }
}