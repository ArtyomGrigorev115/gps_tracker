package com.artyom.gpstracker_hdbh.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")


    /*Из системного времени в форматированное время*/
    fun getTime(timeInMillis: Long): String{

        val calendar: Calendar = Calendar.getInstance()

        /*00:00:00*/
        timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"))

        calendar.setTimeInMillis(timeInMillis)
        val date: Date = calendar.getTime()

        return timeFormatter.format(date)

    }

    fun getDate(): String{
        val calendar = Calendar.getInstance()
        return dateFormatter.format(calendar.getTime())
    }



}