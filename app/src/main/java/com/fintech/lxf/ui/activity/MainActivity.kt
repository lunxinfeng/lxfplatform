package com.fintech.lxf.ui.activity

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.helper.Utils
import com.fintech.lxf.helper.toActivity
import com.fintech.lxf.net.ALL_PERMISSION
import com.fintech.lxf.net.PERMISSIONS_GROUP
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : BaseActivity(),EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnInit.setOnClickListener { toActivity(InitActivity::class.java) }
        btnObserver.setOnClickListener { toActivity(ObserverActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()

        requestAllPermission()
    }


    @AfterPermissionGranted(ALL_PERMISSION)
    fun requestAllPermission() {
        if (!EasyPermissions.hasPermissions(this, *PERMISSIONS_GROUP)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_message),
                    ALL_PERMISSION, *PERMISSIONS_GROUP)
        }
//        else{
//            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)){
//                Utils.goPermissionSetting(this)
//            }
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Utils.goPermissionSetting(this)
        }
    }

}
