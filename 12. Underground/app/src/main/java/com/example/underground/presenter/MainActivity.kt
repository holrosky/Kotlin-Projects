package com.example.underground.presenter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.underground.R
import com.example.underground.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}