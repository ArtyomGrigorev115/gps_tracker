package com.artyom.gpstracker_hdbh.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.utils.showToast


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /*Собственно разметка  PreferenceFragmentCompat */
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()

    }

    /*Инициализация выбранного в списке настроек свойства*/
    private fun init(){
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!

        val changeListener: Preference.OnPreferenceChangeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPrefs()
    }

    /*Слушатель изменений значений свойств Preference*/
    private fun onChangeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener(){ pref: Preference, newValue: Any ->

            /*Какие изменения произошли(время/цвет)*/
            when(pref.key){
                "update_time_key" -> onTimeChange(newValue.toString())
                "color_key" -> pref.icon?.setTint(Color.parseColor(newValue.toString()))

            }
            true
        }
    }

    /*timePref Выбор времени */
    private fun onTimeChange(value: String){
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)


        val title = timePref.title.toString().substringBefore(":")
        val pos = valueArray.indexOf(value)

        timePref.title = "$title: ${nameArray[pos]}"
    }

    private fun onColorChange(){

    }

    /*Метод сохраняет настройки Update time  и Color preferences при переходах между фрагментами
    * и показывает пользователю какое значение уже выбрано*/
    private fun initPrefs(){
        val pref = timePref.preferenceManager.sharedPreferences

        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)


        val title = timePref.title
        val pos = valueArray.indexOf(pref?.getString("update_time_key","3000"))

        timePref.title = "$title: ${nameArray[pos]}"

        //colorPref
        val trackColor = pref?.getString("color_key","#FF009EDA")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}