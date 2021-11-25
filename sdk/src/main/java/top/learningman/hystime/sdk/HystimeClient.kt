package top.learningman.hystime.sdk

import TargetQuery
import TimePiecesQuery
import UserInfoQuery
import UserTargetsQuery
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
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
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Auth", authCode)
                        .build()
                    chain.proceed(request)
                }
                .build()
        ).build()

    init {
        if (!endpoint.isUrl()) {
            throw IllegalArgumentException("endpoint is not a valid url")
        }
        if (authCode.length != 32) {
            throw IllegalArgumentException("authCode length must be 32")
        }

        this.endpoint = endpoint
        this.authCode = authCode
    }

    suspend fun getUserInfo(username: String): UserInfoQuery.User? {
        val resp = client.query(UserInfoQuery(username)).await()
        resp.data?.let {
            return it.user
        }
        Log.e("getUserInfo", resp.errors.toString())
        return null
    }

    suspend fun getUserTargets(username: String): List<UserTargetsQuery.Target>? {
        val resp = client.query(UserTargetsQuery(username)).await()
        resp.data?.user?.let {
            return it.targets
        }
        Log.e("getUserTargets", resp.errors.toString())
        return null
    }

    suspend fun getTarget(username: String): TargetQuery.Target? {
        val resp = client.query(TargetQuery(username)).await()
        resp.data?.let {
            return it.target
        }
        Log.e("getTarget", resp.errors.toString())
        return null
    }

    suspend fun getTargetTimePieces(
        targetId: String,
        first: Int,
        after: Input<String>
    ): TimePiecesQuery.TimePieces? {
        val resp = client.query(TimePiecesQuery(targetId, first, after)).await()
        resp.data?.target?.let {
            return it.timePieces
        }
        Log.e("getTimePiece", resp.errors.toString())
        return null
    }

    suspend fun getLastWeekTargetTimePieces(
        targetId: String
    ):TimePiecesQuery.Node? {
        val resp = client.query(TimePiecesQuery(targetId, 30)).await()

        Log.e("getLastWeekTargetTimePieces", resp.errors.toString())
        return null
    }
}

// Check if url is valid and safe
fun String.isUrl(): Boolean {
    val regex = Regex("^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    return regex.matches(this)
}

