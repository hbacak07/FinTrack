package com.hbacakk.fintrack.core.network.api

import com.hbacakk.fintrack.core.network.model.LoginRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@DisplayName("AuthApi")
class AuthApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApi: AuthApi

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        authApi = retrofit.create(AuthApi::class.java)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    @DisplayName("login başarılı response'u doğru parse eder")
    fun `login parses successful response correctly`() = runTest {
        val jsonResponse = """
            {
                "token": "fake-token",
                "user": {
                    "id": "user-123",
                    "email": "test@example.com",
                    "fullName": "Test User"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
        )

        val result = authApi.login(LoginRequest("test@example.com", "password123"))

        assertEquals("fake-token", result.token)
        assertEquals("test@example.com", result.user.email)
    }

    @Test
    @DisplayName("login isteği doğru endpoint'e ve body'le gider")
    fun `login sends request to correct endpoint`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"token":"t","user":{"id":"1","email":"a@b.com","fullName":"A"}}"""
                )
        )

        authApi.login(LoginRequest("test@example.com", "password123"))

        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("/auth/login", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertEquals("true", recordedRequest.getHeader("No-Auth"))
    }

    @Test
    @DisplayName("401 dönerse HttpException fırlatılır")
    fun `login throws HttpException on 401`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(401)
        )

        try {
            authApi.login(LoginRequest("test@example.com", "wrong-password"))
            assert(false) { "Exception fırlatılmalıydı" }
        } catch (e: retrofit2.HttpException) {
            assertEquals(401, e.code())
        }
    }
}