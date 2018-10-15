package com.fintech.lxf.ui.fragment.login.account

import android.arch.lifecycle.LifecycleObserver
import com.fintech.lxf.App
import com.fintech.lxf.helper.loginType
import com.fintech.lxf.net.ProgressSubscriber
import com.fintech.lxf.net.ResultEntity
import com.fintech.lxf.net.SignRequestBody
import com.fintech.lxf.ui.fragment.login.LoginModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.HashMap

class LoginAccountPresenter(val view: LoginAccountContract.View) : LoginAccountContract.Presenter, LifecycleObserver {
    private val model = LoginModel()

    override fun accountLogin(name:String,password:String,type:String) {
        val request = HashMap<String, String>()
        request.put("userName", name)
        request.put("password", password)
        request.put("payMethod", type)
        request.put("app_version", App.getAppContext().packageManager
                .getPackageInfo(App.getAppContext().packageName,0).versionCode.toString())

        loginType = type
        login(request)
    }

    private fun login(request: HashMap<String, String>){
        service.login(SignRequestBody(request))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(view.context){
                    override fun _onNext(resultEntity: ResultEntity<Map<String, String>>) {
                        val result = resultEntity.result
                        if (result == null){
                            view.loginFail(resultEntity.subMsg?:resultEntity.msg)
                            return
                        }
                        model.saveData(result)

                        view.loginSuccess()
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