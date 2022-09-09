package com.kotlin_project.melon.model

import com.kotlin_project.melon.dto.MusicDTO
import com.kotlin_project.melon.entity.MusicEntity

fun MusicEntity.mapper(id: Long): MusicModel =
    MusicModel(
        id = id,
        streamUrl = streamUrl,
        coverUrl = coverUrl,
        artist = artist,
        track = track
    )

fun MusicDTO.mapper(): List<MusicModel> =
    musics.mapIndexed { index, musicEntity ->
        musicEntity.mapper(index.toLong())
    }

