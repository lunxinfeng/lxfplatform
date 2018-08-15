package com.fintech.lxf.ui.activity.config

import android.content.Context
import com.fintech.lxf.base.BaseView
import com.fintech.lxf.base.BasePresenter


interface ConfigContract {
    interface View: BaseView {
        val context: Context

        fun checkSuccess(hint:String)
        fun checkFail(hint:String)
    }

    interface Presenter: BasePresenter {
        fun check(address:String)
    }
}