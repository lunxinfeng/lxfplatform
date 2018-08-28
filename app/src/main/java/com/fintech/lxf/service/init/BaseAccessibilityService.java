package com.fintech.lxf.service.init;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fintech.lxf.db.DB;
import com.fintech.lxf.db.User;
import com.fintech.lxf.helper.AliPayUI;
import com.fintech.lxf.helper.QrCodeParser;
import com.fintech.lxf.helper.SPHelper;
import com.fintech.lxf.helper.WechatUI;
import com.fintech.lxf.ui.activity.init.InitActivity;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.helper.ExpansionKt.debug;

public abstract class BaseAccessibilityService extends AccessibilityService {
    public static int startPos = 1;
    public static int endPos = 3000;
    public static int offsetTotal = 5;
    public static final int TYPE_ALI = 1;
    public static final int TYPE_WeChat = 2;
    public static boolean singleMode = false;
    public static LinkedList<Integer> singleSet = new LinkedList<>();
    public static int singleCurr = -1;
    protected String currClass;
    private Lock lock = new ReentrantLock();
    protected LinkedList<User> users = new LinkedList<>();
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected boolean isFinish = true;

    public static final String TAG = BaseAccessibilityService.class.getSimpleName();

    @Override
    protected void onServiceConnected() {
        debug(TAG, "onServiceConnected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        debug(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        debug(TAG, "onRebind");
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

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            clearLocalPic();
        }finally {
            posAddAdd();
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
            user.pos_start = startPos;
            user.offset_total = offsetTotal;
            user.qr_str = s;
            user.type = getType();
            user.mode = singleMode?2:1;
            user.amount = (user.pos_curr * user.multiple - user.offset) / 100.0;

            users.offer(user);
//            long id = DB.insert(this, user);
//            debug(TAG, "save: s = " + s);
//            debug(TAG, "save: id = " + id);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "解析图片出错");
            User user = new User();
            user.account = SPHelper.getInstance().getString(AliPayUI.acc);
            user.offset = getOffsetV();
            user.multiple = getbeishu();
            user.pos_curr = getPosV();
            user.pos_end = getEndV();
            user.pos_start = startPos;
            user.offset_total = offsetTotal;
            user.qr_str = null;
            user.type = getType();
            user.mode = singleMode?2:1;
            user.amount = (user.pos_curr * user.multiple - user.offset) / 100.0;

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
        putPosV(startPos);
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

    protected void input() {
        input(0);
    }

    public void input(int num) {
        num++;
        if (num > 2)
            return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            final int finalNum = num;
            Single.timer(200, TimeUnit.MILLISECONDS)
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            input(finalNum);
                        }
                    });
            return;
        }
        List<AccessibilityNodeInfo> etContent = root.findAccessibilityNodeInfosByViewId(amount_et());
        if (etContent == null || etContent.size() == 0) {
            final int finalNum1 = num;
            Single.timer(200, TimeUnit.MILLISECONDS)
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

    protected void click(String view) {
        debug(TAG, "onAccessibilityEvent: event: " + view + "===");
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> node = root.findAccessibilityNodeInfosByViewId(view);
        if (node == null || node.size() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getType() == TYPE_WeChat) {
            click(node.get(0));
        } else {
            node.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }


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

    protected boolean isFinish() {
        int posV = getPosV();

        if (posV > endPos) {
            if (getOffsetV() >= offsetTotal - 1) {
                if (singleMode){
                    if (singleSet.size() == 0){
                        stop();
                        return true;
                    }else{
                        singleCurr = singleSet.poll();
                        startPos = singleCurr;
                        putPosV(startPos);
                        endPos = singleCurr;
                        putOffsetV(0);
                        return false;
                    }
                }else{
                    stop();
                    return true;
                }
            } else {
                resetPos();
            }
        }
        return false;
    }

//    protected void checkEffective() {
//        Observable
//                .create(new ObservableOnSubscribe<List<User>>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<List<User>> emitter) throws Exception {
//                        List<User> users = DB.queryQrNull(BaseAccessibilityService.this, getType());
//                        if (users != null)
//                            emitter.onNext(users);
//                        emitter.onComplete();
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Function<List<User>, ObservableSource<User>>() {
//                    @Override
//                    public ObservableSource<User> apply(List<User> users) throws Exception {
//                        return null;
//                    }
//                })
//                .subscribe(new Observer<User>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(User user) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        List<User> users = DB.queryQrNull(BaseAccessibilityService.this, getType());
//                        if (users == null)
//                            stop();
//                        else
//                            checkEffective();
//                    }
//                });
//    }

    protected void stop() {
        Observable
                .create(new ObservableOnSubscribe<List<User>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<User>> emitter) throws Exception {
                        SystemClock.sleep(3000);
                        Intent intent = new Intent(BaseAccessibilityService.this, InitActivity.class);
                        BaseAccessibilityService.this.startActivity(intent);
                        List<User> users = DB.queryAll(BaseAccessibilityService.this, getType(),getAccount());
                        if (users != null)
                            emitter.onNext(users);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    Disposable d;

                    @Override
                    public void onSubscribe(Disposable d) {
                        this.d = d;
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(final List<User> users) {
                        saveToCSV(users);
                        Log.d(TAG, "onNext: 程序初始化完毕,共存储数据 " + users.size());
                        isFinish = true;
//                        showResult(users);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        d.dispose();
                    }

                    @Override
                    public void onComplete() {
                        compositeDisposable.dispose();
                        disableSelf();
                    }
                });
    }

//    private void showResult(List<User> users) {
//        AlertDialog dialog = new AlertDialog.Builder(BaseAccessibilityService.this)
//                .setMessage("程序初始化完毕,共存储数据" + users.size() + "条。")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            Intent intent = new Intent(BaseAccessibilityService.this, InitActivity.class);
//                            BaseAccessibilityService.this.startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
//                .create();
//        if (Build.VERSION.SDK_INT >= 26){
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//        } else{
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        }
//
//        dialog.show();
//    }


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
        p.moveTo(position.x, position.y);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L));
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, gestureResultCallback, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        debug(TAG,"服务已被销毁onDestroy");
        super.onDestroy();
    }

    private void saveToCSV(List<User> users){
        Observable.fromIterable(users)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(User user) {
                        saveToCSV(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("BaseAccessibilityService.onComplete");
                    }
                });
//        compositeDisposable.add(d);
    }

    private void saveToCSV(User user){
        try {
            CSVWriter csvWriter = getCsvWriter();
            String[] re = new String[]{
                    SPHelper.getInstance().getString(AliPayUI.acc),
                    user.qr_str,
                    (user.pos_curr * user.multiple - user.offset) / 100.0 + ""};
//            debug(TAG,"csv:" + Arrays.toString(re));
            csvWriter.writeNext(re);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private CSVWriter getCsvWriter() throws IOException {
        String filePath = Environment.getExternalStorageDirectory() + "/a_match_pay/ali-" + SPHelper.getInstance().getString(AliPayUI.acc) + "-" + getOffsetV() + "-all" + ".txt";
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath, true), "GBK");
        return new CSVWriter(writer);
    }
}