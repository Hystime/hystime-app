package top.learningman.hystime.data

import UserInfoQuery
import java.util.*

data class UserBean(
    val id: String,
    val username: String,
    val created_at: Date,
) {
    companion object {
        fun fromUserInfoQuery(value: UserInfoQuery.User): UserBean {
            return UserBean(
                value.id,
                value.username,
                value.created_at
            )
        }
    }
}