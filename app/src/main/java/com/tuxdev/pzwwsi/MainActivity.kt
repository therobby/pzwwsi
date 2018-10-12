package com.tuxdev.pzwwsi

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.contentView
import kotlin.concurrent.thread
import android.content.Intent
import android.util.Log


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var backPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        thread {
            Main.studentWebsiteConnection.getKomunikaty().forEach {
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflater.inflate(R.layout.template_message_container, null)  // noot noot

                runOnUiThread {
                    view.findViewById<TextView>(R.id.message_title).text =
                            it.getElementsByClass("news_title").text()

                    view.findViewById<TextView>(R.id.message_date).text =
                            it.getElementsByClass("news_podpis").text()

                    view.findViewById<TextView>(R.id.message_data).text =
                            it.getElementsByClass("news_content").text()

                    main_scroll_layout.addView(view)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (!backPressed) {
                Snackbar.make(currentFocus, resources.getString(R.string.main_back_pressed), Snackbar.LENGTH_LONG)
                        .setDuration(2000)
                        .show()

                backPressed = true
                thread {
                    Thread.sleep(2000)
                    backPressed = !backPressed
                }
            } else {
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
