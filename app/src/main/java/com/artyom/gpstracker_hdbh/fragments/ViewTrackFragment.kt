package com.artyom.gpstracker_hdbh.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.artyom.gpstracker_hdbh.MainApp
import com.artyom.gpstracker_hdbh.MainViewModel
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.security.Policy

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewTrackFragment : Fragment() {

    private var startPoint: GeoPoint? = null

    /*MainViewModel*/
    private val model: MainViewModel by activityViewModels{

        /*В данном случае контекстом выступает класс MainApp : Application в котором инициализируется
        * экземпляр базы данных. Его и передаём в MainViewModel*/
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    private lateinit var binding: ViewTrackBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        settingsOsm()
        // Inflate the layout for this fragment
        binding = ViewTrackBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack()

        binding.fCenter.setOnClickListener(){
            if(startPoint != null){
                binding.map.controller.animateTo(startPoint)
            }

        }
    }

    /*Из лайфдата вытаскием информацио из TrackItem*/
    private fun getTrack() = with(binding){
        model.currentTrack.observe(viewLifecycleOwner){

            val speed = "Average speed: ${it.speed} km/h"
            val distance = "Distance: ${it.distance} km"
            tvData.text = it.date
            tvTime.text = it.time
            tvAverageVel.text = speed
            tvDistance.text = distance

            /*отрисовка линии маршрута на карте*/
            val  polyline = getPolyline(it.geoPoints)
            map.overlays.add(polyline)

            setMarkers(polyline.actualPoints)
            goToStartPosition(polyline.actualPoints[0])
            startPoint = polyline.actualPoints[0]
        }
    }

    /*Зум на точку начала маршрута*/
    private fun goToStartPosition(startPosition: GeoPoint){

        /*сделать зум на начало маршрута*/
        binding.map.controller.zoomTo(18.0)
        binding.map.controller.animateTo(startPosition)
    }

    /*Метод устанавливает маркеры на карту*/
    private fun setMarkers(list: List<GeoPoint>) = with(binding){
        val startMarker = Marker(map)
        val finishMarker = Marker(map)

        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        startMarker.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_start_position)
        finishMarker.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_finish_position2)

        startMarker.position = list[0]
        finishMarker.position = list[list.size - 1]
        map.overlays.add(startMarker)
        map.overlays.add(finishMarker)
    }


    /*Метод собирает  Полилинию по координатам из массива*/
    private fun getPolyline(geoPoints: String): Polyline {
        val polyline = Polyline()
        polyline.outlinePaint.color = Color.parseColor(PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("color_key", "#FF009EDA")
        )

        val list = geoPoints.split("/") // lat:44.556, lon: -7.455

        list.forEach {
            if(it.isEmpty()) return@forEach
            val points = it.split(",") // 44.44 -7.34

            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
    }





    /*Настройки библиотеки osmdroid OpenStreetMapTool
           * В реальном времени качает карты из интернета и показывает их в MapView*/
    private fun settingsOsm(){
        Configuration.getInstance().load(activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
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
        fun newInstance() = ViewTrackFragment()
    }
}