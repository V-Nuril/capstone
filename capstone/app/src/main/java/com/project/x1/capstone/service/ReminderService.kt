package com.project.x1.capstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ReminderService : Service() {

    private var song: MediaPlayer? = null
    private var tts: TextToSpeech? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("ReminderService", "onBind called")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ReminderService", "onStartCommand called")
        val reminderId = intent?.getLongExtra("reminderId", 0)
        val databaseHandler = Data(this)
        val reminder = databaseHandler.getAlarm(reminderId ?: 0)
        showAlarmNotification(reminder)

        val speakText = reminder.title + " " + reminder.description
        tts = TextToSpeech(this,
            TextToSpeech.OnInitListener {
                if (it != TextToSpeech.ERROR) {
                    tts?.language = Locale.ROOT
                    tts?.speak(speakText, TextToSpeech.QUEUE_ADD, null, null)
                } else {
                    Log.d("ReminderService", "Error: $it")
                }
            })
        Log.d("ReminderService", speakText)
        return START_STICKY
    }

    private fun showAlarmNotification(reminder: Reminder) {
        Log.d("ReminderService", "showAlarmNotification called")

        createNotificationChannel(reminder.id.toInt())
        // build notification
        val builder = NotificationCompat.Builder(this, reminder.id.toString())
            .setSmallIcon(R.drawable.app_logo) //notif icon
            .setContentTitle(reminder.title) //notif title
            .setContentText(reminder.description)//notif message
            .setAutoCancel(true) //auto cancel notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) //set priority

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.putExtra("reminderId", reminder.id)
        notificationIntent.putExtra("from", "Notification")

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)
        val notification = builder.build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminder.id.toInt(), notification)
    }

    private fun createNotificationChannel(id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                id.toString(),
                "Reminder Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        Log.d("ReminderService", "onDestroy called")
        super.onDestroy()

        if (song?.isPlaying == true) {
            song?.stop()
            song?.release()
        }

        tts?.stop()
        tts?.shutdown()
    }

}
