package com.nirapod.family.child.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nirapod.family.child.MainActivity
import com.nirapod.family.shared.util.Constants

class SecretDialReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
        val host = intent.data?.host

        if (number == Constants.SECRET_DIAL_CODE || host == "9753") {
            resultData = null // Abort outgoing call

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launchIntent)
        }
    }
}
