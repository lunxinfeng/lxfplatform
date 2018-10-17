package com.fintech.match.pay_2.wxapi

import android.content.Intent
import android.os.Bundle
import com.fintech.lxf.App
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.helper.METHOD_WECHAT
import com.fintech.lxf.helper.loginType
import com.fintech.lxf.helper.toast
import com.fintech.lxf.net.*
import com.fintech.lxf.ui.activity.init.InitActivity
import com.fintech.lxf.ui.activity.login.LoginActivity
import com.fintech.lxf.ui.dlg.BindDialog
import com.fintech.lxf.ui.fragment.login.LoginModel
import com.fintech.lxf.ui.fragment.login.ali_wx.WX_APPID
import com.fintech.lxf.ui.fragment.login.ali_wx.WX_SECRET
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.HashMap

class WXEntryActivity : BaseActivity(),IWXAPIEventHandler {
    override fun onResp(resp: BaseResp) {
        when(resp.errCode){
            0 -> {
                if (resp is SendAuth.Resp){
                    val code = resp.code
                    getAccessToken(code)
                }else{
                    toast("请求类型错误")
                    finish()
                }
            }
            else -> {
                toast(resp.errStr)
                finish()
            }
        }
    }

    override fun onReq(req: BaseReq?) {

    }

    private lateinit var wxApi:IWXAPI
    private val model = LoginModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wxApi = WXAPIFactory.createWXAPI(this, WX_APPID,false)
        wxApi.handleIntent(intent,this)
    }

    private fun getAccessToken(code:String){
        var wx_openId = ""
        val url = StringBuffer()
        url.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=$WX_APPID")
                .append("&secret=$WX_SECRET")
                .append("&code=$code")
                .append("&grant_type=authorization_code")
        ApiProducerModule.create(ApiService::class.java)
                .getWXAccessToken(url.toString())
                .subscribeOn(Schedulers.io())
                .map {
                    val result = it.string()
                    val jsonObject = JSONObject(result)
                    val openId = jsonObject.getString("openid")
                    openId
                }
                .flatMap { openId ->
                    wx_openId = openId
                    val request = HashMap<String, String>()
                    request["uid"] = openId
                    request["app_version"] = App.getAppContext().packageManager
                            .getPackageInfo(App.getAppContext().packageName,0).versionCode.toString()
                    ApiProducerModule.create(ApiService::class.java).postAliCode(SignRequestBody(request))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(this) {
                    override fun _onNext(s: ResultEntity<Map<String, String>>) {

                        when (s.code) {
                            "40002" -> {
                                val dialog = BindDialog(this@WXEntryActivity, BindDialog.TYPE_BIND, BindDialog.ClickListener { name, password ->
                                    val request = HashMap<String, String>()
                                    request["userName"] = name
                                    request["password"] = password
                                    request["uid"] = wx_openId
                                    request["payMethod"] = METHOD_WECHAT
                                    request["app_version"] = App.getAppContext().packageManager
                                            .getPackageInfo(App.getAppContext().packageName, 0).versionCode.toString()
                                    ApiProducerModule.create(ApiService::class.java).bindAli(SignRequestBody(request))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(this@WXEntryActivity) {
                                                override fun _onNext(resultEntity: ResultEntity<Map<String, String>>) {
                                                    val result = resultEntity.result
                                                    if (result == null){
                                                        toast(resultEntity.subMsg?:resultEntity.msg)
                                                        return
                                                    }

                                                    loginType = METHOD_WECHAT
                                                    model.saveData(result)

                                                    startActivity(Intent(this@WXEntryActivity, InitActivity::class.java))
                                                    activitys[LoginActivity::class.java.simpleName]?.finish()
                                                    finish()
                                                }

                                                override fun _onError(error: String) {
                                                    toast(error)
                                                    activitys[LoginActivity::class.java.simpleName]?.finish()
                                                    finish()
                                                }
                                            })
                                })
                                dialog.show()
                            }
                            "10000" -> {
                                val result = s.result

                                loginType = METHOD_WECHAT
                                model.saveData(result)

                                startActivity(Intent(this@WXEntryActivity, InitActivity::class.java))
                                activitys[LoginActivity::class.java.simpleName]?.finish()
                                finish()
                            }
                        }
                    }

                    override fun _onError(error: String) {
                        toast(error)
                        finish()
                    }
                })
    }
}
