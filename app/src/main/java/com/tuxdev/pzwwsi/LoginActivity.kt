package com.tuxdev.pzwwsi

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        zaloguj.setOnClickListener {
            thread {
                val username = login_field.text.toString()
                val password = password_field.text.toString()
                if (!username.isEmpty() && !password.isEmpty()) {
                    if(Main.studentWebsiteConnection.login(username, password)) {
                        val user = Main.studentWebsiteConnection.getStudentName()
                        Log.e("Login",user)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else{
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
