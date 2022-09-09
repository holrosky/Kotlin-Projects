package com.kotlin_project.melon.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.kotlin_project.melon.R
import com.kotlin_project.melon.adapter.PlayListAdapter
import com.kotlin_project.melon.databinding.FragmentPlayerBinding
import com.kotlin_project.melon.dto.MusicDTO
import com.kotlin_project.melon.model.MusicModel
import com.kotlin_project.melon.model.MusicViewModel
import com.kotlin_project.melon.model.mapper
import com.kotlin_project.melon.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment() {
    private var binding: FragmentPlayerBinding? = null

    private lateinit var musicModelViewModel: MusicViewModel

    private var player: SimpleExoPlayer? = null
    private val updateSeekRunnable = Runnable {
        updateSeekTime()
    }
    private lateinit var playListAdapter: PlayListAdapter

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)

        musicModelViewModel = ViewModelProvider(requireActivity()).get(MusicViewModel::class.java)
        binding?.musicModelViewModel = musicModelViewModel
        binding?.lifecycleOwner = viewLifecycleOwner

        musicModelViewModel.isPlaying.observe(viewLifecycleOwner) {
            if (musicModelViewModel.isPlaying.value == false) {
                player?.pause()
            } else {
                player?.play()
            }
        }

        musicModelViewModel.isWatchingPlayListView.observe(viewLifecycleOwner) {
            binding?.playerViewGroup?.isVisible =
                musicModelViewModel.isWatchingPlayListView.value != true
            binding?.playListGroup?.isVisible =
                musicModelViewModel.isWatchingPlayListView.value == true
        }


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayerView()
        initRecyclerView()
        initControlButtons()
        initSeekBar()
        initMusicList(musicModelViewModel.musicList.value.orEmpty())
        getVideoList()


    }

    private fun initControlButtons() {
        binding?.skipNextImageView?.setOnClickListener {
            musicModelViewModel.nextMusic()
            musicModelViewModel.currentPosition.value?.let { currentPosition ->
                player?.seekTo(currentPosition, 0) }

        }

        binding?.skipPreviousImageView?.setOnClickListener {
            musicModelViewModel.previousMusic()
            musicModelViewModel.currentPosition.value?.let { currentPosition ->
                player?.seekTo(currentPosition, 0) }
        }
    }

    private fun initSeekBar() {
        binding?.playerSeekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((seekBar.progress * 1000).toLong())
            }
        })

        binding?.playListSeekBar?.setOnTouchListener { _, _ ->
            false
        }
    }

    private fun initPlayerView() {
        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
        }

        if (musicModelViewModel.currentPosition.value != null && musicModelViewModel.playPosition.value != null) {
            player?.seekTo(
                musicModelViewModel.currentPosition.value!!,
                musicModelViewModel.playPosition.value!!
            )
        }


        binding?.let {
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) {
                        view?.removeCallbacks(updateSeekRunnable)
                    } else {
                        updateSeekTime()
                    }
                }
            })
        }
    }

    private fun updateSeekTime() {
        val duration =
            if (player?.duration!! > 0) player?.duration else musicModelViewModel.duration.value
        val playPosition = player?.currentPosition

        if (duration != null) {
            musicModelViewModel.updateDuration(duration)
        }

        if (playPosition != null) {
            musicModelViewModel.updatePlayPosition(playPosition)
        }

        view?.removeCallbacks(updateSeekRunnable)
        view?.postDelayed(updateSeekRunnable, 1000)

    }

    private fun initRecyclerView() {
        playListAdapter = PlayListAdapter { musicModel ->
            musicModelViewModel.updateMusicInfo(musicModel)
            musicModelViewModel.currentPosition.value?.let { currentPosition ->
                player?.seekTo(currentPosition, 0)
            }
        }

        binding?.playListRecyclerView?.adapter = playListAdapter
        binding?.playListRecyclerView?.layoutManager = LinearLayoutManager(context)

    }

    private fun getVideoList() {
        musicModelViewModel.musicList.value?.let { musicList ->
            if (musicList.isEmpty()) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://run.mocky.io")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                retrofit.create(MusicService::class.java)
                    .also {
                        it.listMusics()
                            .enqueue(object : Callback<MusicDTO> {
                                override fun onResponse(
                                    call: Call<MusicDTO>,
                                    response: Response<MusicDTO>
                                ) {
                                    response.body()?.let { musicDTO ->

                                        musicModelViewModel.musicList.value = musicDTO.mapper()

                                        initMusicList(musicModelViewModel.musicList.value.orEmpty())
                                    }
                                }

                                override fun onFailure(call: Call<MusicDTO>, t: Throwable) {}

                            })
                    }
            }

        }

    }

    private fun initMusicList(list: List<MusicModel>) {
        playListAdapter.submitList(list)

        context?.let {
            player?.addMediaItems(list.map { musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })
        }

        player?.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.removeCallbacks(updateSeekRunnable)
        player?.release()
        binding = null
    }

    companion object {
        fun getInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}