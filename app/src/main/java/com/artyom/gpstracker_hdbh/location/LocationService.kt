package com.artyom.gpstracker_hdbh.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.artyom.gpstracker_hdbh.MainActivity
import com.artyom.gpstracker_hdbh.R

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

        /*Запуск сервиса в фоновом режиме
        * id - 10 идентификатор сервиса
        * notification - уведомление пользоватея в статус баре о том, что в фоновом режиме что-то работает */
        startNotification()
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyLog", "Сервис запустился: onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "Сервис уничтожился: onDestroy()")
        isRunning = false
    }

    /*Запуск  сервиса  в приоритетном фоновом режиме
     * пользователь увидит уведмление, что сервис запущен.
     * При нажатии на уведомление открывается главная активность приложения*/
    private fun startNotification(){

        /*На Android 8++
        * При помощи NotificationManager создать NotificationChannel*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val nChannel: NotificationChannel = NotificationChannel(CHANNEL_ID, "Location Service",NotificationManager.IMPORTANCE_DEFAULT)

            val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(nChannel)
        }

        /*Нажатие на уведомление запускает главную активность приложения в которой запускается ManFragment*/
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,10, notificationIntent, 0)

        /*При помощи Builder получаем объект уведомления*/
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")

            //Выбрать интент с помощью которого будет запкскаться activity
            .setContentIntent(pendingIntent).build()

        /*Запуск  сервиса  в приоритетном фоновом режиме показвает уведомление*/
        startForeground(99,notification)

    }


    companion object{
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
    }

}