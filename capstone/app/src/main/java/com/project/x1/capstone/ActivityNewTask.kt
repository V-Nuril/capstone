package com.project.x1.capstone

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_task.*
import java.util.*


class ActivityNewTask : AppCompatActivity() {

    private lateinit var alarm: AlarmManager
    private lateinit var dataBase: Data
    private val calendar = Calendar.getInstance()
    private var date: DatePickerDialog.OnDateSetListener? = null
    private var hour: Int = 0
    private var minute: Int = 0
    private var saves = Reminder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        dataBase = Data(this)

        if (intent.hasExtra("reminder")) {
            saves = intent.getSerializableExtra("reminder") as Reminder
        }

        date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }

        if (saves.id != 0L) {
            desc_title.setText(saves.title)
            detail_desc.setText(saves.description)
            dateTV.text = saves.date
            timeTV.text = saves.time

            val noX = saves.date.split("/")
            val date = noX[0]
            val month = noX[1]
            val year = noX[2]

            val noZ = saves.time.split(":")
            val hour = noZ[0]
            val minute = noZ[1]

            calendar.set(Calendar.YEAR, year.toInt())
            calendar.set(Calendar.MONTH, month.toInt())
            calendar.set(Calendar.DAY_OF_MONTH, date.toInt())

            calendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
            calendar.set(Calendar.MINUTE, minute.toInt())
            calendar.set(Calendar.SECOND, 0)

            save_button.text = getString(R.string.update)
        } else {
            updateDate()
            save_button.text = getString(R.string.save)
        }

        date_button.setOnClickListener {
            val pickDate = DatePickerDialog(
                this, date, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            pickDate.datePicker.minDate = calendar.timeInMillis
            pickDate.show()
        }

        time_button.setOnClickListener {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)

            val pickTime =
                TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener(function = { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        updateTime(hour, minute)
                    }), hour, minute, true
                )

            pickTime.show()
        }

        save_button.setOnClickListener {
            if (desc_title.text?.isEmpty() == true) {
                Object.showToastMessage(this, "Please select title")
            } else if (timeTV.text == getString(R.string.time)) {
                Object.showToastMessage(this, "Please select time")
            } else {
                val title = desc_title.text.toString()
                val description = detail_desc.text.toString()
                val time = timeTV.text.toString()
                val date = dateTV.text.toString()

                val reminder = Reminder()

                reminder.title = title
                reminder.description = description
                reminder.time = time
                reminder.date = date

                val saveMe: Long
                saveMe = if (saves.id != 0L) {
                    reminder.id = saves.id
                    dataBase.updateAlarm(reminder)
                    saves.id
                } else {
                    dataBase.saveReminder(reminder)
                }
                if (saveMe != 0L) {
                    Log.d("AlarmTime", "Hour: $hour")
                    Log.d("AlarmTime", "Min: $minute")
                    setAlarm(saveMe)
                } else {
                    Object.showToastMessage(this, "Failed to save remainder")
                }
            }
        }
    }

    private fun updateDate() {
        val formattedDate = Object.getFormattedDateInString(calendar.timeInMillis, "dd/MM/YYYY")
        dateTV.text = formattedDate
    }

    @SuppressLint("SetTextI18n")
    private fun updateTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
        timeTV.text = "$hour:$minute"
    }

    private fun setAlarm(savedReminderId: Long) {
        alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderService = ReminderService()
        val reminderReceiverIntent = Intent(this, Alarm::class.java)

        reminderReceiverIntent.putExtra("reminderId", savedReminderId)
        reminderReceiverIntent.putExtra("isServiceRunning",
            startService(reminderService)
        )
        val pendingIntent =
            PendingIntent.getBroadcast(this, savedReminderId.toInt(), reminderReceiverIntent, 0)
        val formattedDate = Object.getFormattedDateInString(calendar.timeInMillis, "dd/MM/YYYY HH:mm")
        Log.d("TimeSetInMillis:", formattedDate)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        Object.showToastMessage(this, "Alarm is set at : $formattedDate")
        finish()
    }

    @Suppress("DEPRECATION")
    private fun startService(reminderService: ReminderService): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (reminderService.javaClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

}

