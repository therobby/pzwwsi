package com.tuxdev.pzwwsi

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.colorAttr
import org.jetbrains.anko.runOnUiThread
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private var mService : MessagesCheckService? = null
    private var mBound = false
    private val mConnect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MessagesCheckService.ThisBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Intent(this, MessagesCheckService::class.java).also { intent ->
            bindService(intent, mConnect, Context.BIND_AUTO_CREATE)
        }

        zaloguj.setOnClickListener {
            if (login_field.text.isBlank() || password_field.text.isBlank()){
                Snackbar.make(it,resources.getString(R.string.login_empty),Snackbar.LENGTH_SHORT)
                        .show()
                return@setOnClickListener
            }

            val button = zaloguj
            val loading = ProgressBar(this)
            loading.isIndeterminate = true

            login_ll3.removeView(button)
            login_ll3.addView(loading)
            thread {
                val username = login_field.text.toString()
                val password = password_field.text.toString()
                if (!username.isEmpty() && !password.isEmpty()) {
                    if(Main.studentWebsiteConnection.login(username, password)) {
                        val user = Main.studentWebsiteConnection.getStudentName()
                        Log.e("Login",user)
                        if(mService != null)
                            Main.studentWebsiteConnection.setService(mService!!)
                        else
                            Log.e("Login","Service is null!")

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        runOnUiThread {
                            login_ll3.removeView(loading)
                            login_ll3.addView(button)

                            Snackbar.make(it,resources.getString(R.string.login_failed),Snackbar.LENGTH_SHORT)
                                    .show()


                        }
                        // error
                    }

                }
                else {
                    // throw error
                }
            }
        }
    }
}
