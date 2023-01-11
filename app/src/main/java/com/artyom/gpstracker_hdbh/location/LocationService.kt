package com.artyom.gpstracker_hdbh.location

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        //Коммуникационный канал с активностью
        return null
    }

    /*
    * START_STICKY сервис перезапускается, после убийства системой в связи с нехваткой памяти.
    * Когда появляется свободная память, то сервис перезапускается
    *
    * START_NOT_STICKY - сервис не перезапускается
    * После перезапуска сервиса он теряет все свои данные*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}