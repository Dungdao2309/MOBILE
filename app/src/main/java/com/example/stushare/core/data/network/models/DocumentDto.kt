package com.example.stushare.core.data.network.models

import com.example.stushare.core.data.models.Document
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocumentDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
    @Json(name = "image_url") val imageUrl: String,

    // ⭐️ THÊM MỚI: Hứng link tải file từ API
    // Cho phép null (? = null) để nếu API cũ chưa có thì App không bị Crash
    @Json(name = "file_url") val fileUrl: String? = null,

    @Json(name = "downloads") val downloads: Int,
    @Json(name = "rating") val rating: Float,
    @Json(name = "author") val author: String,
    @Json(name = "course_code") val courseCode: String
)

fun DocumentDto.toDocumentEntity(): Document {
    return Document(
        id = this.id,
        title = this.title,
        type = this.type,
        imageUrl = this.imageUrl,

        // ⭐️ THÊM MỚI: Map link từ DTO sang Entity
        // Nếu API trả về null thì dùng chuỗi rỗng ""
        fileUrl = this.fileUrl ?: "",

        downloads = this.downloads,
        rating = this.rating.toDouble(),
        author = this.author,
        courseCode = this.courseCode,

        createdAt = System.currentTimeMillis(),
        authorId = null
    )
}