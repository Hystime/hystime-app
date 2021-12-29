package top.learningman.hystime.view

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.widget.NumberPicker
import android.widget.TimePicker

class DurationPickerDialog(
    context: Context?,
    private val callback: OnTimeSetListener?,
) :
    TimePickerDialog(context, callback, 0, 0, true) {

    init {
        this.setTitle("Set duration")
    }

    private var timePicker: TimePicker? = null

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (callback != null && timePicker != null) {
            timePicker!!.clearFocus()
            callback.onTimeSet(timePicker, timePicker!!.hour, timePicker!!.minute)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            timePicker = findViewById(
                Resources.getSystem().getIdentifier("timePicker", "id", "android")
            )

            val hoursPicker = timePicker!!.findViewById<NumberPicker>(
                Resources.getSystem().getIdentifier("hour", "id", "android")
            )

            val maxHour = 100
            hoursPicker.minValue = 0
            hoursPicker.maxValue = maxHour

            val displayedValues: MutableList<String> = ArrayList()

            for (i in 0..maxHour) displayedValues.add(String.format("%d", i))

            hoursPicker.displayedValues = displayedValues.toTypedArray()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}