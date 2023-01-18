package com.artyom.gpstracker_hdbh.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.databinding.FragmentMainBinding
import com.artyom.gpstracker_hdbh.location.LocationService
import com.artyom.gpstracker_hdbh.utils.DialogManager
import com.artyom.gpstracker_hdbh.utils.checkPermission
import com.artyom.gpstracker_hdbh.utils.showToast

import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding

    // TODO: Rename and change types of parameters
   /* private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

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

        /*Если версия Андройд 8++
        * нужна дополнительная проверка на месте запуска сервиса
        * в любом случае  сервис запустится  в приоритетном фоновом режиме в методе LocationService.onStartCommand() -> startNotification()*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity,LocationService::class.java))
        }
        else{
            activity?.startService(Intent(activity,LocationService::class.java))
        }
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
                /*Имплементация интереса.
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