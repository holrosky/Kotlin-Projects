package com.kotlin_project.locationsearch.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.kotlin_project.locationsearch.repository.PoiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel(private val poiRepository: PoiRepository) : ViewModel() {
    private val _buildingName = MutableLiveData<String>()
    private val _fullAddress = MutableLiveData<String>()
    private val _latLng = MutableLiveData<LatLng>()

    val bulidingName: LiveData<String>
        get() = _buildingName
    val fullAddress: LiveData<String>
        get() = _fullAddress
    val latLng: LiveData<LatLng>
        get() = _latLng

    fun requestLocationInfoByLatLon(lat: Double, lon: Double, appKey: String) {
        _latLng.value = LatLng(lat, lon)

        CoroutineScope(Dispatchers.IO).launch {
            poiRepository.getLocationInfoByLatLon(lat, lon, appKey).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _fullAddress.postValue(it.addressInfo.fullAddress)
                        _buildingName.postValue(it.addressInfo.buildingName)
                    }
                }
            }
        }
    }
}