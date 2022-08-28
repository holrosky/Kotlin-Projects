# Tinder (틴더)
**서로 좋아요를 누른 유저들끼리 매칭을 시켜주는 블라인드 데이팅앱**

![Screenshot_20220828_230952](https://user-images.githubusercontent.com/67175445/187078135-649c2dcb-4809-49a3-aad7-5b12477c0d96.png)
![Screenshot_20220828_222711](https://user-images.githubusercontent.com/67175445/187076381-86962784-07b5-4d6d-beed-299a7c0060a2.png)
![Screenshot_20220828_222806](https://user-images.githubusercontent.com/67175445/187076382-05534a4c-7ff3-4622-b972-216142262de9.png)
![KakaoTalk_20220828_222408448](https://user-images.githubusercontent.com/67175445/187076253-fdd26145-f9ad-4e4e-ba14-1d77c3e3e638.gif)



# Firebase Authentication

***Firebase***의 ***Authentication***을 활성화 하여 ***Email*** 및 ***Facebook*** 로그인 기능을 구현함. 

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
  
+ Facebook 로그인
  +  ***페이스북 Developer Document*** (https://developers.facebook.com/docs/facebook-login/android)에 상세히 설명되어 있음.
                

# Firebase RealTime Database
Firebase의 RealTime Database을 활용하여 가입한 유저의 정보와 좋아요 혹은 싫어요를 받은 기록을 저장한다.

+ ***데이터베이스 구조***
  ![DB](https://user-images.githubusercontent.com/67175445/187078215-6044c461-8d7b-4c32-bfd8-2cb3fe04df92.png)

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
  
+ ***좋아요 / 싫어요***
  + 다른 유저에게 좋아요를 보내면 그 유저의 Database에 누른 유저의 UserID가 등록이 되고, 자신에게 좋아요를 보낸 유저중 다른 유저의 UserID가 존재하는지 확인한다.
  ***만약 존재한다면 둘은 서로가 좋아요를 보낸것이므로 매칭으로 등록한다.***
  
    ```kotlin
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
    ```
    
    
# Swipe Animation
실제 Tinder 앱의 Swipe Animation과 가장 비슷한 오픈 라이브러리(https://github.com/yuyakaido/CardStackView) 를 사용함. ***Swipe의 Direction을 손쉽게 사용가능한 것이 장점.
Adapter로 RecyclerViewAdapter를 사용함.***

```kotlin
  override fun onCardSwiped(direction: Direction?) {
      when (direction) {
          Direction.Left -> dislike()
          Direction.Right -> like()
          else -> {

          }
      }
  }
```
    
# RecyclerView
***(복습)*** RecyclerView와 ListView는 비슷하지만 ***RecyclerView는 ListView의 단점을 보완했다.***
ListView는 스크롤을 하여 View가 화면 밖을 나가면 그 View를 제거 후 나중에 다시 생성을 하지만 
***RecyclerView는 ViewHolder로 View를 생성하였다가 필요할때 재사용한다.***
따라서 RecyclerView는 스크롤을 할때 버벅이는 현상을 줄였고 ***메모리 측면에서도 ListView 보다 우위를 점한다.***  

매칭이된 유저들의 목록을 나열할 때 RecyclerView를 활용함.


