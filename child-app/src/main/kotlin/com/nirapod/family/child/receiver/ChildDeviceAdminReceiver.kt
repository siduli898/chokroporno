package com.nirapod.family.child.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ChildDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "নিরাপদফ্যামিলি অ্যাডমিন সুরক্ষা সক্রিয় হয়েছে", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "সতর্কবার্তা: ডিভাইস অ্যাডমিন বন্ধ করলে সন্তানের মনিটরিং ও সুরক্ষা বিঘ্নিত হবে!"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "ডিভাইস অ্যাডমিন সুরক্ষা বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
    }
}
