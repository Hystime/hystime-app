package top.learningman.hystime.sdk

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object ScalarAdapter {
    val dateTimeAdapter = object : CustomTypeAdapter<Date> {
        override fun encode(value: Date): CustomTypeValue<*> {
            return CustomTypeValue.GraphQLString(
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    Locale.US
                ).format(value)
            )
        }

        override fun decode(value: CustomTypeValue<*>): Date {
            val str = value.value.toString()
            return Date.from(Instant.parse(str))
        }
    }
}