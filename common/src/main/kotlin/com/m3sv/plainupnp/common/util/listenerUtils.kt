package com.m3sv.plainupnp.common.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar

fun onItemSelectedListener(block: (Int) -> Unit): AdapterView.OnItemSelectedListener {
    return object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            block(position)
        }
    }
}

fun onSeekBarChangeListener(block: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
    return object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            block(seekBar.progress)
        }
    }
}

fun onTextChangedListener(block: (CharSequence) -> Unit): TextWatcher {
    return object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            // no-op
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            block(s)
        }
    }
}
