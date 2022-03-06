package top.learningman.hystime

object Constant {

    const val TIMER_SETTING_INTENT_KEY = "timer_setting_intent_key"

    const val TIMER_DURATION_INTENT_KEY = "timer_duration_intent_key" // long ms
    const val TIMER_NAME_INTENT_KEY = "timer_name_intent_key" // str
    const val TIMER_TYPE_INTENT_KEY = "timer_type_intent_key" // TimerType

    const val TIMER_BROADCAST_TIME_ACTION = "timer_broadcast_action"
    const val TIMER_BROADCAST_CLEAN_ACTION = "timer_broadcast_clean_action"

    const val TIMER_BROADCAST_PAST_TIME_EXTRA = "timer_broadcast_past_time_extra" // long ms
    const val TIMER_BROADCAST_REMAIN_TIME_EXTRA = "timer_broadcast_remain_time_extra" // long ms

    const val TIMER_BROADCAST_CLEAN_DURATION_EXTRA = "timer_broadcast_clean_duration_extra" // long s
    const val TIMER_BROADCAST_CLEAN_REMAIN_EXTRA = "timer_broadcast_clean_remain_extra" // long s
    const val TIMER_BROADCAST_CLEAN_START_EXTRA = "timer_broadcast_clean_start_extra" // Date
    const val TIMER_BROADCAST_CLEAN_TYPE_EXTRA = "timer_broadcast_clean_type_extra" // TimerType

    const val TIMER_NOTIFICATION_CHANNEL_ID = "timer_notification_channel_id"

    const val TIMER_FULLSCREEN_ACTION = "timer_fullscreen_action"
    const val TIMER_FULLSCREEN_INTENT_TIME_KEY = "timer_fullscreen_intent_time_key"
    const val TIMER_FULLSCREEN_INTENT_TYPE_KEY = "timer_fullscreen_intent_type_key"

    const val TIMER_FRAGMENT_RESUME_ACTION = "timer_fragment_resume_action"
    const val TIMER_FRAGMENT_PAUSE_ACTION = "timer_fragment_pause_action"
    const val TIMER_FRAGMENT_CANCEL_ACTION = "timer_fragment_cancel_action"

    const val FOREGROUND_NOTIFICATION_ID = 114514
}
