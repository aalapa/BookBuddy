package com.bookbuddy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bookbuddy.databinding.ActivityMainBinding
import com.bookbuddy.ui.viewmodel.BookViewModel
import com.bookbuddy.ui.viewmodel.BookViewModelFactory
import com.bookbuddy.utils.CSVHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: BookViewModel

    private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportToCSV(uri)
            }
        }
    }

    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importFromCSV(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("BookBuddy", "MainActivity onCreate")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            viewModel = ViewModelProvider(this, BookViewModelFactory(application))[BookViewModel::class.java]

            setupNavigation()
            setupDrawer()
        } catch (e: Exception) {
            Log.e("BookBuddy", "Error in MainActivity onCreate", e)
            e.printStackTrace()
            throw e
        }
    }

    private fun setupDrawer() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Setup navigation item listener first (doesn't need NavController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_export -> {
                    exportToCSV()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_import -> {
                    importFromCSV()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        // Setup AppBarConfiguration and NavController after view is created
        binding.root.post {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController
            if (navController != null) {
                appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.booksToReadFragment, R.id.dashboardFragment, R.id.alreadyReadFragment),
                    drawerLayout
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return if (navController != null && ::appBarConfiguration.isInitialized) {
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } else {
            // If NavController isn't ready, just close the drawer if it's open
            val drawerLayout: DrawerLayout = binding.drawerLayout
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            } else {
                super.onSupportNavigateUp()
            }
        }
    }

    private fun findNavController(id: Int): NavController? {
        return (supportFragmentManager.findFragmentById(id) as? NavHostFragment)?.navController
    }

    private fun exportToCSV() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "BookBuddy_Export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv")
        }
        exportFileLauncher.launch(intent)
    }

    private fun exportToCSV(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val books = viewModel.getAllBooks()
                withContext(Dispatchers.Main) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        CSVHelper.exportBooksToCSV(books, outputStream)
                        Toast.makeText(this@MainActivity, "Exported ${books.size} books successfully", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@MainActivity, "Error exporting books", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("BookBuddy", "Error exporting CSV", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun importFromCSV() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        importFileLauncher.launch(intent)
    }

    private fun importFromCSV(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val books = contentResolver.openInputStream(uri)?.use { inputStream ->
                    CSVHelper.importBooksFromCSV(inputStream)
                } ?: emptyList()

                if (books.isNotEmpty()) {
                    viewModel.importBooks(books)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Imported ${books.size} books successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "No books found in CSV file", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("BookBuddy", "Error importing CSV", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupNavigation() {
        // Wait for the view to be fully created before accessing NavController
        binding.root.post {
            try {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                if (navHostFragment != null) {
                    val navController = navHostFragment.navController
                    val bottomNav: BottomNavigationView = binding.bottomNavigation
                    bottomNav.setupWithNavController(navController)
                    Log.d("BookBuddy", "MainActivity navigation setup complete")
                } else {
                    Log.e("BookBuddy", "NavHostFragment is null, retrying...")
                    // Retry once more after a short delay
                    binding.root.postDelayed({
                        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                        navHost?.let {
                            val navController = it.navController
                            val bottomNav: BottomNavigationView = binding.bottomNavigation
                            bottomNav.setupWithNavController(navController)
                            Log.d("BookBuddy", "MainActivity navigation setup complete (retry)")
                        } ?: Log.e("BookBuddy", "NavHostFragment still null after retry")
                    }, 100)
                }
            } catch (e: Exception) {
                Log.e("BookBuddy", "Error setting up navigation", e)
                e.printStackTrace()
            }
        }
    }
}

