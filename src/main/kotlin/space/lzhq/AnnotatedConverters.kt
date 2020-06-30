package space.lzhq

import okhttp3.*
import retrofit2.*
import java.lang.reflect.*


class AnnotatedConverterFactory private constructor(private val factories: LinkedHashMap<Class<out Annotation>, Converter.Factory>) : Converter.Factory() {

    fun add(cls: Class<out Annotation>, factory: Converter.Factory): AnnotatedConverterFactory {
        factories[cls] = factory
        return this
    }

    class Builder {

        private val factories: LinkedHashMap<Class<out Annotation>, Converter.Factory> = LinkedHashMap()

        fun add(cls: Class<out Annotation>, factory: Converter.Factory): Builder {
            factories[cls] = factory
            return this
        }

        fun build(): AnnotatedConverterFactory = AnnotatedConverterFactory(factories)

    }

    override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<Annotation>,
            methodAnnotations: Array<Annotation>,
            retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        parameterAnnotations.forEach {
            val factory: Converter.Factory? = factories[it.annotationClass.java]
            if (factory != null) {
                return factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            }
        }
        return null
    }

    override fun responseBodyConverter(
            type: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        annotations.forEach {
            val factory = factories[it.annotationClass.java]
            if (factory != null) {
                return factory.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return null
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class Moshi

@Retention(AnnotationRetention.RUNTIME)
annotation class Gson
