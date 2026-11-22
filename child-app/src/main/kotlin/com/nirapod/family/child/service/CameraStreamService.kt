package com.nirapod.family.child.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nirapod.family.shared.network.SignalingService
import com.nirapod.family.shared.network.WebRTCClient
import com.nirapod.family.shared.util.Constants
import org.webrtc.*

class CameraStreamService : Service() {

    private var webRtcClient: WebRTCClient? = null
    private var videoCapturer: VideoCapturer? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_PARENT_ID = "EXTRA_PARENT_ID"
        const val EXTRA_FACING = "EXTRA_FACING" // "front" or "back"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val parentId = intent.getStringExtra(EXTRA_PARENT_ID) ?: ""
                val facing = intent.getStringExtra(EXTRA_FACING) ?: "front"
                startCameraStream(parentId, facing)
            }
            ACTION_STOP -> stopCameraStream()
        }
        return START_NOT_STICKY
    }

    private fun startCameraStream(parentId: String, facing: String) {
        val notification = NotificationCompat.Builder(this, Constants.CHILD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ক্যামেরা লাইভ সুরক্ষা")
            .setContentText("সন্তানের আশপাশের পরিবেশ মনিটরিং সক্রিয়")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
        startForeground(9755, notification)

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

        val enumerator = Camera2Enumerator(this)
        val deviceNames = enumerator.deviceNames
        var targetDeviceName: String? = null

        val isFront = facing.equals("front", ignoreCase = true)
        for (name in deviceNames) {
            if (isFront && enumerator.isFrontFacing(name)) {
                targetDeviceName = name
                break
            } else if (!isFront && enumerator.isBackFacing(name)) {
                targetDeviceName = name
                break
            }
        }
        if (targetDeviceName == null && deviceNames.isNotEmpty()) {
            targetDeviceName = deviceNames[0]
        }

        if (targetDeviceName != null) {
            videoCapturer = enumerator.createCapturer(targetDeviceName, null)
            val videoSource = webRtcClient?.createVideoSource(false)
            if (videoSource != null) {
                videoCapturer?.initialize(
                    SurfaceTextureHelper.create("CameraStreamThread", webRtcClient?.eglBaseContext),
                    this,
                    videoSource.capturerObserver
                )
                videoCapturer?.startCapture(640, 480, 24)
                webRtcClient?.createVideoTrack(videoSource, "CAMERA_TRACK")
            }
        }

        webRtcClient?.startPeerConnection("camera")
    }

    private fun stopCameraStream() {
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
        stopCameraStream()
    }
}
