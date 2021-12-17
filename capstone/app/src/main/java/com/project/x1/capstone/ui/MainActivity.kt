package com.project.x1.capstone.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.project.x1.capstone.*
import com.project.x1.capstone.function.Alarm
import com.project.x1.capstone.database.Data
import com.project.x1.capstone.entity.Reminder
import com.project.x1.capstone.ui.adapter.Adapter
import com.project.x1.capstone.utils.Object
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), Adapter.OnItemClickListener {

    private lateinit var dataBase: Data
    private lateinit var adapter: Adapter
    private var alarm = mutableListOf<Reminder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        add()
        dataBase = Data(this)
        adapter = Adapter(this)
        rv_reminder.adapter = adapter

        getDataBase()

        val from = intent.getStringExtra("from")
        if (from == "Notification") {
            val reminderId = intent.getLongExtra("reminderId", 0)
            val reminderById = dataBase.getAlarm(reminderId)
            alert(reminderById)
        }

        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText)
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.button_add, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.add_task) {
            val intent = Intent(
                this, ActivityNewTask ::class.java)
            startActivity (intent)
        }
        return true
    }

    private fun add() {
    }

    private fun filterData(query: String) {
        val finalList = if (query.isEmpty()) alarm else alarm.filter {
            it.title.lowercase(Locale.getDefault())
                .contains(query.lowercase(Locale.getDefault())) ||
                    it.description.lowercase(Locale.getDefault()).contains(
                        query.lowercase(
                            Locale.getDefault()
                        )
                    )
        }
        if (finalList.isNotEmpty()) {
            updateList(finalList.toMutableList())
        }
    }

    private fun updateList(finalList: MutableList<Reminder>) {
        adapter.list = finalList
        adapter.notifyDataSetChanged()

        if (alarm.size > 0) {
            rv_reminder.visibility = View.VISIBLE
            data.visibility = View.GONE
        } else {
            rv_reminder.visibility = View.GONE
            data.visibility = View.VISIBLE
        }
    }

    private fun getDataBase() {
        alarm = dataBase.getAll()
        updateList(alarm)
    }

    override fun onResume() {
        super.onResume()
        getDataBase()
    }

    private fun alert(reminder: Reminder) {
        val setter = AlertDialog.Builder(this)
        setter.setTitle(reminder.title)
        setter.setMessage(reminder.description)
        setter.setPositiveButton("STOP ALARM") { dialog, _ ->
            Object.showToastMessage(this, "Your alarm has been stopped")
            dialog.dismiss()
            stopAlarm()
            stopServices()
        }

        val alertDialog = setter.create()
        alertDialog.show()
    }

    private fun stopAlarm() {
        val intent = Intent(this, Alarm::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    private fun stopServices() {
        val service = Intent(this, ReminderService::class.java)
        stopService(service)
    }

    override fun onItemClick(
        reminder: Reminder,
        view: View,
        position: Int
    ) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            if (it.title == getString(R.string.update)) {
                startActivity(
                    Intent(this, ActivityNewTask::class.java)
                        .putExtra("reminder", reminder)
                )
            } else if (it.title == getString(R.string.delete)) {
                dataBase.deleteAlarm(reminder.id)
                getDataBase()
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

}
