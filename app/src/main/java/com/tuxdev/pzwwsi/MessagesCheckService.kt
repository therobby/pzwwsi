package com.tuxdev.pzwwsi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.select.Elements
import kotlin.concurrent.thread

class MessagesCheckService : Service() {

    private val mBinder = ThisBinder()


    private val id = 101
    private val channel = "com.tuxdev.pzwwsi.newinfo"
    private var notificationManager : NotificationManager? = null
    private var oldElements : Elements? = null
    private var cookie = mutableMapOf<String,String>()
    private var isLooperWorking = false

    fun setCookie(cookie : MutableMap<String,String>){
        this.cookie = cookie
    }

    fun startLooper(){
        if(!isLooperWorking) {
            isLooperWorking = true
            thread {
                if(Build.VERSION.SDK_INT >= 26) {
                    val chan = NotificationChannel(channel, "New message", NotificationManager.IMPORTANCE_DEFAULT)
                    chan.enableLights(true)
                    chan.lightColor = Color.RED
                    chan.enableVibration(true)
                    chan.vibrationPattern = longArrayOf(100,200,300,100,200,300)
                    notificationManager?.createNotificationChannel(chan)

                }

                Thread.sleep(5000)  // 5 sec
                notify("TEst!","testowankowo! 123")

                while(isLooperWorking){

                    if(oldElements == null)
                        oldElements = getKomunikaty()
                    else {
                        val newElements = getKomunikaty()
                        if(oldElements!!.first().html() != newElements.first().html()){
                            var i = 0
                            while(oldElements!!.first().html() != newElements[i].html()){
                                if(i >= newElements.size - 1) {
                                    i = -1
                                    break
                                }
                                i++
                            }
                            Log.e("Service","i: $i")
                            if(i < 0)
                                break

                            for(j in 0..i){
                                notify(newElements[j].getElementsByClass("news_title").text(),
                                        newElements[j].getElementsByClass("news_content").text())
                            }
                        }
                    Thread.sleep( 60 * 1000)   // min
                    }
                }
            }
        }
    }

    private fun getKomunikaty() : Elements {
        val page = Jsoup.connect("https://student.wwsi.edu.pl/info")
                .cookies(cookie)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementsByClass("news_box")
    }

    private fun notify(title : String, message : String) {
        val notification = if (Build.VERSION.SDK_INT < 26)
            Notification.Builder(this@MessagesCheckService)
        else
            Notification.Builder(this@MessagesCheckService, channel)

        notification
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.img_logowwsi)

        if (Build.VERSION.SDK_INT >= 26)
            notification
                    .setChannelId(channel)

        notificationManager?.notify(id,notification.build())
    }

    override fun onDestroy() {
        isLooperWorking = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return mBinder
    }

    inner class ThisBinder : Binder(){
        fun getService() : MessagesCheckService = this@MessagesCheckService
    }
}