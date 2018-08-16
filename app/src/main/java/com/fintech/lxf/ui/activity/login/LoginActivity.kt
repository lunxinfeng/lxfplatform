package com.fintech.lxf.ui.activity.login

import android.content.Intent
import android.os.Bundle
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.net.Configuration
import com.fintech.lxf.net.Constants
import com.fintech.lxf.ui.activity.config.ConfigActivity
import com.fintech.lxf.ui.fragment.login.account.LoginAccountFragment
import com.fintech.lxf.ui.fragment.login.ali.LoginAliFragment

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        if (Configuration.noAddress()) {
            startActivity(Intent(this,ConfigActivity::class.java))
            finish()
            return
        } else {
            Constants.baseUrl = Configuration.getUserInfoByKey(Constants.KEY_ADDRESS)
        }

        fragmentManager
                .beginTransaction()
                .add(R.id.frame_content,LoginAliFragment.newInstance())
                .commit()
    }

    override fun onBackPressed() {
        val fragment = fragmentManager.findFragmentById(R.id.frame_content)
        if (fragment is LoginAccountFragment)
            fragment.back()
        else
            super.onBackPressed()
    }
}
