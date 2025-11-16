package com.example.stushare.core.di

import com.example.stushare.core.data.network.models.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonAdapter
import java.lang.reflect.Type

// =======================================================
// SỬA LỖI: CẬP NHẬT FLOAT ADAPTER ĐỂ XỬ LÝ INT HOẶC DOUBLE
// =======================================================
class CustomFloatAdapter : JsonAdapter<Float>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Float {
        // Kiểm tra xem token tiếp theo là SỐ NGUYÊN hay SỐ THỰC
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextDouble().toFloat() // Nếu là 4.0
            JsonReader.Token.STRING -> reader.nextString()?.toFloatOrNull() ?: 0.0f // Nếu là "4.0"
            // Xử lý các trường hợp khác nếu cần
            else -> {
                reader.skipValue() // Bỏ qua giá trị không mong muốn
                0.0f
            }
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Float?) {
        writer.value(value?.toDouble())
    }
}

object FloatAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        // Chỉ áp dụng cho kiểu Float (không phải Float?)
        if (type == Float::class.java || type == java.lang.Float::class.java) {
            return CustomFloatAdapter().nullSafe()
        }
        return null
    }
}
// =======================================================


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://my-json-server.typicode.com/nqthien1509/stushare-api/"

    // 1. "Dạy" Hilt cách tạo Moshi
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            // SỬA LỖI: Đăng ký Adapter dưới dạng Factory
            .add(FloatAdapterFactory) // <-- SỬ DỤNG FACTORY ĐÃ SỬA
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // 2. "Dạy" Hilt cách tạo OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // 3. "Dạy" Hilt cách tạo Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // 4. "Dạy" Hilt cách tạo ApiService
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}