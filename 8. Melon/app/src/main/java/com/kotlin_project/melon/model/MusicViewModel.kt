package com.kotlin_project.melon.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel : ViewModel() {
    private val _duration = MutableLiveData<Long>()
    private val _playPosition = MutableLiveData<Long>()
    private val _musicModel = MutableLiveData<MusicModel>()
    private val _id = MutableLiveData<Long>()
    private val _track = MutableLiveData<String>()
    private val _artist = MutableLiveData<String>()
    private val _coverUrl = MutableLiveData<String>()
    private val _isPlaying = MutableLiveData<Boolean>()
    private val _isWatchingPlayListView = MutableLiveData<Boolean>()
    private val _currentPosition = MutableLiveData<Int>()
    private val _musicList = MutableLiveData<List<MusicModel>>()

    val musicModel: LiveData<MusicModel>
        get() = _musicModel
    val duration: LiveData<Long>
        get() = _duration
    val playPosition: LiveData<Long>
        get() = _playPosition
    val id: LiveData<Long>
        get() = _id
    val track: LiveData<String>
        get() = _track
    val artist: LiveData<String>
        get() = _artist
    val coverUrl: LiveData<String>
        get() = _coverUrl
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying
    val isWatchingPlayListView: LiveData<Boolean>
        get() = _isWatchingPlayListView
    val currentPosition: LiveData<Int>
        get() = _currentPosition
    val musicList: MutableLiveData<List<MusicModel>>
        get() = _musicList

    init {
        _isPlaying.value = false
        _isWatchingPlayListView.value = true
        _musicList.value = emptyList()
    }

    fun updateMusicInfo(musicModel: MusicModel) {
        _id.value = musicModel.id
        _track.value = musicModel.track
        _artist.value = musicModel.artist
        _coverUrl.value = musicModel.coverUrl
        _musicModel.value = musicModel
        _currentPosition.value = _musicList.value?.indexOf(_musicModel.value)
        _isPlaying.value = true
    }

    fun controlButtonClick() {
        _isPlaying.value = _isPlaying.value?.not()
    }

    fun playListButtonClick() {
        _isWatchingPlayListView.value = _isWatchingPlayListView.value?.not()
    }

    fun updateDuration(duration: Long) {
        _duration.value = duration
    }

    fun updatePlayPosition(playPosition: Long) {
        _playPosition.value = playPosition
    }

    fun nextMusic() {
        if (_musicList.value?.isEmpty() == true) return
        _currentPosition.value = if ((_currentPosition.value?.plus(1)) == _musicList.value?.size) 0 else _currentPosition.value?.plus(1)

        _currentPosition.value?.let { _musicList.value?.get(it) }?.let { updateMusicInfo(it) }
    }

    fun previousMusic() {
        if (_musicList.value?.isEmpty() == true) return
        _currentPosition.value = if ((_currentPosition.value?.minus(1))!! < 0) _musicList.value?.lastIndex else _currentPosition.value?.minus(1)

        _currentPosition.value?.let { _musicList.value?.get(it) }?.let { updateMusicInfo(it) }
    }
}