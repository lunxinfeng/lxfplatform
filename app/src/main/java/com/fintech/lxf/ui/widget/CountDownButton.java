package com.fintech.lxf.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class CountDownButton extends android.support.v7.widget.AppCompatButton {
    private OnClickWithCountDown onClickWithCountDownListener;
    private int startTime = 5;
    private int currTime;
    private Timer timer;

    public CountDownButton(Context context) {
        super(context);
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnabled(false);
                final String content = getText().toString();
                currTime = startTime;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                setText(content + "(" + currTime + ")");
                                currTime--;
                                if (currTime<0){
                                    setText(content);
                                    setEnabled(true);
                                    timer.cancel();
                                }
                            }
                        });
                    }
                },0,1000);
                if (onClickWithCountDownListener!=null)
                    onClickWithCountDownListener.onClick(v);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (timer!=null)
            timer.cancel();
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setOnClickWithCountDownListener(OnClickWithCountDown onClickWithCountDownListener) {
        this.onClickWithCountDownListener = onClickWithCountDownListener;
    }

    public interface OnClickWithCountDown{
        void onClick(View v);
    }
}
