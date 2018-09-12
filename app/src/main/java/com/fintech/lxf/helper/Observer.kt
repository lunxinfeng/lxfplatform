package com.fintech.lxf.helper


/**
 * 被观察者
 * Created by lxf on 18-4-28.
 */
interface Observerable<out T> {
    fun addListener(observer: Observer<T>)
    fun removeListener(observer: Observer<T>)
    fun notifyObserver()
}

/**
 * 观察者
 * Created by lxf on 18-4-28.
 */
interface Observer<in T> {
    fun update(oldValue: T, newValue: T)
}

class ObserverBoolean(private var value: Boolean = false) : Observerable<Boolean> {

    private var oldValue = false
    private var newValue = false
    private val observers = mutableListOf<Observer<Boolean>>()

    fun set(value: Boolean) {
        oldValue = this.value
        newValue = value
        this.value = value
        notifyObserver()
    }

    fun get() = value

    override fun addListener(observer: Observer<Boolean>) {
        observers.add(observer)
    }

    override fun removeListener(observer: Observer<Boolean>) {
        observers.remove(observer)
    }

    override fun notifyObserver() {
        observers.forEach { it.update(oldValue, newValue) }
    }
}

class ObserverInt(private var value: Int = 0) : Observerable<Int> {

    private var oldValue = 0
    private var newValue = 0
    private val observers = mutableListOf<Observer<Int>>()

    fun set(value: Int) {
        oldValue = this.value
        newValue = value
        this.value = value
        notifyObserver()
    }

    fun get() = value

    override fun addListener(observer: Observer<Int>) {
        observers.add(observer)
    }

    override fun removeListener(observer: Observer<Int>) {
        observers.remove(observer)
    }

    override fun notifyObserver() {
        observers.forEach { it.update(oldValue, newValue) }
    }
}