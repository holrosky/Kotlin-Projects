package com.kotlin_project.airbnb.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlin_project.airbnb.R
import com.kotlin_project.airbnb.databinding.ItemRecyclerViewAccommodationBinding
import com.kotlin_project.airbnb.model.AccommodationModel

class AccommodationRecyclerViewAdapter :
    ListAdapter<AccommodationModel, AccommodationRecyclerViewAdapter.ItemViewHolder>(diffUtil) {
    inner class ItemViewHolder(private val binding: ItemRecyclerViewAccommodationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(accommodationModel: AccommodationModel) {
            binding.accommodationModel = accommodationModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemRecyclerViewAccommodationBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_recycler_view_accommodation, parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AccommodationModel>() {
            override fun areItemsTheSame(
                oldItem: AccommodationModel,
                newItem: AccommodationModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AccommodationModel,
                newItem: AccommodationModel
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }


}