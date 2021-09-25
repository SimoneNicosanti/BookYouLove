package it.simone.bookyoulove.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.ActivityGuessTheQuoteBinding

class GuessTheQuoteActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGuessTheQuoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuessTheQuoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}