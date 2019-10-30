package com.sungbin.sungstarbook.utils


import android.content.Context
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.sungbin.sungstarbook.dto.MyInformationItem
import com.sungbin.sungstarbook.notification.NotificationManager
import java.lang.Exception

object FirebaseUtils {
    fun subscribe(topic: String, ctx: Context){
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
        catch (e: Exception) {
            Utils.error(ctx, "주제 구독 중에 오류가 발생했습니다.\n${e.message}")
        }
    }

    fun unSubscribe(topic: String, ctx: Context){
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
        catch (e: Exception){
            Utils.error(ctx, "주제 구독 해제 중에 오류가 발생했습니다.\n${e.message}")
        }
    }

    fun showNoti(title:String, content:String, topic: String){
        NotificationManager.sendNotiToFcm(title, content, topic)
    }
}