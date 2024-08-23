package com.android.fit_app

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class LocationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = getLastKnownLocation(locationManager)

        return if (location != null) {
            saveLocationToDatabase(location.latitude, location.longitude)
            Result.success()
        } else {
            Result.retry()  // Попробуйте снова, если не удалось получить местоположение
        }
    }

    private fun getLastKnownLocation(locationManager: LocationManager): Location? {
        // Проверяем разрешения и возвращаем последнее известное местоположение
        return try {
            val provider = LocationManager.GPS_PROVIDER
            locationManager.getLastKnownLocation(provider)
        } catch (e: SecurityException) {
            null
        }
    }

    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        // Реализуйте сохранение данных в базу данных
        // Например:
        val databaseHelper = DatabaseHelper(applicationContext)
        databaseHelper.insertLocation(latitude, longitude)
    }
}
