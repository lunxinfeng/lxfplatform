package com.fintech.lxf.ui.fragment.login.ali

import android.app.Activity
import cn.izis.yztv.base.BaseView
import com.fintech.lxf.base.BasePresenter

interface LoginAliContract {
    interface View: BaseView {
        val context: Activity

        fun loginSuccess()
        fun loginFail(hint:String)
    }

    interface Presenter: BasePresenter {
        fun aliLogin()
    }
}