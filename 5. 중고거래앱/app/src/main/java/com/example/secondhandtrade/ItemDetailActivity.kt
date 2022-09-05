package com.example.secondhandtrade

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.secondhandtrade.Constant.CHILD_CHAT_PATH_STRING
import com.example.secondhandtrade.Constant.USERS_PATH_STRING
import com.example.secondhandtrade.ui.home.ItemModel
import com.example.secondhandtrade.databinding.ActivityItemDetailBinding
import com.example.secondhandtrade.ui.chat.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var itemModel: ItemModel

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            binding = ActivityItemDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            itemModel = intent.getSerializableExtra("itemModel") as ItemModel

            loadData()
            initChatButton()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        binding.apply {
            titleEditText.setText(itemModel.title)

            if(itemModel.price == "0") {
                priceEditText.setText("나눔")
                priceEditText.setTextColor(Color.parseColor("#FF9A1E"))
            } else {
                val formatter: NumberFormat = DecimalFormat("#,###")
                val formattedPrice: String = formatter.format(itemModel.price.toLong())
                priceEditText.setText("${formattedPrice}원")
            }

            setSellerName(itemModel.sellerId)
            detailEditText.setText(itemModel.itemDetail)

            var imgUri = itemModel.imgUrl

            if (imgUri.isEmpty())
                imgUri = Constant.DEFAULT_IMG_URI

            Glide.with(binding.itemImageView)
                .load(imgUri)
                .into(binding.itemImageView)

            if (itemModel.sellerId == auth.currentUser?.uid)
                chatButton.visibility = View.GONE
        }

    }

    private fun initChatButton() {
        if (auth.currentUser != null)
            binding.chatButton.setOnClickListener {
                val key = System.currentTimeMillis()
                val buyerChatRoom = ChatModel(
                    buyerId = auth.currentUser!!.uid,
                    sellerId = itemModel.sellerId,
                    title = itemModel.title,
                    type = "buyer",
                    key = key,
                    price = itemModel.price,
                    imgUrl = itemModel.imgUrl
                )

                val sellerChatRoom = ChatModel(
                    buyerId = auth.currentUser!!.uid,
                    sellerId = itemModel.sellerId,
                    title = itemModel.title,
                    type = "seller",
                    key = key,
                    price = itemModel.price,
                    imgUrl = itemModel.imgUrl
                )

                userDB.child(auth.currentUser!!.uid)
                    .child(CHILD_CHAT_PATH_STRING)
                    .push()
                    .setValue(buyerChatRoom)

                userDB.child(itemModel.sellerId)
                    .child(CHILD_CHAT_PATH_STRING)
                    .push()
                    .setValue(sellerChatRoom)

                val intent = Intent(this, ChatRoomActivity::class.java)

                intent.putExtra("chatModel", buyerChatRoom)
                startActivity(Intent(intent))

                finish()
            }


    }

    private fun setSellerName(sellerId: String) {
        userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        userDB.child(sellerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.sellerIdTextView.text =
                    snapshot.child(Constant.USER_NAME_PATH_STRING).value.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}