package space.lzhq

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.Body
import java.lang.reflect.Type

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Chunked

class ChunkingConverterFactory : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        var isBody = false
        var isChunked = false
        parameterAnnotations.forEach {
            isBody = isBody || it is Body
            isChunked = isChunked || it is Chunked
        }
        if (!isBody || !isChunked) {
            return null
        }

        val delegate = retrofit.nextRequestBodyConverter<Any>(this, type, parameterAnnotations, methodAnnotations)
        return Converter<Any, RequestBody> {
            val realBody = delegate.convert(it)
            object : RequestBody() {
                override fun contentType(): MediaType? {
                    return realBody?.contentType()
                }

                override fun writeTo(sink: BufferedSink) {
                    realBody?.writeTo(sink)
                }
            }
        }
    }
}