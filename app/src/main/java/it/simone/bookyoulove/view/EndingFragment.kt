package it.simone.bookyoulove.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentEndingBinding


class EndingFragment : Fragment() {

    private lateinit var binding : FragmentEndingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEndingBinding.inflate(inflater, container, false)
        return binding.root
    }


}