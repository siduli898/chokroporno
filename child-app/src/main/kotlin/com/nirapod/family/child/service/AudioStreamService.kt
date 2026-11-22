package com.nirapod.family.child.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nirapod.family.shared.network.SignalingService
import com.nirapod.family.shared.network.WebRTCClient
import com.nirapod.family.shared.util.Constants

class AudioStreamService : Service() {

    private var webRtcClient: WebRTCClient? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_PARENT_ID = "EXTRA_PARENT_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val parentId = intent.getStringExtra(EXTRA_PARENT_ID) ?: ""
                startAudioStream(parentId)
            }
            ACTION_STOP -> stopAudioStream()
        }
        return START_NOT_STICKY
    }

    private fun startAudioStream(parentId: String) {
        val notification = NotificationCompat.Builder(this, Constants.CHILD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("অ্যাম্বিয়েন্ট অডিও সুরক্ষা")
            .setContentText("সন্তানের আশপাশের পরিবেষ্টনকারী শব্দ মনিটরিং সক্রিয়")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
        startForeground(9756, notification)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val deviceId = prefs.getString(Constants.KEY_DEVICE_ID, "") ?: ""

        val signaling = SignalingService()
        webRtcClient = WebRTCClient(
            context = this,
            signalingService = signaling,
            localDeviceId = deviceId,
            targetDeviceId = parentId,
            isInitiator = true
        )

        webRtcClient?.createAudioTrack("AMBIENT_AUDIO_TRACK")
        webRtcClient?.startPeerConnection("audio")
    }

    private fun stopAudioStream() {
        try {
            webRtcClient?.close()
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioStream()
    }
}
