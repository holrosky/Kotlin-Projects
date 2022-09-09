# Youtube
***API로 받아온 비디오 목록을 RecyclerView에 보여주며 ExoPlayer로 재생할 수 있다.***

![3](https://user-images.githubusercontent.com/67175445/189037225-89045a62-0030-4f3b-9c6c-6d8e3942cbb4.gif)
![1](https://user-images.githubusercontent.com/67175445/189037319-5cfb2b45-3e6a-4a7f-985a-65add4620864.png)
![2](https://user-images.githubusercontent.com/67175445/189037328-364140b9-4eb9-4fb0-9290-334fb62c15d5.png)



# Retrofit2 (ver. 2.9.0)
***Retrofit***을 활용하여 ***API 정보를 수신***한다. 해당 프로젝트에서는 ***dummy video를 제공해주는 json (https://gist.github.com/deepakpk009/99fd994da714996b296f11c3c371d5ee) 을 Mocky에 등록하여 API를 생성하였다.***

+ Service
  ```kotlin
  interface VideoService {
      @GET("/v3/e8ee2d3b-cd1d-4657-a2f0-b5347e6ad444")
      fun listVideos(): Call<VideoDTO>
  }
  ```
+ DTO
  ```kotlin
  data class VideoDTO(
      val videos: List<VideoModel>
  )
  ```

# MotionLayout
***MotionLayout을 활용하여 하단의 Fragment을 담고있는 FrameLayout을 위아래로 스와이프 가능하게 하였다.*** 문제는 FrameLayout이 화면 전체를 덮고있고 그 안에 Fragment가 작게 보이는 것이기때문에
RecyclerView를 위아래로 스와이프해도 해당 터치을 FrameLayout이 가지고 있으므로 RecyclerView가 움직이지 않는다. 
따라서 MotionLayout을 커스텀마이징하여 Fragment 밖의 영역을 터치하면 해당 터치는 FrameLayout이 흘려보내도록 하여 RecyclerView가 해당 터치를 받을 수 있도록 한다.
```kotlin
class CustomMotionLayout(context: Context, attributeSet: AttributeSet? = null): MotionLayout(context, attributeSet) {
    private var motionTouchStarted = false

    private val mainContainerLayout by lazy {
        findViewById<View>(R.id.mainContainerLayout)
    }

    private val hitRect = Rect()
    private val gestureListener by lazy {
        object: GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                mainContainerLayout.getHitRect(hitRect)

                return hitRect.contains(e1.x.toInt(), e1.y.toInt())
            }
        }
    }

    private val gestureDetector by lazy {
        GestureDetector(context, gestureListener)
    }

    init {
        setTransitionListener(object: TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                motionTouchStarted = false
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}

        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                motionTouchStarted = false
                return super.onTouchEvent(event)
            }
        }

        if (!motionTouchStarted) {
            mainContainerLayout.getHitRect(hitRect)
            motionTouchStarted = hitRect.contains(event.x.toInt(), event.y.toInt())
        }

        return super.onTouchEvent(event) && motionTouchStarted
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)


    }
}
```

# ExoPlayer
***ExoPlayer는 Android 프레임워크에 속하지 않고 Android SDK에서 별도로 배포되는 구글에서 만든 미디어 재생 오픈소스 프로젝트이다.***
다양한 종류의 미디어 파일을 쉽게 재생할 수 있도록 도와주며 준다. 별 다른 설정 없이도 네트워크로부터 미디어를 스트리밍 형태로 불러와 재생할 수도 있고 다양한 포맷들을 지원하며 커스터마이징도 지원한다.
Youtube에서도 ExoPlayer를 사용한다.
```kotlin
fun playVideo(url: String) {
    context?.let {
        val dataSourceFactory = DefaultDataSourceFactory(it)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }
}
```

# Databinding + ViewModel + LiveData
***Databinding***은 ***Android Archictecture Components*** 의 한 부분으로서 UI 요소와 데이터를 프로그램적 방식으로 연결하지 않고, ***선언적 형식으로 결합할 수 있게 도와주는 라이브러리***를 말한다.
이는 ***MVVM 개발 구조를 가능하게 만들어준다는 점에서 매우 중요한 개념***이다. Databinding 은 Viewbinding을 포함한 보다 더 큰 범위로서 작동한다.


  + ***데이터 바인딩을 사용하면, RecyclerView 에 각각의 item 을 set 해주는 작업도 자동으로 진행된다.***
  ```Kotlin
  class VideoAdapter(val onClickListener: (VideoModel) -> Unit) :
      ListAdapter<VideoModel, VideoAdapter.ItemViewHolder>(diffUtil) {

      inner class ItemViewHolder(private val binding: ItemVideoBinding) :
          RecyclerView.ViewHolder(binding.root) {

          fun bind(videoModel: VideoModel) {
              binding.videoModel = videoModel
              binding.videoContainerLayout.setOnClickListener {
                  onClickListener(videoModel)
              }
          }
      }
  }
  ```
  + ***data 가 바뀌면 자동으로 View 를 변경하게 할 수 있다.***
  ```Kotlin
  class VideoAdapter(val onClickListener: (VideoModel) -> Unit) :
      ListAdapter<VideoModel, VideoAdapter.ItemViewHolder>(diffUtil) {

      inner class ItemViewHolder(private val binding: ItemVideoBinding) :
          RecyclerView.ViewHolder(binding.root) {

          fun bind(videoModel: VideoModel) {
              binding.videoModel = videoModel
              binding.videoContainerLayout.setOnClickListener {
                  onClickListener(videoModel)
              }
          }
      }
  }
  ```

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
    

  ***LiveData와 Databinding을 같이 사용하면 그 효율이 극대화 되는데, View에 보여줄 data를 ViewModel이 LiveData형태로 가지고 있으면 Activity나 Fragment에서 해당 data를
  observing 함으로서 data가 변경될때 마다 View의 내용도 업데이트가 되는것을 볼 수 있다.***
  
  ```Kotlin
  class FragmentViewModel: ViewModel() {
      var title = MutableLiveData<String>()

      init {
          title.value = "재생중인 동영상이 없습니다!"
      }

      fun setTitle(title: String) {
          this.title.value = title
      }
  }
  ```
  
  ```Kotlin
  @Override
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
      binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)

      fragmentViewModel = ViewModelProvider(requireActivity()).get(FragmentViewModel::class.java)
      binding?.fragmentViewModel = fragmentViewModel
      binding?.lifecycleOwner = viewLifecycleOwner

      return binding?.root
  }
  ```
  
  + ***주의사항***
    + 위처럼 ***Fragment에서 LiveData를 Observe 할 때에는 lifecycleOwner를 this로 하지말고 viewLifecycleOwner로 해주어야 한다.
    Fragment는 Actvitiy와 다르게 onDestroy 가 호출되지 않은 상태에서 onCreateView 가 여러 번 호출될 수 있다.*** 
    이로 인해 ***Fragment의 Lifecycle은 Destroy 되지 않은 상황에서 LiveData에 새로운 Observer가 등록되어 복수의 Observer가 호출되는 현상이 발생할 가능성이 있기 때문이다.***
    
    + ***ViewModel에서는 Activity나 Fragment의 context를 참조해서는 안된다.*** 만약 참조를 한다면 위에서 언급한대로 ViewModel은 Activity나 Fragment의 생명주기에 영향을 받지
      않기 때문에 만약 Activity나 Fragment가 onDestroy가 되어도 ViewModel에 의해 여전히 참조된다. onDestroy된 Activity나 Fragment는 Garbage Collector에 의해 메모리상에서 
      release가 되어야 하지만 ***ViewModel이 참조하고 있으므로 GC의 대상이 되지않아 메모리상에 계속 존재하게 된다. 즉 메모리 누수 (Memory Leak) 현상이 발생한다.*** 
  
또한, ***데이터 바인딩은 MVP 또는 MVVM 패턴을 구현하기 위해 유용하게 사용된다.*** 하지만 Databinding은 ***많은 클래스 파일이 생성되며 컴파일 속도가 느리다는 단점***이 존재한다. 
따라서 만약 findViewId()를 사용하지 않을 목적만 있다면 Databinding보다 Viewbinding을 활용하는 것이 낫다. ***Databinding은 MVVM 혹은 MVP 아키텍쳐와 같이 활용하는것이 좋다.***

### Databinding Adapter
***XML 내에서 속성값을 마음대로 커스텀마이징*** 할 수 있게 해준다.
```xml
<data>
    <variable
        name="videoModel"
        type="com.kotlin_project.youtube.model.VideoModel" />
</data>
    
<ImageView
    android:id="@+id/thumbnailImageView"
    android:layout_width="0dp"
    android:layout_height="230dp"
    android:scaleType="center"
    app:imageUrl="@{videoModel.thumb}"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```   
위 ImageView는 ```app:imageUrl``` 이라는 기존에 없는 속성을 사용하는데, 이는 ***Databinding Adapter 내에서 정의한 속성이며 마치 하나의 함수처럼 사용이 가능***하다.
   
   
```Kotlin
object BindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String) {

        Glide.with(imageView.context).load(url)
            .into(imageView)
    }

}
```   

위와 같은 방식으로 ***해당 함수를 어떤 속성으로 연결할지 정하는 어노테이션과 정적으로 선언하는 어노테이션을 함수 위에 붙혀 선언***하면 된다.

