package top.learningman.hystime.data

import TimePieceCreateMutation
import java.util.*

data class TimePieceBean(
    val id: Int,
    val start: Date,
    val duration: Int,
    val type: TimePieceType
) {
    enum class TimePieceType(s: String) {
        NORMAL("NORMAL"),
        POMODORO("POMODORO")
    }

    companion object {
        fun fromTimePieceCreateMutation(value: TimePieceCreateMutation.TimePieceCreate) =
            TimePieceBean(
                value.id,
                value.start,
                value.duration,
                TimePieceType.valueOf(value.type.toString())
            )
    }
}
