package com.nirapod.family.parent.ui.monitor

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlipCameraAndroid
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
import androidx.compose.ui.viewinterop.AndroidView
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.StreamCommand
import com.nirapod.family.shared.network.SignalingService
import com.nirapod.family.shared.network.WebRTCClient
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.launch
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraViewScreen(
    deviceId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firebaseRepo = remember { FirebaseRepository() }
    val signaling = remember { SignalingService() }

    var surfaceViewRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var webRtcClient by remember { mutableStateOf<WebRTCClient?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var currentFacing by remember { mutableStateOf("front") }

    fun sendCameraCommand(facing: String) {
        coroutineScope.launch {
            firebaseRepo.sendCommand(
                StreamCommand(
                    command = Constants.CMD_START_CAMERA,
                    targetDeviceId = deviceId,
                    senderId = "PARENT_MONITOR",
                    cameraFacing = facing
                )
            )
        }
    }

    DisposableEffect(deviceId) {
        val client = WebRTCClient(
            context = context,
            signalingService = signaling,
            localDeviceId = "PARENT_MONITOR",
            targetDeviceId = deviceId,
            isInitiator = false,
            onRemoteStreamAdded = { mediaStream ->
                if (mediaStream.videoTracks.isNotEmpty()) {
                    surfaceViewRenderer?.let { renderer ->
                        mediaStream.videoTracks[0].addSink(renderer)
                        isConnected = true
                    }
                }
            }
        )
        webRtcClient = client
        client.startPeerConnection("camera")

        sendCameraCommand(currentFacing)

        onDispose {
            client.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("লাইভ ক্যামেরা পর্যবেক্ষণ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentFacing = if (currentFacing == "front") "back" else "front"
                        sendCameraCommand(currentFacing)
                    }) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", tint = Color.White)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webRtcClient?.eglBaseContext?.let { egl ->
                                init(egl, null)
                                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                setEnableHardwareScaler(true)
                            }
                            surfaceViewRenderer = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (!isConnected) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ক্যামেরা ফিডের সাথে যুক্ত হচ্ছে...",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                Text("ক্যামেরা স্ট্রিমিং বন্ধ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
