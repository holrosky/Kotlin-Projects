package com.kotlin_project.melon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kotlin_project.melon.databinding.ActivityMainBinding
import com.kotlin_project.melon.fragment.PlayerFragment

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlayerFragment.getInstance())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}