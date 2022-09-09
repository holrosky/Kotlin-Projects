# Melon(Music Player)
***API로 받아온 음악 목록을 RecyclerView에 보여주며 ExoPlayer로 재생할 수 있다.***

![1](https://user-images.githubusercontent.com/67175445/189359712-e21ebe9f-f1f9-4bcc-8bd4-7b71edba87cf.png)
![2](https://user-images.githubusercontent.com/67175445/189359716-c4cf1cb5-d04b-4a35-a6b4-50c7d0b65afe.png)



# Retrofit2 (ver. 2.9.0)
***Retrofit***을 활용하여 ***API 정보를 수신***한다. 해당 프로젝트에서는 ***음반 정보를 Json에 담아 Mocky에 등록하여 API를 생성하였다.***

+ Service
  ```kotlin
  interface MusicService {
      @GET("/v3/bdbd4d78-46cd-4f78-852c-598b0766244e")
      fun listMusics(): Call<MusicDTO>
  }
  ```
+ DTO
  ```kotlin
  data class MusicDTO (
      val musics: List<MusicEntity>
  )
  ```
  
+ Entity   
  이번 프로젝트에서는 MusicModel과 MusicEntity를 분리한 후 ***Mapping을 통해 MusicModel을 생성***하였다. 이는 Retrofit을 통해 받아온 음반 정보들 보다 MusicModel에서는 
  다른 부가적인 데이터도 필요하기 때문이다.
  
  ```kotlin
  data class MusicEntity(
      @SerializedName("track") val track: String,
      @SerializedName("streamUrl") val streamUrl: String,
      @SerializedName("artist") val artist: String,
      @SerializedName("coverUrl") val coverUrl: String
  )
  ```
  
  ```kotlin
  fun MusicEntity.mapper(id: Long): MusicModel =
      MusicModel(
          id = id,
          streamUrl = streamUrl,
          coverUrl = coverUrl,
          artist = artist,
          track = track
  )

  fun MusicDTO.mapper(): List<MusicModel> =
      musics.mapIndexed { index, musicEntity ->
          musicEntity.mapper(index.toLong())
  }
  ```
  
# ExoPlayer
***ExoPlayer는 Android 프레임워크에 속하지 않고 Android SDK에서 별도로 배포되는 구글에서 만든 미디어 재생 오픈소스 프로젝트이다.***
다양한 종류의 미디어 파일을 쉽게 재생할 수 있도록 도와주며 준다. 별 다른 설정 없이도 네트워크로부터 미디어를 스트리밍 형태로 불러와 재생할 수도 있고 다양한 포맷들을 지원하며 커스터마이징도 지원한다.
Youtube에서도 ExoPlayer를 사용한다.
```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://run.mocky.io")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

retrofit.create(MusicService::class.java)
    .also {
        it.listMusics()
            .enqueue(object : Callback<MusicDTO> {
                override fun onResponse(
                    call: Call<MusicDTO>,
                    response: Response<MusicDTO>
                ) {
                    response.body()?.let { musicDTO ->

                        musicModelViewModel.musicList.value = musicDTO.mapper()

                        initMusicList(musicModelViewModel.musicList.value.orEmpty())
                    }
                }

                override fun onFailure(call: Call<MusicDTO>, t: Throwable) {}

            })
    }
```

# MVVM (Model - ViewModel - View) 아키텍쳐 패턴 (매우 중요. 복습 필수)
MVVM을 이해하기 위해선 MVVM이 무엇을 보완하기 위해 고안되었는지 그 배경을 알 필요가있다. 

MVVM 패턴 이전에 MVC 패턴 (Model - Veiw - Controller)에서는 ***유저가 이벤트를 발생시키면
Controller가 이를 담당하여 Model과 View에게 업데이트를 요청***하였다. 즉 Controller는 Model과 View를 이어주는 다리역할을 하였으며 Model과 View의 상호 의존도는 느슨해지는 패턴이였다.
하지만 프로젝트의 규모가 커짐과 동시에 ***Controller가 담당하는 작업의 양이 늘어났고 이는 Controller에게 너무 부담이 되었다.***     

이를 해결하기 위해 MVVM 패턴이 고안되었는데, View는 유저가 발생시키는 이벤트를 받고 ViewModel의 데이터를 관찰(Observe)한다. ViewModel은 View가 요청한 데이터를 Model에 요구하고 Model은 해당 데이터를 ViewModel에게 넘겨준다.
여기서 ViewModel의 데이터가 갱신되고 이를 관찰하고 있던 View에서는 UI를 갱신한다.    

***즉 View는 ViewModel을 관찰하고 ViewModel은 Model에 데이터 요청을하고 Data는 ViewModel이 요구하는 데이터만 넘겨주면 되는 구조이다.*** 
이러한 구조 덕분에 View에서는 View에 관한 코드만 작성하면 되기때문에 나중에 View에서 문제가 있다면 View만 보면 된다.    

***상호 의존도가 느슨해 짐으로서 유지 보수가 한결 쉬워지고 코드 가독성이 향상된 것이다.***    

![mvc](https://user-images.githubusercontent.com/67175445/189355090-5943594a-bf9c-4f6f-9795-69c47644fda9.png)
![mvvm](https://user-images.githubusercontent.com/67175445/189355095-c266d216-1da3-4410-858c-7adee5dba6a4.png)   

## ViewModel
Clean Architecture를 쉽게 구현할 수 있도록 라이브러리들을 Android Architecture Components (AAC)라고 부르며 그중 하나가 바로 ***ViewModel***이다.

  + ***탄생배경***   
    Activity는 onCreate부터 시작해서 onDestroy까지의 생명주기를 가진다. 하지만 ***시스템에서 이벤트가 일어나면 Activity는 onDestroy가 되고 다시 onCreate가 되는 경우가 있는데,
    대표적으로 기기의 화면을 회전할때다.*** 이렇게 되면 해당 Activity에 존재하던 View들은 ***다시 onCreate에 의해서 초기화가 이루어지므로 기존에 존재하던 데이터들이 전부 초기화***가 된다.
    이를 해결하기 위해 saveInstanceState 개념이 도입되었지만 담을 수 있는 데이터가 적으며 그 형태또한 제한이 된다. 뿐만 아니라 onCreate에서 이루어지는 작업이므로 UI 컨트롤러에게
    작업 부담을 주어 화면이 로딩되는데 시간이 오래걸릴 수 있다. ***이러한 문제를 해결하기 위해 ViewModel 개념이 도입되었다.***
    
  + ***생명주기***   
    따라서 ***ViewModel은 Activity 혹은 Fragment와 다른 생명 주기를 가진다.***   
    ![life_cycle](https://user-images.githubusercontent.com/67175445/189041429-b4e09b7d-5047-4cf8-ac64-5f27115aab53.png)   
    위와 같이 ***ViewMode은 Activity 혹은 Fragment와 다른 생명 주기를 가지므로 화면의 회전이 일어나는등 Activity의 state가 변경되어도 ViewMdel은 영향을 받지 않는다.***
    
  + ***주의사항***
    + ***Fragment에서 LiveData를 Observe 할 때에는 lifecycleOwner를 this로 하지말고 viewLifecycleOwner로 해주어야 한다.
    Fragment는 Actvitiy와 다르게 onDestroy 가 호출되지 않은 상태에서 onCreateView 가 여러 번 호출될 수 있기 때문이다.*** 
    이로 인해 ***Fragment의 Lifecycle은 Destroy 되지 않은 상황에서 LiveData에 새로운 Observer가 등록되어 복수의 Observer가 호출되는 현상이 발생할 가능성이 있기 때문이다.***
    
    + ***ViewModel에서는 Activity나 Fragment의 context를 참조해서는 안된다.*** 만약 참조를 한다면 위에서 언급한대로 ViewModel은 Activity나 Fragment의 생명주기에 영향을 받기 때문에
      만약 Activity나 Fragment가 onDestroy가 되어도 ViewModel에 의해 여전히 참조된다. onDestroy된 Activity나 Fragment는 Garbage Collector에 의해 메모리상에서 release가 되어야 
      ***하지만 ViewModel이 참조하고 있으므로 GC의 대상이 되지않아 메모리상에 계속 존재하게 된다. 즉 메모리 누수 (Memory Leak) 현상이 발생한다.*** 

안드로이드에서 기본적으로 MVVM 패턴을 구현하기 위해서는 View에서 ViewModel의 데이터를 Observe 하는 코드를 작성한다. 이때 ***ViewModel의 데이터는 LiveData<T> 클래스로 선언***된다.
``` kotlin
musicModelViewModel.track.observe(viewLifecycleOwner) {
    binding?.trackTextView?.text = it
}
```   
위 코드를 통해 ViewModel의 track의 값이 바뀌면 자동으로 TextView의 Text값도 변경된다.

## MVVM + Databinding
위와 같이 데이터를 관찰하지 않아도 ***Databinding을 같이 활용하면 코드의 길이가 비약적으로 줄어들 수 있다.*** XML에서 TextView에 해당 ViewModel의 데이터를 연결해주면
따로 관찰하지 않아도 Data가 자동으로 binding된다.

```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>
        <variable
            name="musicModelViewModel"
            type="com.kotlin_project.melon.model.MusicViewModel" />
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
      
            <TextView
                android:id="@+id/trackTextView"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="30dp"
                android:text="@{musicModelViewModel.track}"
                android:textColor="@color/white"
                android:textSize="20sp"
                android:textStyle="bold"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />
      
    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```   
***Databinding***은 ***Android Archictecture Components*** 의 한 부분으로서 UI 요소와 데이터를 프로그램적 방식으로 연결하지 않고, ***선언적 형식으로 결합할 수 있게 도와준다.***
이는 ***MVVM 패턴을 적용함에 있어서 보다 손쉽게 구현을 가능하게 해준다.

### Databinding Adapter
***XML 내에서 속성값을 마음대로 커스텀마이징*** 할 수 있게 해준다.

```Kotlin
object BindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?) {
        if (url == null)
            return

        Glide.with(imageView.context).load(url)
            .into(imageView)
    }

    @BindingAdapter("controlButtonImage")
    @JvmStatic
    fun loadControlButtonImage(imageView: ImageView, isPlaying: Boolean) {

        if (isPlaying)
            imageView.setImageResource(R.drawable.ic_pause_48)
        else
            imageView.setImageResource(R.drawable.ic_play_48)
    }

    @BindingAdapter("timeText")
    @JvmStatic
    fun setTimeText(textView: TextView, time: Long) {
        textView.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS),
            (time / 1000) % 60
        )
    }
}
```   
```xml
<TextView
  android:id="@+id/currentPlayTimeTextView"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:layout_marginTop="4dp"
  android:textColor="@color/app_color_orange"
  android:textStyle="bold"
  app:layout_constraintStart_toStartOf="@id/playerSeekBar"
  app:layout_constraintTop_toBottomOf="@id/playerSeekBar"
  app:timeText="@{musicModelViewModel.playPosition}" />

<TextView
  android:id="@+id/totalPlayTimeTextView"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:layout_marginTop="4dp"
  android:textColor="@color/gray_97"
  android:textStyle="bold"
  app:layout_constraintEnd_toEndOf="@id/playerSeekBar"
  app:layout_constraintTop_toBottomOf="@id/playerSeekBar"
  app:timeText="@{musicModelViewModel.duration}" />
```
해당 프로젝트에서는 음악의 현재 재생된 시간과 총 재생시간 두가지를 1초마다 업데이트하는데, ViewModel의 현재 얼만큼 재생되었는지에 대한 정보를 담은 playPosition 총 재생 시간 
  정보를 담고있는 duration 값이 업데이트 될 때마다 Databinding에 의해 ```setTimeText``` 함수가 호출되어 UI를 업데이트한다.
  
# 느낀점
  이번 프로젝트에서는 MVVM 패턴을 활용하고자 노력을 많이 했지만 ViewModel과 Model의 구분이 모호하다. 앞으로는 Model에서 Database 혹은 API등의 data 처리를 하여
  ViewModel과 Model을 확실히 구분할 수 있도록 해야겠다.

