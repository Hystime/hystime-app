package top.learningman.hystime.sdk

import com.apollographql.apollo.ApolloClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class HystimeClient(endpoint: String, authCode: String) {
    private val authCode: String
    private val endpoint: String

    private val client = ApolloClient.builder()
        .serverUrl(endpoint)
        .okHttpClient(
            OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor())
                .build()
        ).build()

    init {
        this.endpoint = endpoint
        this.authCode = authCode
    }

    inner class AuthorizationInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("Auth", this@HystimeClient.authCode)
                .build()
            return chain.proceed(request)
        }
    }
}
