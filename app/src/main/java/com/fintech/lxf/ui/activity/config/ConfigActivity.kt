package com.fintech.lxf.ui.activity.config

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.helper.RegexUtil
import com.fintech.lxf.helper.Utils
import com.fintech.lxf.helper.toast
import com.fintech.lxf.net.Constants
import com.fintech.lxf.ui.activity.login.LoginActivity
import kotlinx.android.synthetic.main.activity_config.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class ConfigActivity : BaseActivity(),ConfigContract.View, EasyPermissions.PermissionCallbacks {
    override val context: Context
        get() = this

    private val presenter = ConfigPresenter(this)

    override fun checkSuccess(hint: String) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun checkFail(hint: String) {
        showHint(hint)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        setSupportActionBar(toolbar)

        etAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Constants.baseUrl = s.toString()
                btnNext.isEnabled = RegexUtil.isMatch(RegexUtil.URL,Constants.baseUrl)
            }

        })
        btnNext.setOnClickListener { presenter.check(Constants.baseUrl) }
    }


    override fun onResume() {
        super.onResume()
        requestAllPermission()
    }

    @AfterPermissionGranted(Constants.ALL_PERMISSION)
    fun requestAllPermission() {
        if (!EasyPermissions.hasPermissions(this, *Constants.PERMISSIONS_GROUP)) {
            EasyPermissions.requestPermissions(this, this.getString(R.string.rationale_message),
                    Constants.ALL_PERMISSION, *Constants.PERMISSIONS_GROUP)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Utils.goPermissionSetting(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }
}
