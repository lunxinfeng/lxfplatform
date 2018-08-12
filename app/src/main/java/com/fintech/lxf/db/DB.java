package com.fintech.lxf.db;

import android.content.Context;

import java.util.List;

public class DB {
    public static long insert(Context context, User user){
        return AppDatabase.getInstance(context).userDao().insertUser(user);
    }

    public static User queryLast(Context context,int type){
        return AppDatabase.getInstance(context).userDao().queryLast(type);
    }

    public static List<User> queryAll(Context context, int type){
        return AppDatabase.getInstance(context).userDao().queryAll(type);
    }

    public static int deleteAll(Context context, User... users){
        return AppDatabase.getInstance(context).userDao().delAll(users);
    }
    public static void deleteTable(Context context){
        AppDatabase.getInstance(context).userDao().delTable();
    }

    public static List<User> queryQrNull(Context context, int type){
        return AppDatabase.getInstance(context).userDao().queryQrNull(type);
    }

}
