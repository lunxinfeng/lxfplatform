package com.fintech.lxf.base

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fintech.lxf.helper.toast


abstract class BaseActivity: AppCompatActivity(),BaseView{
    companion object {
        val activitys = HashMap<String, Activity>()
    }

    override fun showHint(content: String) {
        toast(content)
    }

    private fun addActivity(){
        activitys[javaClass.simpleName] = this
    }

    private fun removeActivity(){
        activitys.remove(javaClass.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addActivity()
    }

    override fun onDestroy() {
        removeActivity()
        super.onDestroy()
    }
}