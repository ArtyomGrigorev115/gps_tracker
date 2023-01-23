package com.artyom.gpstracker_hdbh.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.artyom.gpstracker_hdbh.MainApp
import com.artyom.gpstracker_hdbh.MainViewModel
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.databinding.FragmentMainBinding
import com.artyom.gpstracker_hdbh.db.TrackItem
import com.artyom.gpstracker_hdbh.location.LocationModel
import com.artyom.gpstracker_hdbh.location.LocationService
import com.artyom.gpstracker_hdbh.utils.DialogManager
import com.artyom.gpstracker_hdbh.utils.TimeUtils
import com.artyom.gpstracker_hdbh.utils.checkPermission
import com.artyom.gpstracker_hdbh.utils.showToast

import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private var locationModel: LocationModel? = null

    /*Сущность*/
   // private var trackItem: TrackItem? = null


    private var polyline: Polyline? = null
    private var isServiceRunning = false
    private var timer: Timer? = null
    private var firstStart = true

    /*стартовое время таймера*/
    private var startTime = 0L

    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding

    /*MainViewModel*/
    private val model: MainViewModel by activityViewModels{

        /*В данном случае контекстом выступает класс MainApp : Application в котором инициализируется
        * экземпляр базы данных. Его и передаём в MainViewModel*/
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        settingsOsm()
        Log.d("MyLog", "Вызван: onCreateView()")
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MyLog", "Вызван: onViewCreated()")
        registerPermissions()
      //  checkLocPermission()

        /*Запуск Сервиса*/
        //getActivity()?.startService(Intent(getActivity(),LocationService::class.java))

        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()

        /*проверка БД*/
       /* model.tracks.observe(viewLifecycleOwner){
            Log.d("MyLog", "Элементов в базе данных: ${it.size}")
        }*/


    }

    /*Повесить слушатель на нужные кнопки*/
    private fun setOnClicks() = with(binding){
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)

    }

    /*Слушатель кнопок*/
    private fun onClicks(): View.OnClickListener{

        return object: View.OnClickListener{
            override fun onClick(view: View?) {
                when(view!!.id){
                    R.id.fStartStop -> startStopService()
                }
            }
        }
    }

    /*Инициализация Observer-a MainViewModel.locationUpdates*/
    private fun locationUpdates(){

        model.locationUpdates.observe(viewLifecycleOwner) {
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"
            val velocity = "Velocity: ${String.format("%.1f", it.velocity)} m/s; ${
                String.format(
                    "%.1f",
                    3.6f * it.velocity
                )
            } km/h"
            val avgVelocity = "Average Velocity: ${getAverageSpeed(it.distance)} km/h"
            Log.d("MyLog", "Средняя скорость $avgVelocity")

            binding.tvDistance.text = distance
            binding.tvVelocity.text = velocity
            binding.tvAverageVel.text = avgVelocity

            /* Как только данные изменились, то сразу собираем Entity*/
          /*  trackItem = TrackItem(
                null,
                getCurrentTime(),
                TimeUtils.getDate(),
                String.format("%.1f", it.distance / 1000),
                getAverageSpeed(it.distance),
                geopointsToString(it.geoPointList)
            )*/

            locationModel = it

            /*обновление списка пройденных точек маршрута*/
            updatePolyline(it.geoPointList)
        }
    }

    /*обновление TextView по таймеру*/
    private fun updateTime(){

        /*Observer - ждет, когда обновится информация*/
        model.timeData.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }


    /*Инициализация таймера*/
    private fun startTimer(){
        /*если таймер работает, то остановить таймер*/
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime

        /**/
        timer?.schedule(object : TimerTask(){
            override fun run() {

                /*перенести запуск на основной поток*/
                activity?.runOnUiThread(){
                    model.timeData.value = getCurrentTime()
                }
            }
        },1000, 1000)
    }

    /**
     * Get average speed
     *
     * Вычисляет средн. скорость движения
     * @param distance - дистанция
     * @return средняя скорость движения
     */
    private fun getAverageSpeed(distance: Float): String{
        return String.format("%.1f", 3.6f * (distance /  ((System.currentTimeMillis() - startTime) / 1000)) )
    }

    /*Берёт текущее время*/
    private fun getCurrentTime(): String{
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }


    private fun geopointsToString(list: List<GeoPoint>): String{
        val sb = java.lang.StringBuilder()
        list.forEach {
            sb.append("${it.latitude},${it.longitude}/")
        }


        Log.d("MyLog", "geopointsToString(): $sb")
        return sb.toString()
    }



    /*запусить/остановить сервис
    * сервис не запущен -> запустить
    * сервис запущен -> остановить */
    private fun startStopService(){
        if(!isServiceRunning){
            startLocService()
        }
        else{
            activity?.stopService(Intent(activity,LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()


            val track = getTrackItem()
            DialogManager.showSaveDialog(requireContext(), track, object : DialogManager.Listener{
                override fun onClick() {
                    showToast("Маршрут сохранен в базу!")
                    model.insertTrack(track)
                }

            })
        }

        /*В любом случае нужно поменять значение на противоположное:
        * если сервис не запущен тогда false = !false
        * если сервис уже был запущен тогда true = !true
        * Если этого не сделать, то сервис всегда будет перезапускаться, остановить его кнопкой не получится*/
        isServiceRunning = !isServiceRunning
    }

    /*Метод собирает экземплр сущности*/
    private fun getTrackItem(): TrackItem{
       val  trackItem: TrackItem = TrackItem(
            null,
            getCurrentTime(),
            TimeUtils.getDate(),
            String.format("%.1f", locationModel?.distance?.div(1000) ?: 0),
            getAverageSpeed(locationModel?.distance ?: 0.0F),
            geopointsToString(locationModel?.geoPointList ?: listOf())
        )
        return trackItem
    }


    /*Проверяет состояие сервиса по его статической переменной LocationService.isRunning*/
    private fun checkServiceState(){
        isServiceRunning = LocationService.isRunning

        if(isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            /*Если сервис запущено, то и таймер должен идти*/
            startTimer()
        }
    }

    /*Метод запускает сервис*/
    private  fun startLocService(){
        /*Если версия Андройд 8++
        * нужна дополнительная проверка на месте запуска сервиса
        * в любом случае  сервис запустится  в приоритетном фоновом режиме в методе LocationService.onStartCommand() -> startNotification()*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity,LocationService::class.java))
        }
        else{
            activity?.startService(Intent(activity,LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyLog", "Вызван: onResume()")
        /*Проверять разрешения и GPS нужно тут, всякий раз,
        когда фрагмент возвращатеся в активное состояние после метода onPause()*/
        checkLocPermission()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MyLog", "Вызван: onPause()")
    }



    /*Настройки библиотеки osmdroid OpenStreetMapTool
        * В реальном времени качает карты из интернета и показывает их в MapView*/
    private fun settingsOsm(){
        Configuration.getInstance().load(activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    /*определяет местополоение пользователя на карте*/
    private fun initOSM() = with(binding){
        //Полилиния маршрута
        polyline = Polyline()
        polyline?.outlinePaint?.color = Color.BLUE
        //Зум на карте
        map.controller.setZoom(20.0)

        //GPS-провидер, который возвращает местоположение
        val mLocProvider = GpsMyLocationProvider(getActivity())

        //Слой, который отрисовывывает местоположениена карте MapView
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)

        //Включить определение местоположения устройства
        mLocOverlay.enableMyLocation()

        //Карта следует за пользователем, когда он передвигается
        mLocOverlay.enableFollowLocation()

        //метод запускается, как только местоположение было получено
        mLocOverlay.runOnFirstFix {

            //Очистка всех слоёв
            map.overlays.clear()

            //Добавить слои
            map.overlays.add(mLocOverlay)
            //Добавить слой полилинию
            map.overlays.add(polyline)
        }

        //показать нужную точку на карте
       // map.controller.animateTo(GeoPoint(40.4167, -3.70325))
    }

    /*инициализация  Лаунчера pLauncher*/
    private fun registerPermissions(){

        pLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){

            /*Если придёт null, то проверка на true, игаче if() не понимает
            * Пользователь дал разрешения?*/
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                //Загрузка карты итд
                initOSM()
                checkLocationEnabled()
            }
            else{
                showToast("Вы не дали разрешения на использование местоположения!")
            }
        }
    }

    /*Проверка версии Андройда*/
    private fun checkLocPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ){
            checkPermissionAfter10()
        }
        else{
            checkPermissionBefore10()
        }
    }

    /*Метод определяет GPS вкл/выкл*/

    /*Для версии Ведройда 10++
    * Проверка двух разрешений
    * для активного и фонового режима работы*/
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10(){
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ){
            showToast("проверка двух разрешений")
            initOSM()
            checkLocationEnabled()
        }
        else{
            showToast("Запук диалогового окна")
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

    /*Для Андройд версии ниже 10*/
    private fun checkPermissionBefore10(){
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            initOSM()
            checkLocationEnabled()
        }
        else{
            /**/
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    /*Метод определяет GPS вкл/выкл*/
    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isEnabled){
           // showToast("GPS выключен!")

            DialogManager.showLocEnableDialog(activity as AppCompatActivity, object: DialogManager.Listener{
                /*Имплементация интерфейса.
                Логика обработки нажатия на кнопку вынесена из DialogManager в MainFragment
                Тут открываем окно  приложения настроек смартфона*/
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
        else{
            showToast("Location enabled!")
        }
    }

    /*приёмник широковещательных сообщений */
    private val receiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            if(intent?.action == LocationService.LOC_MODEL_INTENT){
                val locModel = intent.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                Log.d("MyLog", "В фрагменте ${this.javaClass::getName} LocalViewModel: ${locModel}")

                /*передача новых данных в модель. Сразу запускает observer*/
                model.locationUpdates.value = locModel
            }
        }
    }

    /*регистрация приёмника*/
    private fun registerLocReceiver(){

        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        val locBroadcastManager = LocalBroadcastManager.getInstance(activity as AppCompatActivity)
        locBroadcastManager.registerReceiver(
            receiver,
            //фильтр интентов, которые нужно принять
            locFilter
        )
    }

    /*Метод добавляет точки в список точек*/
    private fun addPoint(list: List<GeoPoint>){
        polyline?.addPoint(list[list.size - 1])
    }

    /*Метод подгружает все точки после того, как приложение закрыли и заново открыли
    * сервис всё это время работал, но точки в полилинию не добавлялись*/
    private fun fillPolyline(list: List<GeoPoint>){
        list.forEach{
            polyline?.addPoint(it)
        }
    }

    /*Метод определяет нужно ли подгружать точки из массива
    * если приложение было закрыто/открыто, то грузит точки и больше fillPolyline() не вызвается,
    * а вызывается метод addPoint(), который добавляет по одной точке в список*/
    private fun updatePolyline(list: List<GeoPoint>){
        if(list.size > 1 && firstStart){
            fillPolyline(list)
            firstStart = false
        }
        else{
            addPoint(list)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("MyLog", "MainFragment отсоединился")

        LocalBroadcastManager.getInstance(activity as AppCompatActivity).unregisterReceiver(receiver)

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}