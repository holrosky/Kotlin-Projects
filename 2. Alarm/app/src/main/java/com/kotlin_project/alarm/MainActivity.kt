package com.kotlin_project.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val timeTextView by lazy {
        findViewById<TextView>(R.id.timeTextView)
    }

    private val ampmTextView by lazy {
        findViewById<TextView>(R.id.ampmTextView)
    }

    private val alarmOnOffSwitch by lazy {
        findViewById<SwitchCompat>(R.id.alarmOnOffSwitch)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTimeTextView()
        initAlarmOnOffSwitch()

        val alarmDataModel = fetchDataFromSharedPreferences()
        renderView(alarmDataModel)
    }

    private fun initTimeTextView() {
        timeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                val alarmDataModel = saveAlarmData(hour, minute, false)
                renderView(alarmDataModel)

                cancelAlarm()

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun initAlarmOnOffSwitch() {
        alarmOnOffSwitch.setOnClickListener {
            val alarmDataModel = it.tag as? AlarmDataModel ?: return@setOnClickListener
            val newAlarmDataModel = saveAlarmData(
                alarmDataModel.hour,
                alarmDataModel.minute,
                alarmDataModel.onOff.not()
            )
            renderView(newAlarmDataModel)

            if (newAlarmDataModel.onOff) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, newAlarmDataModel.hour)
                    set(Calendar.MINUTE, newAlarmDataModel.minute)

                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }

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
            } else {
                cancelAlarm()
            }


        }
    }

    private fun saveAlarmData(hour: Int, minute: Int, onOff: Boolean): AlarmDataModel {
        val dataModel = AlarmDataModel(
            hour = hour,
            minute = minute,
            onOff = onOff
        )

        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(ALARM_KEY, dataModel.makeDataForDB())
            putBoolean(ONOFF_KEY, dataModel.onOff)
            commit()
        }
        return dataModel
    }

    private fun fetchDataFromSharedPreferences(): AlarmDataModel {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

        val timeDBValue = sharedPreferences.getString(ALARM_KEY, "08:00") ?: "8:00"
        val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
        val alarmData = timeDBValue.split(":")

        val alarmDataModel = AlarmDataModel(
            hour = alarmData[0].toInt(),
            minute = alarmData[1].toInt(),
            onOff = onOffDBValue
        )

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )

        if ((pendingIntent == null) and alarmDataModel.onOff) {
            alarmDataModel.onOff = false
        } else if ((pendingIntent != null) and alarmDataModel.onOff.not()) {
            pendingIntent.cancel()
        }

        return alarmDataModel

    }

    private fun renderView(alarmDataModel: AlarmDataModel) {
        ampmTextView.text = alarmDataModel.ampmText
        timeTextView.text = alarmDataModel.timeText
        alarmOnOffSwitch.isChecked = alarmDataModel.onOff
        alarmOnOffSwitch.text = alarmDataModel.onOffText
        alarmOnOffSwitch.tag = alarmDataModel
    }

    private fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        pendingIntent?.cancel()
    }

    companion object {
        private const val SHARED_PREFERENCES_KEY = "time"
        private const val ALARM_KEY = "alarm"
        private const val ONOFF_KEY = "onOff"
        private const val REQUEST_CODE = 1000
    }
}

