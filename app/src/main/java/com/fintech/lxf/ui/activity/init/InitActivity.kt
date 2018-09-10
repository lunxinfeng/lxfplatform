package com.fintech.lxf.ui.activity.init

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.fintech.lxf.R
import com.fintech.lxf.R.id.*
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
    private val adapter = SingleAmountAdapter(R.layout.item_single_amount)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        if (Configuration.noLogin()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        } else {
            Constants.baseUrl = Configuration.getUserInfoByKey(Constants.KEY_ADDRESS)
        }

        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.menu_init)

        lifecycle.addObserver(prestener)

        et_ali_startPos.onChange { BaseAccessibilityService.startPos = it?.toString()?.toIntOrNull() ?: 1 }
        et_ali_total.onChange { BaseAccessibilityService.endPos = it?.toString()?.toIntOrNull() ?: 3000 }
        et_ali_offsetTotal.onChange {
            BaseAccessibilityService.offsetTotal = it?.toString()?.toIntOrNull() ?: 5
            if (BaseAccessibilityService.offsetTotal>50){
                BaseAccessibilityService.offsetTotal = 50
                et_ali_offsetTotal.setText("50")
            }
        }

        btnAli.setOnClickListener { prestener.startAli() }

        et_ali_account.setText(Configuration.getUserInfoByKey(KEY_ACCOUNT))
        et_ali_startPos.setText(Configuration.getUserInfoByKey(KEY_BEGIN_NUM))
        et_ali_total.setText(Configuration.getUserInfoByKey(KEY_END_NUM))
        et_ali_offsetTotal.setText(Configuration.getUserInfoByKey(KEY_MAX_NUM))

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@InitActivity,3)
            adapter = this@InitActivity.adapter
        }
        tv_ali_offsetTotal.clickN(7,"进入单额打码模式"){ singleAmountMode(true)}
        floatbutton.setOnClickListener { addSingleAmount() }

        configAliVersion(Configuration.getUserInfoByKey(KEY_ALI_VERSION).toIntOrNull()?:0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_init, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_ali_version ->{
                MaterialDialog(this)
                        .title(text = "请选择您的支付宝版本")
                        .listItemsSingleChoice(
                                items = listOf(
                                        "低于10.1.32",
                                        "10.1.32"
                                ),
                                initialSelection = Configuration.getUserInfoByKey(KEY_ALI_VERSION).toIntOrNull()?:0
                        ){_, index, _ ->
                            Configuration.putUserInfo(KEY_ALI_VERSION,index.toString())
                            configAliVersion(index)
                        }
                        .show()
            }
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
            R.id.action_csv ->{
                AlertDialog.Builder(this)
                        .setMessage("生成本地文件")
                        .setPositiveButton("生成csv") { _, _ ->
                            prestener.writeToCSV(".csv")
                        }
                        .setNegativeButton("生成txt") { _, _ ->
                            prestener.writeToCSV(".txt")
                        }
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configAliVersion(index: Int) {
        AliPayUI.et_content = when (index) {
            0 -> "com.alipay.mobile.ui:id/content"
            1 -> "com.alipay.mobile.antui:id/input_edit"
            else -> "com.alipay.mobile.antui:id/input_edit"
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        prestener.updateStartType(intent)
    }

    override fun onResume() {
        super.onResume()
        requestAllPermission()
    }
    override fun uploadComplete(success: Boolean,singleMode:Boolean) {
        val info = if (success) "提交本地数据成功" else "提交本地数据失败"

        if (success){
            SnackUtil.IndefiniteSnackbar(root,info,SnackUtil.Confirm).show()
            if (singleMode) return
            btnAli.text = "当前账户二维码已同步"
            btnAli.isEnabled = false
        }
        else
            SnackUtil.IndefiniteSnackbar(root,info,Color.WHITE,SnackUtil.red,SnackUtil.red,SnackUtil.orange,"重新提交"){
                prestener.upload()
            }.show()
    }

    override fun serverRefuseUpload(singleMode:Boolean) {
        SnackUtil.IndefiniteSnackbar(root,"服务器拒绝上传",SnackUtil.Warning).show()
        if (singleMode) return
        btnAli.text = "当前账户二维码已同步"
        btnAli.isEnabled = false
    }

    @SuppressLint("RestrictedApi")
    override fun singleAmountMode(enter: Boolean) {
        BaseAccessibilityService.singleMode = enter
        BaseAccessibilityService.singleCurr = -1
        if (!enter){
            BaseAccessibilityService.singleSet.clear()
            adapter.data.clear()
            adapter.notifyDataSetChanged()

            BaseAccessibilityService.startPos = et_ali_startPos.text.toString().toInt()
            BaseAccessibilityService.endPos = et_ali_total.text.toString().toInt()
            BaseAccessibilityService.offsetTotal = et_ali_offsetTotal.text.toString().toInt()

            prestener.getLastFromSql()
        }
        showHint("${if (enter) "进入" else "退出"}单额打码模式")
        et_ali_offsetTotal.isEnabled = enter
        floatbutton.visibility = if (enter) View.VISIBLE else View.GONE
        recyclerView.visibility = if (enter) View.VISIBLE else View.GONE
    }

    override fun addSingleAmount() {
        MaterialDialog(this)
                .apply {
                    setCanceledOnTouchOutside(false)
                    setActionButtonEnabled(WhichButton.POSITIVE,false)
                }
                .message(text = "添加单个定额")
                .input(inputType = InputType.TYPE_CLASS_NUMBER){ materialDialog, charSequence ->
                    materialDialog.setActionButtonEnabled(WhichButton.POSITIVE,!charSequence.isEmpty())
                }
                .positiveButton(text = "确定"){
                    val result = it.getInputField()?.text.toString().toInt()
                    if (!BaseAccessibilityService.singleSet.contains(result)){
                        BaseAccessibilityService.singleSet.offer(result)
                        adapter.data.add(it.getInputField()?.text.toString())
                        adapter.notifyDataSetChanged()
                    }else{
                        showHint("不能设置重复金额")
                    }
                }
                .negativeButton(text = "取消")
                .show()
    }

    override fun onBackPressed() {
        if (BaseAccessibilityService.singleMode)
            singleAmountMode(false)
        else
            super.onBackPressed()
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
