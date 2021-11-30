package top.learningman.hystime.sdk

import TargetCreateMutation
import TargetDeleteMutation
import TargetQuery
import TargetTimePiecesQuery
import TargetUpdateMutation
import TestQuery
import TimePieceCreateMutation
import TimePieceDeleteMutation
import TimePieceUpdateMutation
import TimePiecesCreateForTargetMutation
import UserCreateMutation
import UserInfoQuery
import UserTargetsQuery
import UserTimePiecesQuery
import UserUpdateMutation
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloNetworkException
import okhttp3.OkHttpClient
import type.*
import java.util.*
import java.util.concurrent.TimeUnit

class HystimeClient(endpoint: String, authCode: String) {
    enum class Status {
        OK,
        CLIENT_ERROR,
        NETWORK_ERROR,
        PENDING
    }

    private var status: Status = Status.PENDING

    private lateinit var client: ApolloClient

    init {
        if (!endpoint.isUrl()) {
            this.status = Status.CLIENT_ERROR
        }
        if (authCode.length != 8) {
            this.status = Status.CLIENT_ERROR
        }

        if (this.status != Status.CLIENT_ERROR) {
            try {
                client = ApolloClient.builder()
                    .serverUrl(endpoint)
                    .okHttpClient(
                        OkHttpClient.Builder()
                            .addInterceptor { chain ->
                                val request = chain.request().newBuilder()
                                    .addHeader("Auth", authCode)
                                    .build()
                                chain.proceed(request)
                            }.callTimeout(1000, TimeUnit.MILLISECONDS)
                            .build()
                    )
                    .addCustomTypeAdapter(CustomType.DATETIME, ScalarAdapter.DateTimeAdapter)
                    .build()
            } catch (e: Exception) {
                this.status = Status.CLIENT_ERROR
                Log.e("ClientInit", e.errorString())
            }
            status = Status.OK
        }

        instance = this
    }

    suspend fun checkValid(): Boolean {
        return try {
            if (status == Status.CLIENT_ERROR) return false
            val test = client.query(TestQuery()).await()
            if (test.data?.test == true) {
                status = Status.OK
                true
            } else {
                status = Status.NETWORK_ERROR
                false
            }
        } catch (e: ApolloNetworkException) {
            Log.e("HystimeClient", e.errorString())
            false
        }
    }

    private fun isValid() = status == Status.OK

    suspend fun getUserInfo(username: String): UserInfoQuery.User? {
        if (!isValid()) return null
        try {
            val resp = client.query(UserInfoQuery(username)).await()
            resp.data?.let {
                return it.user
            }
            Log.e("getUserInfo", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserInfo", e.errorString())
        }
        return null
    }

    suspend fun getUserTargets(username: String): List<UserTargetsQuery.Target>? {
        if (!isValid()) return null
        try {
            val resp = client.query(UserTargetsQuery(username)).await()
            resp.data?.user?.let {
                return it.targets
            }
            Log.e("getUserTargets", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserTargets", e.errorString())
        }
        return null
    }

    suspend fun getTarget(username: String): TargetQuery.Target? {
        if (!isValid()) return null
        try {
            val resp = client.query(TargetQuery(username)).await()
            resp.data?.let {
                return it.target
            }
            Log.e("getTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getTarget", e.errorString())
        }
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getTargetTimePieces(
        targetID: String,
        first: Int,
        after: Input<String>
    ): TargetTimePiecesQuery.TimePieces? {
        if (!isValid()) return null
        try {
            val resp = client.query(TargetTimePiecesQuery(targetID, first, after)).await()
            resp.data?.target?.let {
                return it.timePieces
            }
            Log.e("getTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getTimePiece", e.errorString())
        }
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getUserTimePieces(
        userID: String,
        first: Int,
        after: Input<String>
    ): UserTimePiecesQuery.Timepieces? {
        if (!isValid()) return null
        try {
            val resp = client.query(UserTimePiecesQuery(userID, first, after)).await()
            resp.data?.let {
                return it.timepieces
            }
            Log.e("getUserTimePieces", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserTimePieces", e.errorString())
        }
        return null
    }

    suspend fun getLastWeekUserTimePieces(
        userID: String
    ): List<UserTimePiecesQuery.Node> {
        if (!isValid()) return emptyList()
        val pieces = mutableListOf<UserTimePiecesQuery.Node>()
        var cursor: Input<String> = Input.absent()
        while (true) {
            getUserTimePieces(userID, 30, cursor)?.let { timePieces ->
                if (timePieces.totalCount == 0) {
                    return pieces.toList()
                }
                for (nd in timePieces.edges) {
                    nd.node.let {
                        if (it.start.inPastWeek()) {
                            pieces.add(nd.node)
                        } else {
                            return pieces.toList()
                        }
                    }
                    if (!timePieces.pageInfo.hasNextPage) {
                        return pieces.toList()
                    }
                    cursor = Input.fromNullable(timePieces.pageInfo.endCursor)
                }
            } ?: return pieces.toList()
        }
    }

    suspend fun getLastWeekTargetTimePieces(
        targetID: String
    ): List<TargetTimePiecesQuery.Node> {
        if (!isValid()) return emptyList()
        val pieces = mutableListOf<TargetTimePiecesQuery.Node>()
        var cursor: Input<String> = Input.absent()
        while (true) {
            getTargetTimePieces(targetID, 30, cursor)?.let { timePieces ->
                if (timePieces.totalCount == 0) {
                    return pieces.toList()
                }
                for (nd in timePieces.edges) {
                    nd.node.let {
                        if (it.start.inPastWeek()) {
                            pieces.add(nd.node)
                        } else {
                            return pieces.toList()
                        }
                    }
                }
                if (!timePieces.pageInfo.hasNextPage) {
                    return pieces.toList()
                }
                cursor = Input.fromNullable(timePieces.pageInfo.endCursor)
            } ?: return pieces.toList()
        }
    }

    suspend fun createUser(username: String): UserCreateMutation.UserCreate? {
        if (!isValid()) return null
        try {
            val input = UserCreateInput(username)
            val resp = client.mutate(UserCreateMutation(input)).await()
            resp.data?.let {
                return it.userCreate
            }
            Log.e("createUser", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createUser", e.errorString())
        }

        return null
    }

    suspend fun updateUser(
        userID: String,
        username: Input<String>
    ): UserUpdateMutation.UserUpdate? {
        if (!isValid()) return null
        try {
            val input = UserUpdateInput(username)
            val resp = client.mutate(UserUpdateMutation(userID, input)).await()
            resp.data?.let {
                return it.userUpdate
            }
            Log.e("updateUser", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateUser", e.errorString())
        }
        return null
    }

    suspend fun createTarget(
        userID: String,
        name: String,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetCreateMutation.TargetCreate? {
        if (!isValid()) return null
        try {
            val input = TargetCreateInput(name, timeSpent, type)
            val resp = client.mutate(TargetCreateMutation(userID, input)).await()
            resp.data?.let {
                return it.targetCreate
            }
            Log.e("createTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTarget", e.errorString())
        }
        return null
    }

    suspend fun updateTarget(
        targetID: String,
        name: Input<String>,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetUpdateMutation.TargetUpdate? {
        if (!isValid()) return null
        try {
            val input = TargetUpdateInput(name, timeSpent, type)
            val resp = client.mutate(TargetUpdateMutation(targetID, input)).await()
            resp.data?.let {
                return it.targetUpdate
            }
            Log.e("updateTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateTarget", e.errorString())
        }
        return null
    }

    suspend fun deleteTarget(
        targetID: String
    ): Boolean {
        if (!isValid()) return false
        try {
            val resp = client.mutate(TargetDeleteMutation(targetID)).await()
            resp.data?.let {
                return it.targetDelete
            }
            Log.e("deleteTarget", resp.errors.toString())
            return false
        } catch (e: ApolloNetworkException) {
            Log.e("deleteTarget", e.errorString())
        }
        return false
    }

    suspend fun createTimePiece(
        targetID: String,
        start: Date,
        duration: Int,
        type: Input<TimePieceType>
    ): TimePieceCreateMutation.TimePieceCreate? {
        if (!isValid()) return null
        try {
            val input = TimePieceCreateInput(start, duration, type)
            val resp = client.mutate(TimePieceCreateMutation(targetID, input)).await()
            resp.data?.let {
                return it.timePieceCreate
            }
            Log.e("createTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTimePiece", e.errorString())
        }
        return null
    }

    suspend fun updateTimePiece(
        timePieceID: Int,
        start: Input<Date>,
        duration: Input<Int>,
        type: Input<TimePieceType>
    ): TimePieceUpdateMutation.TimePieceUpdate? {
        if (!isValid()) return null
        try {
            val input = TimePieceUpdateInput(start, duration, type)
            val resp = client.mutate(TimePieceUpdateMutation(timePieceID, input)).await()
            resp.data?.let {
                return it.timePieceUpdate
            }
            Log.e("updateTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateTimePiece", e.errorString())
        }
        return null
    }

    suspend fun deleteTimePiece(
        timePieceID: Int
    ): Boolean {
        if (!isValid()) return false
        try {
            val resp = client.mutate(TimePieceDeleteMutation(timePieceID)).await()
            resp.data?.let {
                return it.timePieceDelete
            }
            Log.e("deleteTimePiece", resp.errors.toString())
            return false
        } catch (e: ApolloNetworkException) {
            Log.e("deleteTimePiece", e.errorString())
        }
        return false
    }

    suspend fun createTimePiecesForTarget(
        targetID: String,
        inputs: List<TimePieceCreateInput>
    ): List<TimePiecesCreateForTargetMutation.TimePiecesCreateForTarget>? {
        if (!isValid()) return null
        try {
            val resp = client.mutate(TimePiecesCreateForTargetMutation(targetID, inputs)).await()
            resp.data?.let {
                return it.timePiecesCreateForTarget
            }
            Log.e("createTimePiecesForTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTimePiecesForTarget", e.errorString())
        }
        return null
    }

    companion object {
        private var instance: HystimeClient? = null
        fun getInstance(): HystimeClient {
            if (instance == null) {
                instance = getErrorClient() // Error Client, prevent nullptr
            }
            return instance!!
        }

        private fun getErrorClient():HystimeClient = HystimeClient("", "")
    }
}


// Check if url is valid and safe
fun String.isUrl(): Boolean {
    val regex = Regex("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    return regex.matches(this)
}

// Check if date in the past week
fun Date.inPastWeek(): Boolean {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.add(Calendar.DAY_OF_YEAR, 7)
    return cal.time.after(Date())
}

fun Exception.errorString(): String {
    return if (BuildConfig.DEBUG) {
        this.stackTraceToString()
    } else {
        this.localizedMessage ?: "Unknown error"
    }
}