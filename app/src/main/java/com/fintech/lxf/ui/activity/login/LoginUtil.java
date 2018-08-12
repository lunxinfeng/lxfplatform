package com.fintech.lxf.ui.activity.login;


import android.content.Context;


import com.fintech.lxf.db.DB;
import com.fintech.lxf.net.Configuration;

import java.util.Map;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fintech.lxf.net.Constants.*;

public class LoginUtil {

    public static void delLocalData(final Context context) {
        Single.just(1)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        DB.deleteTable(context);
                        Configuration.removeUserInfoByKey(KEY_ALLOW_LOAD);
                        Configuration.removeUserInfoByKey(KEY_BEGIN_NUM);
                        Configuration.removeUserInfoByKey(KEY_END_NUM);
                        Configuration.removeUserInfoByKey(KEY_MAX_NUM);
                    }
                });
    }


    public static void saveData(Map<String, String> result) {
        Configuration.putUserInfo(KEY_USER_NAME, result.get(KEY_USER_NAME));
        Configuration.putUserInfo(KEY_MCH_ID, result.get(KEY_MCH_ID));
        Configuration.putUserInfo(KEY_LOGIN_TOKEN, result.get(KEY_LOGIN_TOKEN));

        Configuration.putUserInfo(KEY_ALLOW_LOAD, result.get(KEY_ALLOW_LOAD));
        Configuration.putUserInfo(KEY_BEGIN_NUM, result.get(KEY_BEGIN_NUM));
        Configuration.putUserInfo(KEY_END_NUM, result.get(KEY_END_NUM));
        Configuration.putUserInfo(KEY_MAX_NUM, result.get(KEY_MAX_NUM));
    }
}
