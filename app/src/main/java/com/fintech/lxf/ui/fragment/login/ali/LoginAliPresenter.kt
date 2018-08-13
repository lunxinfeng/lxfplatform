package com.fintech.lxf.ui.fragment.login.ali

import android.arch.lifecycle.LifecycleObserver
import com.alipay.sdk.app.AuthTask
import com.fintech.lxf.net.ProgressSubscriber
import com.fintech.lxf.net.ResultEntity
import com.fintech.lxf.net.SignRequestBody
import com.fintech.lxf.ui.activity.login.AuthResult
import com.fintech.lxf.ui.fragment.login.LoginModel
import com.fintech.lxf.ui.dlg.BindDialog
import io.reactivex.functions.Function
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.HashMap

class LoginAliPresenter(val view: LoginAliContract.View) : LoginAliContract.Presenter, LifecycleObserver {
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
//                    val resultStatus = authResult.getResultStatus()
//
//                    // 判断resultStatus 为“9000”且result_code
//                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
//                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
//                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
//                        // 传入，则支付账户为该授权账户
//                        Toast.makeText(view.context,
//                                "授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()) + authResult.getAlipayOpenId(), Toast.LENGTH_SHORT)
//                                .show()
//                    } else {
//                        // 其他状态值则为授权失败
//                        Toast.makeText(view.context,
//                                "授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show()
//
//                    }
                    Observable.just(authResult.getAlipayUserId())
                })
                .observeOn(Schedulers.io())
                .flatMap(Function<String, ObservableSource<ResultEntity<Map<String, String>>>> { uid ->
                    ali_user_id = uid
                    val request = HashMap<String, String>()
                    request.put("uid", uid)
                    service.postAliCode(SignRequestBody(request))
                })
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
                                        request.put("payMethod", "2001")
                                        service.bindAli(SignRequestBody(request))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(view.context) {
                                                    override fun _onNext(resultEntity: ResultEntity<Map<String, String>>) {
                                                        println("LoginActivity.onNext")
                                                        val result = resultEntity.result

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

    override val compositeDisposable = CompositeDisposable()

    override fun bindData() {
    }

}