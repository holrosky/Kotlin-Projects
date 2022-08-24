# 책 리뷰
**도서 API를 활용하여 책을 검색할 수 있으며 원하는 책에 리뷰(메모)를 작성할 수 있습니다.**

![3_1](https://user-images.githubusercontent.com/67175445/186327869-428259ea-bb7b-4b92-9154-9bfe231056a7.png)
![3_2](https://user-images.githubusercontent.com/67175445/186327875-9d94ce16-143e-4355-953e-a590d0b1f7f4.png)
![3_3](https://user-images.githubusercontent.com/67175445/186327883-9886f963-d7e0-4fe1-a5ac-a1db33d646a8.png)
![3_4](https://user-images.githubusercontent.com/67175445/186327888-a587eed4-1495-4386-b14e-7809d570f703.png)


# Retrofit2 (ver. 2.9.0)
Retrofit을 이용하여 도서 API를 간편하게 활용할 수 있다. Retrofit은 interface인 service 클레스 안에 함수를 명시하고 각 함수가 
***call***하는 ***DTO (Data Transfer Object)*** 를 생성하여 API를 통해 받은 값을 저장할 수 있다. ***Converter***는 ***json 데이터를 객체로 변환시켜주는 gson***으로 설정한다.

+ Service 클래스   
```kotlin
interface BookService {
    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey: String,
        @Query("query") keyword: String

    ): Call<SearchBookDto>

    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey: String
    ): Call<BestSellerDto>
}
```
+ BestSellerDto 클래스
```kotlin
data class BestSellerDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val books: List<Book>
)
```
+ SearchBookDto 클래스
```kotlin
data class SearchBookDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val books: List<Book>
)
```

# Room (ver. 2.2.6)
**Local DB 개념인 Room을 활용하여 유저가 검색한 기록 및 리뷰를 저장한다.**
+ 검색기록
  + DAO
  ```kotlin
  @Dao
  interface HistoryDao {

      @Query("SELECT * FROM History")
      fun getAll(): List<History>

      @Insert
      fun insertHistory(history: History)

      @Query("DELETE FROM history WHERE keyword == :keyword")
      fun delete(keyword: String)
  }
  ```
  + Entity
  ```kotlin
  @Entity
  data class History(
      @PrimaryKey(autoGenerate = true) val uid: Int?,
      @ColumnInfo(name = "keyword") val keyword: String?
  )
  ```
+ 리뷰
  + DAO
  ```kotlin
  @Dao
  interface ReviewDao {
      @Query("SELECT * FROM review WHERE id == :id")
      fun getReview(id: Int): Review?

      @Insert(onConflict = OnConflictStrategy.REPLACE)
      fun saveReview(review: Review)
  }
  ```
  + Entity
  ```kotlin
  @Entity
  data class Review(
      @PrimaryKey val id: Int?,
      @ColumnInfo(name = "review") val review: String?
  )
  ```
+ AppDatabase
```kotlin
@Database(entities = [History::class, Review::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}
```

# RecyclerView
RecyclerView와 ListView는 비슷하지만 ***RecyclerView는 ListView의 단점을 보완했다.***
ListView는 스크롤을 하여 View가 화면 밖을 나가면 그 View를 제거 후 나중에 다시 생성을 하지만 
***RecyclerView는 ViewHolder로 View를 생성하였다가 필요할때 재사용한다.***
따라서 RecyclerView는 스크롤을 할때 버벅이는 현상을 줄였고 ***메모리 측면에서도 ListView 보다 우위를 점한다.***



API를 통해 받아온 도서 정보를 ***RecyclerView***의 ***Adapter***에 넘겨주어 데이터를 바인딩한다. 도서의 겉표지 이미지는 ***Glide*** 라이브러리를 활용하여 ImageView에 로드한다.
```kotlin
inner class BookItemViewHolder(private val binding: ItemBookBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(bookModel: Book) {
        binding.titleTextView.text = bookModel.title
        binding.descriptionTextView.text = bookModel.description

        binding.root.setOnClickListener{
            ItemClickedListener(bookModel)
        }

        Glide
            .with(binding.coverImageView.context)
            .load(bookModel.coverSmallUrl)
            .into(binding.coverImageView)
    }
}
```

# ViewBinding
ViewBinding 기능을 활용하여 findViewById 보다 더 손쉽게 View에 접근이 가능하다.

+ ViewBinding 활성화 [build.gradle (app)]
```gradle
    viewBinding {
        enabled = true
    }
```
+ ViewBinding 초기화
```kotlin
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}
```
