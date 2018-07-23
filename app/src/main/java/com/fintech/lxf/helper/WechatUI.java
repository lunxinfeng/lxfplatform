package com.fintech.lxf.helper;


import android.os.Environment;

/**
 * wechat ui 控件
 */
public class WechatUI {

    public static final String acc = "acc_wx";
    public static final String startV = "startzV_wx";
    public static final String endV = "endzV_wx";
    public static final String posV = "pos_wx";
    public static final String offsetV = "dis_wx";
    public static final String beishuV = "beishuV_wx";

    public final static String packageName = "com.tencent.mm";

    public final static String BUTTON = "android.widget.Button";
    public final static String EDITTEXT = "android.widget.EditText";
    public final static String LUCKY_MONEY_RECEIVE_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    public final static String LUCKY_MONEY_DETAIL_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    public final static String OPEN_LUCKY_MONEY_BUTTON_ID = "com.tencent.mm:id/b_b";


    //首页
    public final static String page_LauncherUI = "com.tencent.mm.ui.LauncherUI";
    //加号页面
    public final static String page_b = "android.widget.FrameLayout";

    //设置金额
    public final static String page_CollectCreateQRCodeUI = "com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI";
    //加载
    public final static String page_p = "com.tencent.mm.ui.base.parse";

    //收付款
    public final static String page_WalletOfflineCoinPurseUI = "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI";
    //二维码收款
    public final static String page_CollectMainUI = "com.tencent.mm.plugin.collect.ui.CollectMainUI";

    public final static String et_bx = "com.tencent.mm:id/bx";

    public final static String btn_sure = "com.tencent.mm:id/alr"; //确定
    public final static String btn_save = "com.tencent.mm:id/amb"; //保存图片
    public final static String btn_set = "com.tencent.mm:id/ama"; //清除金额 设置金额



    //控件

    public final static String click_0 = "com.tencent.mm:id/tenpay_keyboard_0";
    public final static String click_1 = "com.tencent.mm:id/tenpay_keyboard_1";
    public final static String click_2 = "com.tencent.mm:id/tenpay_keyboard_2";
    public final static String click_3 = "com.tencent.mm:id/tenpay_keyboard_3";
    public final static String click_4 = "com.tencent.mm:id/tenpay_keyboard_4";
    public final static String click_5 = "com.tencent.mm:id/tenpay_keyboard_5";
    public final static String click_6 = "com.tencent.mm:id/tenpay_keyboard_6";
    public final static String click_7 = "com.tencent.mm:id/tenpay_keyboard_7";
    public final static String click_8 = "com.tencent.mm:id/tenpay_keyboard_8";
    public final static String click_9 = "com.tencent.mm:id/tenpay_keyboard_9";
    public final static String click_x = "com.tencent.mm:id/tenpay_keyboard_x";


        //步骤
    public static volatile int steep = 1;
    public static String getPicPath() {
        String dcim_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        return dcim_path + "/tencent/MicroMsg/WeiXin/";
    }
}
