package com.fintech.lxf.service.init;


import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.helper.AliPayUI;

import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.AliPayUI.steep;
import static com.fintech.lxf.helper.ExpansionKt.debug;


public class AlipayAccessibilityService extends BaseAccessibilityService {

    @Override
    protected int getType() {
        return TYPE_ALI;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        try {
            final int eventType = event.getEventType();

            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType) {//页面发生切换
                String className = event.getClassName().toString();
                debug(TAG, "onAccessibilityEvent: ClassName: " + className);


                int posV = getPosV();
                int endV = getEndV();

                if (posV * getbeishu() > endV) {
                    if (getOffsetV() >= 4){
                        stop();
                        return;
                    }else{
                        resetPos();
                    }
                }

                if ("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity".equals(className)) {//设置金额
                    if (steep == 1) {
                        steep = 2;

                        clearLocalPic();

                        inputAndSure();
                    }

                } else if ("com.alipay.mobile.framework.app.ui.DialogHelper$APGenericProgressDialog".equals(className)) {//加载
                    if (steep == 2) {
                        steep = 3;
                    }

                } else if ("com.alipay.mobile.payee.ui.PayeeQRActivity".equals(className)) {

                    if (steep == 3) {//保存图片
                        steep = 1;// 去往第四步  悬空

                        Thread.sleep(300);

                        final AccessibilityNodeInfo root = getRootInActiveWindow();
                        if (root == null) {
                            return;
                        }
                        //如果没有 找到 清除金额 的按钮 ,说明生成定额二维码未成功,中止...
                        final List<AccessibilityNodeInfo> clear = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_Modify_money);
                        if (clear == null || clear.size() == 0) {
                            return;
                        }
                        //保存图片
                        final List<AccessibilityNodeInfo> open = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_save_qrcode);
                        if (open == null || open.size() == 0) {
                            return;
                        }
                        Observable.just(getPosV())
                                .doOnNext(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
                                        open.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }
                                })
                                .observeOn(Schedulers.io())
                                .delay(1100, TimeUnit.MILLISECONDS)
                                .doOnNext(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
                                        save();
                                        clear.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }
                                })
                                .delay(100, TimeUnit.MILLISECONDS)
                                .subscribe(new Observer<Integer>() {
                                    Disposable d;
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        this.d = d;
                                    }

                                    @Override
                                    public void onNext(Integer integer) {
                                        List<AccessibilityNodeInfo> set = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_Modify_money);
                                        if (set == null || set.size() == 0) {
                                            return;
                                        }
                                        set.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        d.dispose();
                                    }

                                    @Override
                                    public void onComplete() {
                                        //回到第一步 回到正轨
                                        steep = 1;
                                        d.dispose();
                                    }
                                });
//                        open.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        Thread.sleep(1100);
//                        //解析图片
//                        save();
//
//                        //清除金额
//                        clear.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        Thread.sleep(100);
//                        //设置金额
//                        List<AccessibilityNodeInfo> set = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_Modify_money);
//                        if (set == null || set.size() == 0) {
//                            return;
//                        }
//                        set.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        //回到第一步 回到正轨
//                        steep = 1;
                    }

                } else if ("com.alipay.mobile.commonui.widget.APNoticePopDialog".equals(className)) {//人气大爆发

                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root == null) {
                        return;
                    }
                    List<AccessibilityNodeInfo> open = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_ensure);
                    if (open == null || open.size() == 0) {
                        return;
                    }
                    open.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    Thread.sleep(10000);
                    inputAndSure();

                    steep = 2;

                }

            }// 页面切换

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}