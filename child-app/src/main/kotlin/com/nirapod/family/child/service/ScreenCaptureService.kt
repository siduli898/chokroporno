package com.nirapod.family.child.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nirapod.family.shared.network.SignalingService
import com.nirapod.family.shared.network.WebRTCClient
import com.nirapod.family.shared.util.Constants
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.VideoCapturer

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var webRtcClient: WebRTCClient? = null
    private var videoCapturer: VideoCapturer? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        const val EXTRA_PARENT_ID = "EXTRA_PARENT_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                val parentId = intent.getStringExtra(EXTRA_PARENT_ID) ?: ""
                startScreenCapture(resultCode, resultData, parentId)
            }
            ACTION_STOP -> stopScreenCapture()
        }
        return START_NOT_STICKY
    }

    private fun startScreenCapture(resultCode: Int, resultData: Intent?, parentId: String) {
        val notification = NotificationCompat.Builder(this, Constants.CHILD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("স্ক্রিন শেয়ারিং সক্রিয়")
            .setContentText("সন্তানের ডিভাইসের স্ক্রিন লাইভ শেয়ার করা হচ্ছে")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

        startForeground(9754, notification)

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

        if (resultData != null) {
            videoCapturer = ScreenCapturerAndroid(resultData, object : MediaProjection.Callback() {
                override fun onStop() {
                    stopScreenCapture()
                }
            })
            val videoSource = webRtcClient?.createVideoSource(true)
            if (videoSource != null) {
                videoCapturer?.initialize(
                    org.webrtc.SurfaceTextureHelper.create("ScreenCaptureThread", webRtcClient?.eglBaseContext),
                    this,
                    videoSource.capturerObserver
                )
                videoCapturer?.startCapture(720, 1280, 24)
                webRtcClient?.createVideoTrack(videoSource, "SCREEN_TRACK")
            }
        }

        webRtcClient?.startPeerConnection("screen")
    }

    private fun stopScreenCapture() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            webRtcClient?.close()
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenCapture()
    }
}
