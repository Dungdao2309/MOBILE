package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.DocumentDto
import com.example.stushare.core.data.network.models.toDocumentEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// LƯU Ý: CẦN THÊM CÁC DÒNG IMPORT SAU VÀO ĐẦU FILE NẾU BỊ LỖI
// import org.junit.Assert.* // import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class DocumentRepositoryImplTest {

    private val mockDocumentDao: DocumentDao = mockk(relaxed = true)
    private val mockApiService: ApiService = mockk(relaxed = true)
    private lateinit var repository: DocumentRepositoryImpl

    // DATA GIẢ LẬP - ĐÃ KHẮC PHỤC LỖI THIẾU THAM SỐ VÀ SAI KIỂU DỮ LIỆU
    private val fakeDocumentDtoList = listOf(
        DocumentDto(
            id = "1",
            title = "Mạng",
            author = "User A",
            courseCode = "CS101",
            downloads = 10,
            rating = 4.5f, // <-- Kiểu Float (cần hậu tố 'f')
            type = "Sách",
            imageUrl = "http://fake.com/img1.jpg" // <-- Tham số giả định
        ),
        DocumentDto(
            id = "2",
            title = "Cơ sở dữ liệu",
            author = "User B",
            courseCode = "CS201",
            downloads = 5,
            rating = 4.0f, // <-- Kiểu Float (cần hậu tố 'f')
            type = "Tài Liệu",
            imageUrl = "http://fake.com/img2.jpg"
        )
    )

    private val expectedEntityList = fakeDocumentDtoList.map {
        // Hàm ánh xạ: Đảm bảo tồn tại DocumentDto.toDocumentEntity()
        it.toDocumentEntity()
    }

    @Before
    fun setup() {
        repository = DocumentRepositoryImpl(mockDocumentDao, mockApiService)
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN 1: REFRESH DỮ LIỆU THÀNH CÔNG
    // -----------------------------------------------------------------
    @Test
    fun `refreshDocuments_success_callsApiAndInsertsIntoDb`() = runTest {
        coEvery { mockApiService.getAllDocuments() } returns fakeDocumentDtoList

        repository.refreshDocuments()

        coVerify(exactly = 1) { mockApiService.getAllDocuments() }
        coVerify(exactly = 1) { mockDocumentDao.insertAllDocuments(expectedEntityList) }
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN 2: REFRESH THẤT BẠI (LỖI MẠNG)
    // -----------------------------------------------------------------
    @Test
    fun `refreshDocuments_networkFailure_throwsNetworkError`() = runTest {
        coEvery { mockApiService.getAllDocuments() } throws IOException()

        assertFailsWith<DataFailureException.NetworkError> {
            repository.refreshDocuments()
        }

        coVerify(exactly = 0) { mockDocumentDao.insertAllDocuments(any()) }
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN 3: REFRESH THẤT BẠI (LỖI HTTP KHÁC)
    // -----------------------------------------------------------------
    @Test
    fun `refreshDocuments_httpFailure_throwsApiError`() = runTest {
        val errorCode = 404
        val responseBody = "".toResponseBody()

        coEvery { mockApiService.getAllDocuments() } throws HttpException(
            Response.error<List<DocumentDto>>(errorCode, responseBody)
        )

        val exception = assertFailsWith<DataFailureException.ApiError> {
            repository.refreshDocuments()
        }

        assert(exception.code == errorCode)
        coVerify(exactly = 0) { mockDocumentDao.insertAllDocuments(any()) }
    }
}