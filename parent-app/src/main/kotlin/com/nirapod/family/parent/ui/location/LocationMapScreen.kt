package com.nirapod.family.parent.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.StreamCommand
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    deviceId: String,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val firebaseRepo = remember { FirebaseRepository() }

    val locationsFlow = remember { firebaseRepo.observeLatestLocations(deviceId, limit = 1) }
    val locations by locationsFlow.collectAsState(initial = emptyList())

    val latest = locations.firstOrNull()
    val defaultPos = LatLng(latest?.latitude ?: 23.8103, latest?.longitude ?: 90.4125)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 15f)
    }

    LaunchedEffect(latest) {
        if (latest != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(latest.latitude, latest.longitude), 16f
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("লাইভ অবস্থান ট্র্যাকিং", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            firebaseRepo.sendCommand(
                                StreamCommand(
                                    command = Constants.CMD_GET_LOCATION,
                                    targetDeviceId = deviceId,
                                    senderId = "PARENT_MONITOR"
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    if (latest != null) {
                        Marker(
                            state = MarkerState(position = LatLng(latest.latitude, latest.longitude)),
                            title = "সন্তানের অবস্থান",
                            snippet = "সঠিকতা: ${latest.accuracy} মিটার"
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "সর্বশেষ আপডেটেড অবস্থান",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (latest != null) {
                        Text(
                            text = "অক্ষাংশ: ${"%.5f".format(latest.latitude)}, দ্রাঘিমাংশ: ${"%.5f".format(latest.longitude)}",
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8)
                        )
                        val df = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
                        Text(
                            text = "সময়: ${df.format(Date(latest.timestamp))}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        Text(
                            text = "কোনো সাম্প্রতিক অবস্থান পাওয়া যায়নি। 'Refresh' বাটনে চাপ দিন।",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
