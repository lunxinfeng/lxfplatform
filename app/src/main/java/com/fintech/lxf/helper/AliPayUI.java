package com.fintech.lxf.helper;


import android.os.Environment;

/**
 * alipay ui 控件
 */
public class AliPayUI {

    public static final String acc = "acc_ali";
    public static final String startV = "startzV";
    public static final String endV = "endzV";
    public static final String posV = "pos";
    public static final String offsetV = "dis";
    public static final String beishuV = "beishuV";


    // -- 个人收钱码
    /**
     * 首页
     */
    public final static  String btn_money = "com.alipay.android.phone.openplatform:id/collect_layout";

    /**
     * 个人收钱 - 设置金额 - 清除金额
     */
    public final static String btn_Modify_money = "com.alipay.mobile.payee:id/payee_QRCodePayModifyMoney";

    /**
     * 个人收钱 - 保存图片
     */
    public final static String btn_save_qrcode = "com.alipay.mobile.payee:id/payee_save_qrcode";

    /**
     * 设置金额 - 金额
     */
    public final static String et_content = "com.alipay.mobile.ui:id/content";

    /**
     * 设置金额 - 确定
     */
    public final static String btn_next = "com.alipay.mobile.payee:id/payee_NextBtn";

    public final static String btn_ensure = "com.alipay.mobile.antui:id/ensure";

    //步骤
    public static volatile int steep = 0;

    public static String getPicPath() {
        String dcim_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        return dcim_path + "/Camera/";
    }
}
