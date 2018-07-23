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

    public static void deleteAll(Context context, User... users){
        AppDatabase.getInstance(context).userDao().delAll(users);
    }

//    public static long insert(Context context, User user){
//        return MatchPayDatabase.getInstance(context).userDao().insertUser(user);
//    }
//
//    public static User queryLast(Context context){
//        return MatchPayDatabase.getInstance(context).userDao().queryLast();
//    }
//
//    public static void deleteAll(Context context,User... users){
//        MatchPayDatabase.getInstance(context).userDao().delAll(users);
//    }
}
