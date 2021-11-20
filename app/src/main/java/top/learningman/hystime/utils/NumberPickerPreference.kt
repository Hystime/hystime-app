package top.learningman.hystime.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder


class NumberPickerPreference : DialogPreference {
    private var picker: NumberPicker? = null
    private var value = 0
        set(value) {
            field = value
            persistInt(this.value)
        }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    fun onCreateDialogView(): View {
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        picker = NumberPicker(context)
        picker!!.layoutParams = layoutParams
        val dialogView = FrameLayout(context)
        dialogView.addView(picker)
        return dialogView
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        picker!!.minValue = MIN_VALUE
        picker!!.maxValue = MAX_VALUE
        picker!!.wrapSelectorWheel = WRAP_SELECTOR_WHEEL
        picker!!.value = value
    }

    override fun onPrepareForRemoval() {
        super.onPrepareForRemoval()
        picker!!.clearFocus()
        val newValue = picker!!.value
        if (callChangeListener(newValue)) {
            value = newValue
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, MIN_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value =
            if (restorePersistedValue) getPersistedInt(MIN_VALUE) else (defaultValue as Int?)!!
    }

    companion object {
        // allowed range
        const val MAX_VALUE = 100
        const val MIN_VALUE = 0

        // enable or disable the 'circular behavior'
        const val WRAP_SELECTOR_WHEEL = true
    }
}