package it.simone.bookyoulove.view

import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentEndedThoughtBinding
import it.simone.bookyoulove.databinding.FragmentEndingBinding
import it.simone.bookyoulove.viewmodel.EndedThoughtViewModel
import it.simone.bookyoulove.viewmodel.ModifyEndedViewModel


class EndedThoughtFragment : Fragment() {

    private lateinit var binding : FragmentEndedThoughtBinding

    private val args : EndedThoughtFragmentArgs by navArgs()

    private val endedThoughtVM : EndedThoughtViewModel by viewModels()

    private var isEditing : Boolean = false

    private lateinit var fragmentMenu : Menu

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
        binding.endedThoughtTitleTextView.text = args.endedDetailTitle + "\n${getString(R.string.final_thought_string)}"
        setUI(isEditing)

        binding.endedThoughtEditText.doOnTextChanged { text, _, _, _ ->
            endedThoughtVM.updateThought(text.toString())
        }

        return binding.root
    }


    private fun setUI(editing: Boolean) {
        binding.endedThoughtEditText.isClickable = editing
        binding.endedThoughtEditText.isLongClickable = editing
        binding.endedThoughtEditText.isFocusable = editing
        binding.endedThoughtEditText.isFocusableInTouchMode = editing
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ended_final_thought_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (isEditing) menu.findItem(R.id.endedFinalThoughtEdit).setIcon(R.drawable.ic_round_save_new_reading_book)
        else menu.findItem(R.id.endedFinalThoughtEdit).setIcon(R.drawable.ic_round_edit_reading_details)
        fragmentMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.endedFinalThoughtEdit -> {
                if (isEditing) {
                    item.setIcon(R.drawable.ic_round_edit_reading_details)
                    endedThoughtVM.saveNewThought(args.endedDetailTitle, args.endedDetailAuthor, args.endedDetailTime)
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