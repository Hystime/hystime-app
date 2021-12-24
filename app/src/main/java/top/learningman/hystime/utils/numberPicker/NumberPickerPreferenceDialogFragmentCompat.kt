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

import android.annotation.SuppressLint
import android.app.Service
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceDialogFragmentCompat
import com.shawnlin.numberpicker.NumberPicker
import top.learningman.hystime.R
import top.learningman.hystime.repo.SharedPrefRepo


class NumberPickerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var mNumberPicker: NumberPicker? = null
    private var mValue = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mValue = savedInstanceState?.getInt(SAVE_STATE_VALUE)
            ?: numberPickerPreference.value
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_VALUE, mValue)
    }

    @SuppressLint("MissingPermission") // Android Studio cannot recognize permission.
    @Suppress("DEPRECATION")
    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        mNumberPicker = view.findViewById(R.id.number_picker)
        val mUnitTextView = view.findViewById<TextView>(R.id.unit_text)
        checkNotNull(mNumberPicker) {
            "Dialog view must contain an NumberPicker with id" +
                    " @id/number_picker"
        }
        val vib = requireActivity().getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        val vibEft = VibrationEffect.createOneShot(20, 125)
        mNumberPicker?.apply {
            minValue = numberPickerPreference.minValue
            maxValue = numberPickerPreference.maxValue
            value = mValue
            wrapSelectorWheel = numberPickerPreference.wrapSelectorWheel
            numberPickerPreference.entries?.let {
                displayedValues = mapToStringArray(it)
            }
            if (SharedPrefRepo.getVibrationStatus()){
                setOnValueChangedListener { _, _, newVal ->
                    mValue = newVal
                    vib.vibrate(vibEft)
                }
            } else {
                setOnValueChangedListener { _, _, newVal ->
                    mValue = newVal
                }
            }
        }

        val unitText = numberPickerPreference.unitText
        if (unitText != null) {
            mUnitTextView.text = unitText
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            // NOTE: this clearFocus() triggers validateInputTextView() internally
            mNumberPicker!!.clearFocus()
            val value = mNumberPicker!!.value
            if (numberPickerPreference.callChangeListener(value)) {
                numberPickerPreference.value = value
            }
        }
    }

    private val numberPickerPreference: NumberPickerPreferenceCompat
        get() = preference as NumberPickerPreferenceCompat

    companion object {
        private const val SAVE_STATE_VALUE = "NumberPickerPreferenceDialogFragmentCompat.value"
        fun newInstance(key: String): NumberPickerPreferenceDialogFragmentCompat {
            val fragment = NumberPickerPreferenceDialogFragmentCompat()
            val args = Bundle(1)
            args.putString(ARG_KEY, key)
            fragment.arguments = args
            return fragment
        }

        private fun mapToStringArray(entries: Array<CharSequence>): Array<String?> {
            val converted = arrayOfNulls<String>(entries.size)
            for (i in entries.indices) {
                converted[i] = entries[i].toString()
            }
            return converted
        }
    }
}