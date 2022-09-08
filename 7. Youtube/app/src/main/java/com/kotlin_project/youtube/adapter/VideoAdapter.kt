package com.kotlin_project.youtube.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlin_project.youtube.R
import com.kotlin_project.youtube.databinding.ItemVideoBinding
import com.kotlin_project.youtube.model.VideoModel

class VideoAdapter(val onClickListener: (VideoModel) -> Unit) :
    ListAdapter<VideoModel, VideoAdapter.ItemViewHolder>(diffUtil) {

    inner class ItemViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(videoModel: VideoModel) {
            binding.videoModel = videoModel
            binding.videoContainerLayout.setOnClickListener {
                onClickListener(videoModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemVideoBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_video, parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(
                oldItem: VideoModel,
                newItem: VideoModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: VideoModel,
                newItem: VideoModel
            ): Boolean {
                return oldItem.sources == newItem.sources
            }
        }
    }
}