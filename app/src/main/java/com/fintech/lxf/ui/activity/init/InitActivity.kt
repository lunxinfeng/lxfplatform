package com.fintech.lxf.ui.activity.init

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.helper.*
import com.fintech.lxf.net.Configuration
import com.fintech.lxf.net.Constants
import com.fintech.lxf.net.Constants.*
import com.fintech.lxf.service.init.BaseAccessibilityService
import com.fintech.lxf.ui.activity.login.LoginActivity
import kotlinx.android.synthetic.main.activity_init.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class InitActivity : BaseActivity(), EasyPermissions.PermissionCallbacks,InitContract.View {
    override val context: Activity
        get() = this
    private val prestener = InitPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        if (Configuration.noLogin()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Constants.baseUrl = Configuration.getUserInfoByKey(Constants.KEY_ADDRESS)
        }

        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.menu_init)

        lifecycle.addObserver(prestener)

        et_ali_startPos.onChange { BaseAccessibilityService.startPos = it?.toString()?.toIntOrNull() ?: 1 }
        et_ali_total.onChange { BaseAccessibilityService.endPos = it?.toString()?.toIntOrNull() ?: 3000 }
        et_ali_offsetTotal.onChange { BaseAccessibilityService.offsetTotal = it?.toString()?.toIntOrNull() ?: 5 }

        btnAli.setOnClickListener { prestener.startAli() }
        btnCSV.setOnClickWithCountDownListener { prestener.writeToCSV() }
        btnUpload.setOnClickListener { prestener.upload() }

        et_ali_account.setText(Configuration.getUserInfoByKey(KEY_ACCOUNT))
        et_ali_startPos.setText(Configuration.getUserInfoByKey(KEY_BEGIN_NUM))
        et_ali_total.setText(Configuration.getUserInfoByKey(KEY_END_NUM))
        et_ali_offsetTotal.setText(Configuration.getUserInfoByKey(KEY_MAX_NUM))
        btnUpload.isEnabled = Configuration.getUserInfoByKey(KEY_USER_NAME) == "1"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_init, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_exit ->{
                AlertDialog.Builder(this)
                        .setMessage("退出账号时是否清除本地保存的二维码数据？")
                        .setPositiveButton("清除") { _, _ ->
                            prestener.exitAccount(true)
                        }
                        .setNegativeButton("保留") { _, _ ->
                            prestener.exitAccount(false)
                        }
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        prestener.updateStartType(intent)
    }

    override fun onResume() {
        super.onResume()
        requestAllPermission()
    }
    override fun uploadComplete(success: Boolean) {
        val info = if (success) "提交本地数据成功" else "提交本地数据失败"

        if (success)
            SnackUtil.IndefiniteSnackbar(root,info,SnackUtil.Confirm).show()
        else
            SnackUtil.IndefiniteSnackbar(root,info,Color.WHITE,SnackUtil.red,SnackUtil.red,SnackUtil.orange,"重新提交"){
                prestener.upload()
            }.show()
    }

    override fun serverRefuseUpload() {
        SnackUtil.IndefiniteSnackbar(root,"服务器拒绝上传",SnackUtil.Warning).show()
    }

    @AfterPermissionGranted(ALL_PERMISSION)
    fun requestAllPermission() {
        if (!EasyPermissions.hasPermissions(this, *PERMISSIONS_GROUP)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_message),
                    ALL_PERMISSION, *PERMISSIONS_GROUP)
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
