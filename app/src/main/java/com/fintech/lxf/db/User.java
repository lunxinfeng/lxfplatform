package com.fintech.lxf.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(primaryKeys = {"account", "pos_curr", "offset", "mode"})
public class User {

    @NonNull
    public String account = "";//账号
    public double amount;
    public String qr_str;//二维码长串

    public int pos_start;//初始pos
    public int pos_curr;//当前pos

    @ColumnInfo
    public int pos_end;//总共pos

    @ColumnInfo
    public int offset;//偏移
    public int offset_total;//总并发数

    @ColumnInfo
    public int multiple;//倍数


    public int type;//1:支付宝  2:微信
    public int mode = 1;//1:正常模式  2:单额模式
    public String deviceId;//设备id

    public long saveTime;

    public User() {
        saveTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "User{" +
                "account='" + account + '\'' +
                ", amount=" + amount +
                ", qr_str='" + qr_str + '\'' +
                ", pos_start=" + pos_start +
                ", pos_curr=" + pos_curr +
                ", pos_end=" + pos_end +
                ", offset=" + offset +
                ", offset_total=" + offset_total +
                ", multiple=" + multiple +
                ", mode=" + type +
                ", mode=" + mode +
                ", deviceId='" + deviceId + '\'' +
                ", saveTime=" + saveTime +
                '}';
    }
}
