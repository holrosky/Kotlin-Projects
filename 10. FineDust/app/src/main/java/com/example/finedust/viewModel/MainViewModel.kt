package com.example.finedust.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finedust.model.airInfo.AirInfo
import com.example.finedust.model.monitoringStation.MonitoringStation
import com.example.finedust.repository.Repository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: Repository
    ) : ViewModel() {

    private val _monitoringStation = MutableLiveData<MonitoringStation>()
    private val _airInfo = MutableLiveData<AirInfo>()
    private var _needToLoadOnStart = true

    val monitoringStation: LiveData<MonitoringStation>
        get() = _monitoringStation

    val airInfo: LiveData<AirInfo>
        get() = _airInfo

    val needToLoadOnStart: Boolean
        get() = _needToLoadOnStart

    fun updateAirInformation(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            val tmCoordinate = repository.getTmCoordinates(longitude = longitude, latitude = latitude)

            _monitoringStation.value = tmCoordinate?.let{
                repository.getNearMonitorStations(it.x!!, it.y!!)
            }

            _airInfo.value = monitoringStation.let {
                repository.getAirInfos(monitoringStation.value?.stationName!!)
            }

            _needToLoadOnStart = false
        }
    }
}