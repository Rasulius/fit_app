package com.android.fit_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.android.fit_app.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var binding: ActivityMainBinding

    private lateinit var stepCountTextView: TextView
    private lateinit var stepStartTime: TextView
    private lateinit var stepEndTime: TextView

    private var firstStepTime: Long = 0
    private var lastStepTime: Long = 0

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var stepAdapter: StepAdapter

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stepCountTextView = binding.stepCountText
        stepStartTime = binding.startTime
        stepEndTime = binding.endTime

        databaseHelper = DatabaseHelper(this)

        stepAdapter = StepAdapter(databaseHelper)
        binding.stepsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.stepsRecyclerView.adapter = stepAdapter

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), PERMISSION_REQUEST_CODE)
        }
        //запросили права если есть, то запустили задачу периодическую
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    // запуск периодических задании
    fun setupPeriodicWork() {
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(20, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(locationWorkRequest)
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            setupPeriodicWork()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun formatTime(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(timeMillis)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER
        ) {
            val currentTime = System.currentTimeMillis()
            if (firstStepTime == 0L) {
                firstStepTime = currentTime
                stepStartTime.text = "Время первого шага: ${formatTime(firstStepTime)}"
            }
            lastStepTime = currentTime
            stepEndTime.text = "Время последнего шага: ${formatTime(lastStepTime)}"

            val stepCount = event.values[0].toInt()
            databaseHelper.addStep(currentTime, stepCount)
            stepCountTextView.text = "Шаги: $stepCount"
            binding.stepsRecyclerView.adapter?.notifyDataSetChanged()

        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
            }
            // Permission granted
        }
    }
}