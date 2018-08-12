package com.fintech.lxf.ui.activity.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.net.Configuration
import com.fintech.lxf.net.Constants
import com.fintech.lxf.ui.activity.InitActivity
import com.fintech.lxf.ui.activity.config.ConfigActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(),LoginContract.View {

    private val presenter = LoginPresenter(this)

    override val context: Activity
        get() = this

    override fun loginSuccess() {
        startActivity(Intent(this, InitActivity::class.java))
        finish()
    }

    override fun loginFail(hint: String) {
        showHint(hint)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLoginAli.setOnClickListener { presenter.aliLogin() }
        tvLoginAccount.setOnClickListener { presenter.accountLogin() }
    }

    override fun onResume() {
        super.onResume()

        if (Configuration.noAddress()) {
            startActivity(Intent(this,ConfigActivity::class.java))
            finish()
        } else {
            Constants.baseUrl = Configuration.getUserInfoByKey(Constants.KEY_ADDRESS)
        }
    }
}
