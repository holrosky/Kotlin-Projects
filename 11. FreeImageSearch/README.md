
# FineDust (미세먼지 측정)
***Air Korea 공공 API와 유저의 현재 위치 정보를 활용하여 현재 위치의 미세먼지 농도 및 기타 공기 질의 상태를 확인할 수 있다.***

![KakaoTalk_20230330_084814952](https://user-images.githubusercontent.com/67175445/228703765-399c152d-9085-4624-ab0d-2ec2339210c1.jpg)




# Retrofit2 (ver. 2.9.0) + okhttp3
***Retrofit을 Tmap API와 같이 활용하여 API 정보를 수신***한다. 해당 프로젝트에서는 ***okhttp3를 Retrofit의 클라이언트로 사용***하였다. ***Retrofit 객체는 싱글턴으로 생성하였고 View - ViewModel - Repository - Model 구조로 진행***하였다.

+ Api Interface
  ```kotlin
  interface AirKoreaApi {
    @GET(
        "B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json"
    )
    suspend fun getNearMonitorStations(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>

    @GET(
        "B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json" +
                "&dataTerm=DAILY" +
                "&ver=1.3"
    )
    suspend fun getAirInfos(
        @Query("stationName") stationName: String
    ): Response<AirInfosResponse>

    companion object {
        private const val BASE_URL = "http://apis.data.go.kr/"

        fun create(): AirKoreaApi {
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(buildOkHttpClient())
                    .build()

            return retrofit.create(AirKoreaApi::class.java)
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

  

# MVVM (Model - ViewModel - View) 패턴

+ Model   
  이번 프로젝트에서의 Model은 아래와 같다
  
  
  ![11](https://user-images.githubusercontent.com/67175445/228704213-e72986fc-9ca9-4a44-a991-7fde792598fe.png)
  
+ ViewModel   
  ```kotlin
  class MainViewModel(
    private val repository: Repository
    ) : ViewModel() {

    private val _monitoringStation = MutableLiveData<MonitoringStation>()
    private val _airInfo = MutableLiveData<AirInfo>()
    private var _needToLoadOnStart = true

    val monitoringStation: LiveData<MonitoringStation>
        get() = _monitoringStation

    val airInfo: LiveData<AirInfo>
        get() = _airInfo

    val needToLoadOnStart: Boolean
        get() = _needToLoadOnStart

    fun updateAirInformation(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            val tmCoordinate = repository.getTmCoordinates(longitude = longitude, latitude = latitude)

            _monitoringStation.value = tmCoordinate?.let{
                repository.getNearMonitorStations(it.x!!, it.y!!)
            }

            _airInfo.value = monitoringStation.let {
                repository.getAirInfos(monitoringStation.value?.stationName!!)
            }

            _needToLoadOnStart = false
          }
      }
  }
  ```
  
+ View   
 
  ```kotlin
  private fun initVariables() {
      fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
      cancellationTokenSource = CancellationTokenSource()

      mainViewModel.monitoringStation.observe(this) {
          binding.stationNameTextView.text = it.stationName
          binding.stationAddressTextView.text = it.addr
      }

      mainViewModel.airInfo.observe(this) { airInfo ->
          (airInfo.khaiGrade ?: AirGrade.UNKNOWN).let { airGrade ->
              binding.root.setBackgroundResource(airGrade.colorResId)
              binding.totalGradeLabelTextView.text = airGrade.label
              binding.totalGradeEmojiTextView.text = airGrade.emoji
          }

          with(airInfo) {
              binding.fineDustInfomationTextView.text =
                  "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: AirGrade.UNKNOWN).emoji}"
              binding.ultraFineDustInfomationTextView.text =
                  "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: AirGrade.UNKNOWN).emoji}"

              with(binding.so2Item) {
                  labelTextView.text = "아황산가스"
                  gradeTextView.text = (so2Grade ?: AirGrade.UNKNOWN).toString()
                  valueTextView.text = "$so2Value ppm"
              }

              with(binding.coItem) {
                  labelTextView.text = "일산화탄소"
                  gradeTextView.text = (coGrade ?: AirGrade.UNKNOWN).toString()
                  valueTextView.text = "$coValue ppm"
              }

              with(binding.o3Item) {
                  labelTextView.text = "오존"
                  gradeTextView.text = (o3Grade ?: AirGrade.UNKNOWN).toString()
                  valueTextView.text = "$o3Value ppm"
              }

              with(binding.no2Item) {
                  labelTextView.text = "이산화질소"
                  gradeTextView.text = (no2Grade ?: AirGrade.UNKNOWN).toString()
                  valueTextView.text = "$no2Value ppm"
              }
          }

          binding.progressBar.visibility = View.GONE
          binding.refresh.isRefreshing = false
          binding.contentLayout.animate()
              .alpha(1F)
              .start()

      }

      binding.refresh.setOnRefreshListener {
          fetchAirStatus()
      }
  }
  ```
  
+ Repository   
 
  ```kotlin
  class Repository {
    private val kakaoApi = KakaoLoactionApi.create()
    private val airKoreaApi = AirKoreaApi.create()

    suspend fun getTmCoordinates(longitude: Double, latitude: Double): Document? =
        kakaoApi.getTmCoordinates(longitude = longitude, latitude = latitude).body()
            ?.documents
            ?.firstOrNull()

    suspend fun getNearMonitorStations(tmX: Double, tmY: Double): MonitoringStation? =
        airKoreaApi.getNearMonitorStations(tmX = tmX, tmY = tmY).body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull {
                it?.tm ?: Double.MAX_VALUE
            }

    suspend fun getAirInfos(stationName: String): AirInfo? =
        airKoreaApi.getAirInfos(stationName = stationName).body()
            ?.response
            ?.body
            ?.airInfos
            ?.firstOrNull()

  }
  ```
  
***View (Activity) 에서는 ViewModel의 LiveData를 Observe한다. 또한 필요시에는 ViewMdel에게 데이터 업데이트를 요청한다.   
ViewModel은 해당 요청을 Repositoty(API)에게 요청을 하고 API로 수신받은 데이터를 Model에 저장함과 동시에 ViewModel에게 전해준다.   
데이터를 받은 ViewModel은 자신의 LiveData를 업데이트하고 이를 관찰하고 있던 View는 UI 작업을 시작한다.   
따라서 View는 로직에 관여하지 않으며 단순히 View에만 신경을 쓴다.***   

# Widget
***Widget을 통해 홈화면에서도 간단하게 미세먼지 정도를 확인할 수 있다.***
```kotlin
class SimpleAirInfoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        ContextCompat.startForegroundService(
            context!!,
            Intent(context, UpdateWidgetService::class.java)
        )
    }

    class UpdateWidgetService : LifecycleService() {
        override fun onCreate() {
            super.onCreate()

            createChannel()
            startForeground(
                NOTIFICATION_ID,
                createNotification()
            )

        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val updateViews = RemoteViews(packageName, R.layout.widget_simple).apply {
                    setTextViewText(
                        R.id.resultTextView,
                        "권한 없음"
                    )
                    setViewVisibility(R.id.labelTextView, View.GONE)
                    setViewVisibility(R.id.gradeLabelTextView, View.GONE)

                }
                updateWidget(updateViews)
                stopSelf()

                return super.onStartCommand(intent, flags, startId)
            }
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location ->
                    lifecycleScope.launch {
                        try{
                            val nearByMonitorStation = Repository().getNearMonitorStations(location.latitude, location.longitude)
                            val airInfo = Repository().getAirInfos(nearByMonitorStation!!.stationName!!)
                            val updateViews = RemoteViews(packageName, R.layout.widget_simple).apply {
                                setViewVisibility(R.id.labelTextView, View.VISIBLE)
                                setViewVisibility(R.id.gradeLabelTextView, View.VISIBLE)

                                val currentGrade = (airInfo?.khaiGrade ?: AirGrade.UNKNOWN)
                                setTextViewText(R.id.resultTextView, currentGrade.emoji)
                                setTextViewText(R.id.gradeLabelTextView, currentGrade.label)
                            }

                            updateWidget(updateViews)
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        } finally {
                            stopSelf()
                        }
                    }
                }

            return super.onStartCommand(intent, flags, startId)
        }

        private fun createChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
                    ?.createNotificationChannel(
                        NotificationChannel(
                            WIDGET_REFRESH_CHANNEL_ID,
                            "위젯 갱신 채널",
                            NotificationManager.IMPORTANCE_LOW
                        )
                    )
            }
        }

        private fun createNotification(): Notification =
            NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                .setChannelId(WIDGET_REFRESH_CHANNEL_ID)
                .build()

        private fun updateWidget(updateViews: RemoteViews) {
            val widgetProvider = ComponentName(this, SimpleAirInfoWidgetProvider::class.java)
            AppWidgetManager.getInstance(this).updateAppWidget(widgetProvider, updateViews)
        }

        override fun onDestroy() {
            super.onDestroy()

            stopForeground(true)
        }
    }

    companion object {
        private const val WIDGET_REFRESH_CHANNEL_ID = "WIDGET_REFRESH_CHANNEL_ID"
        private const val NOTIFICATION_ID = 101
    }
}
```

# API KEY 관리
***API KEY는 민감한 정보이므로 코드상에서 하드코딩으로 넣으면 보안상 취약할 수 있다. 따라서 API KEY를 어딘가에 잘 숨겨두어야 한다.*** 이번 프로젝트에서는 API 키를 ***gradle.properties에 넣어두고 build.gradle에서 해당 API키를 변수로 사용하겠다고 설정***해두었다. 이렇게 하면 마치 API KEY를 gradle.properties에 선언하여 프로젝트내에서 해당 변수 이름으로 KEY를 가져올 수 있게 된다. ***하지만 이 파일은 로컬 저장소에 있기 때문에 Git이나 다른 버전 관리 시스템에 올리지 않도록 주의해야한다. 이 방법은 간단하지만, 로컬 저장소에 직접 저장되기 때문에 상대적으로 덜 안전하다.***

+ build.gradle


  ![image](https://user-images.githubusercontent.com/67175445/228705733-25b0dcb4-5dfd-4fcc-bff0-1e41a6b12b53.png)

+ gradle.properties   

  ![image](https://user-images.githubusercontent.com/67175445/228706031-c8dbe6cc-0e8b-4e9f-963a-a66ff672d394.png)

이 방법 외에도 API KEY를 관리하는 방법은 여러가지가 있다.

1. 로컬 환경 변수 사용

  + 개발자의 로컬 환경 변수를 사용하여 API 키를 저장할 수 있다. 이 방법은 API 키가 로컬 시스템에만 저장되므로 상대적으로 안전하다.
  환경 변수를 설정하고, build.gradle 파일에서 환경 변수를 읽어 사용할 수 있다.

2. Android KeyStore 시스템 사용

  + Android KeyStore 시스템은 안드로이드 기기에 키를 안전하게 저장할 수 있는 방법을 제공한다. 이 시스템은 암호화 키를 안전하게 저장하고 관리할 수 있는 기능을 제공한다. 하지만 API 키와 같은 간단한 문자열 키에는 오버킬일 수 있다.
  
3. 암호화된 SharedPreferences 사용

  + AndroidX 보안 라이브러리를 사용하면, 암호화된 SharedPreferences를 만들어 API 키와 같은 민감한 데이터를 안전하게 저장할 수 있다. 이 방법은 데이터를 암호화하여 저장하므로 상대적으로 안전하다.
