# AirBnB
***API로 받아온 숙소정보를 NaverMap을 이용하여 지도 위에 마커로 표시한다. 유저는 하단의 ViewPager 혹은 BottomSheetBehavior에 있는 숙소 정보를 볼 수 있다.***

![KakaoTalk_20220906_212008430](https://user-images.githubusercontent.com/67175445/188761173-72456e74-4893-4303-a68f-4e9b93ca48a6.gif)
![1](https://user-images.githubusercontent.com/67175445/188761193-129cf974-e52b-4a60-8035-657e1c7ca842.png)
![2](https://user-images.githubusercontent.com/67175445/188761205-8cb1d763-a8c6-4573-a54d-65881f49cab1.png)

# Retrofit2 (ver. 2.9.0)
***Retrofit***을 활용하여 ***API 정보를 수신***한다. 해당 프로젝트에서는 ***숙소 정보를 제공해주는 API를 Mocky를 활용하여 가상으로 만들어 주었다.***

+ Service
  ```kotlin
  interface AccommodationService {
      @GET("https://run.mocky.io/v3/e5faf13d-63ff-412c-b761-48b958370ff5")
      fun getAccommdationList(): Call<AccommodationDTO>
  }
  ```
+ DTO
  ```kotlin
  data class AccommodationDTO (
      val items: List<AccommodationModel>
  )
  ```

# BottomSheetBehavior
안드로이드에서 BottomSheetBehavior를 구현하는 방법은 간단하다. View 속성에 ```app:layout_behavior``` 속성을 추가해주면 된다.
```XML
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/bottomSheetBehavior"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:behavior_peekHeight="100dp"
    app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior"/>
```

***BottomSheetBehavior 속성(Attribute)***



▀ ***BottomSheetBehavior_Layout_android_maxWidth*** : BottomSheet의 최대 가로길이 설정

 

▀ ***BottomSheetBehavior_Layout_behavior_draggable*** : 드래그를 통해서 BottomSheet을 접고 펼칠지 여부, 기본값은 true

 

▀ ***BottomSheetBehavior_Layout_behavior_expandedOffset*** : BottomSheet을 완전히 펼쳤을때 상단에 여백을 주고싶을경우, 그만큼의 Offset.

 

▀ ***BottomSheetBehavior_Layout_behavior_halfExpandedRatio*** : BottomSheet의 상태중에서 STATE_HALF_EXPANDED라는 상태가있는데, 뷰가 절반정도 펼쳐졌을때 이 상태값을 가지게 된다. BottomSheet가 어느정도로 펼쳐졌을때 이 상태가 될지 기준을 정한다. 기본 값은 딱 절반인 0.5다.



▀ ***BottomSheetBehavior_Layout_behavior_hideable*** : true일경우, BottomSheet을 아래로 내려 사라지게 할 수 있다. 

 

▀ ***BottomSheetBehavior_Layout_behavior_peekHeight*** : BottomSheet이 접힌 상태일때 높이를 설정한다. 다시 꺼낼수 있을정도의 높이가 되어야하므로 최소 16dp 이상 잡는 것이 UX에 좋을 것이다.

 

▀ ***BottomSheetBehavior_Layout_behavior_skipCollapsed*** : 완전히 펼친 상태에서 숨김상태로 변할때, 접힘 상태를 스킵할지 하지않을지 여부다. 기본값은 false. 일반적이라면 펼친후에 BottomSheet를 사라지게 한다면 EXPANDED -> HALF_EXPANDED -> COLLAPSED -> HIDDEN이 되겠지만, 이 속성을 true로 설정한다면 중간에 COLLAPSED 단계가 빠지게 된다.


***BottomSheetBehavior 상태(State)***


▀ ***STATE_SETTLING***: (움직이다가) 안정화되는 중

▀ ***STATE_DRAGGING***: 드래그하는 중

▀ ***STATE_HIDDEN***: 숨겨짐

▀ ***STATE_COLLAPED***: 접힘

▀ ***STATE_HALF_EXPANDED***: 절반이 펼쳐짐

▀ ***STATE_EXPANDED***:  완전히 펼쳐짐

# Databinding
Databinding은 Android Archictecture Components 의 한 부분으로서 Ui 요소와 데이터를 프로그램적 방식으로 연결하지 않고, 선언적 형식으로 결합할 수 있게 도와주는 라이브러리를 말한다.
이는 MVVM 개발 구조를 가능하게 만들어준다는 점에서 매우 중요한 개념이다. Databinding 은 Viewbinding을 포함한 보다 더 큰 범위로서 작동한다.

▀ ***Databinding 사용 이유***   
데이터 바인딩을 사용하면, ***데이터를 UI 요소에 연결하기 위해 필요한 코드를 최소화할 수 있다.***
  + findViewId() 를 호출하지 않아도, 자동으로 xml 에 있는 VIew 들을 만들어준다.
    ```Kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)


        binding.accommodationViewPager.adapter = viewPagerAdapter
        binding.includeView.accommodationRecyclerView.adapter = recyclerViewAdapter
        binding.includeView.accommodationRecyclerView.layoutManager = LinearLayoutManager(this)

        behavior = BottomSheetBehavior.from(binding.includeView.bottomSheetBehavior)

        registerClickListner()
    }
    ```
  + RecyclerView 에 각각의 item 을 set 해주는 작업도 자동으로 진행된다.
    ```Kotlin
    class AccommodationRecyclerViewAdapter : ListAdapter<AccommodationModel, AccommodationRecyclerViewAdapter.ItemViewHolder>(diffUtil) {
      inner class ItemViewHolder(private val binding: ItemRecyclerViewAccommodationBinding) :
          RecyclerView.ViewHolder(binding.root) {

          fun bind(accommodationModel: AccommodationModel) {
              binding.accommodationModel = accommodationModel
          }
       }
    }
    ```
  + data 가 바뀌면 자동으로 View 를 변경하게 할 수 있다.
  + xml 리소스만 보고도 View 에 어떤 데이터가 들어가는지 파악이 가능하다.
    ```XML
    <?xml version="1.0" encoding="utf-8"?>
    <layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>
        <variable
            name="accommodationModel"
            type="com.kotlin_project.airbnb.model.AccommodationModel" />
    </data>

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/titleTextView"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:text="@{accommodationModel.title}"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/thumbnailImageView" />

            <TextView
                android:id="@+id/priceTextView"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:text="@{accommodationModel.price}"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/titleTextView" />

        </androidx.constraintlayout.widget.ConstraintLayout>
    </layout>
    ```
  + 코드 가독성이 좋아지고, 상대적으로 코드량이 줄어든다.   
  
또한, ***데이터 바인딩은 MVP 또는 MVVM 패턴을 구현하기 위해 유용하게 사용된다.*** 하지만 Databinding은 ***많은 클래스 파일이 생성되며 컴파일 속도가 느리다는 단점***이 존재한다. 
따라서 만약 findViewId()를 사용하지 않을 목적만 있다면 Databinding보다 Viewbinding을 활용하는 것이 낫다. ***Databinding은 MVVM 혹은 MVP 아키텍쳐와 같이 활용하는것이 좋다.***

### Databinding Adapter
***XML 내에서 속성값을 마음대로 커스텀마이징*** 할 수 있게 해준다.
```XLM
<ImageView
    android:id="@+id/thumbnailImageView"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:layout_margin="24dp"
    app:error="@{@drawable/ic_error}"
    app:layout_constraintDimensionRatio="3:2"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:roundImageUrl="@{accommodationModel.imgUrl}" />
```   
위 ImageView는 ```app:error```와 ```app:roundImageUrl``` 이라는 기존에 없는 속성을 사용하는데, 이는 ***Databinding Adapter 내에서 정의한 속성이며 마치 하나의 함수처럼 사용이 가능***하다.
   
   
```Kotlin
object BindingConversions {
    @BindingAdapter("roundImageUrl", "error")
    @JvmStatic
    fun loadRoundImage(imageView: ImageView, url: String, error: Drawable) {

        Glide.with(imageView.context).load(url)
            .error(error)
            .transform(CenterCrop(), RoundedCorners(dpToPx(imageView.context, 12)))
            .into(imageView)
    }

    @JvmStatic
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }
}
```   

위와 같은 방식으로 ***해당 함수를 어떤 속성으로 연결할지 정하는 어노테이션과 정적으로 선언하는 어노테이션을 함수 위에 붙혀 선언***하면 된다.

