package com.kotlin_project.locationsearch.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kotlin_project.locationsearch.model.ItemModel
import com.kotlin_project.locationsearch.repository.PoiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val poiRepository: PoiRepository) : ViewModel() {
    private val _poiModel = MutableLiveData<List<ItemModel>>()

    val poiModel: LiveData<List<ItemModel>>
        get() = _poiModel

    fun requestKeywordSearch(keyword: String, appKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            poiRepository.getKeywordSearch(keyword, appKey).let { response ->
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        _poiModel.postValue(emptyList())
                        return@launch
                    }

                    _poiModel.postValue(response.body()!!.searchPoiInfo.pois.poi)
                }
            }
        }
    }
}