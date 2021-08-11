package it.simone.bookyoulove.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentEndedThoughtBinding
import it.simone.bookyoulove.viewmodel.DetailEndedViewModel
import it.simone.bookyoulove.viewmodel.EndedThoughtViewModel
import java.util.*


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

        binding.endedThoughtEditText.setText(args.endedFinalThought)
        endedThoughtVM.updateThought(args.endedFinalThought)

        binding.endedThoughtTitleTextView.text = getString(R.string.final_thought_string)
        setUI(isEditing)

        binding.endedThoughtEditText.doOnTextChanged { text, _, _, _ ->
            endedThoughtVM.updateThought(text.toString())
        }

        return binding.root
    }


    private fun setUI(editing: Boolean) {
        binding.endedThoughtEditText.isEnabled = editing
        //TODO("Sistemare colori in night mode")
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
                    endedThoughtVM.saveNewThought(args.endedDetailKeyTitle, args.endedDetailKeyAuthor, args.endedDetailTime)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("changedFinalThoughtKey", binding.endedThoughtEditText.text.toString())
                }
                else {
                    item.setIcon(R.drawable.ic_round_save_new_reading_book)
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