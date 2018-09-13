package com.fintech.lxf.ui.activity.init

import android.app.Activity
import com.fintech.lxf.base.BaseView
import com.fintech.lxf.base.BasePresenter
import com.fintech.lxf.bean.MoreUsedBean
import com.fintech.lxf.db.User

interface InitContract {
    interface View: BaseView {
        val context: Activity

        fun uploadComplete(success:Boolean,singleMode:Boolean = false)
        fun serverRefuseUpload(singleMode:Boolean = false)
        fun showMoreUsedAmount(data:List<MoreUsedBean>)
        fun updateMoreUsedAmount(curr:Int)
        /**
         * 单额打码模式
         */
        fun singleAmountMode(enter:Boolean)
        fun addSingleAmount()
        fun stopAndUpload(users:List<User>)
    }

    interface Presenter: BasePresenter {
        fun getMoreUsedAmount()
        fun startAli()
        fun writeToCSV(type:String)
        fun upload()
        fun getLastFromSql()
        fun stopAndUpload()
        /**
         * @param clear 是否清除本地数据
         */
        fun exitAccount(clear:Boolean)
    }
}