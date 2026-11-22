package com.nirapod.family.parent.ui.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.StreamCommand
import com.nirapod.family.shared.network.SignalingService
import com.nirapod.family.shared.network.WebRTCClient
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMonitorScreen(
    deviceId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firebaseRepo = remember { FirebaseRepository() }
    val signaling = remember { SignalingService() }

    var isConnected by remember { mutableStateOf(false) }

    DisposableEffect(deviceId) {
        val client = WebRTCClient(
            context = context,
            signalingService = signaling,
            localDeviceId = "PARENT_MONITOR",
            targetDeviceId = deviceId,
            isInitiator = false,
            onRemoteStreamAdded = { mediaStream ->
                if (mediaStream.audioTracks.isNotEmpty()) {
                    isConnected = true
                }
            }
        )
        client.startPeerConnection("audio")

        coroutineScope.launch {
            firebaseRepo.sendCommand(
                StreamCommand(
                    command = Constants.CMD_START_AUDIO,
                    targetDeviceId = deviceId,
                    senderId = "PARENT_MONITOR"
                )
            )
        }

        onDispose {
            client.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("লাইভ পরিবেষ্টনকারী অডিও", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF10B981) else Color(0xFF64748B),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isConnected) "পরিবেষ্টনকারী শব্দ শোনা যাচ্ছে..." else "অডিও সংযোগ স্থাপিত হচ্ছে...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "সন্তানের আশপাশের রিয়েলটাইম ভয়েস ও অডিও স্ট্রিম হচ্ছে।",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        firebaseRepo.sendCommand(
                            StreamCommand(
                                command = Constants.CMD_STOP_ALL,
                                targetDeviceId = deviceId,
                                senderId = "PARENT_MONITOR"
                            )
                        )
                    }
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("অডিও মনিটরিং বন্ধ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
