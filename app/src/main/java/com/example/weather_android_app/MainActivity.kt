package com.example.weather_android_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weather_android_app.databinding.ActivityMainBinding
import com.example.weather_android_app.fragments.MainFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance())
            .commit()
    }
}