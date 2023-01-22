package com.gdm.alphageeksales.view.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gdm.alphageeksales.MainActivity
import com.gdm.alphageeksales.databinding.ActivitySplashScreenBinding
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.view.ui.auth.LoginActivity
import com.gdm.alphageeksales.view.ui.welcome_screen.WelcomeActivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.versionNameTv.text = "Version ${this.packageManager.getPackageInfo(this.packageName, 0).versionName}"

        // SharedPref initialize
        SharedPref.init(this)

        // Coroutines start
        lifecycleScope.launch {
            // 3 sec delay
            delay(2000)
            if (SharedPref.getUserID().isEmpty()) {
                startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, WelcomeActivity::class.java))
            }
            finishAffinity()
        }
    }
}