package top.learningman.hystime.data

import TargetCreateMutation
import TargetQuery
import UserTargetsQuery
import java.util.*


data class TargetBean(
    val id: String,
    var name: String,
    var created_at: Date,
    var type: TargetType,
    var timeSpent: Int
) {
    enum class TargetType(s: String) {
        NORMAL("NORMAL"),
        LONGTERM("LONGTERM"),
    }

    companion object {
        fun fromUserTargetQuery(value: UserTargetsQuery.Target) = TargetBean(
            value.id,
            value.name,
            value.created_at,
            TargetType.valueOf(value.type.toString()),
            value.timeSpent
        )

        fun fromTargetQuery(value: TargetQuery.Target) = TargetBean(
            value.id,
            value.name,
            value.created_at,
            TargetType.valueOf(value.type.toString()),
            value.timeSpent
        )

        fun fromTargetCreateMutation(value: TargetCreateMutation.TargetCreate) = TargetBean(
            value.id,
            value.name,
            value.created_at,
            TargetType.valueOf(value.type.toString()),
            value.timeSpent
        )
    }
}