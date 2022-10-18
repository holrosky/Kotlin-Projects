package com.example.finedust

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finedust.databinding.ActivityMainBinding
import com.example.finedust.model.airInfo.AirGrade
import com.example.finedust.repository.Repository
import com.example.finedust.viewModel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var cancellationTokenSource: CancellationTokenSource

    private val mainViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(Repository()) as T
            }
        })[MainViewModel::class.java]
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initVariables()
        requestLocationPermissions()
    }

    @SuppressLint("SetTextI18n")
    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        cancellationTokenSource = CancellationTokenSource()

        mainViewModel.monitoringStation.observe(this) {
            binding.stationNameTextView.text = it.stationName
            binding.stationAddressTextView.text = it.addr
        }

        mainViewModel.airInfo.observe(this) { airInfo ->
            (airInfo.khaiGrade ?: AirGrade.UNKNOWN).let { airGrade ->
                binding.root.setBackgroundResource(airGrade.colorResId)
                binding.totalGradeLabelTextView.text = airGrade.label
                binding.totalGradeEmojiTextView.text = airGrade.emoji
            }

            with(airInfo) {
                binding.fineDustInfomationTextView.text =
                    "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: AirGrade.UNKNOWN).emoji}"
                binding.ultraFineDustInfomationTextView.text =
                    "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: AirGrade.UNKNOWN).emoji}"

                with(binding.so2Item) {
                    labelTextView.text = "아황산가스"
                    gradeTextView.text = (so2Grade ?: AirGrade.UNKNOWN).toString()
                    valueTextView.text = "$so2Value ppm"
                }

                with(binding.coItem) {
                    labelTextView.text = "일산화탄소"
                    gradeTextView.text = (coGrade ?: AirGrade.UNKNOWN).toString()
                    valueTextView.text = "$coValue ppm"
                }

                with(binding.o3Item) {
                    labelTextView.text = "오존"
                    gradeTextView.text = (o3Grade ?: AirGrade.UNKNOWN).toString()
                    valueTextView.text = "$o3Value ppm"
                }

                with(binding.no2Item) {
                    labelTextView.text = "이산화질소"
                    gradeTextView.text = (no2Grade ?: AirGrade.UNKNOWN).toString()
                    valueTextView.text = "$no2Value ppm"
                }
            }

            binding.progressBar.visibility = View.GONE
            binding.refresh.isRefreshing = false
            binding.contentLayout.animate()
                .alpha(1F)
                .start()

        }

        binding.refresh.setOnRefreshListener {
            fetchAirStatus()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionGranted =
            requestCode == REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!backgroundLocationPermissionGranted) {
                requestBackgroundLocationPermissions()
            } else {
                if (mainViewModel.needToLoadOnStart)
                    fetchAirStatus()
            }
        } else {
            if(!locationPermissionGranted) {
                finish()
            } else {
                if (mainViewModel.needToLoadOnStart)
                    fetchAirStatus()
            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource.cancel()
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirStatus() {
        try {
            fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                mainViewModel.updateAirInformation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        } catch (exception: Exception) {
            binding.errorDescriptionTextView.visibility = View.VISIBLE
            binding.contentLayout.alpha = 0F
        }
    }


    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
        private const val REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 200
    }
}