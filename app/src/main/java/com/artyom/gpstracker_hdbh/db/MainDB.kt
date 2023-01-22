package com.artyom.gpstracker_hdbh.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackItem::class], version = 1)
 abstract class MainDb : RoomDatabase() {

    /*DAO*/
    abstract fun getDao(): Dao


    companion object{
        @Volatile
         var INSTANCE: MainDb? = null

       /* fun getDatabase(context: Context) : MainDb{
            if(INSTANCE != null){
                return INSTANCE as MainDb
            }
            else{
                synchronized(this){
                    val instance = Room.databaseBuilder(context.applicationContext, MainDb::class.java, "GpsTracker.db").build()
                    return instance
                }
            }
        }*/

        fun getDatabase(context: Context) : MainDb{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, MainDb::class.java, "GpsTracker.db").build()
                INSTANCE = instance
                return instance
            }
        }

    }
}