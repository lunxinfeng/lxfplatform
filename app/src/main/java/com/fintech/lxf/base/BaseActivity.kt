package com.fintech.lxf.base

import android.support.v7.app.AppCompatActivity
import com.fintech.lxf.helper.toast


abstract class BaseActivity: AppCompatActivity(),BaseView{
    override fun showHint(content: String) {
        toast(content)
    }
}