package it.simone.bookyoulove

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.simone.bookyoulove.databinding.FragmentGuessTheQuoteOpenBinding
import it.simone.bookyoulove.viewmodel.GuessTheQuoteOpenViewModel


class GuessTheQuoteOpenFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentGuessTheQuoteOpenBinding

    private val gtqOpenVM : GuessTheQuoteOpenViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuessTheQuoteOpenBinding.inflate(inflater, container, false)

        binding.let {
            it.gtqOpenFragmentPlayButton.setOnClickListener(this)
            it.gtqOpenFragmentHistoryButton.setOnClickListener(this)
        }
        setObservers()
        gtqOpenVM.canPlayVerify()
        return binding.root
    }

    private fun setObservers() {
        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            binding.run {
                gtqOpenFragmentPlayButton.isEnabled = !isAccessing
                gtqOpenFragmentHistoryButton.isEnabled = !isAccessing
                gtqOpenLoading.root.visibility = if (isAccessing) View.VISIBLE else View.GONE
            }
        }
        gtqOpenVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val canPlayObserver = Observer<Boolean> { canPlay ->
            //binding.gtqOpenFragmentPlayButton.isEnabled = canPlay
            binding.gtqOpenAlertTextView.visibility = if (canPlay) View.GONE else View.VISIBLE
        }
        gtqOpenVM.canPlay.observe(viewLifecycleOwner, canPlayObserver)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.gtqOpenFragmentPlayButton -> {
                findNavController().navigate(GuessTheQuoteOpenFragmentDirections.actionGuessTheQuoteOpenFragmentToGuessTheQuotePlayFragment())
            }
            binding.gtqOpenFragmentHistoryButton -> {
                findNavController().navigate(GuessTheQuoteOpenFragmentDirections.actionGuessTheQuoteOpenFragmentToGuessTheQuoteHistoryFragment())
            }
        }
    }

}