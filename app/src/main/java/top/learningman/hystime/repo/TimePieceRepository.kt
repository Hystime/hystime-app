package top.learningman.hystime.repo

import top.learningman.hystime.sdk.HystimeClient

object TimePieceRepository {
    private val client by lazy {
        HystimeClient.getInstance()
    }

    suspend fun getTimePiece
}