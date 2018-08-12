package com.fintech.lxf.ui.activity.login

import android.app.Activity
import android.content.Context
import cn.izis.yztv.base.BaseView
import com.fintech.lxf.base.BasePresenter

interface LoginContract {
    interface View: BaseView {
        val context: Activity

        fun loginSuccess()
        fun loginFail(hint:String)
    }

    interface Presenter: BasePresenter {
        fun accountLogin()
        fun aliLogin()
    }
}