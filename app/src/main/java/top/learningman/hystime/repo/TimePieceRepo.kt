package top.learningman.hystime.repo

import top.learningman.hystime.data.TimePieceBean
import top.learningman.hystime.sdk.HystimeClient.Companion.Client
import top.learningman.hystime.sdk.HystimeClient.Companion.getInput
import type.TimePieceType
import java.util.*

object TimePieceRepo {
    private val client by Client()

    suspend fun addTimePiece(
        targetId: String,
        start: Date,
        duration: Int,
        type: TimePieceType? = null
    ) =
        client.createTimePiece(targetId, start, duration, getInput(type)).map {
            TimePieceBean.fromTimePieceCreateMutation(it!!)
        }
}