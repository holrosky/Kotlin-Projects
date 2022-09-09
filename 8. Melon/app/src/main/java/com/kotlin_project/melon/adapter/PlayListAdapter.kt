package com.kotlin_project.melon.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlin_project.melon.R
import com.kotlin_project.melon.databinding.ItemMusicBinding
import com.kotlin_project.melon.model.MusicModel

class PlayListAdapter(val onClickListener: (MusicModel) -> Unit) :
    ListAdapter<MusicModel, PlayListAdapter.ItemViewHolder>(diffUtil) {

    inner class ItemViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(musicModel: MusicModel) {
            binding.musicModel = musicModel
            binding.musicContainerLayout.setOnClickListener {
                onClickListener(musicModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemMusicBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_music, parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MusicModel>() {
            override fun areItemsTheSame(
                oldItem: MusicModel,
                newItem: MusicModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MusicModel,
                newItem: MusicModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}