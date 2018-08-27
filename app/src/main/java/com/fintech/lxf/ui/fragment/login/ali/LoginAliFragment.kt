package com.fintech.lxf.ui.fragment.login.ali

import android.app.Activity
import android.os.Bundle
import android.app.Fragment
import android.content.Intent
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseFragment
import com.fintech.lxf.ui.activity.init.InitActivity
import com.fintech.lxf.ui.fragment.login.account.LoginAccountFragment


class LoginAliFragment : BaseFragment(),LoginAliContract.View {
    override val context: Activity
        get() = activity
    private val presenter = LoginAliPresenter(this)

    override fun loginSuccess() {
        startActivity(Intent(activity, InitActivity::class.java))
        activity.finish()
    }

    override fun loginFail(hint: String) {
        showHint(hint)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_login_ali, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = LoginAccountFragment.newInstance()
        fragment.sharedElementEnterTransition = Fade()
        fragment.enterTransition = Fade()
        exitTransition = Fade()
        fragment.sharedElementReturnTransition = Fade()

        val btnLoginAli = view.findViewById<Button>(R.id.btnLoginAli)
        btnLoginAli.setOnClickListener { presenter.aliLogin() }
    }

    companion object {
        fun newInstance(): LoginAliFragment {
            val fragment = LoginAliFragment()
            return fragment
        }
    }

}
