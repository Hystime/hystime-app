package top.learningman.hystime.data

import java.util.*

enum class TargetType(s: String) {
    NORMAL("NORMAL"),
    LONGTERM("LONGTERM"),
}

data class Target(
    val id: String,
    val name: String,
    val created_at: Date,
    val type: TargetType
) {
    companion object {
        fun fromUserTargetQuery(value: UserTargetsQuery.Target){
            val target = Target(
                value.id,
                value.name,
                value.created_at,
                TargetType.valueOf(value.type.toString())
            )
        }
    }
}