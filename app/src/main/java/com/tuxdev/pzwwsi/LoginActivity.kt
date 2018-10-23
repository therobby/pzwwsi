package com.tuxdev.pzwwsi

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_field.setText(defaultSharedPreferences.getString("username", ""))

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

                        defaultSharedPreferences
                                .edit()
                                .putString("username", login_field.text.toString())
                                .apply()

                        //Main.studentWebsiteConnection.setService(this)

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

    override fun onBackPressed() {
        //super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}
