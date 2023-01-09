package com.artyom.gpstracker_hdbh.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.utils.showToast


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timePref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /*Собственно разметка  PreferenceFragmentCompat */
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    /*Инициализация выбранного в списке настроек свойства*/
    private fun init(){
        timePref = findPreference("update_time_key")!!

        val changeListener: Preference.OnPreferenceChangeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
    }

    /*Слушатель изменений значений свойств Preference*/
    private fun onChangeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener(){ pref: Preference, value: Any ->

            val nameArray = resources.getStringArray(R.array.loc_time_update_name)
            val valueArray = resources.getStringArray(R.array.loc_time_update_value)


            val title = pref.title.toString().substringBefore(":")
            val pos = valueArray.indexOf(value)

            pref.title = "$title: ${nameArray[pos]}"

            true
        }
    }
}