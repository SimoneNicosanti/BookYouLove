package it.simone.bookyoulove.view

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import it.simone.bookyoulove.R



class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle? , rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_fragment, rootKey)
    }

}