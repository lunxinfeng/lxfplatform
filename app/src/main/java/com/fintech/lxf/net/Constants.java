package com.fintech.lxf.net;


import android.Manifest;

import okhttp3.MediaType;

public class Constants {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

//    public static final String baseUrl = "https://api.pay.hccf8.com";
//    public static String baseUrl = "https://api.3721gt.com";
    public static String baseUrl = "http://api.trueinfo.cn";

    public static final int RC_PERMISSION = 110;
    public static final int ALL_PERMISSION = 122;

    public static String[] PERMISSIONS_GROUP = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE
    };

    public static final String KEY_SP_ = "userInfo";
    public static final String KEY_MCH_ID = "mchId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LOGIN_TOKEN = "loginToken";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_ALLOW_LOAD = "allowLoad";
    public static final String KEY_BEGIN_NUM = "beginNum";
    public static final String KEY_END_NUM = "endNum";
    public static final String KEY_MAX_NUM = "maxNum";
    public static final String KEY_ALI_VERSION = "aliVersion";
    public static final String KEY_LOGIN_TYPE = "loginType";


}
