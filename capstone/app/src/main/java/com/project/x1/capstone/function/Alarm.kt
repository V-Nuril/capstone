package com.project.x1.capstone.function

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.x1.capstone.ReminderService

class Alarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val id = intent?.getLongExtra("reminderId", 0)
        val startService = intent?.getBooleanExtra("isServiceRunning", false)

        val intent = Intent(context, ReminderService::class.java)
        intent.putExtra("reminderId", id)
        if (!startService!!) {
            context.startService(intent)
        }
    }
}