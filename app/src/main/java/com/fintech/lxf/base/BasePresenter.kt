package com.fintech.lxf.base

import com.fintech.lxf.net.ApiProducerModule
import com.fintech.lxf.net.ApiService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

interface BasePresenter {
    val compositeDisposable: CompositeDisposable
    val service: ApiService
        get() = ApiProducerModule.create(ApiService::class.java)

    fun bindData()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun clearDisposable() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }

}