package com.fintech.lxf.service.init2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.db.DB;
import com.fintech.lxf.db.User;
import com.fintech.lxf.helper.AliPayUI;
import com.fintech.lxf.ui.activity.InitActivity;
import com.opencsv.CSVWriter;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.AliPayUI.steep;
import static com.fintech.lxf.helper.ExpansionKt.debug;


public class AlipayAccessibilityService extends BaseAccessibilityService {
    private int lastType;
    private int lastSteep;

    @Override
    protected int getType() {
        return TYPE_ALI;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isNonNormal(event)) {
            parserEvent(event);
            lastType = event.getEventType();
        }
    }

    private boolean isNonNormal(AccessibilityEvent event) {//会打开两次设置金额页面
        return lastType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && lastSteep == 5
                && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getClassName().toString().equals("com.alipay.mobile.payee.ui.PayeeQRActivity");
    }


    private synchronized void parserEvent(AccessibilityEvent event) {
        debug(TAG, "onAccessibilityEvent: event: " + event);
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                currClass = event.getClassName().toString();

                if (isFinish()) return;

                switch (currClass) {
                    case "com.eg.android.AlipayGphone.AlipayLogin"://首页
                        debug(TAG, "onAccessibilityEvent: 首页: " + steep);
                        if (steep == 0) {
                            lastSteep = steep;
                            steep = 1;

                            main(0);
                        }
                        break;
                    case "com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity":
                        debug(TAG, "onAccessibilityEvent: 设置金额页面: " + steep);
                        if (steep == 3) {//steep == 3网络不好时可能会发生
                            lastSteep = steep;
                            click(amount_btnSure());
                        }
                        if (steep == 0 ||steep == 1 || steep == 2) {
                            lastSteep = steep;
                            steep = 2;
//                            clearLocalPic();
                            input();
                            click(amount_btnSure());
                        }
                        break;
                    case "com.alipay.mobile.framework.app.ui.DialogHelper$APGenericProgressDialog":
                        debug(TAG, "onAccessibilityEvent: 设置金额加载框: " + steep);
                        if (steep == 2) {
                            lastSteep = steep;
                            steep = 3;
                        }
                        break;
                    case "com.alipay.mobile.payee.ui.PayeeQRActivity":
                        debug(TAG, "onAccessibilityEvent: 二维码页面: " + steep);
                        if (steep == 1) {
                            lastSteep = steep;
                            click(qr_set());
                        }
                        if (steep == 0 || steep == 3 || steep == 2) {//保存图片
                            lastSteep = steep;
                            steep = 4;

                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("清除金额");
                                if (node != null && node.size() > 0) {


                                    clearLocalPic();
                                    File file = getPicFile();
                                    File[] files = file.listFiles();
                                    debug(TAG, "本地图片数量：" + (files == null ? 0 : files.length));
                                    click(qr_save());
                                }

                                if (steep == 0) {
                                    List<AccessibilityNodeInfo> node_2 = root.findAccessibilityNodeInfosByText("设置金额");
                                    if (node_2 != null && node_2.size() > 0) {
                                        node_2.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        debug(TAG, "click-------->设置金额");
                                    }
                                }
                            }
                        }
                        break;
                    case "com.alipay.mobile.commonui.widget.APNoticePopDialog"://人气大爆发
                        AccessibilityNodeInfo root = getRootInActiveWindow();
                        if (root == null) {
                            return;
                        }
                        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_ensure);
                        if (node == null || node.size() == 0) {
                            return;
                        }
                        node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
                    if (root == null) return;
                    List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("设置金额");
                    if (node != null && node.size() > 0) {
                        node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        debug(TAG, "click-------->设置金额");
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
                        event.getText().get(0).toString().equals("已保存到系统相册")) {//保存数据库,清除图片
                    lastSteep = steep;
                    steep = 5;
                    save();
//                    click(qr_set());
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root == null) return;
                    List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("清除金额");
                    if (node != null && node.size() > 0)
                        node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
        }
        debug(TAG, "onAccessibilityEvent: --------------------------------------------------------------------- ");
    }

    private void main(int reStartNum) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            reStartNum++;
            SystemClock.sleep(3000);
            if (reStartNum < 3)
                main(reStartNum);
            return;
        }
        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("首页");
        if (node == null || node.size() == 0) {
            reStartNum++;
            SystemClock.sleep(3000);
            if (reStartNum < 3)
                main(reStartNum);
            return;
        }
        node.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        debug(TAG, "click-------->首页");
        SystemClock.sleep(1500);

        List<AccessibilityNodeInfo> node2 = root.findAccessibilityNodeInfosByText("收钱");
        if (node2 == null || node2.size() == 0) {
            reStartNum++;
            SystemClock.sleep(3000);
            if (reStartNum < 3)
                main(reStartNum);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            node2.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            debug(TAG, "click-------->收钱");
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        lastReStart = System.currentTimeMillis();
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
                        AlertDialog dialog = new AlertDialog.Builder(AlipayAccessibilityService.this)
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
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (users.size() > 0) {
                            long id = DB.insert(AlipayAccessibilityService.this, users.poll());
                            debug(TAG, "=========DB========: id = " + id);
                            reStartNum = 0;
                        } else {
                            long currTime = System.currentTimeMillis();
                            User last = DB.queryLast(AlipayAccessibilityService.this, TYPE_ALI);
                            if (last != null) {
                                long lastTime = last.saveTime;
                                if (currTime - Math.max(lastTime, lastReStart) > 30 * 1000) {
                                    if (++reStartNum > 3) {
                                        debug(TAG, "已连续重启3次，不能正常运行，请手动重启。");
                                        disableSelf();
                                        onComplete();
                                        return;
                                    }
                                    lastReStart = currTime;
                                    debug(TAG, "=========DB========: 半分钟未检测到新数据，重新启动系统");
                                    Intent intent = new Intent(AlipayAccessibilityService.this, InitActivity.class);
                                    intent.putExtra("reStart", TYPE_ALI);
                                    AlipayAccessibilityService.this.startActivity(intent);
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