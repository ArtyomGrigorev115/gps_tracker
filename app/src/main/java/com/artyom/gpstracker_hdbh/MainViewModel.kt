package com.artyom.gpstracker_hdbh

import androidx.lifecycle.*
import com.artyom.gpstracker_hdbh.db.MainDb
import com.artyom.gpstracker_hdbh.db.TrackItem
import com.artyom.gpstracker_hdbh.location.LocationModel
import kotlinx.coroutines.launch

class MainViewModel(db: MainDb) : ViewModel() {

    /*DAO*/
    val dao = db.getDao()

    /*обновление данных в LocationModel*/
    val locationUpdates = MutableLiveData<LocationModel>()

    /*обновление textview таймера*/
    val timeData = MutableLiveData<String>()

    val tracks = dao.getAllTracks().asLiveData()

    /*Метод добавляет запись в БД*/
    fun insertTrack(trackItem: TrackItem) = viewModelScope.launch {
        dao.insertTrack(trackItem)
    }

    /*Метод удаляет запись из БД*/
    fun deleteTrack(trackItem: TrackItem) = viewModelScope.launch {
        dao.deleteTrack(trackItem)
    }


    /*Статическая фабрика, котора создаёт MainViewModel с конструктором в который передаётся
    * экземпляр базы данных */
    @Suppress("UNCHECKED_CAST")
    class ViewModelFactory(private val db: MainDb) : ViewModelProvider.Factory{

        @Throws(IllegalArgumentException::class)
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if(modelClass.isAssignableFrom(MainViewModel::class.java)){
                //БД теперь на месте
                return MainViewModel(db) as T
            }
            throw IllegalArgumentException("Unknow ViewModel class")

        }
    }

}