package space.lzhq

import okhttp3.*
import org.jsoup.*
import retrofit2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.collections.LinkedHashSet

data class Page(
        val title: String,
        val links: List<String>
)

interface PageService {
    @GET
    fun get(@Url url: HttpUrl): Call<Page>
}

class PageConverter : Converter<ResponseBody, Page> {

    companion object {
        val FACTORY = object : Converter.Factory() {
            override fun responseBodyConverter(
                    type: Type,
                    annotations: Array<Annotation>,
                    retrofit: Retrofit
            ): Converter<ResponseBody, *>? =
                    if (type == Page::class.java) {
                        PageConverter()
                    } else {
                        null
                    }
        }
    }

    override fun convert(responseBody: ResponseBody): Page? {
        val document = Jsoup.parse(responseBody.string())
        val links = document.select("a[href]").map { it.attr("href") }
        return Page(document.title(), links)
    }

}

class Crawler(private val pageService: PageService) {

    private val hosts = ConcurrentHashMap<String, AtomicInteger>()
    private val fetchedUrl = Collections.synchronizedSet(LinkedHashSet<HttpUrl>())

    fun crawlPage(url: HttpUrl) {
        var hostCount = AtomicInteger()
        val previous = hosts.putIfAbsent(url.host(), hostCount)
        if (previous != null) {
            hostCount = previous
        }
        if (hostCount.incrementAndGet() > 100) {
            return
        }

        pageService.get(url).enqueue(
                object : Callback<Page> {
                    override fun onFailure(call: Call<Page>, t: Throwable) {
                        println("${call.request().url()}: failed: $t")
                    }

                    override fun onResponse(call: Call<Page>, response: Response<Page>) {
                        if (!response.isSuccessful) {
                            println("${call.request().url()}: failed: ${response.code()}")
                            return
                        }

                        val page = response.body()
                        val base = response.raw().request().url()
                        println("$base: ${page?.title}")

                        page?.links?.forEach {
                            val linkUrl = base.resolve(it)
                            if (linkUrl != null && fetchedUrl.add(linkUrl)) {
                                crawlPage(linkUrl)
                            }
                        }
                    }
                }
        )
    }
}