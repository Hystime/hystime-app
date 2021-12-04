package top.learningman.hystime.repo

import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.sdk.HystimeClient

object TargetRepository {
    private val client by lazy {
        HystimeClient.getInstance()
    }

    suspend fun getUserTargets(username: String) = client.getUserTargets(username).map {
        it!!.map { item ->
            TargetBean.fromUserTargetQuery(item)
        }
    }

    suspend fun getTarget(targetId: String) = client.getTarget(targetId).map {
        TargetBean.fromTargetQuery(it!!)
    }
}