package com.example.stushare.core.data.repository

import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.network.models.DocumentDto
import com.example.stushare.core.data.network.models.toDocumentEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class DocumentRepositoryImplTest {

    private val mockDocumentDao: DocumentDao = mockk(relaxed = true)
    private val mockApiService: ApiService = mockk(relaxed = true)
    private val mockSettingsRepository: SettingsRepository = mockk(relaxed = true)

    private lateinit var repository: DocumentRepositoryImpl

    private val fakeDocumentDtoList = listOf(
        DocumentDto("1", "Mạng", "Sách", "http://fake.com/img1.jpg", 10, 4.5f, "User A", "CS101"),
        DocumentDto("2", "Cơ sở dữ liệu", "Tài Liệu", "http://fake.com/img2.jpg", 5, 4.0f, "User B", "CS201")
    )
    private val expectedEntityList = fakeDocumentDtoList.map { it.toDocumentEntity() }

    @Before
    fun setup() {
        repository = DocumentRepositoryImpl(mockDocumentDao, mockApiService, mockSettingsRepository)
    }

    @Test
    fun `refreshDocuments_success_callsApiAndInsertsIntoDb`() = runTest {
        // GIVEN
        coEvery { mockApiService.getAllDocuments() } returns fakeDocumentDtoList
        // Mock hành vi cho các hàm suspend
        coEvery { mockDocumentDao.deleteAllDocuments() } returns Unit
        coEvery { mockDocumentDao.insertAllDocuments(any()) } returns Unit
        coEvery { mockSettingsRepository.updateLastRefreshTimestamp() } returns Unit

        // WHEN
        repository.refreshDocuments()

        // THEN
        coVerify(exactly = 1) { mockApiService.getAllDocuments() }
        coVerify(exactly = 1) { mockDocumentDao.deleteAllDocuments() }
        coVerify(exactly = 1) { mockDocumentDao.insertAllDocuments(expectedEntityList) }
        coVerify(exactly = 1) { mockSettingsRepository.updateLastRefreshTimestamp() }
    }

    @Test
    fun `refreshDocumentsIfStale - KHÔNG gọi API nếu dữ liệu còn mới`() = runTest {
        val currentTime = System.currentTimeMillis()
        val lastRefreshTime = currentTime - (5 * 60 * 1000)
        every { mockSettingsRepository.lastRefreshTimestamp } returns flowOf(lastRefreshTime)

        repository.refreshDocumentsIfStale()

        coVerify(exactly = 0) { mockApiService.getAllDocuments() }
    }

    @Test
    fun `refreshDocumentsIfStale - GỌI API nếu dữ liệu đã cũ`() = runTest {
        val currentTime = System.currentTimeMillis()
        val lastRefreshTime = currentTime - (20 * 60 * 1000)
        every { mockSettingsRepository.lastRefreshTimestamp } returns flowOf(lastRefreshTime)
        coEvery { mockApiService.getAllDocuments() } returns emptyList()

        coEvery { mockDocumentDao.deleteAllDocuments() } returns Unit
        coEvery { mockDocumentDao.insertAllDocuments(any()) } returns Unit
        coEvery { mockSettingsRepository.updateLastRefreshTimestamp() } returns Unit

        repository.refreshDocumentsIfStale()

        coVerify(exactly = 1) { mockApiService.getAllDocuments() }
    }

    @Test
    fun `refreshDocumentsIfStale - GỌI API nếu chưa tải lần nào`() = runTest {
        every { mockSettingsRepository.lastRefreshTimestamp } returns flowOf(0L)
        coEvery { mockApiService.getAllDocuments() } returns emptyList()

        coEvery { mockDocumentDao.deleteAllDocuments() } returns Unit
        coEvery { mockDocumentDao.insertAllDocuments(any()) } returns Unit
        coEvery { mockSettingsRepository.updateLastRefreshTimestamp() } returns Unit

        repository.refreshDocumentsIfStale()

        coVerify(exactly = 1) { mockApiService.getAllDocuments() }
    }

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
        coEvery { mockApiService.getAllDocuments() } throws HttpException(Response.error<List<DocumentDto>>(errorCode, responseBody))

        val exception = assertFailsWith<DataFailureException.ApiError> {
            repository.refreshDocuments()
        }
        assertEquals(errorCode, exception.code)
        coVerify(exactly = 0) { mockDocumentDao.insertAllDocuments(any()) }
    }
}