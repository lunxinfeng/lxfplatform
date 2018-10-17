package com.fintech.lxf.ui.fragment.login.account


import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseFragment
import com.fintech.lxf.helper.METHOD_ALI
import com.fintech.lxf.helper.METHOD_WECHAT
import com.fintech.lxf.ui.activity.init.InitActivity
import com.fintech.lxf.ui.fragment.login.ali_wx.LoginAliFragment
import kotlinx.android.synthetic.main.fragment_login_account.*


class LoginAccountFragment : BaseFragment(),LoginAccountContract.View {
    override val context: Activity
        get() = activity
    private val presenter = LoginAccountPresenter(this)
    private var btnBind:Button? = null

    override fun loginSuccess() {
        startActivity(Intent(activity, InitActivity::class.java))
        activity.finish()
    }

    override fun loginFail(hint: String) {
        showHint(hint)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBind = view.findViewById<Button>(R.id.btnBind)
        val et_account = view.findViewById<EditText>(R.id.et_account)
        val et_password = view.findViewById<EditText>(R.id.et_password)
        btnBind?.transitionName = "login"

        btnBind?.setOnClickListener { presenter.accountLogin(et_account.text.toString(),et_password.text.toString(),
                if (rbAli.isChecked) METHOD_ALI else METHOD_WECHAT) }
    }

    fun back(){
        val fragment = LoginAliFragment.newInstance()
        fragment.sharedElementEnterTransition = Fade()
        fragment.enterTransition = Fade()
        exitTransition = Fade()
        fragment.sharedElementReturnTransition = Fade()
        fragmentManager
                .beginTransaction()
                .addSharedElement(btnBind, "login")
                .replace(R.id.frame_content, fragment)
                .commit()
    }


    companion object {
        fun newInstance(): LoginAccountFragment {
            val fragment = LoginAccountFragment()
            return fragment
        }
    }

}
