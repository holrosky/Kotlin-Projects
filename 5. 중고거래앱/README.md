# 중고거래앱
**유저는 물건을 등록할 수 있으며 다른 유저가 올린 물건을 확인할 수 있으며 구매자와 판매자간 실시간 채팅을 할 수 있다.**

![1](https://user-images.githubusercontent.com/67175445/188542147-120b952d-f1b7-44b3-8a1f-467174346b69.png)
![2](https://user-images.githubusercontent.com/67175445/188542153-75f1a1a1-db30-47e9-a81a-2403f14a9c6f.png)
![3](https://user-images.githubusercontent.com/67175445/188542159-c662f32f-fb8e-4801-b890-3b3f0ecfd085.png)
![4](https://user-images.githubusercontent.com/67175445/188542174-308f4066-68d9-4657-8694-3a827b7d53dc.png)
![7](https://user-images.githubusercontent.com/67175445/188542191-5950e5bd-0391-4949-b7d0-a0f3bae4c2ab.png)
![5](https://user-images.githubusercontent.com/67175445/188542271-1b59e73b-5531-46fc-b89e-f11f36908826.png)
![6](https://user-images.githubusercontent.com/67175445/188542315-7125a25f-dc38-4f2f-8982-917acd85b193.png)


# Firebase Authentication

***Firebase의 Authentication을 활성화 하여 Email 및 Facebook 로그인 기능을 구현함.*** 

+ ***Email 로그인 예외처리***
  + ***FirebaseAuthWeakPasswordException*** : 비밀번호가 6자리 이상이 아닐경우
  + ***FirebaseAuthInvalidCredentialsException*** : 아이디가 이메일 형식이 아닐경우
  + ***FirebaseAuthInvalidUserException*** : 아이디 혹은 비밀번호가 틀린경우
  + ***FirebaseTooManyRequestsException*** : 너무 많은 로그인을 시도한 경우  

  ```kotlin
  auth.signInWithEmailAndPassword(id, password)
    .addOnCompleteListener { login ->
        if (login.isSuccessful)
            loginSuccess()
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
  ```

+ ***Email 회원가입 예외처리***
  + ***FirebaseAuthWeakPasswordException*** : 비밀번호가 6자리 이상이 아닐경우
  + ***FirebaseAuthInvalidCredentialsException*** : 아이디가 이메일 형식이 아닐경우
  + ***FirebaseAuthUserCollisionException*** : 이미 사용중인 이메일인 경우
  
  ```kotlin
  auth.createUserWithEmailAndPassword(id, password)
    .addOnCompleteListener { signUp ->
        if (signUp.isSuccessful) {
            Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
            loginSuccess()
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
  ```
  
+ ***비밀번호 변경 예외처리***
  + ***FirebaseAuthWeakPasswordException*** 예외를 사용하여 ***올바른 비밀번호로 바꾸는지 확인***한다.
  
+ ***Facebook 로그인***
  +  ***페이스북 Developer Document*** (https://developers.facebook.com/docs/facebook-login/android)에 상세히 설명되어 있음.
                

# Firebase RealTime Database
***Firebase의 RealTime Database을 활용하여 가입한 유저의 정보, 유저가 업로드한 물품 그리고 유저간의 채팅을 저장한다.***

+ ***유저 데이터베이스 구조***
  ![user_DB](https://user-images.githubusercontent.com/67175445/188542969-b64c7b5c-ba75-4201-951a-2b76844a83a7.png)


  + ***회원가입시 유저 등록***
    ```kotlin
      private fun loginSuccess() {
          if (auth.currentUser == null) {
              Toast.makeText(this, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()
              return
          }

          val userId = auth.currentUser?.uid.orEmpty()
          val currentUserDB = Firebase.database.reference.child(USERS_PATH_STRING).child(userId)
          val user = mutableMapOf<String, Any>()
          user[USER_ID_PATH_STRING] = userId
          currentUserDB.updateChildren(user)

          finish()
      }
    ```
  + ***닉네임 등록***
    ```kotlin
      private fun saveUserName(name: String) {
          val userId = getCurrentUserId()
          val currentUserDB = userDB.child(userId)
          val user = mutableMapOf<String, Any>()
          user[USER_ID_PATH_STRING] = userId
          user[USER_NAME_PATH_STRING] = name
          currentUserDB.updateChildren(user)

          getUnselectedUsers()
      }
    ```
  
+ ***아이템 데이터베이스 구조***
  ![item_DB](https://user-images.githubusercontent.com/67175445/188543241-6cef008d-a9c8-4e58-8813-332571e71a41.png)
  
  유저는 FloatingButton을 클릭하여 물품을 등록 할 수 있다. ***만약 로그인이 안되어있는 상태라면 로그인창으로 넘어간다.*** 사진을 가져오기위해
  유저로부터 사진 ***접근 권한***을 받아야한다. ***만약 유저가 권한을 거부하면 나중에 권한을 요청하기 전 권한이 왜 필요한지 팝업으로 알려준 후 권한을 다시 요청한다. 이는
  안드로이드 공식 문서에서 권장하는 방식이다.*** 유저가 등록한 사진은 ***Firebase Storage***에 등록된다.
  ![permission_flowchart](https://user-images.githubusercontent.com/67175445/188543868-3bf45fa4-e774-44de-8e1c-081d895db318.png)

    ```kotlin
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
    ```
    
  + ***아이템 등록 에외처리***
    + ***물품명, 가격, 상세설명은 필수***적으로 입력을 해야한다. 
    + ***상품 이미지는 등록을 안할경우 기본 사진으로 대체되어 업로드***가 된다. 
    + ***가격이 0원이라면 "나눔"으로 보여진다.***

  + ***판매자가 자신의 물품을 클릭하면 "채팅하기" 버튼을 숨김으로서 자신과 채팅하는 것을 방지한다.***
    
+ ***채팅 데이터베이스 구조***
  ![chat_DB](https://user-images.githubusercontent.com/67175445/188544742-f5df2f61-5a1b-4eb6-b2ba-bf4b4d4ba0ae.png)
  
  구매자가 판매자에게 메세지를 보내면 ***구매자와 판매자의 유저 데이터베이스에 해당 채팅방의 고유 ID가 추가***되여 유저의 채팅방 목록을 가져올 수 있으며
  ***채팅방은 채팅 데이터베이스에도 추가되어 유저들이나눈 대화를 저장***한다. 채팅을 데이터베이스에 저장할때는 ***채팅 data class를 통째로 저장***한다.
  
  ```kotlin
  data class ChatRoomModel(
    val senderId: String,
    val message: String,
    val time: Long
  ): Serializable {
      constructor(): this("", "", 0)
  }
  ```


# Firebase Storage
***Firebase의 Storage를 활용하여 유저가 등록한 물품의 이미지를 저장한다.***
```kotlin
private fun initUploadItemButton() {
    uploadItemButton.setOnClickListener {
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
```
***successHandler***와 ***errorHandler***를 등록하여 물품을 등록할 때 ***이미지 업로드가 성공적으로 이루어졌는지 확인***한다. 성공이라면 물품을 등록하고 실패하면 에러 Toast 메세지를 띄운다.



