package com.artyom.gpstracker_hdbh.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.artyom.gpstracker_hdbh.MainActivity
import com.artyom.gpstracker_hdbh.R
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import org.osmdroid.util.GeoPoint

class LocationService : Service() {

    /* суммированная дистанция*/
    private var distance = 0.0f

    private var lastLocation: Location? = null

    /*FusedLocationProviderClient*/
    private lateinit var locProvider: FusedLocationProviderClient

    /*запрос местоположения*/
    private lateinit var locRequest: LocationRequest

    /*список точек маршрута*/
    private lateinit var geoPointsList: ArrayList<GeoPoint>


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
        startLocationUpdates()
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        geoPointsList = ArrayList()
        Log.d("MyLog", "Сервис запустился: onCreate()")
        initLocation()



    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "Сервис уничтожился: onDestroy()")
        isRunning = false
        locProvider.removeLocationUpdates(lockCallBack)
    }

    /*callback для locProvider.requestLocationUpdates
    * принимает данные о местоположении от FusedLocationProviderClient*/
    private val lockCallBack = object : LocationCallback(){

        /*сведения о местоположении доступны*/
        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
        }

        /*получить результат о местоположении*/
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            /*Вычисление пройденной дистанции путём суммирования разницы между  ними*/
            if(lastLocation != null && currentLocation != null){

               //Todo: на эмуляторе со скоростью проблеммы. Пока без проверки скорости
               /* if(currentLocation.speed > 0.2){
                    *//*суммирование разнци между старым местоположением и новым. Тем местоположением, которое прило в метод*//*
                    distance += lastLocation?.distanceTo(currentLocation ?: lastLocation) ?: 0.0f
                }*/

                distance += lastLocation?.distanceTo(currentLocation ?: lastLocation) ?: 0.0f
                //при каждом обновлении местоположения точка добавляется в список
                geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))

                val locModel = LocationModel(currentLocation.speed, distance, geoPointsList)

                sendLocData(locModel)
            }


            /*новое местоположение смартфона записать в старое местоположение*/
            lastLocation = currentLocation


            Log.d("MyLog", "Местоположение: ${locationResult.lastLocation?.latitude}")
            Log.d("MyLog", "Дистанция: $distance")
        }
    }

    /*передаёт  сведения о местоположении, скорости, список точек
     * (объёкт LocationModel) в MainFragment*/
    private fun sendLocData(locModel: LocationModel){
        val intent = Intent(LOC_MODEL_INTENT)
        intent.putExtra(LOC_MODEL_INTENT, locModel)

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

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

        /*При помощи Builder получить объект уведомления*/
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")

            //Выбрать интент с помощью которого будет запкскаться activity
            .setContentIntent(pendingIntent).build()

        /*Запуск  сервиса  в приоритетном фоновом режиме показвает уведомление*/
        startForeground(99,notification)

    }

    /*инициализация объекта FusedLocationProviderClient и LocationRequest*/
    private fun initLocation(){
        locRequest = LocationRequest.create()

        /*интервал обновлений, который в настройках выберает пользователь*/
        locRequest.interval = 5000

        /*Самый быстрый интервал обновления местоположения*/
        locRequest.fastestInterval = 5000

        locRequest.priority = PRIORITY_HIGH_ACCURACY

        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    /*Подписывается на получение сведений о местоположении*/
    private fun startLocationUpdates(){

        /*Проверка разрешений*/
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        /*запросить сведения о местоположении*/
        locProvider.requestLocationUpdates(
            locRequest,
            lockCallBack,
            Looper.myLooper()
        )
    }


    companion object{

        const val LOC_MODEL_INTENT = "loc_intent"
        const val CHANNEL_ID = "channel_1"
        var isRunning = false

        /*Время запуска сервиса и таймера*/
        var startTime = 0L
    }

}