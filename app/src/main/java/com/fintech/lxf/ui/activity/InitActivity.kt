package com.fintech.lxf.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.db.DB
import com.fintech.lxf.db.User
import com.fintech.lxf.helper.*
import com.fintech.lxf.service.init2.AlipayAccessibilityService
import com.fintech.lxf.service.init2.BaseAccessibilityService
import com.fintech.lxf.service.init2.WechatAccessibilityService
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_init.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class InitActivity : BaseActivity() {
    private var startType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        startType = intent.getIntExtra("reStart", 0)

        SPHelper.getInstance().init(this)

        et_ali_startPos.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                BaseAccessibilityService.startPos = s?.toString()?.toIntOrNull()?:1
            }

        })

        et_ali_total.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                BaseAccessibilityService.endPos = s?.toString()?.toIntOrNull()?:3000
            }

        })
        et_ali_offsetTotal.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                BaseAccessibilityService.offsetTotal = s?.toString()?.toIntOrNull()?:5
            }

        })
        btnAli.setOnClickListener { startAli() }
        btnWeChat.setOnClickListener { startWeChat() }
        btnClearAli.setOnClickListener { clearSqlLocal(BaseAccessibilityService.TYPE_ALI) }
        btnClearWechat.setOnClickListener { clearSqlLocal(BaseAccessibilityService.TYPE_WeChat) }
    }

    private fun clearSqlLocal(type: Int) {
        Observable
                .create<Int> { emitter ->
                    val datas = DB.queryAll(this, type)
                    if (datas != null) {
                        val result = DB.deleteAll(this, *datas.toTypedArray())
                        emitter.onNext(result)
                    }
                    emitter.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .subscribe(object :Observer<Int>{
                    var d: Disposable? = null
                    override fun onComplete() {
                        getDataFromSql()
                        d?.dispose()
                    }

                    override fun onSubscribe(d: Disposable) {
                        this.d = d
                    }

                    override fun onNext(t: Int) {
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        d?.dispose()
                    }

                })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startType = intent.getIntExtra("reStart", 0)
    }

    override fun onResume() {
        super.onResume()

        getDataFromSql()
    }

    private fun getDataFromSql() {
        Observable
                .create<User> { emitter ->
                    val ali_last = DB.queryLast(this, BaseAccessibilityService.TYPE_ALI)
                    val wechat_last = DB.queryLast(this, BaseAccessibilityService.TYPE_WeChat)
                    if (ali_last != null)
                        emitter.onNext(ali_last)
//                    else
//                        emitter.onNext(User().apply {
//                            type = BaseAccessibilityService.TYPE_ALI
//                            account = ""
//                            pos_curr = 1
//                            pos_end = 300000
//                            offset = 0
//                        })
                    if (wechat_last != null)
                        emitter.onNext(wechat_last)
//                    else
//                        emitter.onNext(User().apply {
//                            type = BaseAccessibilityService.TYPE_WeChat
//                            account = ""
//                            pos_curr = 1
//                            pos_end = 300000
//                            offset = 0
//                        })

                    emitter.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { last ->
                    when (last.type) {
                        BaseAccessibilityService.TYPE_ALI -> {
                            et_ali_account.setText(last.account)
                            et_ali_pos.setText(last.pos_curr.toString())
                            et_ali_total.setText(last.pos_end.toString())
                            et_ali_offset.setText(last.offset.toString())
                        }
                        BaseAccessibilityService.TYPE_WeChat -> {
                            et_wx_account.setText(last.account)
                            et_wx_pos.setText(last.pos_curr.toString())
                            et_wx_total.setText(last.pos_end.toString())
                            et_wx_offset.setText(last.offset.toString())
                        }

                    }
                }
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    debug(localClassName, "startType:$startType")
                    when (startType) {
                        1 -> {
                            btnAli.performClick()
                        }
                        2 -> {
                            btnWeChat.performClick()
                        }
                    }
                }
                .subscribe(object : Observer<User> {
                    internal var d: Disposable? = null
                    override fun onSubscribe(d: Disposable) {
                        this.d = d
                    }

                    override fun onNext(last: User) {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        d?.dispose()
                    }

                    override fun onComplete() {
                        d?.dispose()
                    }
                })
    }

    private fun startAli() {
        val accessibilitySettingsOn = AccessibilityHelper.isAccessibilitySettingsOn(applicationContext, AlipayAccessibilityService::class.java)
        if (!accessibilitySettingsOn) {
            AlertDialog.Builder(this)
                    .setMessage("请先打开支付宝辅助插件。")
                    .setCancelable(false)
                    .setPositiveButton("去打开") { _, _ ->
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("取消") { _, _ ->
                        //                        finish()
                    }
                    .show()
            return
        }
        val acc = et_ali_account.text.toString().trim()
        val pos = et_ali_pos.text.toString().trim().toIntOrNull() ?: BaseAccessibilityService.startPos
        val offset = et_ali_offset.text.toString().trim().toIntOrNull() ?: 0
        val beishu = 100
        val end = et_ali_total.text.toString().trim().toIntOrNull() ?: BaseAccessibilityService.endPos

        SPHelper.getInstance().putString(AliPayUI.acc, acc)
        SPHelper.getInstance().putInt(AliPayUI.posV, pos)
        SPHelper.getInstance().putInt(AliPayUI.startV, pos)
        SPHelper.getInstance().putInt(AliPayUI.endV, end)
        SPHelper.getInstance().putInt(AliPayUI.offsetV, offset)
        SPHelper.getInstance().putInt(AliPayUI.beishuV, beishu)

        AliPayUI.steep = 0

        val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/"
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdir()
        }
        Utils.launchAlipayAPP(this)
    }

    private fun startWeChat() {
        val accessibilitySettingsOn_wechat = AccessibilityHelper.isAccessibilitySettingsOn(applicationContext, WechatAccessibilityService::class.java)
        if (!accessibilitySettingsOn_wechat) {
            AlertDialog.Builder(this)
                    .setMessage("请先打开微信辅助插件。")
                    .setCancelable(false)
                    .setPositiveButton("去打开") { _, _ ->
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("取消") { _, _ ->
                        //                        finish()
                    }
                    .show()
            return
        }
        val acc = et_wx_account.text.toString().trim()
        val pos = et_wx_pos.text.toString().trim().toIntOrNull() ?: 1
        val offset = et_wx_offset.text.toString().trim().toIntOrNull() ?: 0
        val beishu = 100
        val end = et_wx_total.text.toString().trim().toIntOrNull() ?: 3000

        SPHelper.getInstance().putString(WechatUI.acc, acc)
        SPHelper.getInstance().putInt(WechatUI.posV, pos)
        SPHelper.getInstance().putInt(WechatUI.startV, pos)
        SPHelper.getInstance().putInt(WechatUI.endV, end)
        SPHelper.getInstance().putInt(WechatUI.offsetV, offset)
        SPHelper.getInstance().putInt(WechatUI.beishuV, beishu)

        WechatUI.steep = -2

        val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/"
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdir()
        }
        Utils.launchWechatAPP(this)
    }
}
