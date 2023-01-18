package com.artyom.gpstracker_hdbh

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.artyom.gpstracker_hdbh.location.LocationModel

class MainViewModel : ViewModel() {
    /*обновление данных в LocationModel*/
    val locationUpdates = MutableLiveData<LocationModel>()

    /*обновление textview таймера*/
    val timeData = MutableLiveData<String>()

}