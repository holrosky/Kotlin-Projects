# 알람
**설정한 시간에 알람이 울립니다.**

![Screenshot_20220818_124235](https://user-images.githubusercontent.com/67175445/185288636-7558aab0-aa15-4ef9-a0ee-85a00ea2d465.png)
![Screenshot_20220818_124436](https://user-images.githubusercontent.com/67175445/185288746-a514f242-4e17-4111-b58a-2894867b623c.png)
![Screenshot_20220818_124255](https://user-images.githubusercontent.com/67175445/185288642-a9748d9e-0caa-4da1-9a2f-0c40380533f1.png)


# BroadcastReceiver
***Activity, Service, Content Provider***와 더불어 안드로이드 4대 컴포넌트중 하나를 담당하는 ***BroadcastReceiver***는 안드로이드 시스템으로부터 
발생하는 이벤트(eg. 배터리 정보, 통화 수발신 등)를 call-back 받을 수 있다. BroadcastReceiver를 활용하여 설정한 알람 시간이 되면 Notification을 생성한다.

# Notification
안드로이드 버전 오레오 (SDK 26) 이상 부터는 Notification을 생성할 때 반드시 Notification Channel도 같이 생성해주어야 한다.
```kotlin
private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "알람",
            NotificationManager.IMPORTANCE_HIGH
        )

        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }
}
```    

# PendingIntent / AlarmManager
PendingIntent는 다른 앱(프로세스)가 PendingIntent를 생성한 앱과 같은 권한을 가지게 하여 다른 앱(프로세스)에서 Intent를 실행 할 수 있게 해준다.
이를 AlarmManager와 같이 활용하여 알람을 정해진 시간에 울리게 할 수 있다.

```kotlin
val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
val intent = Intent(this, AlarmReceiver::class.java)
val pendingIntent = PendingIntent.getBroadcast(
    this,
    REQUEST_CODE,
    intent,
    PendingIntent.FLAG_MUTABLE
)

alarmManager.setInexactRepeating(
    AlarmManager.RTC_WAKEUP,
    calendar.timeInMillis,
    AlarmManager.INTERVAL_DAY,
    pendingIntent
)
```    
+ ## PendingIntent Flags
  - FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체한다.
  - FLAG_CANCEL_CURRENT : 현재 인텐트가 이미 등록되어있다면 삭제하고, 다시 등록한다.
  - FLAG_NO_CREATE : 이미 등록된 인텐트가 있다면, 아무것도 하지 않는다.
  - FLAG_ONE_SHOT : 한번 사용되면, 그 다음에 다시 사용되지 않는다.

+ ## AlarmManager Type
  - ELAPSED_REALTIME : 기기가 부팅된 후 경과한 시간을 기준으로, 상대적인 시간을 사용하여 알람을 발생시킨다. 기기가 절전모드(doze)에 있을 때는 알람을 발생시키지 않고 해제되면 발생시칸다.
  - ELAPSED_REALTIME_WAKEUP : ELAPSED_REALTIME와 동일하지만 절전모드일 때 알람을 발생시킨다.
  - RTC : Real Time Clock을 사용하여 알람을 발생시킵니다. 절전모드일 때는 알람을 발생시키지 않는다.
  - RTC_WAKEUP : RTC와 동일하지만 절전모드일 때 알람을 발생시킨다.

+ ## 디바이스가 절전모드일 때도 동작하게 만들기
  디바이스가 절전모드(Doze)일 때는 setExact()으로 등록된 알람은 발생하지 않는다. 다음 API를 사용하면 절전모드에서도 알람이 발생한다.

  - setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) : set()과 동일하지만 절전모드에서도 동작하는 API이다.
  - setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) : setExact()과 동일하지만 절전모드에서도 동작하는 API이다.


# 알람 정보 Data class
유저가 지정한 알람의 정보는 Data class 객체로 관리를 한다. 해당 객체는 생성자를 통해 시간 및 알림 꺼짐/켜짐 정보를 받으며 이 정보들을 가공하여 데이터를 반환해준다. 
```kotlin
data class AlarmDataModel(
    val hour: Int,
    val minute: Int,
    var onOff: Boolean
) {
    val timeText: String
        get() {
            val h = if (hour > 12) hour - 12 else hour
            val m = minute

            return "${"%02d".format(h)}:${"%02d".format(m)}"
        }

    val ampmText: String
        get() {
            return if (hour >= 12) "PM" else "AM"
        }

    val onOffText: String
        get() {
            return if (onOff) "알람 켜짐" else "알람 꺼짐"
        }

    fun makeDataForDB(): String {
        return "$hour:$minute"
    }
  }
}
```


