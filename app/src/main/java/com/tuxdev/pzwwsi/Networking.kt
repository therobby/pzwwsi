package com.tuxdev.pzwwsi
import android.sax.Element
import android.util.Log
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.select.Elements

class Networking {
    private var loginCookies = mutableMapOf<String,String>()
    private var url = "https://student.wwsi.edu.pl/plany"

    fun login(username : String, password : String) : Boolean {
        val loginForm = Jsoup.connect(url)
                .timeout(2000)
                .method(Connection.Method.GET)
                .userAgent(HttpConnection.DEFAULT_UA)
                .execute()
        this.loginCookies = loginForm.cookies()
        //val loginCookies = mutableMapOf<String,String>()

        val loginResult = Jsoup.connect(url)
                .data("login", username, "password", password, "login_send", "send")
                .cookies(loginCookies)
                .method(Connection.Method.POST)
                .userAgent(HttpConnection.DEFAULT_UA)
                .execute()

        val page = Jsoup.connect(url)
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        val userTag = page.getElementById("loginInfo").text()
        Log.e("Login",userTag)
        return userTag.contains("Zalogowany")
    }

    fun getStudentName() : String{
        val page = Jsoup.connect(url)
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
        return page.getElementById("loginInfo").getElementsByTag("b").first().text()
    }

    fun getKomunikaty() : Elements {
        val page = Jsoup.connect("https://student.wwsi.edu.pl/info")
                .cookies(loginCookies)
                .userAgent(HttpConnection.DEFAULT_UA)
                .get()
         return page.getElementsByClass("news_box")
    }
}