package com.fintech.lxf.service.init2;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.db.DB;
import com.fintech.lxf.db.User;
import com.fintech.lxf.helper.AliPayUI;
import com.fintech.lxf.helper.QrCodeParser;
import com.fintech.lxf.helper.SPHelper;
import com.fintech.lxf.helper.WechatUI;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.ExpansionKt.debug;

public abstract class BaseAccessibilityService extends AccessibilityService {
    public static final int TYPE_ALI = 1;
    public static final int TYPE_WeChat = 2;
    protected String currClass;
    private Lock lock = new ReentrantLock();
    protected LinkedList<User> users = new LinkedList<>();

    public static final String TAG = BaseAccessibilityService.class.getSimpleName();

    @Override
    protected void onServiceConnected() {
        debug(TAG, "onServiceConnected");
    }

    protected String getAccount() {
        switch (getType()) {
            case TYPE_ALI:
                return SPHelper.getInstance().getString(AliPayUI.acc);
            case TYPE_WeChat:
                return SPHelper.getInstance().getString(WechatUI.acc);
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected int getPosV() {
        switch (getType()) {
            case TYPE_ALI:
                return SPHelper.getInstance().getInt(AliPayUI.posV);
            case TYPE_WeChat:
                return SPHelper.getInstance().getInt(WechatUI.posV);
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected int getOffsetV() {
        switch (getType()) {
            case TYPE_ALI:
                return SPHelper.getInstance().getInt(AliPayUI.offsetV);
            case TYPE_WeChat:
                return SPHelper.getInstance().getInt(WechatUI.offsetV);
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected int getEndV() {
        switch (getType()) {
            case TYPE_ALI:
                return SPHelper.getInstance().getInt(AliPayUI.endV);
            case TYPE_WeChat:
                return SPHelper.getInstance().getInt(WechatUI.endV);
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected File getPicFile() {
        switch (getType()) {
            case TYPE_ALI:
                return new File(AliPayUI.getPicPath());
            case TYPE_WeChat:
                return new File(WechatUI.getPicPath());
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected int getbeishu() {
        switch (getType()) {
            case TYPE_ALI:
                return SPHelper.getInstance().getInt(AliPayUI.beishuV);
            case TYPE_WeChat:
                return SPHelper.getInstance().getInt(WechatUI.beishuV);
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected double getAmount() {
        return (getPosV() * getbeishu() - getOffsetV()) / 100.0;
    }

    protected void putPosV(int posV) {
        switch (getType()) {
            case TYPE_ALI:
                SPHelper.getInstance().putInt(AliPayUI.posV, posV);
                break;
            case TYPE_WeChat:
                SPHelper.getInstance().putInt(WechatUI.posV, posV);
                break;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected void putOffsetV(int offsetV) {
        switch (getType()) {
            case TYPE_ALI:
                SPHelper.getInstance().putInt(AliPayUI.offsetV, offsetV);
                break;
            case TYPE_WeChat:
                SPHelper.getInstance().putInt(WechatUI.offsetV, offsetV);
                break;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected abstract int getType();

    protected String amount_et() {
        switch (getType()) {
            case TYPE_ALI:
                return AliPayUI.et_content;
            case TYPE_WeChat:
                return WechatUI.et_bx;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected String amount_btnSure() {
        switch (getType()) {
            case TYPE_ALI:
                return AliPayUI.btn_next;
            case TYPE_WeChat:
                return WechatUI.btn_sure;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    protected String qr_set() {
        switch (getType()) {
            case TYPE_ALI:
                return AliPayUI.btn_Modify_money;
            case TYPE_WeChat:
                return WechatUI.btn_set;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }
    protected String qr_save() {
        switch (getType()) {
            case TYPE_ALI:
                return AliPayUI.btn_save_qrcode;
            case TYPE_WeChat:
                return WechatUI.btn_save;
            default:
                throw new RuntimeException("必须是微信或者支付宝");
        }
    }

    public boolean save() {
        File picFile = getPicFile();
        File[] files = picFile.listFiles();
        if (files == null || files.length != 1) {
            clearLocalPic();
            return false;
        }
        Arrays.sort(files);
        try {
            save(files);
            posAddAdd();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            clearLocalPic();
        }
        return false;
    }

    //存入数据库
    public void save(final File[] files) throws Exception {
        String s;
        try {
            s = decodeImg(files[0]);
            User user = new User();
            user.account = SPHelper.getInstance().getString(AliPayUI.acc);
            user.offset = getOffsetV();
            user.multiple = getbeishu();
            user.pos_curr = getPosV();
            user.pos_end = getEndV();
            user.qr_str = s;
            user.type = getType();

            users.offer(user);
//            long id = DB.insert(this, user);
//            debug(TAG, "save: s = " + s);
//            debug(TAG, "save: id = " + id);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG,"解析图片出错");
            User user = new User();
            user.account = SPHelper.getInstance().getString(AliPayUI.acc);
            user.offset = getOffsetV();
            user.multiple = getbeishu();
            user.pos_curr = getPosV();
            user.pos_end = getEndV();
            user.qr_str = null;
            user.type = getType();

            users.offer(user);
//            long id = DB.insert(this, user);
//            debug(TAG, "save: s = " + null);
//            debug(TAG, "save: id = " + id);
        }
    }


    private String decodeImg(File file) throws Exception {
        return QrCodeParser.decodeImg(file.getPath());
    }

    public void posAddAdd() {
        int posV = getPosV();
        posV++;
        putPosV(posV);
    }

    public void resetPos() {
        putPosV(1);
        offsetAddAdd();
    }

    public void offsetAddAdd() {
        int offsetV = getOffsetV();
        offsetV++;
        putOffsetV(offsetV);
    }

    public void clearLocalPic() {
        File file = getPicFile();
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
//            deletePic(getApplicationContext(), f.getPath());
            f.delete();
        }
//        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{getPicPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
//            public void onScanCompleted(String path, Uri uri) {
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                mediaScanIntent.setData(uri);
//                getApplicationContext().sendBroadcast(mediaScanIntent);
//            }
//        });
//
//        SPHelper.getInstance().putInt(AliPayUI.startV, getPosV());//重新开始
    }

    public void deletePic(Context context, String picPath) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        String where = MediaStore.Images.Media.DATA + "='" + picPath + "'";
        //删除图片
        mContentResolver.delete(uri, where, null);


    }

    protected void input(){
        input(0);
    }
    public void input(int num) {
        num++;
        if (num>2)
            return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root ==null){
            final int finalNum = num;
            Single.timer(200,TimeUnit.MILLISECONDS)
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            input(finalNum);
                        }
                    });
            return;
        }
        List<AccessibilityNodeInfo> etContent = root.findAccessibilityNodeInfosByViewId(amount_et());
        if (etContent == null || etContent.size() == 0){
            final int finalNum1 = num;
            Single.timer(200,TimeUnit.MILLISECONDS)
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            input(finalNum1);
                        }
                    });
            return;
        }
        AccessibilityNodeInfo nodeInfo = etContent.get(0);

        performEditText(nodeInfo, getAmount() + "");
    }

    protected void click(String view){
        click(view,0);
    }
    protected void click(final String view, int num){
        debug(TAG, "onAccessibilityEvent: event: " + view + "===" + num);
        num++;
        if (num>2)
            return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null){
//            final int finalNum = num;
//            Single.timer(500,TimeUnit.MILLISECONDS)
//                    .subscribe(new Consumer<Long>() {
//                        @Override
//                        public void accept(Long aLong) throws Exception {
//                            click(view, finalNum);
//                        }
//                    });
            return;
        }
        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByViewId(view);
        if (node == null || node.size() == 0) {
//            final int finalNum = num;
//            Single.timer(500,TimeUnit.MILLISECONDS)
//                    .subscribe(new Consumer<Long>() {
//                        @Override
//                        public void accept(Long aLong) throws Exception {
//                            click(view, finalNum);
//                        }
//                    });
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getType() == TYPE_WeChat) {
            click(node.get(0));
        }else{
            node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }


//    public void reset() throws InterruptedException {
//        AccessibilityNodeInfo root = getRootInActiveWindow();
//        if (root == null) {
//            return;
//        }
//        List<AccessibilityNodeInfo> clear = root.findAccessibilityNodeInfosByText("清除金额");
//        if (clear == null || clear.size() == 0) {
//            return;
//        }
//        clear.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        Thread.sleep(100);
//
//        List<AccessibilityNodeInfo> set = root.findAccessibilityNodeInfosByText("设置金额");
//        if (set == null || set.size() == 0) {
//            return;
//        }
//        set.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//
//        AliPayUI.steep = 1;
//    }

    public void performEditText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT, AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
            arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, true);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY, arguments);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    @Override
    public void onInterrupt() {
        debug(TAG, "onInterrupt");
    }

    protected boolean isFinish(){
        int posV = getPosV();
        int endV = getEndV();

        if (posV * getbeishu() > endV) {
            if (getOffsetV() >= 4) {
                stop();
                return true;
            } else {
                resetPos();
            }
        }
        return false;
    }

//    protected void next(){
//        try {
//            lock.lock();
//            AccessibilityNodeInfo root = getRootInActiveWindow();
//            final List<AccessibilityNodeInfo> clear = root.findAccessibilityNodeInfosByViewId(qr_set());//清除金额
//            final List<AccessibilityNodeInfo> save = root.findAccessibilityNodeInfosByViewId(qr_save());//保存图片
//            final List<AccessibilityNodeInfo> set = root.findAccessibilityNodeInfosByViewId(qr_set());//设置金额
//            if (clear != null && clear.size() != 0 && clear.get(0).getText().toString().equals("清除金额")) {
//                Observable.just(1)
//                        .subscribeOn(Schedulers.io())
//                        .doOnNext(new Consumer<Integer>() {
//                            @Override
//                            public void accept(Integer integer) throws Exception {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getType() == TYPE_WeChat) {
//                                    click(save.get(0));
//                                } else {
//                                    save.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                                }
//                            }
//                        })
//                        .delay(200, TimeUnit.MILLISECONDS)
//                        .doOnNext(new Consumer<Integer>() {
//                            @Override
//                            public void accept(Integer integer) throws Exception {
//                                save();
//                            }
//                        })
//                        .subscribe(new Observer<Integer>() {
//                            Disposable d;
//
//                            @Override
//                            public void onSubscribe(Disposable d) {
//                                this.d = d;
//                            }
//
//                            @Override
//                            public void onNext(Integer integer) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getType() == TYPE_WeChat) {
//                                    click(clear.get(0));
//                                } else {
//                                    clear.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                                }
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                e.printStackTrace();
//                                d.dispose();
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                d.dispose();
//                            }
//                        });
//            } else if (set != null && set.size() != 0) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getType() == TYPE_WeChat) {
//                    click(set.get(0));
//                } else {
//                    set.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            lock.unlock();
//        }
//    }

    protected void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf();
        }
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        List<User> users = DB.queryAll(BaseAccessibilityService.this, getType());
                        if (users != null)
                            emitter.onNext(users.size());
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    Disposable d;

                    @Override
                    public void onSubscribe(Disposable d) {
                        this.d = d;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: 程序初始化完毕,共存储数据 " + integer);
                        AlertDialog dialog = new AlertDialog.Builder(BaseAccessibilityService.this)
                                .setMessage("程序初始化完毕,共存储数据" + integer + "条。")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            PackageManager packageManager = BaseAccessibilityService.this.getApplicationContext().getPackageManager();
                                            Intent intent = packageManager.
                                                    getLaunchIntentForPackage("com.fintech.match.pay");
                                            BaseAccessibilityService.this.startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .create();
                        if (Build.VERSION.SDK_INT >= 26)
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        else
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                        dialog.show();
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void click(final AccessibilityNodeInfo node) {
        click(node, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "click: " + node.getText());
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "click: cancel " + node.getText());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void click(AccessibilityNodeInfo node, GestureResultCallback gestureResultCallback) {
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        Point position = new Point(rect.centerX(), rect.centerY());
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(position.x, position.y + 200);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L));
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, gestureResultCallback, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        disableSelf();
        super.onDestroy();
    }
}