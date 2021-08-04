package it.simone.bookyoulove.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsBinding


class ChartsFragment : Fragment() {

    private lateinit var binding : FragmentChartsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

}