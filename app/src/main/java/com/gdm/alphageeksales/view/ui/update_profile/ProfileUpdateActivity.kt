package com.gdm.alphageeksales.view.ui.update_profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.databinding.ActivityProfileUpdateBinding
import com.gdm.alphageeksales.utils.Communicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileUpdateActivity : AppCompatActivity(),Communicator {
    private lateinit var binding:ActivityProfileUpdateBinding
    private val navController by lazy { Navigation.findNavController(this, R.id.frame_stepper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stepper.setupWithNavController(navController)
        binding.toolbar.title = "Profile Setup"
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(findNavController(R.id.frame_stepper))
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.frame_stepper).navigateUp()

    override fun onBackPressed() {
        if (binding.stepper.currentStep == 0) {
            super.onBackPressed()
        } else {
            findNavController(R.id.frame_stepper).navigateUp()
        }
    }

    override fun changeView() {
        binding.stepper.goToNextStep()
    }
}