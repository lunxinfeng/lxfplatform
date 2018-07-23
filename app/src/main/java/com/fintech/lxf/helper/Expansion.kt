package com.fintech.lxf.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

var DEBUG = true

fun Activity.toActivity(clazz: Class<*>) = startActivity(Intent(this,clazz))

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(applicationContext, msg, duration).show()

fun debug(tag: String, msg: String) = if (DEBUG) Log.d(tag, msg) else null