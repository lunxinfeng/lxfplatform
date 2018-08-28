package com.fintech.lxf.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.fintech.lxf.R


class Label(context: Context,attrs: AttributeSet): LinearLayout(context,attrs) {
    private val textView = TextView(context)
    private val image = ImageView(context)

    init {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(Color.parseColor("#B8DB6D"))
        gravity = Gravity.CENTER
        textView.apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 18f
            val param = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
            layoutParams = param
        }
        image.apply {
            val param = LinearLayout.LayoutParams(48,48)
            layoutParams = param
            setImageResource(R.mipmap.close)
            setOnClickListener { onRemoveListener?.onRemove(text) }
        }
        addView(textView)
        addView(image)
    }

    var text = ""
        set(value) {
            field = value
            textView.text = value
        }
    var textSize = 18f
        set(value) {
            field = value
            textView.textSize = value
        }

    var textColor = Color.WHITE
        set(value) {
            field = value
            textView.setTextColor(value)
        }

    var onRemoveListener:OnRemoveListener? = null

    interface OnRemoveListener{
        fun onRemove(content:String)
    }
}