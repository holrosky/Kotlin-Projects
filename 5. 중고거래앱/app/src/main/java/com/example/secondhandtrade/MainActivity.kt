package com.example.secondhandtrade

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.secondhandtrade.Constant.USERS_PATH_STRING
import com.example.secondhandtrade.Constant.USER_ID_PATH_STRING
import com.example.secondhandtrade.Constant.USER_NAME_PATH_STRING
import com.example.secondhandtrade.ui.chat.ChatFragment
import com.example.secondhandtrade.ui.home.HomeFragment
import com.example.secondhandtrade.ui.myPage.MyPageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val bottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.bottomNavigationView)
    }

    private val homeFragment by lazy {
        HomeFragment()
    }

    private val chatFragment by lazy {
        ChatFragment()
    }

    private val myPageFragment by lazy {
        MyPageFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userDB = Firebase.database.reference.child(USERS_PATH_STRING)
        auth = FirebaseAuth.getInstance()

        replaceFragment(homeFragment)
        initBottomNavigationView()

    }

    override fun onStart() {
        super.onStart()

        requestUserName()
    }

    private fun initBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.chat -> replaceFragment(chatFragment)
                R.id.myPage -> replaceFragment(myPageFragment)
            }

            true
        }
    }

    private fun requestUserName() {
        if (auth.currentUser != null) {
            val currentUserDB = userDB.child(getCurrentUserId())
            currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(USER_NAME_PATH_STRING).value == null) {
                        showNameInputPopup()
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun showNameInputPopup() {
        val dialog = UserNameDialog(this)
        dialog.showDialog()

        dialog.setOnClickListener(object : UserNameDialog.ButtonClickListener {
            override fun onSaveButtonClick(userName: String) {
                if (userName.isEmpty())
                    Toast.makeText(this@MainActivity, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
                else
                    saveUserName(userName)
            }
        })
    }

    private fun getCurrentUserId(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인 상태가 아닙니다!", Toast.LENGTH_SHORT).show()
            finish()
        }

        return auth.currentUser?.uid.orEmpty()
    }

    private fun saveUserName(name: String) {
        val userId = getCurrentUserId()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user[USER_ID_PATH_STRING] = userId
        user[USER_NAME_PATH_STRING] = name
        currentUserDB.updateChildren(user)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.frameLayout, fragment)
                commit()
            }
    }
}