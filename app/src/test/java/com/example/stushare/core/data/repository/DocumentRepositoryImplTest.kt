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

@ExperimentalCoroutinesApi
class DocumentRepositoryImplTest {

    private val mockDocumentDao: DocumentDao = mockk(relaxed = true)
    private val mockApiService: ApiService = mockk(relaxed = true)
    // ⭐️ THAY ĐỔI 1: Thêm mock cho SettingsRepository
    private val mockSettingsRepository: SettingsRepository = mockk(relaxed = true)

    private lateinit var repository: DocumentRepositoryImpl

    // DATA GIẢ LẬP
    private val fakeDocumentDtoList = listOf(
        DocumentDto(
            id = "1",
            title = "Mạng",
            author = "User A",
            courseCode = "CS101",
            downloads = 10,
            rating = 4.5f,
            type = "Sách",
            imageUrl = "http://fake.com/img1.jpg"
        ),
        DocumentDto(
            id = "2",
            title = "Cơ sở dữ liệu",
            author = "User B",
            courseCode = "CS201",
            downloads = 5,
            rating = 4.0f,
            type = "Tài Liệu",
            imageUrl = "http://fake.com/img2.jpg"
        )
    )

    private val expectedEntityList = fakeDocumentDtoList.map {
        it.toDocumentEntity()
    }

    @Before
    fun setup() {
        // ⭐️ THAY ĐỔI 2: Truyền mockSettingsRepository vào Constructor
        repository = DocumentRepositoryImpl(mockDocumentDao, mockApiService, mockSettingsRepository)
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN 1: REFRESH DỮ LIỆU THÀNH CÔNG
    // -----------------------------------------------------------------
    @Test
    fun `refreshDocuments_success_callsApiAndInsertsIntoDb`() = runTest {
        coEvery { mockApiService.getAllDocuments() } returns fakeDocumentDtoList

        repository.refreshDocuments()

        coVerify(exactly = 1) { mockApiService.getAllDocuments() }
        // ⭐️ THAY ĐỔI 3 (Quan trọng): Kiểm tra xem hàm deleteAll có được gọi không (vì logic mới có xóa trước khi thêm)
        coVerify(exactly = 1) { mockDocumentDao.deleteAllDocuments() }
        coVerify(exactly = 1) { mockDocumentDao.insertAllDocuments(expectedEntityList) }

        // ⭐️ THAY ĐỔI 4: Kiểm tra xem timestamp có được cập nhật không
        coVerify(exactly = 1) { mockSettingsRepository.updateLastRefreshTimestamp() }
    }

    // (Các test case còn lại giữ nguyên, không cần sửa gì thêm)
    // ...
    @Test
    fun `refreshDocuments_networkFailure_throwsNetworkError`() = runTest {
        coEvery { mockApiService.getAllDocuments() } throws IOException()

        assertFailsWith<DataFailureException.NetworkError> {
            repository.refreshDocuments()
        }

        coVerify(exactly = 0) { mockDocumentDao.insertAllDocuments(any()) }
    }

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