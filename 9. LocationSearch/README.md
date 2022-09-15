
# LocationSearch (위치검색)
***Tmap API를 활용하여 키워드 검색 결과를 보여주며 주소를 클릭시 GoogleMaps 위에 Marker를 찍는다. 지도의 아무곳을 길게 터치하면 
Latitude와 Longitude를 받아와 실시간으로 Tmap API로 부터 주소 정보를 받아온다.***

![1](https://user-images.githubusercontent.com/67175445/189518101-d856cbb6-d6f9-423b-9831-c44a61b8cb33.png)
![2](https://user-images.githubusercontent.com/67175445/189518103-42fee5a1-3f49-47b5-b72b-6ed93f467daf.png)



# Retrofit2 (ver. 2.9.0) + okhttp3
***Retrofit을 Tmap API와 같이 활용하여 API 정보를 수신***한다. 해당 프로젝트에서는 ***okhttp3를 Retrofit의 클라이언트로 사용***하였다.   
또한 지난 프로젝트와 달리 MVVM 패턴 구현에 조금더 신경을 써서 ***Retrofit 객체는 싱글턴으로 생성하였고 View - ViewModel - Repository - Model 구조로 진행***하였다.

+ Api Interface
  ```kotlin
  interface PoiApi {
      @GET("/tmap/pois")
      suspend fun getKeyowrdSearch(
          @Query("version") version: Int = 1,
          @Query("searchKeyword") keyword: String,
          @Header("appKey") appKey: String,
          @Query("count") count: Int = 20
      ): Response<SearchPoiInfoModel>

      @GET("/tmap/geo/reversegeocoding")
      suspend fun getLocationInfoByLatLon(
          @Header("appKey") appKey: String,
          @Query("version") version: Int = 1,
          @Query("lat") lat: Double,
          @Query("lon") lon: Double
      ): Response<AddressInfoModel>

      companion object {
          private const val BASE_URL = "https://apis.openapi.sk.com"

          fun create(): PoiApi {
               val retrofit =
                  Retrofit.Builder()
                      .baseUrl(BASE_URL)
                      .addConverterFactory(GsonConverterFactory.create())
                      .client(buildOkHttpClient())
                      .build()


              return retrofit.create(PoiApi::class.java)
          }

          private fun buildOkHttpClient(): OkHttpClient {
              val interceptor = HttpLoggingInterceptor()
              if (BuildConfig.DEBUG) {
                  interceptor.level = HttpLoggingInterceptor.Level.BODY
              } else {
                  interceptor.level = HttpLoggingInterceptor.Level.NONE
              }
              return OkHttpClient.Builder()
                  .connectTimeout(5, TimeUnit.SECONDS)
                  .addInterceptor(interceptor)
                  .build()
          }

      }
  }
  ```

  

# MVVM (Model - ViewModel - View) 아키텍쳐 패턴 + Databinding
이번 프로젝트부터는 MVVM 패턴을 조금더 정확하게 사용하기 위해 노력하였다. 구조는 아래와 같다.
![다운로드](https://user-images.githubusercontent.com/67175445/189507460-5e25779b-286a-4fef-b1e9-a9c740fa9a48.png)   

+ Model   
  이번 프로젝트에서는 여러개의 Model이 사용되었다. 
  ```kotlin
  data class AddressInfoModel(
      val addressInfo: LocationInfoModel
  )

  data class LocationInfoModel(
      @SerializedName("fullAddress")
      val fullAddress: String?,
      @SerializedName("addressType")
      val addressType: String?,
      @SerializedName("city_do")
      val cityDo: String?,
      @SerializedName("gu_gun")
      val guGun: String?,
      @SerializedName("eup_myun")
      val eupMyun: String?,
      @SerializedName("adminDong")
      val adminDong: String?,
      @SerializedName("adminDongCode")
      val adminDongCode: String?,
      @SerializedName("legalDong")
      val legalDong: String?,
      @SerializedName("legalDongCode")
      val legalDongCode: String?,
      @SerializedName("ri")
      val ri: String?,
      @SerializedName("bunji")
      val bunji: String?,
      @SerializedName("roadName")
      val roadName: String?,
      @SerializedName("buildingIndex")
      val buildingIndex: String?,
      @SerializedName("buildingName")
      val buildingName: String?,
      @SerializedName("mappingDistance")
      val mappingDistance: String?,
      @SerializedName("roadCode")
      val roadCode: String?,
  )
  ```
  ```kotlin
  data class SearchPoiInfoModel (
    val searchPoiInfo: PoisModel
  )

  data class PoisModel (
      val pois: PoiModel
  )

  data class PoiModel (
      val poi: List<ItemModel>
  )

  @Parcelize
  data class ItemModel(
      val id: String? = null,
      val name: String? = null,
      val telNo: String? = null,
      val frontLat: Float = 0.0f,
      val frontLon: Float = 0.0f,
      val noorLat: Float = 0.0f,
      val noorLon: Float = 0.0f,
      val upperAddrName: String? = null,
      val middleAddrName: String? = null,
      val lowerAddrName: String? = null,
      val detailAddrName: String? = null,
      val firstNo: String? = null,
      val secondNo: String? = null,
      val roadName: String? = null,
      val firstBuildNo: String? = null,
      val secondBuildNo: String? = null,
      val mlClass: String? = null,
      val radius: String? = null,
      val bizName: String? = null,
      val upperBizName: String? = null,
      val middleBizName: String? = null,
      val lowerBizName: String? = null,
      val detailBizName: String? = null,
      val rpFlag: String? = null,
      val parkFlag: String? = null,
      val detailInfoFlag: String? = null,
      val desc: String? = null
  ) : Parcelable
  ```
  
+ ViewModel   
  ```kotlin
  class MainViewModel(private val poiRepository: PoiRepository) : ViewModel() {
      private val _poiModel = MutableLiveData<List<ItemModel>>()

      val poiModel: LiveData<List<ItemModel>>
          get() = _poiModel

      fun requestKeywordSearch(keyword: String, appKey: String) {
          CoroutineScope(Dispatchers.IO).launch {
              poiRepository.getKeywordSearch(keyword, appKey).let { response ->
                  if (response.isSuccessful) {
                      if (response.body() == null) {
                          _poiModel.postValue(emptyList())
                          return@launch
                      }

                      _poiModel.postValue(response.body()!!.searchPoiInfo.pois.poi)
                  }
              }
          }
      }
  }
  ```
  ```kotlin
  class MapViewModel(private val poiRepository: PoiRepository) : ViewModel() {
      private val _buildingName = MutableLiveData<String>()
      private val _fullAddress = MutableLiveData<String>()
      private val _latLng = MutableLiveData<LatLng>()

      val bulidingName: LiveData<String>
          get() = _buildingName
      val fullAddress: LiveData<String>
          get() = _fullAddress
      val latLng: LiveData<LatLng>
          get() = _latLng

      fun requestLocationInfoByLatLon(lat: Double, lon: Double, appKey: String) {
          _latLng.value = LatLng(lat, lon)

          CoroutineScope(Dispatchers.IO).launch {
              poiRepository.getLocationInfoByLatLon(lat, lon, appKey).let { response ->
                  if (response.isSuccessful) {
                      response.body()?.let {
                          _fullAddress.postValue(it.addressInfo.fullAddress)
                          _buildingName.postValue(it.addressInfo.buildingName)
                      }
                  }
              }
          }
      }
  }
  ```
+ View   
 
  ```kotlin
  private fun initViewModel() {
      viewModelFactory = MainViewModelFactory(PoiRepository())
      mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)

      binding.viewModel = mapViewModel
      binding.lifecycleOwner = this

      mapViewModel.latLng.observe(this) { latLng ->
          markerOptions.position(latLng)

          if (alreadyLoaded)
              map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
          else {
              map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
              alreadyLoaded = true
          }

          clearAndAddMarker()
      }

      mapViewModel.bulidingName.observe(this) { buildingName ->
          if(buildingName.isEmpty())
              markerOptions.title(" ")
          else
              markerOptions.title(buildingName)

          clearAndAddMarker()
      }

      mapViewModel.fullAddress.observe(this) { fullAddress ->
          markerOptions.snippet(fullAddress)
          clearAndAddMarker()
      }

  }
  ```
  
***View (Activity) 에서는 ViewModel의 LiveData를 Observe한다. 또한 필요시에는 ViewMdel에게 데이터 업데이트를 요청한다.   
ViewModel은 해당 요청을 Repositoty(API)에게 요청을 하고 API로 수신받은 데이터를 Model에 저장함과 동시에 ViewModel에게 전해준다.   
데이터를 받은 ViewModel은 자신의 LiveData를 업데이트하고 이를 관찰하고 있던 View는 UI 작업을 시작한다.   
따라서 View는 로직에 관여하지 않으며 단순히 View에만 신경을 쓴다.***   

# Coroutine (매우 중요)
***Coroutine은 메모리를 효율적으로 관리하면서 비동기 처리를 가능하게 해준다.*** 기존의 Thread와 비슷한 기능을 수행하지만 차이가 있다. Thread는 한번 시작되면 끝날때 까지 실행되지만 ***Coroutine은 각각의 routine이 원하는 곳에서 작업을 중단 및 재개가 가능***하다. 또한 Coroutine은 Thread 보다 작은 개념으로 Thread안에 존재하게 되므로 Context Switch 같은 등의 이유로 오버헤드가 발생하지 않는다. ***각각의 routine은 launch 키워드 scope 단위 블록으로 묶어서 코드로 작성하기 때문에 코드도 간결해지는 장점이 있다.*** 해당 프로젝트에서는 Tmap으로 Api 요청을 보내고 응답을 받는 작업을 Coroutine으로 처리하였다.

```kotlin
fun requestKeywordSearch(keyword: String, appKey: String) {
    CoroutineScope(Dispatchers.IO).launch {
        poiRepository.getKeywordSearch(keyword, appKey).let { response ->
            if (response.isSuccessful) {
                if (response.body() == null) {
                    _poiModel.postValue(emptyList())
                    return@launch
                }

                _poiModel.postValue(response.body()!!.searchPoiInfo.pois.poi)
            }
        }
    }
}
```

### Coroutine Scope
+ ***GlobalScope*** : 앱의 생명주기와 함께 동작한다. 따라서 별도 생명주기 관리가 필요없으므로 앱의 시작부터 종료까지 장시간 실행되는 Coroutine에 적합하다.
+ ***CoroutineScope*** : 필요할 때만 실행되고 완료되면 Coroutine을 종료하는 Scope이다.
+ ***ViewModelScope*** : Jetpack 아키텍처의 ViewModel 컴포넌트 사용시 ViewModel 인스턴스에서 사용하기 위해 제공되는 Scope이다. 생명 주기 또한 ViewModel을 따라간다.

### Coroutine Dispatcher
+ ***Default*** : 안드로이드 기본 쓰레드풀을 사용하며 CPU를 많이 요구하는 작업에 최적화 되어있다.
+ ***IO*** : 이미지 다운로드 및 파일 입출력 등 입출력에 최적화 되어있다 (네트워크, 디스크, DB 작업 등)
+ ***Main*** : 안드로이드 기본 쓰레드에서 Coroutine을 실행하며 UI와 상호작용에 최적화 되어있다.
+ ***Unconfined*** : 호출한 Context를 기본으로 사용하되 중지 후 다시 실행될 때 Context가 바뀌면 바뀐 컨텍스트를 사용한다.

### Suspend
코루틴의 가장 큰 특징이다. ***Coroutine scope 안에서 Suspend가 붙은 함수를 실행하게 되면 기존에 실행되던 작업을 멈추고 Suspend가 붙은 함수를 먼저 처리한다. 함수가 처리가 되면 중지되었던 기존 코드가 재개되어 다시 실행된다. 여기서 중요한점은 함수를 실행할 때 기존에 실행하던 작업을 중지한다고 해서 Thread가 멈추지 않는다는 점이다.***
```kotlin
suspend fun getKeywordSearch(keyword: String,
                             appKey: String) = api.getKeyowrdSearch(keyword = keyword, appKey = appKey)

suspend fun getLocationInfoByLatLon(lat: Double, lon: Double,
                            appKey: String) = api.getLocationInfoByLatLon(lat = lat, lon = lon, appKey = appKey)
```
