package com.kotlin_project.airbnb

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.kotlin_project.airbnb.adapter.AccommodationRecyclerViewAdapter
import com.kotlin_project.airbnb.adapter.AccommodationViewPagerAdapter
import com.kotlin_project.airbnb.binding.ItemClickListener
import com.kotlin_project.airbnb.databinding.ActivityMainBinding
import com.kotlin_project.airbnb.dto.AccommodationDTO
import com.kotlin_project.airbnb.model.AccommodationModel
import com.kotlin_project.airbnb.service.AccommodationService
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var behavior: BottomSheetBehavior<View>
    private val viewPagerAdapter = AccommodationViewPagerAdapter()
    private val recyclerViewAdapter = AccommodationRecyclerViewAdapter()
    private var mBackWait: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)


        binding.accommodationViewPager.adapter = viewPagerAdapter
        binding.includeView.accommodationRecyclerView.adapter = recyclerViewAdapter
        binding.includeView.accommodationRecyclerView.layoutManager = LinearLayoutManager(this)

        behavior = BottomSheetBehavior.from(binding.includeView.bottomSheetBehavior)

        registerClickListner()
    }

    private fun registerClickListner() {
        binding.accommodationViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedAccommodationModel = viewPagerAdapter.currentList[position]
                val cameraUpdate = CameraUpdate
                    .scrollTo(LatLng(selectedAccommodationModel.lat, selectedAccommodationModel.lng))
                    .animate(CameraAnimation.Easing)

                naverMap.moveCamera(cameraUpdate)
            }
        })

        viewPagerAdapter.itemClickListener(object: ItemClickListener {
            override fun sendValue(value: AccommodationModel, position: Int) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "[숙소 공유]\n숙소이름 : ${value.title}\n가격 : ${value.price}\n사진 : ${value.imgUrl}")
                    type = "text/plain"
                }

                startActivity(Intent.createChooser(intent, null))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onResume()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 6.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.497885, 127.027512))

        naverMap.moveCamera(cameraUpdate)

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false

        binding.currentLocationButton.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getAccommodationFromAPI()
    }

    private fun getAccommodationFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(AccommodationService::class.java).also {
            it.getAccommdationList()
                .enqueue(object : Callback<AccommodationDTO> {
                    override fun onResponse(
                        call: Call<AccommodationDTO>,
                        response: Response<AccommodationDTO>
                    ) {
                        if (response.isSuccessful.not()) {
                            return
                        }

                        response.body()?.let { dto ->
                            updateMarker(dto.items)
                            viewPagerAdapter.submitList(dto.items)
                            recyclerViewAdapter.submitList(dto.items)

                            binding.includeView.recyclerViewTitleTextview.text = "${dto.items.size}개의 숙소를 찾았습니다!"
                        }
                    }

                    override fun onFailure(call: Call<AccommodationDTO>, t: Throwable) {}
                })
        }
    }

    private fun updateMarker(accommodationModels: List<AccommodationModel>) {
        accommodationModels.forEach { accommodationModel ->
            val marker = Marker()
            marker.onClickListener = this
            marker.position = LatLng(accommodationModel.lat, accommodationModel.lng)
            marker.map = naverMap
            marker.tag = accommodationModel.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }

            return
        }
    }

    override fun onClick(overlay: Overlay): Boolean {
        val selectedAccommodationModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }

        selectedAccommodationModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            binding.accommodationViewPager.currentItem = position
        }

        return true
    }

    override fun onBackPressed() {
        when {
            behavior.state != STATE_COLLAPSED -> behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            System.currentTimeMillis() - mBackWait >= 2000 -> {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                finish()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


}