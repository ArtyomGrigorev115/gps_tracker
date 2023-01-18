package com.artyom.gpstracker_hdbh.location

import org.osmdroid.util.GeoPoint

/*В модель данных записвается информация, которая приходит из сервиса
* Модель передаём в фрагмент*/
data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,

    /*список координат точек по которым строится маршрут
    *GeoPoint(lat,lon)координаты одной точки */
    val geoPointList: ArrayList<GeoPoint>
    ) : java.io.Serializable
