package com.fintech.lxf.service.init;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.fintech.lxf.db.DB;
import com.fintech.lxf.db.User;
import com.fintech.lxf.ui.activity.init.InitActivity;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.ExpansionKt.debug;
import static com.fintech.lxf.helper.WechatUI.steep;


public class WechatAccessibilityService extends BaseAccessibilityService {
    private int lastType;
    private int lastSteep;

    @Override
    protected int getType() {
        return TYPE_WeChat;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        if (!isNonNormal(event)){
//            parserEvent(event);
//            lastType = event.getEventType();
//        }
        parserEvent(event);
    }

    private boolean isNonNormal(AccessibilityEvent event) {//会打开两次设置金额页面
        return lastType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && lastSteep == 5
                && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getClassName().toString().equals("com.alipay.mobile.payee.ui.PayeeQRActivity");
    }

    //com.tencent.mm.plugin.mall.ui.MallIndexUI  我的钱包页面
    //com.tencent.mm.plugin.offline.ui.WalletOfflineEntranceUI 收付款页面
    //com.tencent.mm:id/cdj   我
    //com.tencent.mm:id/c_b   收付款
    //com.tencent.mm:id/e18   二维码收款
    private synchronized void parserEvent(AccessibilityEvent event) {
        debug(TAG, "onAccessibilityEvent: event: " + event);
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                currClass = event.getClassName().toString();

                if (isFinish()) return;

                switch (currClass) {
                    case "com.tencent.mm.ui.LauncherUI"://首页
                        debug(TAG, "onAccessibilityEvent: 首页: " + steep);
                        if (steep == -2) {
                            lastSteep = steep;
                            steep = -1;

                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("我");
                                if (node != null && node.size() > 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        node.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        debug(TAG, "click-------->我");
                                    }
                                }
                            }

                            SystemClock.sleep(1000);

                            root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("钱包");
                                if (node != null && node.size() > 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        node.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        debug(TAG, "click-------->钱包");
                                    }
                                }
                            }
                        }
                        break;
                    case "com.tencent.mm.plugin.mall.ui.MallIndexUI":
                        debug(TAG, "onAccessibilityEvent: 我的钱包: " + steep);
                        if (steep == -1) {
                            lastSteep = steep;
                            steep = 0;
                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("收付款");
                                if (node != null && node.size() > 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        node.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        debug(TAG, "click-------->收付款");
                                    }
                                }
                            }
                        }
                        break;
//                    case "com.tencent.mm.plugin.offline.ui.WalletOfflineEntranceUI"://
                    case "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"://
                        debug(TAG, "onAccessibilityEvent: 收付款页面: " + steep);
                        if (steep == 0) {
                            lastSteep = steep;
                            steep = 1;
                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("二维码收款");
                                if (node != null && node.size() > 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        node.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        debug(TAG, "click-------->二维码收款");
                                    }
                                }
                            }
                        }
                        break;
                    case "com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI":
                        debug(TAG, "onAccessibilityEvent: 设置金额页面: " + steep);
                        if (steep == 3) {//steep == 3网络不好时可能会发生
                            lastSteep = steep;
                            click(amount_btnSure());
                        }
                        if (steep == 1 || steep == 2) {
                            lastSteep = steep;
                            steep = 2;
//                            clearLocalPic();
                            input();
                            click(amount_btnSure());
                        }
                        break;
                    case "com.tencent.mm.ui.base.p":
                        debug(TAG, "onAccessibilityEvent: 设置金额加载框: " + steep);
                        if (steep == 2) {
                            lastSteep = steep;
                            steep = 3;
                        }
                        break;
                    case "com.tencent.mm.plugin.collect.ui.CollectMainUI":
                        debug(TAG, "onAccessibilityEvent: 二维码页面: " + steep);
                        if (steep == 1) {
                            lastSteep = steep;
//                            click(qr_set());
                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("设置金额");
                                if (node != null && node.size() > 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        click(node.get(0));
                                        debug(TAG, "click-------->设置金额");
                                    }
                                }
                            }
                        }
                        if (steep == 3 || steep == 2) {//保存图片
                            lastSteep = steep;
                            steep = 4;

                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("清除金额");
                                if (node != null && node.size() > 0) {


                                    clearLocalPic();
                                    File file = getPicFile();
                                    File[] files = file.listFiles();
                                    debug(TAG, "本地图片数量：" + files.length);
                                    AccessibilityNodeInfo root1 = getRootInActiveWindow();
                                    if (root1 != null) {
                                        List<AccessibilityNodeInfo> node1 = root.findAccessibilityNodeInfosByText("保存收款码");
                                        if (node1 != null && node1.size() > 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                node1.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                click(node1.get(0));
                                                debug(TAG, "click-------->保存收款码");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (steep == -2) {
                            lastSteep = steep;


                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("清除金额");
                                if (node != null && node.size() > 0) {
                                    steep = 4;

                                    clearLocalPic();
                                    File file = getPicFile();
                                    File[] files = file.listFiles();
                                    debug(TAG, "本地图片数量：" + files.length);
                                    AccessibilityNodeInfo root1 = getRootInActiveWindow();
                                    if (root1 != null) {
                                        List<AccessibilityNodeInfo> node1 = root.findAccessibilityNodeInfosByText("保存收款码");
                                        if (node1 != null && node1.size() > 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                node1.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                click(node1.get(0));
                                                debug(TAG, "click-------->保存收款码");
                                            }
                                        }
                                    }
                                } else {
                                    steep = 1;

                                    AccessibilityNodeInfo root1 = getRootInActiveWindow();
                                    if (root1 != null) {
                                        List<AccessibilityNodeInfo> node1 = root.findAccessibilityNodeInfosByText("设置金额");
                                        if (node1 != null && node1.size() > 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                click(node1.get(0));
                                                debug(TAG, "click-------->设置金额");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                debug(TAG, "onAccessibilityEvent: 内容改变: " + steep);
                if (steep == 5) {
                    lastSteep = steep;
                    steep = 1;
//                    click(qr_set());
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root != null) {
                        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("设置金额");
                        if (node != null && node.size() > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                click(node.get(0));
                                debug(TAG, "click-------->设置金额");
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                debug(TAG, "onAccessibilityEvent: 通知改变: " + steep);
                if (event.getClassName().toString().contains("Toast") &&
                        event.getText().get(0).toString().contains("网络")) {//设置金额点击确定时，网络出错
                    if (steep == 3 || steep == 2) {
                        lastSteep = steep;
                        click(amount_btnSure());
                    }
                    return;
                }
                if (steep != 4) return;
                if (event.getClassName().toString().contains("Toast") &&
                        event.getText().get(0).toString().contains("微信：图片已保存")) {//保存数据库,清除图片
                    lastSteep = steep;
                    steep = 5;
                    save();
//                    click(qr_set());
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root != null) {
                        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("清除金额");
                        if (node != null && node.size() > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                click(node.get(0));
                                debug(TAG, "click-------->清除金额");
                            }
                        }
                    }
                }
                break;
        }
        debug(TAG, "onAccessibilityEvent: --------------------------------------------------------------------- ");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        db();
    }

    private long lastReStart = 0;
    private int reStartNum = 0;

    private void db() {
        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        AlertDialog dialog = new AlertDialog.Builder(WechatAccessibilityService.this)
                                .setMessage("程序出现异常，自动重启5次未解决，请手动重启。")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        if (Build.VERSION.SDK_INT >= 26)
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        else
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                        dialog.show();
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Long>() {
                    Disposable d;

                    @Override
                    public void onSubscribe(Disposable d) {
                        this.d = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (users.size() > 0) {
                            long id = DB.insert(WechatAccessibilityService.this, users.poll());
                            debug(TAG, "=========DB========: id = " + id);
                            reStartNum = 0;
                        } else {
                            long currTime = System.currentTimeMillis();
                            User last = DB.queryLast(WechatAccessibilityService.this, TYPE_WeChat,getAccount(),singleMode?2:1);
                            if (last != null) {
                                long lastTime = last.saveTime;
                                if (currTime - Math.max(lastTime, lastReStart) > 30 * 1000) {
                                    if (++reStartNum > 5) {
                                        debug(TAG, "已连续重启5次，不能正常运行，请手动重启。");
//                                        disableSelf();
//                                        onComplete();
                                        return;
                                    }
                                    lastReStart = currTime;
                                    debug(TAG, "=========DB========: 半分钟未检测到新数据，重新启动系统");
                                    Intent intent = new Intent(WechatAccessibilityService.this, InitActivity.class);
                                    intent.putExtra("reStart", TYPE_WeChat);
                                    WechatAccessibilityService.this.startActivity(intent);
                                } else {
                                    debug(TAG, "=========DB========: list is null");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        d.dispose();
                    }

                    @Override
                    public void onComplete() {
                        d.dispose();
                    }
                });
    }
}