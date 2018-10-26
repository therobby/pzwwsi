package com.tuxdev.pzwwsi

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlin.concurrent.thread
import android.content.Intent
import android.util.Log

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var backPressed = false
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_info)

        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = InfoMessages()
        fragmentTransaction.add(R.id.main_act, fragment)
        fragmentTransaction.commit()
        toolbar.title = resources.getString(R.string.nav_info)
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
            R.id.nav_info -> {
                try {
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    val fragment = InfoMessages()
                    fragmentTransaction.replace(R.id.main_act, fragment)
                            .addToBackStack(null)
                            .commit()
                    toolbar.title = resources.getString(R.string.nav_info)
                } catch (e: Exception) {
                    Log.e("Navigation_Change", e.message)
                }
            }
            R.id.nav_plan -> {
                try {
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    val fragment = ScheduleFragment()
                    fragmentTransaction.replace(R.id.main_act, fragment)
                            .addToBackStack(null)
                            .commit()
                    toolbar.title = resources.getString(R.string.nav_plan)
                } catch (e: Exception) {
                    Log.e("Navigation_Change", e.message)
                }
            }
            R.id.nav_logout -> {
                thread{
                    Main.studentWebsiteConnection.logout()
                    runOnUiThread {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }


        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
