package com.kotlin_project.locationsearch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.kotlin_project.locationsearch.databinding.ActivityMapBinding
import com.kotlin_project.locationsearch.model.ItemModel
import com.kotlin_project.locationsearch.repository.PoiRepository
import com.kotlin_project.locationsearch.viewModel.MainViewModelFactory
import com.kotlin_project.locationsearch.viewModel.MapViewModel

class MapActivity: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private lateinit var map: GoogleMap
    private lateinit var markerOptions: MarkerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        setContentView(binding.root)

        initGoogleMap()
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map

        this.map.setOnMapLongClickListener {
            mapViewModel.requestLocationInfoByLatLon(it.latitude, it.longitude, resources.getString(R.string.tmap_api_key))

        }

        initViewModel()

        if (!alreadyLoaded) {
            val itemModel = intent.getParcelableExtra<ItemModel>(ITEM_MODEL_KEY_STRING)

            if (itemModel != null) {
                mapViewModel.requestLocationInfoByLatLon(itemModel.frontLat.toDouble(), itemModel.noorLon.toDouble(), resources.getString(R.string.tmap_api_key))
            }
        }
    }

    private fun initGoogleMap() {
        markerOptions = MarkerOptions()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initViewModel() {
        viewModelFactory = MainViewModelFactory(PoiRepository())
        mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)

        binding.viewModel = mapViewModel
        binding.lifecycleOwner = this

        mapViewModel.latLng.observe(this) { latLng ->
            markerOptions.position(latLng)

            if (alreadyLoaded)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
            else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                alreadyLoaded = true
            }

            clearAndAddMarker()
        }

        mapViewModel.bulidingName.observe(this) { buildingName ->
            if(buildingName.isEmpty())
                markerOptions.title(" ")
            else
                markerOptions.title(buildingName)

            clearAndAddMarker()
        }

        mapViewModel.fullAddress.observe(this) { fullAddress ->
            markerOptions.snippet(fullAddress)
            clearAndAddMarker()
        }

    }

    private fun clearAndAddMarker() {
        map.clear()
        map.addMarker(markerOptions)!!.showInfoWindow()
    }

    companion object {
        const val ITEM_MODEL_KEY_STRING = "item_model"
        const val CAMERA_ZOOM_LEVEL = 17f
        var alreadyLoaded = false
    }
}