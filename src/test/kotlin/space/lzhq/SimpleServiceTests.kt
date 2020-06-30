package space.lzhq

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.Test

class SimpleServiceTests {

    @Test
    fun testSimpleService() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val gitHub = retrofit.create(GitHub::class.java)

        val call = gitHub.contributors("square", "retrofit")

        val contributors = call.execute().body()
        contributors?.forEach {
            println("${it.login} (${it.contributions})")
        }
    }

}