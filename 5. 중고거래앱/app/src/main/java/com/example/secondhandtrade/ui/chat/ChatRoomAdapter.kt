package com.example.secondhandtrade.ui.chat


import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandtrade.Constant
import com.example.secondhandtrade.Constant.DEFAULT_IMG_URI
import com.example.secondhandtrade.Constant.USERS_PATH_STRING
import com.example.secondhandtrade.databinding.ChatBinding
import com.example.secondhandtrade.databinding.ItemMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomAdapter: ListAdapter<ChatRoomModel, ChatRoomAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        fun bind(chatRoomModel: ChatRoomModel) {
            userDB.child(chatRoomModel.senderId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.senderTextView.text =
                        "${snapshot.child(Constant.USER_NAME_PATH_STRING).value.toString()}님의 메세지 : "
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            binding.messageTextView.text = chatRoomModel.message
            val format = SimpleDateFormat("MM월 dd일 HH시 mm분")
            val date = Date(chatRoomModel.time)

            binding.timeTextview.text = format.format(date).toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatRoomModel>() {
            override fun areItemsTheSame(oldItem: ChatRoomModel, newItem: ChatRoomModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatRoomModel, newItem: ChatRoomModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}