package com.fintech.lxf.net

import android.Manifest
import okhttp3.MediaType


//val JSON = MediaType.parse("application/json; charset=utf-8")

const val baseUrl = "https://api.pay.hccf8.com"
//    public static final String baseUrl = "http://10.0.8.91:50182";
//    public static final String baseUrl = "http://10.0.8.223:59980";

const val RC_PERMISSION = 110
const val ALL_PERMISSION = 122
val PERMISSIONS_GROUP = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

//val KEY_SP_ = "userInfo"
//val KEY_MCH_ID = "mchId"
//val KEY_USER_NAME = "userName"
//val KEY_PASSWORD = "password"
//val KEY_LOGIN_TOKEN = "loginToken"