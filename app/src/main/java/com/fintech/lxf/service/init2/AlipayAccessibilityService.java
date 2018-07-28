package com.fintech.lxf.service.init2;

import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.db.DB;
import com.fintech.lxf.db.User;
import com.fintech.lxf.helper.AliPayUI;
import com.fintech.lxf.ui.activity.InitActivity;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.AliPayUI.steep;
import static com.fintech.lxf.helper.ExpansionKt.debug;


public class AlipayAccessibilityService extends BaseAccessibilityService {
    private List<AccessibilityEvent> events = new ArrayList<>();

    @Override
    protected int getType() {
        return TYPE_ALI;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        if (events.size() == 0 && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
//            events.add(event);
//
//        if (events.size()>0){
//            AccessibilityEvent last = events.get(events.size() - 1);
//            switch (last.getEventType()){
//                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                    if (last.getClassName().equals("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity")){
//
//                    }
//            }
//        }
        parserEvent(event);
    }


//    private boolean isFirst(AccessibilityEvent event){
//
//    }

    private synchronized void parserEvent(AccessibilityEvent event) {
        debug(TAG, "onAccessibilityEvent: event: " + event);
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                currClass = event.getClassName().toString();

                if (isFinish()) return;

                switch (currClass) {
                    case "com.eg.android.AlipayGphone.AlipayLogin"://首页
                        debug(TAG, "onAccessibilityEvent: 首页: " + steep);
//                        click(AliPayUI.btn_money);
//                        SystemClock.sleep(200);
                        AccessibilityNodeInfo root = getRootInActiveWindow();
                        List<AccessibilityNodeInfo> node2 = root.findAccessibilityNodeInfosByViewId(AliPayUI.btn_money);
                        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByText("收钱");
                        if(node == null || node.size() == 0)return;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            click(node.get(0).getParent());
                            System.out.println(node.get(0).getParent().getViewIdResourceName());
                            click(node.get(0).getParent().getViewIdResourceName());
                        }
                        break;
                    case "com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity":
                        debug(TAG, "onAccessibilityEvent: 设置金额页面: " + steep);
                        if (steep == 3) {//steep == 3网络不好时可能会发生
                            click(amount_btnSure());
                        }
                        if (steep == 1) {
                            steep = 2;
                            clearLocalPic();
                            input();
                            click(amount_btnSure());
                        }
                        break;
                    case "com.alipay.mobile.framework.app.ui.DialogHelper$APGenericProgressDialog":
                        debug(TAG, "onAccessibilityEvent: 设置金额加载框: " + steep);
                        if (steep == 2) steep = 3;
                        break;
                    case "com.alipay.mobile.payee.ui.PayeeQRActivity":
                        debug(TAG, "onAccessibilityEvent: 二维码页面: " + steep);
                        if (steep == 1){
                            click(qr_set());
                        }
                        if (steep == 4 || steep == 3 || steep == 2) {//保存图片
                            steep = 4;

                            File file = getPicFile();
                            File[] files = file.listFiles();
                            debug(TAG, "本地图片数量：" + files.length);
                            click(qr_save());
                        }
                        break;
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                debug(TAG, "onAccessibilityEvent: 内容改变: " + steep);
                if (steep == 5) {
                    steep = 1;
                    click(qr_set());
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                debug(TAG, "onAccessibilityEvent: 通知改变: " + steep);
                if (event.getClassName().toString().contains("Toast") &&
                        event.getText().get(0).toString().contains("网络")) {//设置金额点击确定时，网络出错
                    if (steep == 3 || steep == 2)
                        click(amount_btnSure());
                    return;
                }
                if (steep != 4) return;
                if (event.getClassName().toString().contains("Toast") &&
                        event.getText().get(0).toString().equals("已保存到系统相册")) {//保存数据库,清除图片
                    steep = 5;
                    save();
                    click(qr_set());
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

    private void db(){
        Flowable.interval(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (users.size()>0){
                            long id = DB.insert(AlipayAccessibilityService.this, users.poll());
                            debug(TAG, "=========DB========: id = " + id);
                        }else {
                            long currTime = System.currentTimeMillis();
                            User last = DB.queryLast(AlipayAccessibilityService.this,TYPE_ALI);
                            if (last!=null){
                                long lastTime = last.saveTime;
                                if (currTime - lastTime > 30 *1000){
                                    debug(TAG, "=========DB========: 半分钟未检测到新数据，重新启动系统");
                                    Intent intent = new Intent(AlipayAccessibilityService.this, InitActivity.class);
                                    intent.putExtra("reStart",TYPE_ALI);
                                    AlipayAccessibilityService.this.startActivity(intent);
                                }else{
                                    debug(TAG, "=========DB========: list is null");
                                }
                            }
                        }
                    }
                });
    }
}