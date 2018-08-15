package com.fintech.lxf.ui.fragment.login.account

import android.app.Activity
import com.fintech.lxf.base.BaseView
import com.fintech.lxf.base.BasePresenter

interface LoginAccountContract {
    interface View: BaseView {
        val context: Activity

        fun loginSuccess()
        fun loginFail(hint:String)
    }

    interface Presenter: BasePresenter {
        fun accountLogin(name:String,password:String)
    }
}