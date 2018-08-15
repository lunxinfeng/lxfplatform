package com.fintech.lxf.net;

import android.content.Context;
import android.content.DialogInterface;

import com.fintech.lxf.R;
import com.fintech.lxf.ui.dlg.WaitDialog;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by lxf on 2016/10/16.
 */
public abstract class ProgressSubscriber<T> implements Observer<T> {
    //    弱引用防止内存泄露
    private WeakReference<Context> mActivity;
    //    是否能取消请求
    private boolean cancel;
    //    是否显示请求框
    private boolean show;
    //    加载框
    private WaitDialog waitDialog;
    private Disposable mDisposable;

    /**
     * 默认显示且不可取消 加载框
     */
    protected ProgressSubscriber(Context context) {
        this.mActivity = new WeakReference<>(context);
        this.show = true;
        this.cancel = false;
        initProgressDialog(null);
    }

    protected ProgressSubscriber(Context context, String msg) {
        this.mActivity = new WeakReference<>(context);
        this.show = true;
        this.cancel = false;
        initProgressDialog(msg);
    }

    public ProgressSubscriber(Context context, boolean show, boolean cancel) {
        this.mActivity = new WeakReference<>(context);
        this.show = show;
        this.cancel = cancel;
        initProgressDialog(null);
    }


    /**
     * 初始化加载框
     */
    private void initProgressDialog(String msg) {
        Context context = mActivity.get();
        if (waitDialog == null && context != null && show) {
            if (msg == null)
                waitDialog = new WaitDialog(context, R.style.dialog);
            else
                waitDialog = new WaitDialog(context, R.style.dialog, msg);
            waitDialog.setCancelable(cancel);
            if (cancel) {
                waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        onCancelProgress();
                    }
                });
            }
        }
    }


    /**
     * 显示加载框
     */
    private void showProgressDialog() {
        Context context = mActivity.get();
        if (waitDialog == null || context == null) return;
        if (!waitDialog.isShowing()) {
            waitDialog.show();
        }
    }


    /**
     * 隐藏dialog，并解除订阅，防止内存泄漏
     */
    private void dismissProgressDialog() {
        if (waitDialog != null && waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    /**
     * 最先调用
     *
     */
    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        showProgressDialog();
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onComplete() {
        dismissProgressDialog();
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     */
    @Override
    public void onError(Throwable e) {
        Context context = mActivity.get();
        if (context == null) return;
        if (e instanceof SocketTimeoutException) {
            _onError("网络连接超时");
        } else if (e instanceof ConnectException) {
            _onError("网络连接超时");
        } else if (e instanceof ServerException) {
            _onError(e.getMessage());
        } else {
            _onError(e.getMessage());
        }
        dismissProgressDialog();
    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    private void onCancelProgress() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    public abstract void _onNext(T t);

    public abstract void _onError(String error);
}