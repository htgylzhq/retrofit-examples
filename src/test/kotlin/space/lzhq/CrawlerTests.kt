package space.lzhq

import kotlinx.coroutines.delay
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class CrawlerTests {

    @Test
    fun testCrawler() {
        val okHttpClient = OkHttpClient.Builder()
                .dispatcher(Dispatcher(Executors.newFixedThreadPool(20)).apply {
                    this.maxRequests = 20
                    this.maxRequestsPerHost = 1
                })
                .connectionPool(ConnectionPool(100, 30, TimeUnit.SECONDS))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(HttpUrl.get("https://www.oschina.net/"))
                .addConverterFactory(PageConverter.FACTORY)
                .client(okHttpClient)
                .build()
        val pageService = retrofit.create(PageService::class.java)
        val crawler = Crawler(pageService)
        crawler.crawlPage(HttpUrl.get("https://www.oschina.net"))

        TimeUnit.SECONDS.sleep(10)
    }

}