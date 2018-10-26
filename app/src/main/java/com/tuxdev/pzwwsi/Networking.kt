package com.tuxdev.pzwwsi

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.InputStream
import java.io.Serializable

class Networking {
    private var loginCookies = mutableMapOf<String, String>()
    private var url = "https://student.wwsi.edu.pl/plany"
    private var schedule = PlanProcessor()
    private var meetTable: List<Element> = listOf()
    private var group = ""

    fun login(username: String, password: String): Boolean {
        try {
            val loginForm = Jsoup.connect(url)
                    .timeout(2000)
                    .method(Connection.Method.GET)
                    .userAgent(HttpConnection.DEFAULT_UA)
                    .execute()
            this.loginCookies = loginForm.cookies()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        val loginResult = Jsoup.connect(url)
                .data("login", username, "password", password, "login_send", "send")
                .cookies(loginCookies)
                .method(Connection.Method.POST)
                .userAgent(HttpConnection.DEFAULT_UA)
                .execute()

        Log.e("Dupa", loginResult.body().toString())

        val page = Jsoup.connect(url)
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        val userTag = page.getElementById("loginInfo").text()
        Log.e("Login", userTag)

        group = getStudentGroup()

        return userTag.contains("Zalogowany")
    }

    fun getStudentName(): String {
        val page = Jsoup.connect(url)
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementById("loginInfo").getElementsByTag("b").first().text()
    }

    fun getStudentGroup(): String {
        if (group.isEmpty()) {
            val page = Jsoup.connect("https://student.wwsi.edu.pl/mdane")
                    .cookies(loginCookies)
                    .userAgent(HttpConnection.DEFAULT_UA)
                    .get()

            group = page.getElementsByClass("bg_blue").first().getElementsByTag("dd").last().text()
        }
        return group
    }

    private fun getPlanDzienneLink(): String {
        val page = Jsoup.connect("https://student.wwsi.edu.pl/plany")
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementById("rightcolumn").getElementsByClass("plan").first().attr("href")
    }

    fun getPlan(): InputStream {
        val page = Jsoup.connect("https://student.wwsi.edu.pl/${getPlanDzienneLink()}")
                .cookies(loginCookies)
                .ignoreContentType(true)
                .execute()
        return page.bodyStream()
    }

    fun getCurrentMeet(): Int {
        if (meetTable.isEmpty()) {
            val doc = Jsoup.connect("https://student.wwsi.edu.pl/terminy")
                    .userAgent(HttpConnection.DEFAULT_UA)
                    .get()

            meetTable = doc.getElementsByTag("td").take(51)
        }

        val pack = ArrayList<DatePackage>()

        for (i in 0 until meetTable.size step 3) {
            pack.add(DatePackage(meetTable[i].text().toInt(),
                    meetTable[i + 1].text().dropLastWhile { it == '*' },
                    meetTable[i + 2].text().dropLastWhile { it == '*' }))
        }

        var nr = 0
        pack.forEach {
            if (it.currentWeek()) {
                nr = it.nr
                return@forEach
            }
        }

        return nr
    }

    fun getKomunikaty(): Elements {
        val page = Jsoup.connect("https://student.wwsi.edu.pl/info")
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementsByClass("news_box")
    }

    fun logout() {
        Jsoup.connect("https://student.wwsi.edu.pl/,logout")
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
    }

    fun loadSchedule() {
        schedule.load(Main.studentWebsiteConnection.getPlan())
    }

    fun isScheduleLoaded() = schedule.isLoaded()

    fun getScheduleDays(group: String) = schedule.getDays(group)

    fun setService(context: Context) {
        val service = Intent(context, MessagesCheckService::class.java)
        service.putExtra("cookie", loginCookies as Serializable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service)
        else
            context.startService(service)
    }
}