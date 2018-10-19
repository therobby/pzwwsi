package com.tuxdev.pzwwsi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.select.Elements
import java.util.*
import kotlin.concurrent.thread

class MessagesCheckService : Service() {
    private val id = 101
    private val channel = "WWSI New Info"
    private var oldElements: Elements? = null
    private var cookie = mutableMapOf<String, String>()
    private var isLooperWorking = false

    private fun startLooper() {
        if (!isLooperWorking) {
            isLooperWorking = true

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= 26) {
                val chan = NotificationChannel(channel, "New message", NotificationManager.IMPORTANCE_DEFAULT)
                chan.enableLights(true)
                chan.lightColor = Color.RED
                chan.enableVibration(true)
                chan.vibrationPattern = longArrayOf(100, 200, 300)
                if(!notificationManager.notificationChannels.contains(chan))
                    notificationManager.createNotificationChannel(chan)

            }

            thread {
                notify(notificationManager, "TEst!", Random().nextInt().toString())
                While@while (isLooperWorking) {
                    Log.e("Loopy", "Looper")
                    Thread.sleep(60 * 1000)   // 1 min

                    try {
                        if (oldElements == null)
                            oldElements = getMessages()
                        else {
                            val newElements = getMessages()
                            if (oldElements!!.first().html() != newElements.first().html()) {
                                var i = 0
                                while (oldElements!!.first().html() != newElements[i].html()) {
                                    if (i >= newElements.size - 1) {
                                        i = -1
                                        break
                                    }
                                    i++
                                }
                                Log.e("Service", "i: $i")
                                if (i < 0)
                                    break@While

                                for (j in 0..i) {
                                    notify(notificationManager,
                                            newElements[j].getElementsByClass("news_title").text(),
                                            newElements[j].getElementsByClass("news_content").text())
                                }
                            }
                        }
                    }catch (e : Exception){
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun getMessages(): Elements {
        Log.e("Service", "Get Messages")
        val page = Jsoup.connect("https://student.wwsi.edu.pl/info")
                .cookies(cookie)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementsByClass("news_box")
    }

    private fun notify(notificationManager : NotificationManager, title: String, message: String) {
        val notification = if (Build.VERSION.SDK_INT < 26)
            @Suppress("DEPRECATION")
            Notification.Builder(this@MessagesCheckService)
        else
            Notification.Builder(this@MessagesCheckService, channel)

        notification
                .setContentTitle(title)
                .setContentText(message)
                //.setLargeIcon(R.mipmap.logo_foreground)
                .setSmallIcon(R.mipmap.logo_foreground)

        if (Build.VERSION.SDK_INT >= 26)
            notification
                    .setChannelId(channel)

        notificationManager.notify(id, notification.build())
    }

    override fun onCreate() {
        val foregroundNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "WWSI Messages Checker"
                    val channelName = "WWSI Messages Checker"
                    val chan = NotificationChannel(channelId,
                            channelName, NotificationManager.IMPORTANCE_HIGH)
                    //chan.lightColor = Color.BLUE
                    chan.importance = NotificationManager.IMPORTANCE_NONE
                    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                    if(!foregroundNotificationManager.notificationChannels.contains(chan))
                        foregroundNotificationManager.createNotificationChannel(chan)
                    channelId
                } else {
                    ""
                }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.logo_foreground)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId(channelId)
                .build()
        startForeground(id + 1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.e("Service", "Start")
        @Suppress("UNCHECKED_CAST")
        cookie = intent.getSerializableExtra("cookie") as MutableMap<String, String>
        startLooper()

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        isLooperWorking = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}