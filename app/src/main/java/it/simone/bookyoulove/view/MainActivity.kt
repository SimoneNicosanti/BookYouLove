package it.simone.bookyoulove.view

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.simone.bookyoulove.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R

const val READING_BOOK_CALLER = 0
const val READ_BOOK_CALLER = 1
const val TBR_BOOK_CALLER = 2

const val MIN_PAGES_AMOUNT = 0
const val MAX_PAGES_AMOUNT = 4096

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


class MainActivity : AppCompatActivity() , BottomNavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener{

    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration


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


    /*
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return when(item.itemId) {
            R.id.navViewMenuReadingItem -> {
                //La navigazione è eseguita SOLO se la destinazione corrente è diversa da quella in cui si vuole andare
                if (navController.currentDestination?.id != R.id.readingFragment) {
                    val action = ReadingFragmentDirections.actionGlobalReadingFragment()
                    navController.navigate(action)
                }
                //binding.drawerLayout.closeDrawers()
                true
            }

            R.id.navViewMenuReadItem -> {
                if (navController.currentDestination?.id != R.id.endedFragment) {
                    val action = EndedFragmentDirections.actionGlobalReadListFragment()
                    navController.navigate(action)
                }
                //binding.drawerLayout.closeDrawers()
                true
            }

            R.id.navViewMenuSettingsItem -> {
                if (navController.currentDestination?.id != R.id.settingsFragment) {
                    val action = SettingsFragmentDirections.actionGlobalSettingsFragment()
                    navController.navigate(action)
                }
                //binding.drawerLayout.closeDrawers()
                true
            }

            R.id.navViewOthersItem -> {
                val popupMenu = PopupMenu(this, binding.bottomNavigationView)
                val inflater: MenuInflater = popupMenu.menuInflater
                inflater.inflate(R.menu.navigation_view_others, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.gravity = Gravity.RIGHT
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
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
                    val action = SettingsFragmentDirections.actionGlobalSettingsFragment()
                    navController.navigate(action)
                }
                //binding.drawerLayout.closeDrawers()
                true
            }
            else -> false


        }
    }
}