package com.kotlin_project.youtube

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin_project.youtube.adapter.VideoAdapter
import com.kotlin_project.youtube.databinding.ActivityMainBinding
import com.kotlin_project.youtube.dto.VideoDTO
import com.kotlin_project.youtube.fragment.PlayerFragment
import com.kotlin_project.youtube.model.FragmentViewModel
import com.kotlin_project.youtube.model.VideoModel
import com.kotlin_project.youtube.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var videoAdapter: VideoAdapter
    private val playerFragment = PlayerFragment()
    private var mBackWait: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val fragmentViewModel = ViewModelProviders.of(this).get(FragmentViewModel::class.java)
        fragmentViewModel.title.observe(this) {

        }


        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, playerFragment)
            .commit()

        videoAdapter = VideoAdapter { videoModel ->
            supportFragmentManager.fragments.find {
                it is PlayerFragment
            }?.let {
                (it as PlayerFragment).playVideo(videoModel.title, videoModel.sources)
            }
        }

        binding.mainRecyclerView.adapter = videoAdapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)

        getVideoList()
    }

    private fun getVideoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(VideoService::class.java).also {
            it.listVideos()
                .enqueue(object: Callback<VideoDTO> {
                    override fun onResponse(call: Call<VideoDTO>, response: Response<VideoDTO>) {
                        if (response.isSuccessful.not()) {
                            return
                        }

                        response.body()?.let { videoDto ->
                            videoAdapter.submitList(videoDto.videos)
                        }
                    }

                    override fun onFailure(call: Call<VideoDTO>, t: Throwable) {}

                })
        }
    }

    override fun onBackPressed() {

        if(playerFragment.getIsAtStart()) {
            if(System.currentTimeMillis() - mBackWait >= 2000) {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                finish()
            }
        } else {
            playerFragment.closeFragment()
        }



    }
}