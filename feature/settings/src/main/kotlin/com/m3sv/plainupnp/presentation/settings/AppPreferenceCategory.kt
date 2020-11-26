package com.m3sv.plainupnp.presentation.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

class AppPreferenceCategory : PreferenceCategory {
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.findViewById(android.R.id.title) as TextView) {
            TextViewCompat.setTextAppearance(
                this,
                R.style.TextAppearance_MaterialComponents_Subtitle1
            )
            setTextColor(resources.getColor(R.color.colorPrimary))
        }
    }

}
