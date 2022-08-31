package com.example.secondhandtrade.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.secondhandtrade.Constant
import com.example.secondhandtrade.LoginActivity
import com.example.secondhandtrade.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment: Fragment(R.layout.fragment_chat) {

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userDB = Firebase.database.reference.child(Constant.USERS_PATH_STRING)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}