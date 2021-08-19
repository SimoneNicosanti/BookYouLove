package it.simone.bookyoulove.view.tbr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentTbrBinding


class TbrFragment : Fragment() {

    private lateinit var binding : FragmentTbrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTbrBinding.inflate(inflater, container, false)

        setObservers()
        return binding.root
    }

    private fun setObservers() {

    }

}