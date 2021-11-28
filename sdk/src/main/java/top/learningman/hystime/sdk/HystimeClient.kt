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

class HystimeClient(endpoint: String, authCode: String) {
    enum class Status {
        OK,
        ERROR,
        PENDING
    }

    private var status: Status = Status.PENDING

    private lateinit var client: ApolloClient

    init {
        if (!endpoint.isUrl()) {
            this.status = Status.ERROR
        }
        if (authCode.length != 8) {
            this.status = Status.ERROR
        }

        if (this.status != Status.ERROR) {
            client = ApolloClient.builder()
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
                .addCustomTypeAdapter(CustomType.DATETIME, ScalarAdapter.DateTimeAdapter)
                .build()
        }
    }

    suspend fun isValid(): Boolean {
        return if (status == Status.PENDING) {
            try {
                val test = client.query(TestQuery()).await()
                if (test.data?.test == true) {
                    status = Status.OK
                    true
                } else {
                    status = Status.ERROR
                    false
                }
            } catch (e: ApolloNetworkException) {
                Log.e("HystimeClient", e.message ?: "")
                false
            }
        } else {
            status == Status.OK
        }
    }

    suspend fun getUserInfo(username: String): UserInfoQuery.User? {
        try {
            val resp = client.query(UserInfoQuery(username)).await()
            resp.data?.let {
                return it.user
            }
            Log.e("getUserInfo", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserInfo", e.message.toString())
        }
        return null
    }

    suspend fun getUserTargets(username: String): List<UserTargetsQuery.Target>? {
        try {
            val resp = client.query(UserTargetsQuery(username)).await()
            resp.data?.user?.let {
                return it.targets
            }
            Log.e("getUserTargets", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserTargets", e.message.toString())
        }
        return null
    }

    suspend fun getTarget(username: String): TargetQuery.Target? {
        try {
            val resp = client.query(TargetQuery(username)).await()
            resp.data?.let {
                return it.target
            }
            Log.e("getTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getTarget", e.message.toString())
        }
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getTargetTimePieces(
        targetID: String,
        first: Int,
        after: Input<String>
    ): TargetTimePiecesQuery.TimePieces? {
        try {
            val resp = client.query(TargetTimePiecesQuery(targetID, first, after)).await()
            resp.data?.target?.let {
                return it.timePieces
            }
            Log.e("getTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getTimePiece", e.message.toString())
        }
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getUserTimePieces(
        userID: String,
        first: Int,
        after: Input<String>
    ): UserTimePiecesQuery.Timepieces? {
        try {
            val resp = client.query(UserTimePiecesQuery(userID, first, after)).await()
            resp.data?.let {
                return it.timepieces
            }
            Log.e("getUserTimePieces", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("getUserTimePieces", e.message.toString())
        }
        return null
    }

    suspend fun getLastWeekUserTimePieces(
        userID: String
    ): List<UserTimePiecesQuery.Node> {
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
            }
        }
    }

    suspend fun getLastWeekTargetTimePieces(
        targetID: String
    ): List<TargetTimePiecesQuery.Node> {
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
        try {
            val input = UserCreateInput(username)
            val resp = client.mutate(UserCreateMutation(input)).await()
            resp.data?.let {
                return it.userCreate
            }
            Log.e("createUser", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createUser", e.message.toString())
        }

        return null
    }

    suspend fun updateUser(
        userID: String,
        username: Input<String>
    ): UserUpdateMutation.UserUpdate? {
        try {
            val input = UserUpdateInput(username)
            val resp = client.mutate(UserUpdateMutation(userID, input)).await()
            resp.data?.let {
                return it.userUpdate
            }
            Log.e("updateUser", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateUser", e.message.toString())
        }
        return null
    }

    suspend fun createTarget(
        userID: String,
        name: String,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetCreateMutation.TargetCreate? {
        try {
            val input = TargetCreateInput(name, timeSpent, type)
            val resp = client.mutate(TargetCreateMutation(userID, input)).await()
            resp.data?.let {
                return it.targetCreate
            }
            Log.e("createTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTarget", e.message.toString())
        }
        return null
    }

    suspend fun updateTarget(
        targetID: String,
        name: Input<String>,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetUpdateMutation.TargetUpdate? {
        try {
            val input = TargetUpdateInput(name, timeSpent, type)
            val resp = client.mutate(TargetUpdateMutation(targetID, input)).await()
            resp.data?.let {
                return it.targetUpdate
            }
            Log.e("updateTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateTarget", e.message.toString())
        }
        return null
    }

    suspend fun deleteTarget(
        targetID: String
    ): Boolean {
        try {
            val resp = client.mutate(TargetDeleteMutation(targetID)).await()
            resp.data?.let {
                return it.targetDelete
            }
            Log.e("deleteTarget", resp.errors.toString())
            return false
        } catch (e: ApolloNetworkException) {
            Log.e("deleteTarget", e.message.toString())
        }
        return false
    }

    suspend fun createTimePiece(
        targetID: String,
        start: Date,
        duration: Int,
        type: Input<TimePieceType>
    ): TimePieceCreateMutation.TimePieceCreate? {
        try {
            val input = TimePieceCreateInput(start, duration, type)
            val resp = client.mutate(TimePieceCreateMutation(targetID, input)).await()
            resp.data?.let {
                return it.timePieceCreate
            }
            Log.e("createTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTimePiece", e.message.toString())
        }
        return null
    }

    suspend fun updateTimePiece(
        timePieceID: Int,
        start: Input<Date>,
        duration: Input<Int>,
        type: Input<TimePieceType>
    ): TimePieceUpdateMutation.TimePieceUpdate? {
        try {
            val input = TimePieceUpdateInput(start, duration, type)
            val resp = client.mutate(TimePieceUpdateMutation(timePieceID, input)).await()
            resp.data?.let {
                return it.timePieceUpdate
            }
            Log.e("updateTimePiece", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("updateTimePiece", e.message.toString())
        }
        return null
    }

    suspend fun deleteTimePiece(
        timePieceID: Int
    ): Boolean {
        try {
            val resp = client.mutate(TimePieceDeleteMutation(timePieceID)).await()
            resp.data?.let {
                return it.timePieceDelete
            }
            Log.e("deleteTimePiece", resp.errors.toString())
            return false
        } catch (e: ApolloNetworkException) {
            Log.e("deleteTimePiece", e.message.toString())
        }
        return false
    }

    suspend fun createTimePiecesForTarget(
        targetID: String,
        inputs: List<TimePieceCreateInput>
    ): List<TimePiecesCreateForTargetMutation.TimePiecesCreateForTarget>? {
        try {
            val resp = client.mutate(TimePiecesCreateForTargetMutation(targetID, inputs)).await()
            resp.data?.let {
                return it.timePiecesCreateForTarget
            }
            Log.e("createTimePiecesForTarget", resp.errors.toString())
            return null
        } catch (e: ApolloNetworkException) {
            Log.e("createTimePiecesForTarget", e.message.toString())
        }
        return null
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

