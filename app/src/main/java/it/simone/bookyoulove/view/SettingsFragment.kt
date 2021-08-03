package it.simone.bookyoulove.view

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat
import it.simone.bookyoulove.R
import it.simone.bookyoulove.viewmodel.EndedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val endedVM : EndedViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle? , rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_fragment, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "endedOrderPreference" -> {
                endedVM.changedEndedArrayOrder = true
                CoroutineScope(Dispatchers.Main).launch {
                    endedVM.sortBookArray()
                    Log.i("Nicosanti", "Settings : Cambiato Order")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}