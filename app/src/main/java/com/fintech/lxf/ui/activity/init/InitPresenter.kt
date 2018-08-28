package com.fintech.lxf.ui.activity.init

import android.app.ActivityManager
import android.app.AlertDialog
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import com.fintech.lxf.db.DB
import com.fintech.lxf.db.User
import com.fintech.lxf.helper.*
import com.fintech.lxf.net.*
import com.fintech.lxf.service.init.AlipayAccessibilityService
import com.fintech.lxf.service.init.BaseAccessibilityService
import com.fintech.lxf.ui.activity.login.LoginActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class InitPresenter(val view: InitContract.View) : InitContract.Presenter, LifecycleObserver {
    override val compositeDisposable = CompositeDisposable()
    private val model = InitModel()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        updateStartType(view.context.intent)
        SPHelper.getInstance().init(view.context)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
//        if (Configuration.getUserInfoByKey(Constants.KEY_ALLOW_LOAD) != "1")
//            view.serverRefuseUpload()
        getLastFromSql()
    }

    fun updateStartType(intent: Intent) {
        model.startType = intent.getIntExtra("reStart", 0)
    }

    override fun startAli() {
        val accessibilitySettingsOn = AccessibilityHelper.isAccessibilitySettingsOn(view.context, AlipayAccessibilityService::class.java)
        if (!accessibilitySettingsOn) {
            AlertDialog.Builder(view.context)
                    .setMessage("请先打开支付宝辅助插件。")
                    .setCancelable(false)
                    .setPositiveButton("去打开") { _, _ ->
                        view.context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .setNegativeButton("取消") { _, _ ->
                    }
                    .show()
            return
        }

        model.reStart()

        val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/"
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdir()
        }
        Utils.launchAlipayAPP(view.context)
    }

    override fun writeToCSV(type:String) {
        Single
                .create(SingleOnSubscribe<List<User>> { emitter ->
                    model.delLocalCSV()
                    val users = DB.queryAll(view.context, BaseAccessibilityService.TYPE_ALI,Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
                    if (users != null)
                        emitter.onSuccess(users)
                })
                .subscribeOn(Schedulers.io())
                .subscribe { users ->
                    model.writeToCSV(users,type,true)
                }
    }

    override fun upload() {
        AlertDialog.Builder(view.context)
                .setMessage("确认要上传本地数据吗？")
                .setCancelable(false)
                .setPositiveButton("确认") { _, _ ->
                    uploadToServer()
                }
                .setNegativeButton("取消") { _, _ ->
                }
                .show()
    }

    override fun getLastFromSql() {
        Observable
                .create<User> { emitter ->
                    val last = DB.queryLast(view.context, BaseAccessibilityService.TYPE_ALI,Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT),if (BaseAccessibilityService.singleMode)2 else 1)
                    if (last != null)
                        emitter.onNext(last)
                    emitter.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    when (model.startType) {
                        InitModel.STRAT_TYPE_ERROR_NORMAL -> {
                            startAli()
                        }
                        InitModel.STRAT_TYPE_KILL_BACKGROUND -> {
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    val am = view.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                                    am.killBackgroundProcesses("com.eg.android.AlipayGphone")
                                    SystemClock.sleep(1000)
                                    startAli()
                                }
                            },20*1000)
                        }
                    }
                    model.startType = 0
                }
                .subscribe(object : ProgressSubscriber<User>(view.context) {
                    override fun _onNext(last: User) {
                        model.last = last
                        if (last.pos_curr == BaseAccessibilityService.endPos &&
                                last.offset == BaseAccessibilityService.offsetTotal - 1 &&
                                (!BaseAccessibilityService.singleMode || BaseAccessibilityService.singleSet.size == 0)) {
                            if (Configuration.getUserInfoByKey(Constants.KEY_ALLOW_LOAD) == "1")
                                uploadToServer()
                            else
                                view.serverRefuseUpload(BaseAccessibilityService.singleMode)
                        }
                    }

                    override fun _onError(error: String) {
                        view.showHint(error)
                    }
                })
    }

    private fun uploadToServer() {
        Observable
                .create<List<User>> { emitter ->
                    model.delLocalCSV()
                    val users = DB.queryAll(view.context, BaseAccessibilityService.TYPE_ALI,Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
                    if (users != null)
                        emitter.onNext(users)
                    emitter.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .flatMap { users ->
                    val files = model.writeToCSV(users)
//                    Observable.fromIterable(files)
                    Observable.just(files)
                }
                .flatMap { path ->
                    val file = File(path.split(";")[0])
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val des = Configuration.getUserInfoByKey(Constants.KEY_USER_NAME)
                    val desBody = RequestBody.create(MediaType.parse("multipart/form-data"), des)
                    val des2 = "2001"
                    val desBody2 = RequestBody.create(MediaType.parse("multipart/form-data"), des2)
                    val desBody3 = RequestBody.create(MediaType.parse("multipart/form-data"), path.split(";")[1])

                    val map = HashMap<String, RequestBody>()
                    map.put("userName", desBody)
                    map.put("payMethod", desBody2)
                    map.put("version", desBody3)

                    ApiProducerModule.create(ApiService::class.java)
                            .upload(map, body)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    model.delLocalCSV()
                }
                .subscribe(object : ProgressSubscriber<ResponseBody>(view.context) {
                    override fun _onNext(t: ResponseBody) {
                        if (t.string().contains("success")) {
                            if (!BaseAccessibilityService.singleMode)
                                Configuration.putUserInfo(Constants.KEY_ALLOW_LOAD, "-1")
                            view.uploadComplete(true,BaseAccessibilityService.singleMode)
                        } else
                            view.uploadComplete(false)

                    }

                    override fun _onError(error: String) {
                        view.uploadComplete(false)
                    }
                })
    }

    override fun exitAccount(clear: Boolean) {
        if (clear)
            model.clearQRData(view.context)
        model.clearUserInfo()
        view.context.toActivity(LoginActivity::class.java)
        view.context.finish()
    }

    override fun bindData() {

    }

}