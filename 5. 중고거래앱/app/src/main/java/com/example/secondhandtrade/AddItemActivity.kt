package com.example.secondhandtrade

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.secondhandtrade.Constant.ITEM_PATH_STRING
import com.example.secondhandtrade.Constant.ITEM_PHOTO_PATH_STRING
import com.example.secondhandtrade.ui.home.ItemModel
import com.facebook.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class AddItemActivity : AppCompatActivity() {

    private val itemImageView by lazy {
        findViewById<ImageView>(R.id.itemImageView)
    }

    private val uploadImgButton by lazy {
        findViewById<Button>(R.id.uploadImgButton)
    }

    private val uploadItemButton by lazy {
        findViewById<Button>(R.id.uploadItemButton)
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }

    private val itemDB: DatabaseReference by lazy {
        Firebase.database.reference.child(ITEM_PATH_STRING)
    }

    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        initUploadImgButton()
        initUploadItemButton()

    }

    private fun initUploadImgButton() {
        uploadImgButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionPopup()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }

            }
        }
    }

    private fun initUploadItemButton() {
        uploadItemButton.setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString()
            val detail = findViewById<EditText>(R.id.detailEditText).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            if (title.isEmpty()) {
                Toast.makeText(this, "물품명을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (price.isEmpty()) {
                Toast.makeText(this, "가격을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (detail.isEmpty()) {
                Toast.makeText(this, "상세설명을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (auth.currentUser == null) {
                Toast.makeText(this, "로그인 후 이용해주세요!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            showProgressBar()

            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadItem(sellerId, title, price, detail, uri)
                    },
                    errorHandler = {
                        Toast.makeText(this, "사진 업로드에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                )
            } else {
                uploadItem(sellerId, title, price, detail, "")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "사진첩 접근에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK)

            when (requestCode) {
                2000 -> {
                    val uri = data?.data
                    if (uri != null) {
                        itemImageView.setImageURI(uri)
                        selectedUri = uri
                    } else {
                        Toast.makeText(this, "사진을 가져오지 못했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    Toast.makeText(this, "사진을 가져오지 못했습니다!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showPermissionPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해서 권한 승인을 해주세요.")
            .setPositiveButton("확인") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .create()
            .show()
    }

    private fun uploadItem(
        sellerId: String,
        title: String,
        price: String,
        detail: String,
        imageUri: String
    ) {
        val model =
            ItemModel(sellerId, title, System.currentTimeMillis(), price, imageUri, detail)
        itemDB.push().setValue(model)

        hideProgressBar()
        finish()
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child(ITEM_PHOTO_PATH_STRING).child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child(ITEM_PHOTO_PATH_STRING).child(fileName).downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }

    }

    private fun showProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
    }

    private fun hideProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("물건 등록을 취소하시겠습니까?")
            .setNegativeButton("취소") { _, _ ->}
            .setPositiveButton("확인") { _, _ ->
                finish()
            }
            .create()
            .show()
    }
}