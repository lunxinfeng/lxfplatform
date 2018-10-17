package com.fintech.lxf.ui.fragment.login.ali_wx

import android.arch.lifecycle.LifecycleObserver
import android.text.TextUtils
import com.alipay.sdk.app.AuthTask
import com.fintech.lxf.App
import com.fintech.lxf.helper.METHOD_ALI
import com.fintech.lxf.helper.loginType
import com.fintech.lxf.net.ProgressSubscriber
import com.fintech.lxf.net.ResultEntity
import com.fintech.lxf.net.SignRequestBody
import com.fintech.lxf.ui.activity.login.AuthResult
import com.fintech.lxf.ui.fragment.login.LoginModel
import com.fintech.lxf.ui.dlg.BindDialog
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.functions.Function
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.HashMap

class LoginAliPresenter(val view: LoginAliContract.View) : LoginAliContract.Presenter
        , LifecycleObserver {
    private val model = LoginModel()
    private var ali_user_id = ""
    override fun aliLogin() {
        service.getAliLoginUrl()
                .subscribeOn(Schedulers.io())
                .flatMap(Function<ResultEntity<Map<String, String>>, ObservableSource<Map<String, String>>> { resultEntity ->
                    val authTask = AuthTask(view.context)
                    val url = resultEntity.result["url"]
                    // 调用授权接口，获取授权结果
                    val result = authTask.authV2(url, true)
                    Observable.just(result)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Function<Map<String, String>, ObservableSource<String>> { stringStringMap ->
                    val authResult = AuthResult(stringStringMap, true)
                    val resultStatus = authResult.resultStatus

                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.resultCode, "200")){
                        Observable.just(authResult.alipayUserId)
                    }else{
                        Observable.error(Exception("授权失败"))
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap { uid ->
                    ali_user_id = uid
                    val request = HashMap<String, String>()
                    request["uid"] = uid
                    request["app_version"] = App.getAppContext().packageManager
                            .getPackageInfo(App.getAppContext().packageName,0).versionCode.toString()
                    service.postAliCode(SignRequestBody(request))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(view.context){
                    override fun _onNext(s: ResultEntity<Map<String, String>>) {
                        when (s.code) {
                            "40002" -> {
                                val dialog = BindDialog(view.context, BindDialog.TYPE_BIND, object : BindDialog.ClickListener {
                                    override fun onClick(name: String, password: String) {
                                        val request = HashMap<String, String>()
                                        request.put("userName", name)
                                        request.put("password", password)
                                        request.put("uid", ali_user_id)
                                        request.put("payMethod", METHOD_ALI)
                                        request.put("app_version", App.getAppContext().packageManager
                                                .getPackageInfo(App.getAppContext().packageName,0).versionCode.toString())
                                        service.bindAli(SignRequestBody(request))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(view.context) {
                                                    override fun _onNext(resultEntity: ResultEntity<Map<String, String>>) {
                                                        val result = resultEntity.result
                                                        if (result == null){
                                                            view.loginFail(resultEntity.subMsg?:resultEntity.msg)
                                                            return
                                                        }

                                                        loginType = METHOD_ALI
                                                        model.saveData(result)

                                                        view.loginSuccess()
                                                    }

                                                    override fun _onError(error: String) {
                                                        view.loginFail(error)
                                                    }
                                                })
                                    }
                                })
                                dialog.show()
                            }
                            "10000" -> {
                                val result = s.result

                                loginType = METHOD_ALI
                                model.saveData(result)

                                view.loginSuccess()
                            }
                        }
                    }

                    override fun _onError(error: String) {
                        view.loginFail(error)
                    }

                })
    }

    override fun wechatLogin() {
        val api = WXAPIFactory.createWXAPI(view.context,WX_APPID,true)
        api.registerApp(WX_APPID)

        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        api.sendReq(req)
    }

    override val compositeDisposable = CompositeDisposable()

    override fun bindData() {
    }

}