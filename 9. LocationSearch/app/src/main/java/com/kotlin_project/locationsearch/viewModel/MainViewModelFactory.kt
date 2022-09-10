package com.kotlin_project.locationsearch.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin_project.locationsearch.repository.PoiRepository

class MainViewModelFactory(private val poisRepository: PoiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(PoiRepository::class.java).newInstance(poisRepository)
    }
}