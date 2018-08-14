package com.fintech.lxf.helper;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fintech.lxf.R;


/**
 * <p/>
 * Created by lxf on 2016/7/29.
 */
public class SnackUtil {
    /**
     * 蓝色背景
     */
    public static final int Info = 1;
    /**
     * 绿色背景
     */
    public static final int Confirm = 2;
    /**
     * 黄色背景
     */
    public static final int Warning = 3;
    /**
     * 红色背景
     */
    public static final int Alert = 4;


    public static int red = 0xfff44336;
    public static int green = 0xff4caf50;
    public static int blue = 0xff2195f3;
    public static int orange = 0xffffc107;

    /**
     * 短显示Snackbar，自定义颜色
     *
     * @param view
     * @param message
     * @param messageColor
     * @param backgroundColor
     * @return
     */
    public static Snackbar ShortSnackbar(View view, String message, int messageColor, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        setSnackbarColor(snackbar, messageColor, 0, 0, backgroundColor);
        return snackbar;
    }

    /**
     * 长显示Snackbar，自定义颜色
     *
     * @param view
     * @param message
     * @param messageColor
     * @param backgroundColor
     * @return
     */
    public static Snackbar LongSnackbar(View view, String message, int messageColor, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        setSnackbarColor(snackbar, messageColor, 0, 0, backgroundColor);
        return snackbar;
    }

    /**
     * 自定义时常显示Snackbar，自定义颜色
     *
     * @param view
     * @param message
     * @param messageColor
     * @param backgroundColor
     * @return
     */
    public static Snackbar IndefiniteSnackbar(View view, String message, int messageColor, int actionColor, int actionColor_focus, int backgroundColor, String action, View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(action, clickListener);
        setSnackbarColor(snackbar, messageColor, actionColor, actionColor_focus, backgroundColor);
        return snackbar;
    }

    /**
     * 短显示Snackbar，可选预设类型
     *
     * @param view
     * @param message
     * @param type
     * @return
     */
    public static Snackbar ShortSnackbar(View view, String message, int type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        switchType(snackbar, type);
        return snackbar;
    }

    /**
     * 长显示Snackbar，可选预设类型
     *
     * @param view
     * @param message
     * @param type
     * @return
     */
    public static Snackbar LongSnackbar(View view, String message, int type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        switchType(snackbar, type);
        return snackbar;
    }

    /**
     * 自定义时常显示Snackbar，可选预设类型
     *
     * @param view
     * @param message
     * @param type
     * @return
     */
    public static Snackbar IndefiniteSnackbar(View view, String message, int type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        switchType(snackbar, type);
        return snackbar;
    }

    //选择预设类型
    private static void switchType(Snackbar snackbar, int type) {
        switch (type) {
            case Info:
                setSnackbarColor(snackbar, blue);
                break;
            case Confirm:
                setSnackbarColor(snackbar, green);
                break;
            case Warning:
                setSnackbarColor(snackbar, orange);
                break;
            case Alert:
                setSnackbarColor(snackbar, Color.YELLOW, 0, 0, red);
                break;
        }
    }

    /**
     * 设置Snackbar背景颜色
     *
     * @param snackbar
     * @param backgroundColor 背景颜色  0表示用默认颜色
     */
    public static void setSnackbarColor(Snackbar snackbar, int backgroundColor) {
        View view = snackbar.getView();
        if (view != null) {
            if (backgroundColor != 0) view.setBackgroundColor(backgroundColor);
        }
    }

    /**
     * 设置Snackbar文字和背景颜色
     *
     * @param snackbar          snackbar
     * @param messageColor      文本信息颜色  0表示用默认颜色
     * @param actionColor       action颜色  0表示用默认颜色
     * @param actionColor_focus action获得焦点的颜色  0表示没有
     * @param backgroundColor   背景颜色  0表示用默认颜色
     */
    public static void setSnackbarColor(Snackbar snackbar, int messageColor, final int actionColor, final int actionColor_focus, int backgroundColor) {
        View view = snackbar.getView();
        TextView message = (TextView) view.findViewById(R.id.snackbar_text);
        final Button action = (Button) view.findViewById(R.id.snackbar_action);
        if (actionColor_focus == 0) {
            if (actionColor != 0) {
                snackbar.setActionTextColor(actionColor);
            }
        } else {
//            snackbar.setActionTextColor(colorStateList);
            action.setClickable(true);
            action.setFocusable(true);
            action.setTextColor(actionColor);
            action.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        action.setTextColor(actionColor_focus);
                    else
                        action.setTextColor(actionColor);
                }
            });
        }

        if (backgroundColor != 0) view.setBackgroundColor(backgroundColor);
        if (messageColor != 0) message.setTextColor(messageColor);

    }
}
