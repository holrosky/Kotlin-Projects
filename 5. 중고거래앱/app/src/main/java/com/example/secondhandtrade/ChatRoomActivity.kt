package com.example.secondhandtrade

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandtrade.Constant.CHAT_PATH_STRING
import com.example.secondhandtrade.Constant.USERS_PATH_STRING
import com.example.secondhandtrade.databinding.ActivityChatRoomBinding
import com.example.secondhandtrade.ui.chat.ChatModel
import com.example.secondhandtrade.ui.chat.ChatRoomAdapter
import com.example.secondhandtrade.ui.chat.ChatRoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatModel: ChatModel

    private lateinit var chatDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val chatList = mutableListOf<ChatRoomModel>()
    private val adapter = ChatRoomAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            binding = ActivityChatRoomBinding.inflate(layoutInflater)

            setContentView(binding.root)

            binding.chatRoomRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.chatRoomRecyclerView.adapter = adapter

            chatModel = intent.getSerializableExtra("chatModel") as ChatModel

            initChatDB()
            initChatLabelTextView()
            initSendButton()
        }
    }

    private fun initChatLabelTextView() {
        userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        val anotherPersonid = when (chatModel.type) {
            "seller" -> chatModel.buyerId
            "buyer" -> chatModel.sellerId
            else -> "Unknown"
        }

        userDB.child(anotherPersonid).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.chatLabelTextView.text =
                    "${snapshot.child(Constant.USER_NAME_PATH_STRING).value.toString()}님과 채팅"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initChatDB() {
        chatDB = Firebase.database.reference.child(CHAT_PATH_STRING).child("${chatModel.key}")

        chatDB.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatRoomItem = snapshot.getValue(ChatRoomModel::class.java)
                chatRoomItem ?: return

                chatList.add(chatRoomItem)
                adapter.submitList(chatList)
                adapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun initSendButton() {
        binding.sendButton.setOnClickListener {
            if (auth.currentUser != null) {
                val chatRoomModel = ChatRoomModel(
                    senderId = auth.currentUser!!.uid,
                    message = binding.messageEditText.text.toString(),
                    time = System.currentTimeMillis()
                )

                chatDB.push().setValue(chatRoomModel)
                binding.messageEditText.text.clear()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}