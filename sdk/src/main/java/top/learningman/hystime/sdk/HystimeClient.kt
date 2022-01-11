package top.learningman.hystime.sdk

import TargetCreateMutation
import TargetDeleteMutation
import TargetLastWeekTimePiecesQuery
import TargetTimePiecesQuery
import TargetUpdateMutation
import TestQuery
import TimePieceCreateMutation
import TimePieceDeleteMutation
import TimePieceUpdateMutation
import TimePiecesCreateForTargetMutation
import UserCreateMutation
import UserInfoQuery
import UserLastWeekTimePiecesQuery
import UserTargetsQuery
import UserTimePiecesQuery
import UserUpdateMutation
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.coroutines.await
import okhttp3.OkHttpClient
import type.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class HystimeClient(endpoint: String, authCode: String) {
    enum class Status {
        OK,
        CLIENT_ERROR,
        NETWORK_ERROR,
        UNKNOWN_ERROR,
        PENDING
    }

    private var status: Status = Status.PENDING

    private lateinit var client: ApolloClient

    init {
        if (!endpoint.isUrl()) {
            this.status = Status.CLIENT_ERROR
        }

        if (this.status != Status.CLIENT_ERROR) {
            try {
                client = ApolloClient.builder()
                    .serverUrl(endpoint)
                    .okHttpClient(
                        OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
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
        }
        instance = this
    }


    suspend fun refreshValid(): Result<Boolean> = wrap(ignoreCheck = true) {
        val err = Error("Client Error")
        if (status == Status.CLIENT_ERROR) throw err
        val test = client.query(TestQuery()).await()
        if (test.data == null) {
            status = Status.UNKNOWN_ERROR
            throw Error("WTF is this error?")
        } else if (test.data!!.test) {
            status = Status.OK
            true
        } else {
            status = Status.UNKNOWN_ERROR
            throw Error("WTF is this error?")
            // Almost impossible to happen, but still handle it.
        }
    }

    fun isValid() = status == Status.OK

    suspend fun getUserInfo(username: String) = wrap {
        queryData(UserInfoQuery(username)).user
    }

    suspend fun getUserTargets(username: String) = wrap {
        queryData(UserTargetsQuery(username)).user?.targets
    }

    suspend fun getTargetTimePieces(
        username: String,
        targetID: String,
        first: Int,
        after: Input<String>
    ) = wrap {
        queryData(TargetTimePiecesQuery(username, targetID, first, after)).user?.target?.timePieces
    }

    suspend fun getUserTimePieces(
        userID: String,
        first: Int,
        after: Input<String>
    ) = wrap {
        queryData(UserTimePiecesQuery(userID, first, after)).user?.timePieces
    }

    suspend fun getUserLastWeekTimePieces(
        username: String
    ) = wrap {
        queryData(UserLastWeekTimePiecesQuery(username)).user?.lastWeekTimePieces
    }

    suspend fun getTargetLastWeekTimePieces(
        username: String,
        targetID: String
    ) = wrap {
        queryData(
            TargetLastWeekTimePiecesQuery(
                username,
                targetID
            )
        ).user?.target?.lastWeekTimePieces
    }

    suspend fun createUser(username: String) = wrap {
        val input = UserCreateInput(username)
        mutateData(UserCreateMutation(input)).userCreate
    }

    suspend fun updateUser(
        userID: String,
        username: Input<String>
    ) = wrap {
        val input = UserUpdateInput(username)
        mutateData(UserUpdateMutation(userID, input)).userUpdate
    }

    suspend fun createTarget(
        userID: String,
        name: String,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ) = wrap {
        val input = TargetCreateInput(name, timeSpent, type)
        mutateData(TargetCreateMutation(userID, input)).targetCreate
    }

    suspend fun updateTarget(
        targetID: String,
        name: Input<String>,
        timeSpent: Input<Int>,
        type: Input<TargetType>
    ) = wrap {
        val input = TargetUpdateInput(name, timeSpent, type)
        mutateData(TargetUpdateMutation(targetID, input)).targetUpdate
    }

    suspend fun deleteTarget(
        targetID: String
    ) = wrap {
        mutateData(TargetDeleteMutation(targetID)).targetDelete
    }

    suspend fun createTimePiece(
        targetID: String,
        start: Date,
        duration: Int,
        type: Input<TimePieceType>
    ) = wrap {
        val input = TimePieceCreateInput(start, duration, type)
        mutateData(TimePieceCreateMutation(targetID, input)).timePieceCreate
    }

    suspend fun updateTimePiece(
        timePieceID: Int,
        start: Input<Date>,
        duration: Input<Int>,
        type: Input<TimePieceType>
    ) = wrap {
        val input = TimePieceUpdateInput(start, duration, type)
        mutateData(TimePieceUpdateMutation(timePieceID, input)).timePieceUpdate
    }

    suspend fun deleteTimePiece(
        timePieceID: Int
    ) = wrap {
        mutateData(TimePieceDeleteMutation(timePieceID)).timePieceDelete
    }

    suspend fun createTimePiecesForTarget(
        targetID: String,
        inputs: List<TimePieceCreateInput>
    ) = wrap {
        mutateData(TimePiecesCreateForTargetMutation(targetID, inputs)).timePiecesCreateForTarget
    }

    companion object {
        fun <T> getInput(value: T?): Input<T> {
            return Input.fromNullable(value)
        }

        private var instance: HystimeClient? = null
        fun getInstance(): HystimeClient {
            if (instance == null) {
                instance = getErrorClient() // Error Client, prevent nullptr
            }
            return instance!!
        }

        class Client : ReadOnlyProperty<Any?, HystimeClient> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): HystimeClient {
                return getInstance()
            }
        }

        private fun getErrorClient(): HystimeClient = HystimeClient("", "")

        private suspend fun <T> wrap(
            ignoreCheck: Boolean = false,
            block: suspend () -> T
        ): Result<T> {
            if (!ignoreCheck) {
                if (instance == null) {
                    return Result.failure(Exception("HystimeClient is null"))
                }
                if (!instance!!.isValid()) {
                    return Result.failure(Exception("HystimeClient is invalid"))
                }
            }
            val result = try {
                Result.success(block.invoke())
            } catch (e: Throwable) {
                Result.failure(e)
            }
            return result
        }

        private suspend fun <D : Operation.Data, T, V : Operation.Variables> queryData(query: Query<D, T, V>): T {
            // FIXME: AS for now, there's no support for definitely non-null return type.
            // T should be changed to T & Any after kotlin 1.7 released.
            val resp = instance!!.client.query(query).await()
            if (resp.errors.isNullOrEmpty()) {
                if (resp.data == null) {
                    throw ServerInternalException("No data collected.")
                }
                return resp.data!!
            } else {
                throw ServerInternalException(resp.errors!!)
            }
        }

        private suspend fun <D : Operation.Data, T, V : Operation.Variables> mutateData(mutate: Mutation<D, T, V>): T {
            // FIXME: AS for now, there's no support for definitely non-null return type.
            // T should be changed to T & Any after kotlin 1.7 released.
            val resp = instance!!.client.mutate(mutate).await()
            if (resp.errors.isNullOrEmpty()) {
                if (resp.data == null) {
                    throw ServerInternalException("No data collected.")
                }
                return resp.data!!
            } else {
                throw ServerInternalException(resp.errors!!)
            }
        }

        class ServerInternalException : Exception {
            constructor(s: String) : super(s)

            constructor(s: List<com.apollographql.apollo.api.Error>) : super(s.map {
                it.message
            }.joinToString("\n") { it })
        }
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

fun Throwable.errorString(): String {
    return if (BuildConfig.DEBUG) {
        this.stackTraceToString()
    } else {
        this.localizedMessage ?: "Unknown error"
    }
}