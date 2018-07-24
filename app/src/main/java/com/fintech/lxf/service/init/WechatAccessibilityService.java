package com.fintech.lxf.service.init;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.helper.WechatUI;

import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.ExpansionKt.debug;
import static com.fintech.lxf.helper.WechatUI.steep;


public class WechatAccessibilityService extends BaseAccessibilityService {

    @Override
    protected int getType() {
        return TYPE_WeChat;
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

                if ("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI".equals(className)) {//设置金额
                    if (steep == 3){//steep == 3网络不好时可能会发生
                        sure();
                    }
                    if (steep == 1) {
                        steep = 2;

                        clearLocalPic();

                        input();

                        sure();
                    }


                } else if ("com.tencent.mm.ui.base.p".equals(className)) {//加载
                    if (steep == 2) {
                        steep = 3;
                    }

                } else if ("com.tencent.mm.plugin.collect.ui.CollectMainUI".equals(className)) {

                    if (steep == 2 || steep == 3) {//保存图片
                        steep = 1;// 去往第四步  悬空

                        Thread.sleep(300);

                        final AccessibilityNodeInfo root = getRootInActiveWindow();
                        if (root == null) {
                            return;
                        }
                        //如果没有 找到 清除金额 的按钮 ,说明生成定额二维码未成功,中止...
                        final List<AccessibilityNodeInfo> clear = root.findAccessibilityNodeInfosByViewId(WechatUI.btn_set);
                        if (clear == null || clear.size() == 0) {
                            return;
                        }
                        //保存图片
                        final List<AccessibilityNodeInfo> open = root.findAccessibilityNodeInfosByViewId(WechatUI.btn_save);
                        if (open == null || open.size() == 0) {
                            return;
                        }
                        Observable.just(getPosV())
                                .doOnNext(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
//                                        open.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            click(open.get(0));
                                        }else{
                                            open.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        }
                                    }
                                })
                                .observeOn(Schedulers.io())
                                .delay(1500, TimeUnit.MILLISECONDS)
                                .doOnNext(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
                                        save();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            click(clear.get(0));
                                        }else{
                                            clear.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        }
                                    }
                                })
                                .delay(300, TimeUnit.MILLISECONDS)
                                .subscribe(new Observer<Integer>() {
                                    Disposable d;
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        this.d = d;
                                    }

                                    @Override
                                    public void onNext(Integer integer) {
                                        List<AccessibilityNodeInfo> set = root.findAccessibilityNodeInfosByViewId(WechatUI.btn_set);
                                        if (set == null || set.size() == 0) {
                                            return;
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            click(set.get(0));
                                        }else{
                                            set.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        }
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
                    }
                }
            }// 页面切换

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void click(final AccessibilityNodeInfo node) {
        click(node, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                debug(TAG, "click: " + node.getText());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void click(AccessibilityNodeInfo node, AccessibilityService.GestureResultCallback gestureResultCallback) {
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        Point position = new Point(rect.centerX(), rect.centerY());
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(position.x, position.y);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L));
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, gestureResultCallback, null);

    }
}