package it.simone.bookyoulove.view.ended

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentEndedThoughtBinding
import it.simone.bookyoulove.viewmodel.EndedThoughtViewModel


class EndedThoughtFragment : Fragment() {

    private lateinit var binding : FragmentEndedThoughtBinding

    private val args : EndedThoughtFragmentArgs by navArgs()

    private val endedThoughtVM : EndedThoughtViewModel by viewModels()

    private var isEditing : Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEditing = savedInstanceState?.getBoolean("isEditing") ?: false
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        binding = FragmentEndedThoughtBinding.inflate(inflater, container, false)


        binding.endedThoughtTitleTextView.text = getString(R.string.final_thought_string)
        setUI(isEditing)

        binding.endedThoughtEditText.doOnTextChanged { text, _, _, _ ->
            endedThoughtVM.updateThought(text.toString())
        }

        setObserver()

        //Modifica il testo della editText che triggera il doOnTextChanged, il quale modifica il testo della TextView
        binding.endedThoughtEditText.setText(args.endedFinalThought)
        binding.endedThoughtTextView.movementMethod = ScrollingMovementMethod()
        //endedThoughtVM.updateThought(args.endedFinalThought)

        return binding.root
    }

    private fun setObserver() {
        val currentThoughtObserver = Observer<String> { currentThought ->
            binding.endedThoughtTextView.text = currentThought
        }
        endedThoughtVM.currentThought.observe(viewLifecycleOwner, currentThoughtObserver)
    }


    private fun setUI(editing: Boolean) {
        if (editing) {
            binding.endedThoughtEditText.visibility = View.VISIBLE
            binding.endedThoughtEditText.isEnabled = true
            binding.endedThoughtTextView.visibility = View.GONE
        }
        else {
            binding.endedThoughtEditText.visibility = View.GONE
            binding.endedThoughtEditText.isEnabled = false
            binding.endedThoughtTextView.visibility = View.VISIBLE
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ended_final_thought_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (isEditing) menu.findItem(R.id.endedFinalThoughtEdit).setIcon(R.drawable.ic_round_save_new_reading_book)
        else menu.findItem(R.id.endedFinalThoughtEdit).setIcon(R.drawable.ic_round_edit_reading_details)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.endedFinalThoughtEdit -> {
                if (isEditing) {
                    item.setIcon(R.drawable.ic_round_edit_reading_details)
                    endedThoughtVM.saveNewThought(args.endedBookId)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("changedFinalThoughtKey", binding.endedThoughtEditText.text.toString())
                }
                else {
                    item.setIcon(R.drawable.ic_round_save_new_reading_book)
                    //binding.endedThoughtEditText.setText(binding.endedThoughtTextView.text)
                }
                isEditing = !isEditing
                setUI(isEditing)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isEditing", isEditing)
    }
}