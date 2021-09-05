package it.simone.bookyoulove.view

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.simone.bookyoulove.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.view.reading.ReadingFragmentDirections

const val START_DATE_SETTER = 0
const val END_DATE_SETTER = 1

const val READING_BOOK_STATE = 0
const val ENDED_BOOK_STATE = 1
const val TBR_BOOK_STATE = 2

const val PAPER_SUPPORT = "Paper"
const val EBOOK_SUPPORT = "eBook"
const val AUDIOBOOK_SUPPORT = "AudioBook"

const val SORT_START_DATE = 0
const val SORT_END_DATE = 1

const val SORT_BY_TITLE = 0
const val SORT_BY_AUTHOR = 1

const val SEARCH_BY_TITLE = 0
const val SEARCH_BY_AUTHOR = 1
const val SEARCH_BY_RATE = 2
const val SEARCH_BY_YEAR = 3
const val SEARCH_BY_TITLE_OR_AUTHOR = 4

const val TAG = "BookYouLove"


class MainActivity : AppCompatActivity() , BottomNavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener{

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Picasso.get().setIndicatorsEnabled(true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }


    //@RequiresApi(Build.VERSION_CODES.Q)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return when(item.itemId) {
            R.id.navViewMenuReadingItem -> {
                //La navigazione è eseguita SOLO se la destinazione corrente è diversa da quella in cui si vuole andare
                if (navController.currentDestination?.id != R.id.readingFragment) {
                    val action = ReadingFragmentDirections.actionGlobalReadingFragment()
                    navController.navigate(action)
                }
                true
            }

            R.id.navViewMenuReadItem -> {
                if (navController.currentDestination?.id != R.id.endedFragment) {
                    val action = ReadingFragmentDirections.actionGlobalReadListFragment()
                    navController.navigate(action)
                }
                true
            }

            R.id.navViewMenuTbrItem -> {
                if (navController.currentDestination?.id != R.id.tbrFragment) {
                    val action = ReadingFragmentDirections.actionGlobalTbrFragment()
                    navController.navigate(action)
                }
                true
            }


            R.id.navViewOthersItem -> {
                val popupMenu = PopupMenu(this, binding.bottomNavigationView)
                val inflater: MenuInflater = popupMenu.menuInflater
                inflater.inflate(R.menu.navigation_view_others, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.gravity = Gravity.END
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    popupMenu.setForceShowIcon(true)
                }
                popupMenu.show()
                true
            }
            else -> true
        }
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return when (item?.itemId) {

            R.id.navViewMenuSettingsItem -> {
                if (navController.currentDestination?.id != R.id.settingsFragment) {
                    val action = ReadingFragmentDirections.actionGlobalSettingsFragment()
                    navController.navigate(action)
                }
                true
            }

            R.id.navViewMenuChartsItem -> {
                if (navController.currentDestination?.id != R.id.chartsFragment) {
                    val action = ReadingFragmentDirections.actionGlobalChartsFragment()
                    navController.navigate(action)
                }
                true
            }

            R.id.navViewMenuQuotesItem -> {
                if (navController.currentDestination?.id != R.id.quoteListFragment) {
                    val action = ReadingFragmentDirections.actionGlobalQuoteListFragment(0L)
                    navController.popBackStack(R.id.readingFragment, false)
                    navController.navigate(action)
                }
                true
            }

            else -> false
        }
    }
}

fun setViewEnable(isEnabled : Boolean, activity: Activity) {
    if (isEnabled) activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    else activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}