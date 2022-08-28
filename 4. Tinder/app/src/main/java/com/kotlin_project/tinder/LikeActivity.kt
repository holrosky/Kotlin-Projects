package com.kotlin_project.tinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FacebookAuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kotlin_project.tinder.Constant.DISLIKE_PATH_STRING
import com.kotlin_project.tinder.Constant.LIKED_BY_PATH_STRING
import com.kotlin_project.tinder.Constant.LIKE_PATH_STRING
import com.kotlin_project.tinder.Constant.MATCH_PATH_STRING
import com.kotlin_project.tinder.Constant.USERS_PATH_STRING
import com.kotlin_project.tinder.Constant.USER_ID_PATH_STRING
import com.kotlin_project.tinder.Constant.USER_NAME_PATH_STRING
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity(), CardStackListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var userDB: DatabaseReference
    private lateinit var cardStackAdapter: CardStackAdapter

    private val cardStackViewLayoutManager by lazy {
        CardStackLayoutManager(this, this)
    }

    private val cardItems = mutableListOf<CardItem>()


    private val cardStackView by lazy {
        findViewById<CardStackView>(R.id.card_stack_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        auth = FirebaseAuth.getInstance()
        userDB = Firebase.database.reference.child(USERS_PATH_STRING)

        initCurrentUserDB()
        initCardStackView()
        initCheckMatchButton()
        initSignOutButton()
    }

    private fun initCurrentUserDB() {
        val currentUserDB = userDB.child(getCurrentUserId())
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(USER_NAME_PATH_STRING).value == null) {
                    showNameInputPopup()
                    return
                }

                getUnselectedUsers()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initCardStackView() {
        cardStackAdapter = CardStackAdapter()

        cardStackView.layoutManager = cardStackViewLayoutManager
        cardStackView.adapter = cardStackAdapter
    }

    private fun initCheckMatchButton() {
        val checkMatchButton = findViewById<Button>(R.id.checkMatchButton)

        checkMatchButton.setOnClickListener {
            startActivity(Intent(this, MatchedUserActivity::class.java))
        }
    }

    private fun initSignOutButton() {
        val signOutButton = findViewById<Button>(R.id.signOutButton)

        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showNameInputPopup() {
        val dialog = UserNameDialog(this)
        dialog.showDialog()

        dialog.setOnClickListener(object : UserNameDialog.ButtonClickListener {
            override fun onSaveButtonClick(userName: String) {
                if (userName.isEmpty())
                    Toast.makeText(this@LikeActivity, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
                else
                    saveUserName(userName)
            }
        })
    }

    private fun saveUserName(name: String) {
        val userId = getCurrentUserId()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user[USER_ID_PATH_STRING] = userId
        user[USER_NAME_PATH_STRING] = name
        currentUserDB.updateChildren(user)

        getUnselectedUsers()
    }

    private fun getCurrentUserId(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인 상태가 아닙니다!", Toast.LENGTH_SHORT).show()
            finish()
        }

        return auth.currentUser?.uid.orEmpty()
    }

    private fun getUnselectedUsers() {
        userDB.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child(USER_ID_PATH_STRING).value != getCurrentUserId()
                    && snapshot.child(LIKED_BY_PATH_STRING).child(LIKE_PATH_STRING).hasChild(getCurrentUserId()).not()
                    && snapshot.child(LIKED_BY_PATH_STRING).child(DISLIKE_PATH_STRING).hasChild(getCurrentUserId()).not()
                ) {

                    val userId = snapshot.child(USER_ID_PATH_STRING).value.toString()
                    var name = "undecided"

                    if (snapshot.child(USER_NAME_PATH_STRING).value != null) {
                        name = snapshot.child(USER_NAME_PATH_STRING).value.toString()
                    }

                    cardItems.add(CardItem(userId, name))
                    cardStackAdapter.submitList(cardItems)
                    cardStackAdapter.notifyDataSetChanged()
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItems.find {
                    it.userId == snapshot.key
                }?.let {
                    it.userName = snapshot.child(USER_NAME_PATH_STRING).value.toString()
                }

                cardStackAdapter.submitList(cardItems)
                cardStackAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun like() {
        val card = cardItems[cardStackViewLayoutManager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child(LIKED_BY_PATH_STRING)
            .child(LIKE_PATH_STRING)
            .child(getCurrentUserId())
            .setValue(true)

        saveMatch(card.userId)

        Toast.makeText(this, "${card.userName}님을 Like 하셨습니다!", Toast.LENGTH_SHORT).show()
    }

    private fun dislike() {
        val card = cardItems[cardStackViewLayoutManager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child(LIKED_BY_PATH_STRING)
            .child(DISLIKE_PATH_STRING)
            .child(getCurrentUserId())
            .setValue(true)

        Toast.makeText(this, "${card.userName}님을 Dislike 하셨습니다!", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatch(otherUserId: String) {
        val otherUserDB = userDB.child(getCurrentUserId())
            .child(LIKED_BY_PATH_STRING)
            .child(LIKE_PATH_STRING)
            .child(otherUserId)

        otherUserDB.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == true) {
                    userDB.child(getCurrentUserId())
                        .child(LIKED_BY_PATH_STRING)
                        .child(MATCH_PATH_STRING)
                        .child(otherUserId)
                        .setValue(true)

                    userDB.child(otherUserId)
                        .child(LIKED_BY_PATH_STRING)
                        .child(MATCH_PATH_STRING)
                        .child(getCurrentUserId())
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {}

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Left -> dislike()
            Direction.Right -> like()
            else -> {

            }
        }
    }

    override fun onCardRewound() {}

    override fun onCardCanceled() {}

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}