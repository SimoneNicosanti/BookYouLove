package it.simone.bookyoulove.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.simone.bookyoulove.Constants.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.GoogleBooksSearchAdapter
import it.simone.bookyoulove.databinding.FragmentGoogleBooksSearchBinding
import it.simone.bookyoulove.model.GoogleBooksApi
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.viewmodel.GoogleBooksSearchViewModel


class GoogleBooksSearchFragment : Fragment() , GoogleBooksSearchAdapter.OnNetworkBookListItemClickListener, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentGoogleBooksSearchBinding
    private val googleBooksSearchVM : GoogleBooksSearchViewModel by viewModels()

    private var queryText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            queryText = it.getString("queryText").toString()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGoogleBooksSearchBinding.inflate(inflater, container, false)
        //setViewEnable(true, requireActivity())

        binding.googleBooksSearchSearchView.setQuery(queryText, false)
        binding.googleBooksSearchSearchView.setOnQueryTextListener(this)

        setViewEnable(true, requireActivity())

        setObservers()
        return binding.root
    }

    private fun setObservers() {
        val isAccessingObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.googleBooksSearchLoading.root.visibility = View.VISIBLE
            }
            else {
                setViewEnable(true, requireActivity())
                binding.googleBooksSearchLoading.root.visibility = View.GONE
            }
        }
        googleBooksSearchVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val currentNetworkBookListObserver = Observer<MutableList<GoogleBooksApi.NetworkBook>> {
            binding.googleBooksSearchRecyclerView.adapter = GoogleBooksSearchAdapter(it, this)
        }
        googleBooksSearchVM.currentNetworkBookList.observe(viewLifecycleOwner, currentNetworkBookListObserver)

        val currentResponseCodeObserver = Observer<Int> {
            if (it == ISBN_INTERNET_ACCESS_ERROR) {
                val alertDialog = AlertDialogFragment()
                val args = bundleOf("alertDialogTitleKey" to getString(R.string.no_internet_connection_string))
                alertDialog.arguments = args
                alertDialog.show(childFragmentManager, "")
            }
        }
        googleBooksSearchVM.currentResponseCode.observe(viewLifecycleOwner, currentResponseCodeObserver)
    }

    override fun onNetworkBookListItemClick(position: Int) {
        binding.googleBooksSearchRecyclerView.adapter?.let {
            it as GoogleBooksSearchAdapter
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedGoogleBook", it.networkBookList[position])
            findNavController().popBackStack()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null && query != "") {
            binding.googleBooksSearchSearchView.clearFocus()
            googleBooksSearchVM.askBooksByTitle(query)
        }
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        queryText = p0 ?: ""
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("queryText", queryText)
    }

}