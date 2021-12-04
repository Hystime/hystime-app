package top.learningman.hystime.repo

import top.learningman.hystime.data.UserBean
import top.learningman.hystime.sdk.HystimeClient

object UserRepository {
    private val client by lazy {
        HystimeClient.getInstance()
    }

    suspend fun getUser(username: String) = client.getUserInfo(username).map {
        UserBean.fromUserInfoQuery(it!!)
    }
}
