package com.fintech.lxf.ui.activity.init

import android.app.Activity
import com.fintech.lxf.base.BaseView
import com.fintech.lxf.base.BasePresenter

interface InitContract {
    interface View: BaseView {
        val context: Activity

        fun uploadComplete(success:Boolean,singleMode:Boolean = false)
        fun serverRefuseUpload(singleMode:Boolean = false)
        /**
         * 单额打码模式
         */
        fun singleAmountMode(enter:Boolean)
        fun addSingleAmount()
    }

    interface Presenter: BasePresenter {
        fun getMoreUsedAmount()
        fun startAli()
        fun writeToCSV(type:String)
        fun upload()
        fun getLastFromSql()
        /**
         * @param clear 是否清除本地数据
         */
        fun exitAccount(clear:Boolean)
    }
}