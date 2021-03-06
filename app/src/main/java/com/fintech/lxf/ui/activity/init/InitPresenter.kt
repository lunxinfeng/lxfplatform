package com.fintech.lxf.ui.activity.init

import android.app.ActivityManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import com.afollestad.materialdialogs.MaterialDialog
import com.fintech.lxf.bean.ConstantAmountDto
import com.fintech.lxf.bean.MoreUsedBean
import com.fintech.lxf.db.DB
import com.fintech.lxf.db.User
import com.fintech.lxf.helper.*
import com.fintech.lxf.net.*
import com.fintech.lxf.service.init.AlipayAccessibilityService
import com.fintech.lxf.service.init.BaseAccessibilityService
import com.fintech.lxf.service.init.WechatAccessibilityService
import com.fintech.lxf.ui.activity.login.LoginActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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

    private var isAli = true

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        isAli = loginType == METHOD_ALI
        updateStartType(view.context.intent)
        SPHelper.getInstance().init(view.context)
        getMoreUsedAmount()
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

    override fun getMoreUsedAmount() {
        val obervable_1 = Observable
                .create<User> { emitter ->
                    val last = DB.queryLast(view.context, BaseAccessibilityService.TYPE_ALI, Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
                    if (last != null)
                        emitter.onNext(last)
                    else
                        emitter.onNext(User())
                    emitter.onComplete()
                }
        val request = HashMap<String, String>()
        request["mchId"] = Configuration.getUserInfoByKey(Constants.KEY_MCH_ID)
        val obervable_2 = service.getMoreUsedAmount(SignRequestBody(request).sign())
        Observable
                .zip(obervable_1, obervable_2, BiFunction<User?, ResultEntity<List<ConstantAmountDto>>?, List<MoreUsedBean>> { lastUser, t2 ->
                    BaseAccessibilityService.moreUsedAmount.clear()
                    val list = mutableListOf<MoreUsedBean>()
                    t2.result
                            ?.sortedBy { it.constantAmount }
                            ?.filter {
                                val result = lastUser.account.isEmpty()
                                        || lastUser.mode != BaseAccessibilityService.MODE_MORE_USED
                                        || it.constantAmount > lastUser.pos_curr

                                list.add(MoreUsedBean(it.constantAmount, !result))
                                result
                            }
                            ?.forEach { BaseAccessibilityService.moreUsedAmount.offer(it) }

                    list
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<List<MoreUsedBean>>(view.context) {
                    override fun _onNext(t: List<MoreUsedBean>) {
                        view.showMoreUsedAmount(t)
                    }

                    override fun _onError(error: String?) {
                        view.showHint(error ?: "未知错误")
                    }
                })
    }

    override fun startAli() {
        val accessibilitySettingsOn = AccessibilityHelper.isAccessibilitySettingsOn(view.context, AlipayAccessibilityService::class.java)
        if (!accessibilitySettingsOn) {
            MaterialDialog(view.context)
                    .message(text = "请先打开支付宝辅助插件。")
                    .positiveButton(text = "去打开"){
                        view.context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .negativeButton(text = "取消")
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

    override fun startWechat() {
        val accessibilitySettingsOn = AccessibilityHelper.isAccessibilitySettingsOn(view.context, WechatAccessibilityService::class.java)
        if (!accessibilitySettingsOn) {
            MaterialDialog(view.context)
                    .message(text = "请先打开微信辅助插件。")
                    .positiveButton(text = "去打开"){
                        view.context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .negativeButton(text = "取消")
                    .show()
            return
        }

        model.reStart()

        val filePath = Environment.getExternalStorageDirectory().toString() + "/a_match_pay/"
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdir()
        }
        Utils.launchWechatAPP(view.context)
    }

    override fun writeToCSV(type: String) {
        Single
                .create(SingleOnSubscribe<List<User>> { emitter ->
                    model.delLocalCSV()
                    val users = DB.queryAll(view.context, BaseAccessibilityService.TYPE_ALI, Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
                    if (users != null)
                        emitter.onSuccess(users)
                })
                .subscribeOn(Schedulers.io())
                .subscribe { users ->
                    model.writeToCSV(users, type, true)
                }
    }

    override fun upload() {
        MaterialDialog(view.context)
                .apply { setCancelable(false) }
                .message(text = "确认要上传本地数据吗？")
                .positiveButton(text = "确认"){
                    uploadToServer()
                }
                .negativeButton(text = "取消")
                .show()
    }

    override fun getLastFromSql() {
        Observable
                .create<User> { emitter ->
                    val last = DB.queryLast(view.context, if (isAli) BaseAccessibilityService.TYPE_ALI else BaseAccessibilityService.TYPE_WeChat,
                            Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
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
                            if (isAli)
                                startAli()
                            else
                                startWechat()
                        }
                        InitModel.STRAT_TYPE_KILL_BACKGROUND -> {
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    val am = view.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                                    am.killBackgroundProcesses(if (isAli) "com.eg.android.AlipayGphone" else "com.tencent.mm")
                                    SystemClock.sleep(1000)
                                    if (isAli)
                                        startAli()
                                    else
                                        startWechat()
                                }
                            }, 20 * 1000)
                        }
                    }
                    model.startType = 0
                }
                .subscribe(object : ProgressSubscriber<User>(view.context) {
                    override fun _onNext(last: User) {
                        model.last = last
                        if (last.pos_curr == BaseAccessibilityService.endPos &&
                                last.offset == BaseAccessibilityService.offsetTotal - 1 &&
                                ((!BaseAccessibilityService.singleMode && BaseAccessibilityService.moreUsedAmount.size == 0) ||
                                        BaseAccessibilityService.singleSet.size == 0)) {
                            if (Configuration.getUserInfoByKey(Constants.KEY_ALLOW_LOAD) == "1")
                                uploadToServer()
                            else
                                view.serverRefuseUpload(BaseAccessibilityService.singleMode)
                        } else {
                            if (Configuration.getUserInfoByKey(Constants.KEY_ALLOW_LOAD) != "1")
                                view.serverRefuseUpload(BaseAccessibilityService.singleMode)
                        }

                        if (last.mode == BaseAccessibilityService.MODE_MORE_USED){
                            view.updateMoreUsedAmount(last.pos_curr)
                        }
                    }

                    override fun _onError(error: String) {
                        view.showHint(error)
                    }
                })
    }

    override fun stopAndUpload() {
        Observable
                .create<List<User>> { emitter ->
                    val users = DB.queryAll(view.context, BaseAccessibilityService.TYPE_ALI, Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
                    if (users != null)
                        emitter.onNext(users)
                    emitter.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ProgressSubscriber<List<User>>(view.context) {
                    override fun _onNext(users: List<User>) {
                        view.stopAndUpload(users)
                    }

                    override fun _onError(error: String) {
                        view.showHint(error)
                    }
                })
    }

    fun uploadToServer() {
        Observable
                .create<List<User>> { emitter ->
                    model.delLocalCSV()
                    val users = DB.queryAll(view.context, if (isAli) BaseAccessibilityService.TYPE_ALI else BaseAccessibilityService.TYPE_WeChat,
                            Configuration.getUserInfoByKey(Constants.KEY_ACCOUNT))
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
                    val des2 = if (isAli) METHOD_ALI else METHOD_WECHAT
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
                            view.uploadComplete(true, BaseAccessibilityService.singleMode)
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