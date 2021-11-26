package top.learningman.hystime.sdk

import TargetQuery
import TargetTimePiecesQuery
import UserInfoQuery
import UserTargetsQuery
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import okhttp3.OkHttpClient
import type.CustomType
import java.util.*

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
        )
        .addCustomTypeAdapter(CustomType.DATETIME, ScalarAdapter.dateTimeAdapter)
        .build()

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
    ): TargetTimePiecesQuery.TimePieces? {
        val resp = client.query(TargetTimePiecesQuery(targetId, first, after)).await()
        resp.data?.target?.let {
            return it.timePieces
        }
        Log.e("getTimePiece", resp.errors.toString())
        return null
    }

    suspend fun getLastWeekTargetTimePieces(
        targetId: String
    ): List<TargetTimePiecesQuery.Node> {
        val pieces = mutableListOf<TargetTimePiecesQuery.Node>()
        while (true) {
            val resp = client.query(TargetTimePiecesQuery(targetId, 30)).await()
            resp.data?.target?.timePieces?.let { timePieces ->
                if (timePieces.totalCount == 0) {
                    return pieces.toList();
                }
                for (nd in timePieces.edges) {
                    nd.node.let {
                        if (it.start.inAWeek()) {
                            pieces.add(nd.node)
                        } else {
                            return pieces.toList()
                        }
                    }
                }
                if (!timePieces.pageInfo.hasNextPage){
                    return pieces.toList()
                }
            }
        }
    }
}

// Check if url is valid and safe
fun String.isUrl(): Boolean {
    val regex = Regex("^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    return regex.matches(this)
}

// Check if date in the past week
fun Date.inAWeek(): Boolean {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DAY_OF_YEAR, 7)
    return cal.time.after(Date())
}

