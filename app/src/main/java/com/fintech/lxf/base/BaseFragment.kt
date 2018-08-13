package com.fintech.lxf.base

import android.app.Fragment
import cn.izis.yztv.base.BaseView
import com.fintech.lxf.helper.toast


abstract class BaseFragment: Fragment(),BaseView {
    override fun showHint(content: String) {
        activity.toast(content)
    }
}