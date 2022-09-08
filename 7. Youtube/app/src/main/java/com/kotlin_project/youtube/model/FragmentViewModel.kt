package com.kotlin_project.youtube.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentViewModel: ViewModel() {
    var title = MutableLiveData<String>()

    init {
        title.value = "재생중인 동영상이 없습니다!"
    }

    fun setTitle(title: String) {
        this.title.value = title
    }
}