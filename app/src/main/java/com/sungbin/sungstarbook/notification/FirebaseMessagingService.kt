package com.sungbin.sungstarbook.notification

import com.google.firebase.messaging.RemoteMessage
import com.sungbin.sungstarbook.utils.Utils
import me.leolin.shortcutbadger.ShortcutBadger


class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        //if(Utils.checkMyAppRunning(applicationContext)) return
        NotificationManager.setGroupName("SungStarBook")
        NotificationManager.createChannel(applicationContext,
            "Message Notification",
            "SungStarBook Message Notification")
        NotificationManager.showNormalNotification(applicationContext,
            1, remoteMessage!!.data["title"]!!,
            remoteMessage.data["body"]!!)
        val preBadge = Utils.readData(applicationContext, "badge", "1")!!.toInt()
        val nowBadge = preBadge + 1
        Utils.saveData(applicationContext, "badge", nowBadge.toString())
        ShortcutBadger.applyCount(applicationContext, nowBadge)
    }

    override fun onNewToken(token: String?) {
        Utils.saveData(applicationContext, "fcmToken", token!!)
    }

}

