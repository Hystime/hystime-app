/*
 * Copyright (C) 2017 Haruki Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.learningman.hystime.utils.numberPicker

import android.content.Context
import android.util.AttributeSet
import android.content.res.TypedArray
import top.learningman.hystime.R
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.preference.DialogPreference

class NumberPickerPreferenceCompat : DialogPreference {
    private var mValue = 0
    private var mMaxValue = Int.MAX_VALUE
    private var mMinValue = Int.MIN_VALUE
    private var mValueSet = false
    var unitText: CharSequence? = null
    var wrapSelectorWheel = true
    var entries: Array<CharSequence>? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.NumberPickerPreferenceCompat, defStyleAttr, defStyleRes
        )
        mMinValue = ta.getInt(R.styleable.NumberPickerPreferenceCompat_minValue, mMinValue)
        mMaxValue = ta.getInt(R.styleable.NumberPickerPreferenceCompat_maxValue, mMaxValue)
        unitText = ta.getString(R.styleable.NumberPickerPreferenceCompat_unitText)
        wrapSelectorWheel =
            ta.getBoolean(R.styleable.NumberPickerPreferenceCompat_wrapSelectorWheel, true)
        entries = ta.getTextArray(R.styleable.NumberPickerPreferenceCompat_entries)
        if (entries == null) {
            entries = ta.getTextArray(R.styleable.NumberPickerPreferenceCompat_android_entries)
        }
        ta.recycle()
        dialogLayoutResource = R.layout.preference_dialog_number_picker
    }
    /**
     * Gets the value from the [android.content.SharedPreferences].
     *
     * @return The current preference value.
     */// Always persist/notify the first time.
    /**
     * Saves the value to the [android.content.SharedPreferences].
     *
     * @param value The value to save
     */
    var value: Int
        get() = mValue
        set(value) {
            val changed = this.value != value

            // Always persist/notify the first time.
            if (changed || !mValueSet) {
                mValue = value
                mValueSet = true
                persistInt(value)
                if (changed) {
                    notifyChanged()
                }
            }
        }
    var minValue: Int
        get() = if (mMinValue == Int.MIN_VALUE) 0 else mMinValue
        set(minValue) {
            mMinValue = minValue
        }
    var maxValue: Int
        get() = if (mMaxValue == Int.MAX_VALUE) 0 else mMaxValue
        set(maxValue) {
            mMaxValue = maxValue
        }

    fun setEntries(@ArrayRes entriesResId: Int) {
        entries = context.resources.getTextArray(entriesResId)
    }

    fun setUnitText(@StringRes unitTextResId: Int) {
        unitText = context.resources.getText(unitTextResId)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt(
            clamp(
                if (defaultValue != null) defaultValue as Int else 0,
                mMinValue,
                mMaxValue
            )
        )
    }

    override fun getSummary(): CharSequence {
        return "$value $unitText"
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }
        val myState = SavedState(superState)
        myState.value = value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        value = myState.value
    }

    private class SavedState : BaseSavedState {
        var value = 0

        constructor(source: Parcel) : super(source) {
            value = source.readInt()
        }

        constructor(superState: Parcelable?) : super(superState) {}

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(value)
        }

        companion object {
            @JvmField
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private fun clamp(value: Int, min: Int, max: Int): Int {
            return value.coerceAtLeast(min).coerceAtMost(max)
        }
    }
}