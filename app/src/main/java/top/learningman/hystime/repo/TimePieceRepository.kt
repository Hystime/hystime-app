package top.learningman.hystime.repo

import top.learningman.hystime.data.TimePieceBean
import top.learningman.hystime.sdk.HystimeClient.Companion.Client

object TimePieceRepository {
    private val client by Client()

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