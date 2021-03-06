package it.simone.bookyoulove.view


import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import it.simone.bookyoulove.R


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_fragment, rootKey)

        findPreference<Preference>("googleDriveEnter")?.onPreferenceClickListener = this

    }

    override fun onPreferenceClick(preference: Preference?): Boolean {

        if (preference?.key == "googleDriveEnter") {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToGoogleDriveFragment())
            return true
        }
        return false
    }
}
