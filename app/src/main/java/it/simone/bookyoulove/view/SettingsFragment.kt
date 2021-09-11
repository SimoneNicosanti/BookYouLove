package it.simone.bookyoulove.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import it.simone.bookyoulove.R


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setViewEnable(true, requireActivity())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
