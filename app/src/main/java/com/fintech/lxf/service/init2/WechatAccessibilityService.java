package com.fintech.lxf.service.init2;


import android.view.accessibility.AccessibilityEvent;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
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
//        debug(TAG, "onAccessibilityEvent: event: " + event);
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                currClass = event.getClassName().toString();

                if (isFinish()) return;

                switch (currClass) {
                    case "com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI":
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
                    case "com.tencent.mm.ui.base.p":
                        if (steep == 2) steep = 3;
                        break;
                    case "com.tencent.mm.plugin.collect.ui.CollectMainUI":
                        if (steep == 2 || steep == 3) {//保存图片
                            click(qr_save());
                        }
                        break;
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (steep == 4) {
                    steep = 1;
                    click(qr_set());
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (event.getClassName().toString().contains("Toast") &&
                        event.getText().get(0).toString().contains("微信：图片已保存")){
                    steep = 4;
                    Single.just(1)
                            .observeOn(Schedulers.io())
                            .doOnSuccess(new Consumer<Integer>() {
                                @Override
                                public void accept(Integer integer) throws Exception {
                                    save();
                                }
                            })
                            .subscribe(new SingleObserver<Integer>() {
                                Disposable d;
                                @Override
                                public void onSubscribe(Disposable d) {
                                    this.d = d;
                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    click(qr_set());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    d.dispose();
                                }
                            });
                }
                break;
        }
    }
}