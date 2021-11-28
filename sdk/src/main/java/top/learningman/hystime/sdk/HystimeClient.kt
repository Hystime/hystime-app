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
import okhttp3.OkHttpClient
import type.*
import java.util.*

class HystimeClient(endpoint: String, authCode: String) {
    enum class Status {
        OK,
        ERROR,
        PENDING
    }

    private val authCode: String
    private val endpoint: String

    private var status: Status = Status.PENDING

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
        .addCustomTypeAdapter(CustomType.DATETIME, ScalarAdapter.DateTimeAdapter)
        .build()

    init {
        if (!endpoint.isUrl()) {
            this.status = Status.ERROR
        }
        if (authCode.length != 32) {
            this.status = Status.ERROR
        }

        this.endpoint = endpoint
        this.authCode = authCode
    }

    suspend fun isValid(): Boolean {
        return if (status == Status.PENDING) {
            val test = client.query(TestQuery()).await()
            if (test.data?.test == true) {
                status = Status.OK
                true
            } else {
                status = Status.ERROR
                false
            }
        } else {
            status == Status.OK
        }
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

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getTargetTimePieces(
        targetID: String,
        first: Int,
        after: Input<String>
    ): TargetTimePiecesQuery.TimePieces? {
        val resp = client.query(TargetTimePiecesQuery(targetID, first, after)).await()
        resp.data?.target?.let {
            return it.timePieces
        }
        Log.e("getTimePiece", resp.errors.toString())
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getUserTimePieces(
        userID: String,
        first: Int,
        after: Input<String>
    ): UserTimePiecesQuery.Timepieces? {
        val resp = client.query(UserTimePiecesQuery(userID, first, after)).await()
        resp.data?.let {
            return it.timepieces
        }
        Log.e("getUserTimePieces", resp.errors.toString())
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
        val input = UserCreateInput(username)
        val resp = client.mutate(UserCreateMutation(input)).await()
        resp.data?.let {
            return it.userCreate
        }
        Log.e("createUser", resp.errors.toString())
        return null
    }

    suspend fun updateUser(
        userID: String,
        username: Input<String>
    ): UserUpdateMutation.UserUpdate? {
        val input = UserUpdateInput(username)
        val resp = client.mutate(UserUpdateMutation(userID, input)).await()
        resp.data?.let {
            return it.userUpdate
        }
        Log.e("updateUser", resp.errors.toString())
        return null
    }

    suspend fun createTarget(
        userID: String,
        name: String,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetCreateMutation.TargetCreate? {
        val input = TargetCreateInput(name, timeSpent, type)
        val resp = client.mutate(TargetCreateMutation(userID, input)).await()
        resp.data?.let {
            return it.targetCreate
        }
        Log.e("createTarget", resp.errors.toString())
        return null
    }

    suspend fun updateTarget(
        targetID: String,
        name: Input<String>,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ): TargetUpdateMutation.TargetUpdate? {
        val input = TargetUpdateInput(name, timeSpent, type)
        val resp = client.mutate(TargetUpdateMutation(targetID, input)).await()
        resp.data?.let {
            return it.targetUpdate
        }
        Log.e("updateTarget", resp.errors.toString())
        return null
    }

    suspend fun deleteTarget(
        targetID: String
    ): Boolean {
        val resp = client.mutate(TargetDeleteMutation(targetID)).await()
        resp.data?.let {
            return it.targetDelete
        }
        Log.e("deleteTarget", resp.errors.toString())
        return false
    }

    suspend fun createTimePiece(
        targetID: String,
        start: Date,
        duration: Int,
        type: Input<TimePieceType>
    ): TimePieceCreateMutation.TimePieceCreate? {
        val input = TimePieceCreateInput(start, duration, type)
        val resp = client.mutate(TimePieceCreateMutation(targetID, input)).await()
        resp.data?.let {
            return it.timePieceCreate
        }
        Log.e("createTimePiece", resp.errors.toString())
        return null
    }

    suspend fun updateTimePiece(
        timePieceID: Int,
        start: Input<Date>,
        duration: Input<Int>,
        type: Input<TimePieceType>
    ): TimePieceUpdateMutation.TimePieceUpdate? {
        val input = TimePieceUpdateInput(start, duration, type)
        val resp = client.mutate(TimePieceUpdateMutation(timePieceID, input)).await()
        resp.data?.let {
            return it.timePieceUpdate
        }
        Log.e("updateTimePiece", resp.errors.toString())
        return null
    }

    suspend fun deleteTimePiece(
        timePieceID: Int
    ): Boolean {
        val resp = client.mutate(TimePieceDeleteMutation(timePieceID)).await()
        resp.data?.let {
            return it.timePieceDelete
        }
        Log.e("deleteTimePiece", resp.errors.toString())
        return false
    }

    suspend fun createTimePiecesForTarget(
        targetID: String,
        inputs: List<TimePieceCreateInput>
    ): List<TimePiecesCreateForTargetMutation.TimePiecesCreateForTarget>? {
        val resp = client.mutate(TimePiecesCreateForTargetMutation(targetID, inputs)).await()
        resp.data?.let {
            return it.timePiecesCreateForTarget
        }
        Log.e("createTimePiecesForTarget", resp.errors.toString())
        return null
    }

}

// Check if url is valid and safe
fun String.isUrl(): Boolean {
    val regex = Regex("^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
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

