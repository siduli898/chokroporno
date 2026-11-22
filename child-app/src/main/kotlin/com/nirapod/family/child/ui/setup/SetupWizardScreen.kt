package com.nirapod.family.child.ui.setup

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nirapod.family.child.receiver.ChildDeviceAdminReceiver
import com.nirapod.family.child.service.ChildMonitorService

data class PermissionStep(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val actionText: String
)

@Composable
fun SetupWizardScreen(
    context: Context,
    onSetupComplete: () -> Unit
) {
    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    val adminComponent = remember { ComponentName(context, ChildDeviceAdminReceiver::class.java) }
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var isAdminActive by remember { mutableStateOf(dpm.isAdminActive(adminComponent)) }
    var isBatteryIgnored by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else true
        )
    }

    var hasCamera by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasAudio by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasLocation by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var hasBackgroundLocation by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCamera = permissions[Manifest.permission.CAMERA] ?: hasCamera
        hasAudio = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudio
        hasLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasBackgroundLocation = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: hasBackgroundLocation
        }
    }

    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isAdminActive = dpm.isAdminActive(adminComponent)
    }

    val steps = listOf(
        PermissionStep(
            "admin",
            "১. ডিভাইস অ্যাডমিন সুরক্ষা",
            "অ্যাপটিকে অনাকাঙ্ক্ষিত আনইনস্টলেশন থেকে রক্ষা করতে ডিভাইস অ্যাডমিন সক্রিয় করুন।",
            Icons.Default.Security,
            isAdminActive,
            "অ্যাডমিন সক্রিয় করুন"
        ),
        PermissionStep(
            "camera",
            "২. ক্যামেরা অনুমতি",
            "প্রয়োজনে সন্তানের আশপাশের পরিবেশ লাইভ দেখতে ক্যামেরা অনুমতি প্রয়োজন।",
            Icons.Default.Videocam,
            hasCamera,
            "অনুমতি দিন"
        ),
        PermissionStep(
            "audio",
            "৩. মাইক্রোফোন অডিও অনুমতি",
            "সন্তানের সুরক্ষার্থে লাইভ পরিবেষ্টনকারী শব্দ (অ্যাম্বিয়েন্ট অডিও) শুনতে মাইক্রোফোন অনুমতি প্রয়োজন।",
            Icons.Default.Mic,
            hasAudio,
            "অনুমতি দিন"
        ),
        PermissionStep(
            "location",
            "৪. সঠিক লোকেশন ও ব্যাকগ্রাউন্ড ট্র্যাকিং",
            "সন্তান কোথায় আছে সার্বক্ষণিক ট্র্যাক করতে ব্যাকগ্রাউন্ড লোকেশন প্রয়োজন।",
            Icons.Default.LocationOn,
            hasLocation && hasBackgroundLocation,
            "অনুমতি দিন"
        ),
        PermissionStep(
            "battery",
            "৫. ব্যাটারি অপ্টিমাইজেশন নিষ্ক্রিয়",
            "ব্যাকগ্রাউন্ডে নিরবচ্ছিন্ন সুরক্ষার জন্য ব্যাটারি সেভার অপ্টিমাইজেশন বন্ধ রাখুন।",
            Icons.Default.BatteryChargingFull,
            isBatteryIgnored,
            "অপ্টিমাইজেশন ছাড় দিন"
        )
    )

    val allDone = isAdminActive && hasCamera && hasAudio && hasLocation && isBatteryIgnored

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Text(
            text = "সেটআপ উইজার্ড",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "নিরাপদ সুরক্ষার জন্য নিচের ধাপগুলো ক্রমান্বয়ে সম্পন্ন করুন।",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(steps) { _, step ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (step.isGranted) Color(0xFF132F2B) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = if (step.isGranted) Color(0xFF10B981) else Color(0xFF38BDF8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = step.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = step.description, fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (step.isGranted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Granted",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Button(
                                onClick = {
                                    when (step.id) {
                                        "admin" -> {
                                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "প্যারেন্টাল সিকিউরিটি সক্রিয় করতে অ্যাডমিন অনুমতি দিন।")
                                            }
                                            adminLauncher.launch(intent)
                                        }
                                        "camera" -> permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                        "audio" -> permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                        "location" -> {
                                            val perms = mutableListOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                            }
                                            permissionLauncher.launch(perms.toTypedArray())
                                        }
                                        "battery" -> {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                                isBatteryIgnored = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("অনুমতি", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                ChildMonitorService.startService(context)
                onSetupComplete()
            },
            enabled = allDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                disabledContainerColor = Color(0xFF334155)
            )
        ) {
            Text(
                text = if (allDone) "সেটআপ সম্পন্ন করুন ও সুরক্ষা চালু করুন" else "সকল অনুমতি দিয়ে এগিয়ে যান",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
