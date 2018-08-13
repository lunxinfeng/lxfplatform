package com.fintech.lxf.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast

var DEBUG = true

fun Activity.toActivity(clazz: Class<*>) = startActivity(Intent(this,clazz))

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(applicationContext, msg, duration).show()

fun debug(tag: String, msg: String) = if (DEBUG) Log.d(tag, msg) else null

fun isEmpty(content:String?) = TextUtils.isEmpty(content)

fun EditText.onChange(onChange:(s: CharSequence?) -> Unit){
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChange.invoke(s)
        }
    })
}