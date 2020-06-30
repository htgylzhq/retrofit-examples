package space.lzhq

import com.google.gson.*
import okhttp3.mockwebserver.*
import retrofit2.*
import retrofit2.converter.gson.*
import retrofit2.converter.moshi.*
import retrofit2.http.*
import kotlin.test.*

data class Library(
        val name: String
)

interface Service {

    @GET("/")
    @Moshi
    fun exampleMoshi(): Call<Library>

    @GET("/")
    @Gson
    fun exampleGson(): Call<Library>

    @GET("/")
    fun exampleDefault(): Call<Library>

}

class AnnotatedConverterFactoryTests {

    private val server = MockWebServer()
    private lateinit var service: Service

    @BeforeTest
    fun setup() {
        server.start()

        val mishiConverterFactory = MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().build())
        val gsonConverterFactory = GsonConverterFactory.create(GsonBuilder().create())

        val retrofit = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(
                        AnnotatedConverterFactory.Builder()
                                .add(Moshi::class.java, mishiConverterFactory)
                                .add(Gson::class.java, gsonConverterFactory)
                                .build()
                )
                .addConverterFactory(gsonConverterFactory)
                .build()
        service = retrofit.create(Service::class.java)
    }

    @Test
    fun testMoshi() {
        server.enqueue(MockResponse().setBody("""{"name": "Moshi"}"""))
        val library = service.exampleMoshi().execute().body()
        println(library)
    }

    @Test
    fun testGson() {
        server.enqueue(MockResponse().setBody("""{"name": "Gson"}"""))
        val library = service.exampleGson().execute().body()
        println(library)
    }

    @Test
    fun testDefault() {
        server.enqueue(MockResponse().setBody("""{"name": "Gson"}"""))
        val library = service.exampleDefault().execute().body()
        println(library)
    }

    @AfterTest
    fun teardown() {
        server.shutdown()
    }

}