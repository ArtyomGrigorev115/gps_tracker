package com.artyom.gpstracker_hdbh.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.artyom.gpstracker_hdbh.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /*Собственно разметка  PreferenceFragmentCompat */
        setPreferencesFromResource(R.xml.main_preference, rootKey)
    }

}