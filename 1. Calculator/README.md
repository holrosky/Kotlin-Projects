# Kotlin-Projects (1. Calculator)
**+(덧셈), -(뺄셈), /(나눗셈), \*(곱셈), %(모듈러), ()(괄호)를 활용한 기본 계산기 입니다.   
계산 기록이 자동으로 저장되며 일괄 삭제가 가능합니다.**
# 완성본
![calculator](https://user-images.githubusercontent.com/67175445/183231335-4724d5c3-3944-4fc4-870f-e714e24d060e.png)
![calculator_history](https://user-images.githubusercontent.com/67175445/183231547-814838f0-87bf-4a33-b00e-d1e1352b332e.png)

## 연산 알고리즘
### Evaluation of Postfix Expression
![Infix-Evaluation](https://user-images.githubusercontent.com/67175445/183234080-47eec252-e2d5-49ec-be91-5ee9862aafd5.png)
### 예외
계산기는 숫자를 추가할 때마다 실시간으로 현재까지의 수식을 계산하여 보여주어야 한다. 
따라서 여는 괄호(***'('***)후에 닫는 괄호(***')'***)가 없을 수 있는데, 
이런 경우에는 operator에서 pop한 값이 여는 괄호(***'('***)가 아닐 때까지 계속 pop을 해준다.
### Complexity :   
Expression의 char를 하나씩 push 또는 pop을 하기때문에 시간복잡도는 ***O(N)*** 이다.


## Layout
1. TableLayout (키패드)
2. ContrainLayout
3. LinearLayout (계산 기록의 리스트)

## Room (ver. 2.2.6)
**Local DB 개념인 Room을 활용하여 유저가 계산한 기록을 저장한다.**
1. DAO 클래스   
```kotlin
@Dao
interface HistoryDAO {

    @Query(value = "SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query(value = "DELETE FROM history")
    fun deleteAll()
}
```
2. Model 클래스
```kotlin
@Entity
data class History(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "expression") val expression: String?,
    @ColumnInfo(name = "result") val result: String?
)
```
3. Database 클래스
```kotlin
@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun HistoryDAO(): HistoryDAO
}
```
## Thread
MainThread(UIThread)에서는 네트워크 작업 또는 데이터베이스 작업과 같은 무거운 작업을 실행하면 ANR (Android Not Responding) 에러가 발생할 수 있으므로 
Room 을 활용한 DB에 계산 기록을 저장 혹은 불러오기를 할때는 새로운 Thread를 생성하여 작업을 하도록 한다.   
새롭게 생성한 Thread 내부에서 UI에 접근을 하려면 Handler 혹은 이미 mHandler가 내포된 runOnUiThread를 활용하도록 한다.   

### 새로운 Thread를 활용하여 데이터 저장
```kotlin
Thread(Runnable {
   db.HistoryDAO().insertHistory(History(null, expression, result))
}).start()
```

### 계산 기록을 불러온 후 UI 업데이트
```kotlin
Thread(Runnable {
    db.HistoryDAO().getAll().reversed().forEach {
        runOnUiThread {
            val historyView =
                LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
            historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
            historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

            historyLinearLayout.addView(historyView)
        }
    }
}).start()
```


