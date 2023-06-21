package com.monke.mopermission

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class MoPermissionBus {
    companion object {
        private var instance: MoPermissionBus? = null

        internal fun getInstance(): MoPermissionBus? {
            if (instance == null) {
                synchronized(MoPermissionBus::class.java) {
                    if (instance == null) {
                        instance = MoPermissionBus()
                    }
                }
            }
            return instance
        }
    }

    private val liveDatas = HashMap<String, MutableLiveData<List<String>>>()

    private constructor()

    @MainThread
    internal fun sendData(key: String, permission: List<String>?) {
        if (liveDatas.contains(key)) {
            liveDatas.getValue(key).value = permission
            liveDatas.remove(key)
        }
    }

    internal fun removeLiveData(key: String?) {
        if (liveDatas.contains(key)) {
            liveDatas.remove(key)
        }
    }

    @MainThread
    internal fun registerJustReportOnce(key: String, observer: Observer<List<String>>) {
        if (!liveDatas.contains(key)) {
            val liveData: MutableLiveData<List<String>> = MutableLiveData()
            liveDatas[key] = liveData
            liveData.observeForever(observer)
        }
    }
}