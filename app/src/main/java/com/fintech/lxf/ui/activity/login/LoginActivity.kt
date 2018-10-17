package com.fintech.lxf.ui.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.helper.Utils
import com.fintech.lxf.helper.clickN
import com.fintech.lxf.net.Constants
import com.fintech.lxf.ui.fragment.login.account.LoginAccountFragment
import com.fintech.lxf.ui.fragment.login.ali_wx.LoginAliFragment
import kotlinx.android.synthetic.main.activity_login2.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class LoginActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

//        if (Configuration.noAddress()) {
//            startActivity(Intent(this,ConfigActivity::class.java))
//            finish()
//            return
//        } else {
//            Constants.baseUrl = Configuration.getUserInfoByKey(Constants.KEY_ADDRESS)
//        }

        fragmentManager
                .beginTransaction()
                .add(R.id.frame_content,LoginAliFragment.newInstance())
                .commit()

        app_bar_image.clickN(7,"进入账号登录"){
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_content,LoginAccountFragment.newInstance())
                    .commit()
        }

        getAllApps(this)
    }

    fun getAllApps(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pm = context.packageManager

        val list =  pm.queryIntentActivities(intent, 0)
        list.forEach { println("${it.activityInfo.packageName}\t${it.loadLabel(pm)}") }
    }

    override fun onBackPressed() {
        val fragment = fragmentManager.findFragmentById(R.id.frame_content)
        if (fragment is LoginAccountFragment)
            fragment.back()
        else
            super.onBackPressed()
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
