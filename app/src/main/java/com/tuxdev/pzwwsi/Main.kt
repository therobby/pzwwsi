package com.tuxdev.pzwwsi

import android.app.Application

class Main : Application() {
    companion object {
        val studentWebsiteConnection = Networking()
    }
}