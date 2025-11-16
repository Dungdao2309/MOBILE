package com.example.stushare.core.data.network.models

import com.example.stushare.core.data.models.DocumentRequest
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "authorName") val authorName: String
)

// Hàm "dịch" từ DTO (API) sang Entity (Database)
fun RequestDto.toRequestEntity(): DocumentRequest {
    return DocumentRequest(
        id = this.id,
        title = this.title,
        authorName = this.authorName
    )
}