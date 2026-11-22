package com.nirapod.family.child.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nirapod.family.child.service.ChildMonitorService
import com.nirapod.family.shared.util.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            val isSetupDone = prefs.getBoolean(Constants.KEY_IS_SETUP_DONE, false)
            if (isSetupDone) {
                ChildMonitorService.startService(context)
            }
        }
    }
}
