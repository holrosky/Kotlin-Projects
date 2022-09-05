package com.example.secondhandtrade.ui.myPage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.secondhandtrade.Constant
import com.example.secondhandtrade.LoginActivity
import com.example.secondhandtrade.MainActivity
import com.example.secondhandtrade.R
import com.example.secondhandtrade.databinding.FragmentMyPageBinding
import com.example.secondhandtrade.ui.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MyPageFragment : Fragment(R.layout.fragment_my_page) {

    private var binding: FragmentMyPageBinding? = null
    private lateinit var fragmentMyPageBinding: FragmentMyPageBinding

    private lateinit var userDB: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(context, LoginActivity::class.java))
        } else {
            userDB = Firebase.database.reference.child(Constant.USERS_PATH_STRING)
                .child(getCurrentUserId())

            fragmentMyPageBinding = FragmentMyPageBinding.bind(view)
            binding = fragmentMyPageBinding

            initSignOutButton()
            initSaveButton()
            loadUserData()
        }
    }

    private fun initSignOutButton() {
        fragmentMyPageBinding.signOutButton.setOnClickListener {
            auth.signOut()
            (activity as MainActivity).bottomNavigationViewPerformClick(R.id.home)
            Toast.makeText(context, "로그아웃 되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initSaveButton() {
        fragmentMyPageBinding.saveButton.setOnClickListener {
            if (fragmentMyPageBinding.nameEditText.text.isEmpty()) {
                Toast.makeText(context, "이름을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUserInfo()
        }
    }

    private fun loadUserData() {

        userDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fragmentMyPageBinding.nameEditText.setText(snapshot.child(Constant.USER_NAME_PATH_STRING).value.toString())
                fragmentMyPageBinding.emailEditText.setText(snapshot.child(Constant.USER_EMAIL_PATH_STRING).value.toString())
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCurrentUserId(): String {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인 상태가 아닙니다!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(context, LoginActivity::class.java))
        }

        return auth.currentUser?.uid.orEmpty()
    }

    private fun saveUserInfo() {
        val userName = mutableMapOf<String, Any>()
        val userEmail = mutableMapOf<String, Any>()
        val userId = getCurrentUserId()
        userName[Constant.USER_ID_PATH_STRING] = userId
        userName[Constant.USER_NAME_PATH_STRING] =
            fragmentMyPageBinding.nameEditText.text.toString()

        userEmail[Constant.USER_ID_PATH_STRING] = userId
        userEmail[Constant.USER_EMAIL_PATH_STRING] =
            fragmentMyPageBinding.emailEditText.text.toString()

        if (fragmentMyPageBinding.passwordEditText.text.isEmpty()) {
            userDB.apply {
                updateChildren(userName)
                updateChildren(userEmail)
            }

            Toast.makeText(context, "저장되었습니다!", Toast.LENGTH_SHORT).show()
        } else {
            auth.currentUser?.updatePassword(fragmentMyPageBinding.passwordEditText.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userDB.apply {
                            updateChildren(userName)
                            updateChildren(userEmail)
                        }

                        Toast.makeText(context, "저장되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthWeakPasswordException -> "비밀번호는 6자리 이상이여야 합니다!"
                            is FirebaseAuthInvalidCredentialsException -> "아이디는 이메일 형식으로 입력해주세요!"
                            is FirebaseAuthUserCollisionException -> "이미 사용중인 아이디 입니다!"
                            else -> "알수없는 에러가 발생하였습니다!"
                        }

                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }

                }
        }

    }

}