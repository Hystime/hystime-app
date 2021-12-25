package top.learningman.hystime.repo

import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.sdk.HystimeClient.Companion.Client
import top.learningman.hystime.sdk.HystimeClient.Companion.getInput
import type.TargetType

object TargetRepo {
    private val client by Client()

    suspend fun getUserTargets(username: String) = client.getUserTargets(username).map {
        it!!.map { item ->
            TargetBean.fromUserTargetQuery(item)
        }
    }

    suspend fun getTarget(targetId: String) = client.getTarget(targetId).map {
        TargetBean.fromTargetQuery(it!!)
    }

    suspend fun addTarget(
        userID: String,
        name: String,
        timeSpent: Int? = null,
        type: TargetType? = null
    ) = client.createTarget(
        userID, name,
        getInput(timeSpent),
        getInput(type)
    ).map {
        TargetBean.fromTargetCreateMutation(it!!)
    }
}