package com.example.stushare.core.data.network.models

import com.example.stushare.core.data.models.Document
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 1. Đánh dấu để Moshi biết đây là file JSON
@JsonClass(generateAdapter = true)
data class DocumentDto(
    // ID là String vì API thường trả về JSON String
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "downloads") val downloads: Int,
    @Json(name = "rating") val rating: Float, // Vẫn là Float từ API
    @Json(name = "author") val author: String,
    @Json(name = "course_code") val courseCode: String
)

/**
 * Hàm "Ánh xạ" (Mapper)
 * ĐÃ CẢI TIẾN: Chuyển đổi kiểu dữ liệu (String -> Long, Float -> Double)
 */
fun DocumentDto.toDocumentEntity(): Document {
    return Document(
        // CHUYỂN ĐỔI ID: Từ String sang Long (hoặc Int nếu entity là Int)
        id = this.id.toLongOrNull() ?: 0L, // Dùng toLongOrNull() và cung cấp giá trị mặc định (0L)
        title = this.title,
        type = this.type,
        imageUrl = this.imageUrl,
        downloads = this.downloads,
        // CHUYỂN ĐỔI RATING: Từ Float sang Double (Giả định Entity dùng Double)
        rating = this.rating.toDouble(),
        author = this.author,
        courseCode = this.courseCode
    )
}