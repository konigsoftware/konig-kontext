package com.konigsoftware.kontext

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.logging.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal class ExampleApiClient(private val baseUrl: String) {
    private val httpClient = OkHttpClient.Builder().dispatcher(Dispatcher().let {
        it.maxRequestsPerHost = 10
        it
    }).build()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val logger = Logger.getLogger("com.konigsoftware.kontext.ExampleApiClient")

    data class BalanceResponse(val balance: String)

    suspend fun getBalance(userId: String, customerId: String): BalanceResponse? {
        val request =
            Request.Builder().url("$baseUrl/$userId/balance").get().addHeader("Customer-Id", customerId).build()

        httpClient.newCall(request).executeSuspending().use {
            val responseBody = it.body?.string() ?: ""

            if (!it.isSuccessful) {
                logger.severe("Call to $baseUrl/$userId/balance was unsuccessful. Error code: ${it.code}")
                return null
            }

            return parseJson(moshi.adapter(BalanceResponse::class.java), responseBody)
                ?: run {
                    logger.severe("Unable to parse BalanceResponse from response body: $responseBody")
                    null
                }
        }
    }

    private suspend fun Call.executeSuspending(): Response {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
    }

    private fun <T> parseJson(adapter: JsonAdapter<T>, json: String): T? = adapter.fromJson(json)

    inline fun <reified T> T.toJson(): String {
        val adapter = moshi.adapter(T::class.java)
        return adapter.toJson(this)
    }
}
