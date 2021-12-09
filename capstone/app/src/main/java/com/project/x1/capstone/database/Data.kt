package com.project.x1.capstone.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Data(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Database"
        private const val TABLE_REMINDER = "Reminder"
        private const val ID = "id"
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val TIME = "time"
        private const val DATE = "date"
        private const val CREATED_TIME = "createdTime"
        private const val MODIFIED_TIME = "modifiedTime"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE " + TABLE_REMINDER + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE + " TEXT,"
                + DESCRIPTION + " TEXT,"
                + TIME + " TEXT,"
                + DATE + " TEXT,"
                + CREATED_TIME + " TEXT,"
                + MODIFIED_TIME + " TEXT " + ")")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDER")
        onCreate(db)
    }

    fun saveReminder(reminder: Reminder): Long {
        val db = this.writableDatabase
        val value = ContentValues()
        value.put(TITLE, reminder.title)
        value.put(DESCRIPTION, reminder.description)
        value.put(TIME, reminder.time)
        value.put(DATE, reminder.date)
        value.put(CREATED_TIME, System.currentTimeMillis())
        value.put(MODIFIED_TIME, System.currentTimeMillis())
        val success = db.insert(TABLE_REMINDER, null, value)
        db.close()
        return success
    }

    fun getAlarm(reminderId: Long): Reminder {
        val reminder = Reminder()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_REMINDER WHERE $ID = '$reminderId'"
        val cursor = db.rawQuery(query, null)
        if (cursor.count < 1) {
            cursor.close()
            return reminder
        } else {
            cursor.moveToFirst()

            val id = cursor.getString(0).toLong()
            val title = cursor.getString(1)
            val description = cursor.getString(2)
            val time = cursor.getString(3)
            val date = cursor.getString(4)
            val createdTime = cursor.getLong(5)
            val modifiedTime = cursor.getLong(6)

            reminder.id = id
            reminder.title = title
            reminder.description = description
            reminder.date = date
            reminder.time = time
            reminder.createdTime = createdTime
            reminder.modifiedTime = modifiedTime
        }
        cursor.close()
        db.close()
        return reminder
    }

    fun updateAlarm(reminder: Reminder): Int {
        val db = this.writableDatabase
        val data = ContentValues()
        data.put(ID, reminder.id)
        data.put(TITLE, reminder.title)
        data.put(DESCRIPTION, reminder.description)
        data.put(DATE, reminder.date)
        data.put(TIME, reminder.time)
        data.put(CREATED_TIME, reminder.createdTime)
        data.put(MODIFIED_TIME, System.currentTimeMillis())

        val success = db.update(TABLE_REMINDER, data, "$ID=" + reminder.id, null)
        db.close()
        return success
    }

    fun deleteAlarm(id: Long): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, id)
        val rowId = db.delete(TABLE_REMINDER, "$ID=$id", null)
        db.close()
        return rowId
    }

    fun getAll(): MutableList<Reminder> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("select * from $TABLE_REMINDER ORDER BY $MODIFIED_TIME DESC", null)
        val reminderList = mutableListOf<Reminder>()

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val reminder = Reminder()
                val id = cursor.getString(0).toLong()
                val title = cursor.getString(1)
                val description = cursor.getString(2)
                val time = cursor.getString(3)
                val date = cursor.getString(4)
                val createdTime = cursor.getLong(5)
                val modifiedTime = cursor.getLong(6)

                reminder.id = id
                reminder.title = title
                reminder.description = description
                reminder.date = date
                reminder.time = time
                reminder.createdTime = createdTime
                reminder.modifiedTime = modifiedTime

                reminderList.add(reminder)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return reminderList
    }

}