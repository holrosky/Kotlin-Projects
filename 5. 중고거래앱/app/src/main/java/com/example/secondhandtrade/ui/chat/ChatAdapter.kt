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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import java.text.NumberFormat

class ChatAdapter(val onItemClicked: (ChatModel) -> Unit) : ListAdapter<ChatModel, ChatAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        @SuppressLint("SetTextI18n")
        fun bind(chatModel: ChatModel) {
            binding.titleTextView.text = chatModel.title
            binding.root.setOnClickListener {
                onItemClicked(chatModel)
            }

            if(chatModel.price == "0") {
                binding.priceTextView.text = "나눔"
                binding.priceTextView.setTextColor(Color.parseColor("#FF9A1E"))
            } else {
                val formatter: NumberFormat = DecimalFormat("#,###")
                val formattedPrice: String = formatter.format(chatModel.price.toLong())
                binding.priceTextView.text = "${formattedPrice}원"
            }

            binding.typeTextView.text = when (chatModel.type) {
                "seller" -> "구매자"
                "buyer" -> "판매자"
                else -> "상대방"
            }

            val anotherPersonid = when(chatModel.type) {
                "seller" -> chatModel.buyerId
                "buyer" -> chatModel.sellerId
                else -> "Unknown"
            }

            userDB.child(anotherPersonid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.otherPersonName.text =
                        snapshot.child(Constant.USER_NAME_PATH_STRING).value.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })


            var imgUri = chatModel.imgUrl

            if (imgUri.isEmpty())
                imgUri = DEFAULT_IMG_URI

            Glide.with(binding.thumbnailImageView)
                .load(imgUri)
                .into(binding.thumbnailImageView)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatModel>() {
            override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem.key == newItem.key
            }

        }
    }
}