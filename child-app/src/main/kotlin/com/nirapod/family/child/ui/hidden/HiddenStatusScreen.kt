package com.nirapod.family.child.ui.hidden

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirapod.family.child.service.ChildMonitorService
import com.nirapod.family.shared.util.Constants

@Composable
fun HiddenStatusScreen(context: Context) {
    val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    var isHidden by remember { mutableStateOf(prefs.getBoolean(Constants.KEY_IS_HIDDEN, false)) }

    fun toggleHideApp(hide: Boolean) {
        val p = context.packageManager
        val componentName = ComponentName(context, "com.nirapod.family.child.LauncherAlias")
        val state = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        p.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
        prefs.edit().putBoolean(Constants.KEY_IS_HIDDEN, hide).apply()
        isHidden = hide
        val msg = if (hide) "অ্যাপ আইকন হাইড করা হয়েছে। ডায়াল প্যাডে *#9753# চেপে খুলুন।" else "অ্যাপ আইকন শো করা হয়েছে।"
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    LaunchedEffect(Unit) {
        ChildMonitorService.startService(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFF1E293B), shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "সুরক্ষা সক্রিয় রয়েছে",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ডিভাইসটি সফলভাবে পেয়ার করা হয়েছে এবং ব্যাকগ্রাউন্ড সুরক্ষা মনিটরিং চলছে।",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "গোপন আনলক কোড: ${Constants.SECRET_DIAL_CODE}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "অ্যাপ হাইড করার পর ফোন ডায়ালারে গিয়ে ${Constants.SECRET_DIAL_CODE} চাপলেই আবার এই স্ক্রিনটি চালু হবে।",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { toggleHideApp(!isHidden) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHidden) Color(0xFF10B981) else Color(0xFFDC2626)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHidden) "অ্যাপ আনহাইড করুন" else "লঞ্চার থেকে হাইড করুন",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
