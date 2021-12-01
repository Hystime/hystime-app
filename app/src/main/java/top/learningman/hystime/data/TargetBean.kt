package top.learningman.hystime.data

import java.util.*

enum class TargetType(s: String) {
    NORMAL("NORMAL"),
    LONGTERM("LONGTERM"),
}

data class TargetBean(
    val id: String,
    val name: String,
    val created_at: Date,
    val type: TargetType,
    val timeSpent: Int
) {
    companion object {
        fun fromUserTargetQuery(value: UserTargetsQuery.Target) = TargetBean(
            value.id,
            value.name,
            value.created_at,
            TargetType.valueOf(value.type.toString()),
            value.timeSpent
        )
    }
}