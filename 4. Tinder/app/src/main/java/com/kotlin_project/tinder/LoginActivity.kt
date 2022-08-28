package com.kotlin_project.tinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val idEditText: EditText by lazy {
        findViewById(R.id.idEditText)
    }

    private val passwordEditText: EditText by lazy {
        findViewById(R.id.passwordEditText)
    }

    private lateinit var callBackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        callBackManager = CallbackManager.Factory.create()

        initLoginButton()
        initSignUpButton()
        initFacebookLoginButton()
    }

    private fun initLoginButton() {
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하여 주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener { login ->
                    if (login.isSuccessful)
                        finish()
                    else {
                        val errorMsg = when (login.exception) {
                            is FirebaseAuthWeakPasswordException -> "비밀번호는 6자리 이상이여야 합니다!"
                            is FirebaseAuthInvalidCredentialsException -> "아이디는 이메일 형식으로 입력해주세요!"
                            is FirebaseAuthInvalidUserException -> "아이디 혹은 비밀번호를 확인해주세요!"
                            is FirebaseTooManyRequestsException -> "너무 많은 로그인 시도를 하였습니다. 잠시 후 다시 시도해주세요."
                            else -> "알수없는 에러가 발생하였습니다!"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initSignUpButton() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하여 주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener { signUp ->
                    if (signUp.isSuccessful) {
                        Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = when (signUp.exception) {
                            is FirebaseAuthWeakPasswordException -> "비밀번호는 6자리 이상이여야 합니다!"
                            is FirebaseAuthInvalidCredentialsException -> "아이디는 이메일 형식으로 입력해주세요!"
                            is FirebaseAuthUserCollisionException -> "이미 사용중인 아이디 입니다!"
                            else -> "알수없는 에러가 발생하였습니다!"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initFacebookLoginButton() {
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLoginButton)

        facebookLoginButton.setPermissions("email", "public_profile")
        facebookLoginButton.registerCallback(
            callBackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)

                    Firebase.auth.signInWithCredential(credential)
                        .addOnCompleteListener {
                            finish()
                        }
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException?) {
                    Toast.makeText(this@LoginActivity, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                }

            })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callBackManager.onActivityResult(requestCode, resultCode, data)
    }
}