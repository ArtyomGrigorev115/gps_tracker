package com.artyom.gpstracker_hdbh

import android.app.Application
import com.artyom.gpstracker_hdbh.db.MainDb

class MainApp: Application() {

    val database by lazy() {MainDb.getDatabase(this)}
}