package space.lzhq

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.test.*


class ChunkingConverterTests {

    data class Repo(
        val owner: String,
        val name: String
    )

    interface Service {
        @POST("/")
        fun sendNormal(@Body repo: Repo): Call<ResponseBody>

        @POST("/")
        fun sendChunked(@Body @Chunked repo: Repo): Call<ResponseBody>
    }

    private val server = MockWebServer()
    private lateinit var service: Service

    @BeforeTest
    fun setup() {
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ChunkingConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(Service::class.java)
    }

    @Test
    fun testSendNormal() {
        server.enqueue(MockResponse())

        val repo = Repo("square", "retrofit")
        service.sendNormal(repo).execute()
        val request = server.takeRequest()
        println("Normal @Body Transfer-Encoding: ${request.getHeader("Transfer-Encoding")}")
    }

    @Test
    fun testSendChunked() {
        server.enqueue(MockResponse())

        val repo = Repo("square", "retrofit")
        service.sendChunked(repo).execute()
        val request = server.takeRequest()
        println("@Chunked @Body Transfer-Encoding: ${request.getHeader("Transfer-Encoding")}")
    }

    @AfterTest
    fun teardown() {
        server.shutdown()
    }
}