package se.rebeccazadig.bokholken.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        binding.bottomNavigationView.setupWithNavController(navController)

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn == true) {
                navController.navigate(R.id.action_to_advertsFragment)
            } else {
                navController.navigate(R.id.action_to_login_nav_graph)
            }
        }
    }
}
