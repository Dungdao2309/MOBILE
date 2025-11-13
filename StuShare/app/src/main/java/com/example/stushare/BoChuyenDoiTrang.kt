package com.example.stushare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stushare.databinding.ItemTrangGioiThieuBinding

class BoChuyenDoiTrang(private val danhSachTrang: List<DuLieuTrang>) :
    RecyclerView.Adapter<BoChuyenDoiTrang.NguoiGiuView>() {

    // Inner class nắm giữ View
    inner class NguoiGiuView(val binding: ItemTrangGioiThieuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun ganDuLieu(trang: DuLieuTrang) {
            binding.tvTieuDe.text = trang.tieuDe
            binding.tvMoTa.text = trang.moTa
            binding.anhMinhHoa.setImageResource(trang.hinhAnh)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NguoiGiuView {
        val binding = ItemTrangGioiThieuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NguoiGiuView(binding)
    }

    override fun onBindViewHolder(holder: NguoiGiuView, position: Int) {
        holder.ganDuLieu(danhSachTrang[position])
    }

    override fun getItemCount(): Int = danhSachTrang.size
}