package com.kotlin_project.locationsearch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlin_project.locationsearch.R
import com.kotlin_project.locationsearch.databinding.ItemSearchBinding
import com.kotlin_project.locationsearch.model.ItemModel

class SearchRecyclerViewAdapter(val ItemClickListener: (ItemModel) -> Unit) :
    ListAdapter<ItemModel, SearchRecyclerViewAdapter.ItemViewHolder>(diffUtil) {

    inner class ItemViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemModel: ItemModel) {
            binding.itemModel = itemModel

            binding.root.setOnClickListener {
                ItemClickListener(itemModel)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemSearchBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_search, parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ItemModel>() {
            override fun areItemsTheSame(
                oldItem: ItemModel,
                newItem: ItemModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ItemModel,
                newItem: ItemModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}