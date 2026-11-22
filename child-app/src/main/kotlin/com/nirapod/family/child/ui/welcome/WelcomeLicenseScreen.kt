package com.nirapod.family.child.ui.welcome

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.firebase.LicenseRepository
import com.nirapod.family.shared.models.Device
import com.nirapod.family.shared.util.Constants
import com.nirapod.family.shared.util.DeviceFingerprint
import kotlinx.coroutines.launch

@Composable
fun WelcomeLicenseScreen(
    context: Context,
    onLicenseActivated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val licenseRepo = remember { LicenseRepository() }
    val firebaseRepo = remember { FirebaseRepository() }

    var licenseKey by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val deviceId = remember { DeviceFingerprint.getDeviceId(context) }
    val deviceName = remember { DeviceFingerprint.getDeviceName() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "চাইল্ড অ্যাপ অ্যাক্টিভেশন",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "প্যারেন্ট অ্যাপে থাকা আপনার লাইসেন্স কী দিন এবং ডিভাইসটি সুরক্ষিতভাবে যুক্ত করুন।",
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ডিভাইস আইডি: $deviceId",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "ডিভাইস মডেল: $deviceName",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = licenseKey,
                    onValueChange = { licenseKey = it.trim().uppercase() },
                    label = { Text("লাইসেন্স কী (যেমন NIRAPOD-XXXX)") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (licenseKey.isBlank()) {
                            errorMessage = "অনুগ্রহ করে লাইসেন্স কী লিখুন"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = licenseRepo.validateAndBindLicense(licenseKey, deviceId)
                            if (result.isSuccess) {
                                // Save locally
                                val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putString(Constants.KEY_LICENSE_KEY, licenseKey)
                                    .putString(Constants.KEY_DEVICE_ID, deviceId)
                                    .apply()

                                // Register device in firestore
                                val device = Device(
                                    deviceId = deviceId,
                                    licenseKey = licenseKey,
                                    deviceName = deviceName,
                                    model = android.os.Build.MODEL,
                                    osVersion = DeviceFingerprint.getOsVersion(),
                                    isOnline = true
                                )
                                firebaseRepo.registerOrUpdateDevice(device)
                                isLoading = false
                                onLicenseActivated()
                            } else {
                                isLoading = false
                                errorMessage = result.exceptionOrNull()?.message ?: "লাইসেন্স ভ্যালিডেশন ব্যর্থ হয়েছে"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("অ্যাক্টিভেট ও এগিয়ে যান", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
