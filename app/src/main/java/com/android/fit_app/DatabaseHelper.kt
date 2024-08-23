package com.android.fit_app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


// Вспомогательный класс для работы с базой данных. В базе храним последнюю активность и координаты

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "step_data.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_STEPS = "steps"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_STEP_COUNT = "step_count"



        private const val TABLE_CREATE = """
            CREATE TABLE $TABLE_STEPS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP TEXT,
                $COLUMN_STEP_COUNT INTEGER
            );
        """

        private const val TABLE_LOCATION = "location"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"

        private const val SQL_CREATE_LOCATION_TABLE = """
            CREATE TABLE $TABLE_LOCATION (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
        db.execSQL(SQL_CREATE_LOCATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STEPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATION")
        onCreate(db)
    }

    fun addStep(timestamp: Long, stepCount: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_STEP_COUNT, stepCount)
        }
        db.insert(TABLE_STEPS, null, values)
    }

    // получить все шаги из базы
    fun getAllSteps(): List<Step> {
        val steps = mutableListOf<Step>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_STEPS,
            arrayOf(COLUMN_TIMESTAMP, COLUMN_STEP_COUNT),
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                val timestamp = getLong(getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val stepCount = getInt(getColumnIndexOrThrow(COLUMN_STEP_COUNT))
                steps.add(Step(timestamp, stepCount))
            }
            close()
        }
        return steps
    }

    fun insertLocation(latitude: Double, longitude: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("locations", null, values)
    }
}