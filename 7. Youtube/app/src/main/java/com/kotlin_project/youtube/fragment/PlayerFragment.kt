package com.kotlin_project.youtube.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.kotlin_project.youtube.MainActivity
import com.kotlin_project.youtube.R
import com.kotlin_project.youtube.adapter.VideoAdapter
import com.kotlin_project.youtube.databinding.FragmentPlayerBinding
import com.kotlin_project.youtube.dto.VideoDTO
import com.kotlin_project.youtube.model.FragmentViewModel
import com.kotlin_project.youtube.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Math.abs


class PlayerFragment: Fragment() {

    private var binding: FragmentPlayerBinding? = null
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var player: SimpleExoPlayer
    private lateinit var fragmentViewModel: FragmentViewModel
    private var isAtStart = true

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)

        fragmentViewModel = ViewModelProvider(requireActivity()).get(FragmentViewModel::class.java)
        binding?.fragmentViewModel = fragmentViewModel
        binding?.lifecycleOwner = viewLifecycleOwner

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMotionLayoutEvent()
        initRecyclerView()
        initPlayer()
        initControlButton()
        getVideoList()
    }


    private fun initMotionLayoutEvent() {
        binding?.playerMotionLayout?.setTransitionListener(object: MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                (activity as MainActivity).findViewById<MotionLayout>(R.id.mainMotionLayout).progress =
                    abs(progress)


            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (binding?.playerMotionLayout?.progress == 0.0f) {
                    Log.e("onTransitionChange", "start")
                    isAtStart = true
                } else {
                    Log.e("onTransitionChange", "end")
                    isAtStart = false
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}
        })

        binding?.mainContainerLayout?.setOnClickListener {
            binding?.playerMotionLayout?.transitionToEnd()
        }
    }

    private fun initRecyclerView() {
        videoAdapter = VideoAdapter { videoModel ->
            playVideo(videoModel.title, videoModel.sources)
        }

        binding?.fragmentRecyclerView?.adapter = videoAdapter
        binding?.fragmentRecyclerView?.layoutManager = LinearLayoutManager(context)

    }

    private fun initPlayer() {
        context?.let { player = SimpleExoPlayer.Builder(it).build() }
        binding?.playerView?.player = player
        player.addListener(object: Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (isPlaying) {
                    binding?.bottomPlayerControlButton?.setImageResource(R.drawable.ic_pause)
                } else {
                    binding?.bottomPlayerControlButton?.setImageResource(R.drawable.ic_play)
                }
            }
        })
    }

    private fun initControlButton() {
        binding?.bottomPlayerControlButton?.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
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


    fun playVideo(title: String, url: String) {
        context?.let {
            val dataSourceFactory = DefaultDataSourceFactory(it)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

            player.setMediaSource(mediaSource)
            player.prepare()
            player.play()
        }

        binding?.let {
            it.playerMotionLayout.transitionToEnd()
            fragmentViewModel.setTitle(title)
        }

    }

    fun getIsAtStart(): Boolean {
        return isAtStart
    }

    fun closeFragment() {
        binding?.playerMotionLayout?.transitionToStart()
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        player.release()
    }
}