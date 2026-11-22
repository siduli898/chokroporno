package com.nirapod.family.parent.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirapod.family.shared.auth.AuthRepository
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.Device
import com.nirapod.family.shared.models.StreamCommand
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScreenMirror: (String) -> Unit,
    onNavigateToCamera: (String) -> Unit,
    onNavigateToAudio: (String) -> Unit,
    onNavigateToLocation: (String) -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }
    val firebaseRepo = remember { FirebaseRepository() }

    val user = authRepo.currentUser
    val licenseKey = remember { "NIRAPOD-PREMIUM-2026" } // Default or loaded from profile

    val devicesFlow = remember { firebaseRepo.observeDevicesForParent(licenseKey) }
    val deviceList by devicesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NirapodFamily Guardian", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLicense) {
                        Icon(Icons.Default.VpnKey, contentDescription = "License", tint = Color(0xFF94A3B8))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF94A3B8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E293B)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("ড্যাশবোর্ড") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDevices,
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    label = { Text("ডিভাইসসমূহ") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToLicense,
                    icon = { Icon(Icons.Default.Key, contentDescription = null) },
                    label = { Text("লাইসেন্স") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("সেটিংস") }
                )
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Status Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "লাইসেন্স: $licenseKey",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "সংযুক্ত সক্রিয় ডিভাইস: ${deviceList.size} টি",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "সংযুক্ত চাইল্ড ডিভাইসসমূহ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (deviceList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DevicesOther,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো চাইল্ড ডিভাইস যুক্ত নেই",
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "চাইল্ড অ্যাপটি সন্তানের ফোনে ইনস্টল করে লাইসেন্স কী দিন।",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(deviceList) { device ->
                        DeviceCard(
                            device = device,
                            onScreenMirror = { onNavigateToScreenMirror(device.deviceId) },
                            onCamera = { onNavigateToCamera(device.deviceId) },
                            onAudio = { onNavigateToAudio(device.deviceId) },
                            onLocation = { onNavigateToLocation(device.deviceId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onScreenMirror: () -> Unit,
    onCamera: () -> Unit,
    onAudio: () -> Unit,
    onLocation: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (device.isOnline) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = device.deviceName.ifBlank { "চাইল্ড ডিভাইস (${device.deviceId.take(6)})" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "ব্যাটারি: ${device.batteryLevel}%",
                    fontSize = 12.sp,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${device.model} • ${device.osVersion}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(12.dp))

            // Action Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton(
                    icon = Icons.Default.ScreenShare,
                    label = "স্ক্রিন",
                    color = Color(0xFF2563EB),
                    onClick = onScreenMirror
                )
                ActionButton(
                    icon = Icons.Default.Videocam,
                    label = "ক্যামেরা",
                    color = Color(0xFF0EA5E9),
                    onClick = onCamera
                )
                ActionButton(
                    icon = Icons.Default.Mic,
                    label = "অডিও",
                    color = Color(0xFF10B981),
                    onClick = onAudio
                )
                ActionButton(
                    icon = Icons.Default.LocationOn,
                    label = "লোকেশন",
                    color = Color(0xFFF59E0B),
                    onClick = onLocation
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
    }
}
