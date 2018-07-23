package com.fintech.lxf.net


import com.fintech.lxf.helper.DEBUG
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object Net {

    @Volatile
    var okHttpClient: OkHttpClient? = null

    private fun okHttpClient(): OkHttpClient? {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()

            builder.connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)

            if (DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            }

            okHttpClient = builder.build()
        }

        return okHttpClient
    }

    private fun restAdapter(): Retrofit {
        val builder = Retrofit.Builder()
        builder.client(okHttpClient()!!)
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
        return builder.build()
    }

    fun <T> create(service: Class<T>): T {
        return restAdapter().create(service)
    }
}
