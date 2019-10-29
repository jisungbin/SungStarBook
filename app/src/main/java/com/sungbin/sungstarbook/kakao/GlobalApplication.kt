package com.sungbin.sungstarbook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import com.kakao.auth.KakaoSDK
import com.sungbin.sungstarbook.kakao.KakaoSDKAdapter


class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        obj = this
        KakaoSDK.init(KakaoSDKAdapter())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var obj: GlobalApplication? = null
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var currentActivity: Activity? = null

        fun getGlobalApplicationContext(): GlobalApplication? {
            return obj
        }

        fun getCurrentActivity(): Activity? {
            return currentActivity
        }

        fun setCurrentActivity(currentActivity: Activity) {
            this.currentActivity = currentActivity
        }
    }
}