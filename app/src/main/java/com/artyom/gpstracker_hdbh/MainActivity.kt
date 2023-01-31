package com.artyom.gpstracker_hdbh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.artyom.gpstracker_hdbh.databinding.ActivityMainBinding
import com.artyom.gpstracker_hdbh.fragments.MainFragment
import com.artyom.gpstracker_hdbh.fragments.SettingsFragment
import com.artyom.gpstracker_hdbh.fragments.TracksFragment
import com.artyom.gpstracker_hdbh.utils.openFragment
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBottomNavClicks()

        /*Если нчего нажато не было,
        то в любом случае открывается стартовый фрагмент*/
        openFragment(MainFragment.newInstance())
    }

    /**
     * Переключение между фрагментами
     *
     */
    private fun onBottomNavClicks(){

        binding.bNan.setOnItemSelectedListener {
            when(it.itemId){
                R.id.id_home -> openFragment(MainFragment.newInstance())
                R.id.id_tracks -> openFragment(TracksFragment.newInstance())
                R.id.id_settings -> openFragment(SettingsFragment())
            }
            true
        }
    }
}
