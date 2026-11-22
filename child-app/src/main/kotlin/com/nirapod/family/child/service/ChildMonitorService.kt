package com.nirapod.family.child.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.nirapod.family.child.R
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.LocationPoint
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.*

class ChildMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val firebaseRepo = FirebaseRepository()
    private var deviceId: String = ""

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, ChildMonitorService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        deviceId = prefs.getString(Constants.KEY_DEVICE_ID, "") ?: ""

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationUpdates()
        startForeground(Constants.CHILD_NOTIFICATION_ID, createNotification())
        startHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, Constants.CHILD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("সুরক্ষা সেবা সক্রিয়")
            .setContentText("প্যারেন্টাল মনিটরিং ব্যাকগ্রাউন্ডে চলছে")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60_000L)
            .setMinUpdateIntervalMillis(30_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    saveLocation(loc)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (_: SecurityException) {}
    }

    private fun saveLocation(loc: Location) {
        if (deviceId.isEmpty()) return
        serviceScope.launch {
            val pt = LocationPoint(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracy = loc.accuracy,
                speed = loc.speed,
                timestamp = System.currentTimeMillis()
            )
            firebaseRepo.saveLocation(deviceId, pt)
        }
    }

    private fun startHeartbeat() {
        serviceScope.launch {
            while (isActive) {
                if (deviceId.isNotEmpty()) {
                    val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
                    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
                    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                    firebaseRepo.updateDeviceHeartbeat(
                        deviceId = deviceId,
                        battery = batteryPct,
                        isCharging = isCharging,
                        isOnline = true
                    )
                }
                delay(30_000L) // Heartbeat every 30 sec
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}
