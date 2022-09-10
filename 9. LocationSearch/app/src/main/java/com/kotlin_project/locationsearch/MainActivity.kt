package com.kotlin_project.locationsearch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin_project.locationsearch.MapActivity.Companion.alreadyLoaded
import com.kotlin_project.locationsearch.adapter.SearchRecyclerViewAdapter
import com.kotlin_project.locationsearch.databinding.ActivityMainBinding
import com.kotlin_project.locationsearch.repository.PoiRepository
import com.kotlin_project.locationsearch.viewModel.MainViewModel
import com.kotlin_project.locationsearch.viewModel.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SearchRecyclerViewAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        initAdapter()
        initViews()
        initViewModel()
    }

    private fun initAdapter() {
        adapter = SearchRecyclerViewAdapter { itemModel ->
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(ITEM_MODEL_KEY_STRING, itemModel)
            startActivity(intent)
        }
    }

    private fun initViews() {
        binding.noSearchResultTextView.isVisible = false
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter

        binding.searchButton.setOnClickListener {
            mainViewModel.requestKeywordSearch(binding.searchEditText.text.toString(), resources.getString(R.string.tmap_api_key))
        }
    }

    private fun initViewModel() {
        viewModelFactory = MainViewModelFactory(PoiRepository())
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        mainViewModel.poiModel.observe(this) {
            adapter.submitList(it)
            binding.noSearchResultTextView.isVisible = it.isEmpty()
            binding.searchRecyclerView.isVisible = it.isNotEmpty()
        }
    }

    override fun onResume() {
        super.onResume()
        alreadyLoaded = false
    }

    companion object {
        const val ITEM_MODEL_KEY_STRING = "item_model"
    }
}