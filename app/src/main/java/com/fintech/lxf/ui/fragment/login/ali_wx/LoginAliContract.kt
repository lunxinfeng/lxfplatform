package com.fintech.lxf.ui.fragment.login.ali_wx

import android.app.Activity
import com.fintech.lxf.base.BaseView
import com.fintech.lxf.base.BasePresenter

interface LoginAliContract {
    interface View: BaseView {
        val context: Activity

        fun loginSuccess()
        fun loginFail(hint:String)
    }

    interface Presenter: BasePresenter {
        fun aliLogin()
        fun wechatLogin()
    }
}