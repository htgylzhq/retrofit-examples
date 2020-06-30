package space.lzhq

import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Contributor(
        val login: String,
        val contributions: Int
)

interface GitHub {

    @GET("/repos/{owner}/{repo}/contributors")
    fun contributors(@Path("owner") owner: String, @Path("repo") repo: String): Call<List<Contributor>>

}
