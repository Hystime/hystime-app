package top.learningman.hystime.repo

import top.learningman.hystime.data.UserBean
import top.learningman.hystime.sdk.HystimeClient.Companion.Client

object UserRepo {
    private val client by Client()

    suspend fun getUser(username: String) = client.getUserInfo(username).map {
        UserBean.fromUserInfoQuery(it)
    }
}
