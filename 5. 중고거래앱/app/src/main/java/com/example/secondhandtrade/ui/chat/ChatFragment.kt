package com.example.secondhandtrade.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandtrade.*
import com.example.secondhandtrade.Constant.CHILD_CHAT_PATH_STRING
import com.example.secondhandtrade.Constant.USERS_PATH_STRING
import com.example.secondhandtrade.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment: Fragment(R.layout.fragment_chat) {

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var binding: FragmentChatBinding? = null
    private val chatList = mutableListOf<ChatModel>()

    private lateinit var chatAdapter: ChatAdapter

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val chatModel = snapshot.getValue(ChatModel::class.java)
            chatModel ?: return

            chatList.add(chatModel)
            chatAdapter.submitList(chatList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser?.uid == null) {
            startActivity(Intent(context, LoginActivity::class.java))
        } else {
            val fragmentChatBinding = FragmentChatBinding.bind(view)
            binding = fragmentChatBinding

            chatList.clear()

            userDB = Firebase.database.reference.child(USERS_PATH_STRING).child(auth.currentUser!!.uid).child(CHILD_CHAT_PATH_STRING)
            chatAdapter = ChatAdapter(onItemClicked = { chatModel ->
                val intent = Intent(context, ChatRoomActivity::class.java)

                intent.putExtra("chatModel", chatModel)
                startActivity(Intent(intent))

            })

            fragmentChatBinding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
            fragmentChatBinding.chatRecyclerView.adapter = chatAdapter

            userDB.addChildEventListener(listener)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        chatAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userDB.removeEventListener(listener)
    }
}