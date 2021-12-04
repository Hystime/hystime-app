package top.learningman.hystime.repo

import top.learningman.hystime.data.TimePieceBean
import top.learningman.hystime.sdk.HystimeClient

object TimePieceRepository {
    private val client by lazy {
        HystimeClient.getInstance()
    }

    suspend fun getUserLastWeekTimePieces(username: String) =
        client.getUserLastWeekTimePieces(username).map {
            it!!.map { item ->
                TimePieceBean.fromUserLastWeekTimePieces(item)
            }
        }

    suspend fun getTargetLastWeekTimePieces(targetId: String) =
        client.getTargetLastWeekTimePieces(targetId).map {
            it!!.map { item ->
                TimePieceBean.fromTargetLastWeekTimePieces(item)
            }
        }
}