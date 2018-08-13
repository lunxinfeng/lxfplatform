package com.fintech.lxf.ui.activity.init

import android.content.Context
import android.os.Environment
import com.fintech.lxf.db.DB
import com.fintech.lxf.db.User
import com.fintech.lxf.helper.AliPayUI
import com.fintech.lxf.helper.SPHelper
import com.fintech.lxf.net.Configuration
import com.fintech.lxf.net.Constants
import com.fintech.lxf.net.Constants.*
import com.fintech.lxf.service.init.BaseAccessibilityService
import com.opencsv.CSVWriter
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class InitModel {
    var last: User? = null
    var startType = 0

    companion object {
        val STRAT_TYPE_ERROR_NORMAL = 1000
        val STRAT_TYPE_KILL_BACKGROUND = 2000
    }

    fun reStart(){
        val acc = last?.account?:Configuration.getUserInfoByKey(Constants.KEY_USER_NAME)
        val pos = last?.pos_curr ?: BaseAccessibilityService.startPos
        val offset = last?.offset ?: 0
        val beishu = 100
        val end = last?.pos_end ?: BaseAccessibilityService.endPos

        SPHelper.getInstance().putString(AliPayUI.acc, acc)
        SPHelper.getInstance().putInt(AliPayUI.posV, pos)
        SPHelper.getInstance().putInt(AliPayUI.startV, pos)
        SPHelper.getInstance().putInt(AliPayUI.endV, end)
        SPHelper.getInstance().putInt(AliPayUI.offsetV, offset)
        SPHelper.getInstance().putInt(AliPayUI.beishuV, beishu)

        AliPayUI.steep = 0
    }

    fun delLocalCSV() {
        val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/"
        val file = File(filePath)
        if (file.isDirectory)
            file.listFiles()
                    .forEach { it.delete() }
    }

    fun writeToCSV(users: List<User>): MutableList<String> {
        val files = mutableListOf<String>()
        val n = users.size / 12000
        for (i in 0..n) {
            val start = i * 12000
            val end = (i + 1) * 12000
            val users_ = users.subList(start, if (end > users.size) users.size else end)

            val index = if (i == n) 10000 else i
            val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/ali-" +
                    SPHelper.getInstance().getString(AliPayUI.acc) + "-" + i + "-all" + ".txt"
            val writer = CSVWriter(OutputStreamWriter(FileOutputStream(filePath, true), "GBK"))

            users_
                    .map {
                        arrayOf(it.qr_str, ((it.pos_curr * it.multiple - it.offset) / 100.0).toString())
                    }
                    .forEach { writer.writeNext(it) }

            writer.close()

            files.add("$filePath;$index")
        }
        return files
    }

    fun clearQRData(context: Context){
        Single.just(1)
                .subscribeOn(Schedulers.io())
                .subscribe(Consumer {
                    DB.deleteTable(context)

                    Configuration.removeUserInfoByKey(KEY_ALLOW_LOAD)
                    Configuration.removeUserInfoByKey(KEY_BEGIN_NUM)
                    Configuration.removeUserInfoByKey(KEY_END_NUM)
                    Configuration.removeUserInfoByKey(KEY_MAX_NUM)
                })
    }

    fun clearUserInfo(){
        Configuration.clearUserInfo()
    }
}