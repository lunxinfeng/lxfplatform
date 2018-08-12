package com.fintech.lxf.ui.activity.config

import android.arch.lifecycle.LifecycleObserver
import com.fintech.lxf.net.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.HashMap


class ConfigPresenter(val view: ConfigContract.View) : ConfigContract.Presenter, LifecycleObserver {
    override val compositeDisposable = CompositeDisposable()

    override fun bindData() {
    }

    override fun check(address: String) {

        val request = HashMap<String, String>()
        request.put("userName", "13397610459@001")
        request.put("password", "123456")

        service.login(SignRequestBody(request))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<ResultEntity<Map<String, String>>>(view.context) {
                    override fun _onNext(t: ResultEntity<Map<String, String>>?) {
                        t?.let {
                            if (it.subMsg.contains("用户不存在")) {
                                Configuration.putUserInfo(Constants.KEY_ADDRESS, address)
                                view.checkSuccess("服务器地址验证成功")
                            }
                        }
                    }

                    override fun _onError(error: String?) {
                        if (error?.contains("用户不存在") == true) {
                            Configuration.putUserInfo(Constants.KEY_ADDRESS, address)
                            view.checkSuccess("服务器地址验证成功")
                        }else{
                            view.checkFail(error?:"")
                        }
                    }
                })
    }

}