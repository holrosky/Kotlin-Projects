package com.kotlin_project.tinder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kotlin_project.tinder.Constant.LIKED_BY_PATH_STRING
import com.kotlin_project.tinder.Constant.MATCH_PATH_STRING
import com.kotlin_project.tinder.Constant.USERS_PATH_STRING
import com.kotlin_project.tinder.Constant.USER_NAME_PATH_STRING

class MatchedUserActivity:AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var userDB: DatabaseReference
    private lateinit var matchedUserRecyclerViewAdapter: MatchedUserRecyclerViewAdapter
    private val cardItems = mutableListOf<CardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        auth = FirebaseAuth.getInstance()
        userDB = Firebase.database.reference.child(USERS_PATH_STRING)
        matchedUserRecyclerViewAdapter = MatchedUserRecyclerViewAdapter()

        userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        initMatchedUserRecyclerView()
        getMatchUsers()

    }

    private fun initMatchedUserRecyclerView() {
        val matchedUserRecyclerView = findViewById<RecyclerView>(R.id.matchedUserRecyclerView)

        matchedUserRecyclerView.layoutManager = LinearLayoutManager(this)
        matchedUserRecyclerView.adapter = matchedUserRecyclerViewAdapter
    }

    private fun getMatchUsers() {
        val matchDB = userDB.child(getCurrentUserId()).child(LIKED_BY_PATH_STRING).child(
            MATCH_PATH_STRING)

        matchDB.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key?.isNotEmpty() == true) {
                    getUserByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun getUserByKey(userId: String) {
        userDB.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItems.add(CardItem(userId, snapshot.child(USER_NAME_PATH_STRING).value.toString()))
                matchedUserRecyclerViewAdapter.submitList(cardItems)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun getCurrentUserId(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인 상태가 아닙니다!", Toast.LENGTH_SHORT).show()
            finish()
        }

        return auth.currentUser?.uid.orEmpty()
    }

}