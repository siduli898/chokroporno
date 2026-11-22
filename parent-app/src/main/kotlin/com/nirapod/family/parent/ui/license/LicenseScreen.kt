package com.nirapod.family.parent.ui.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirapod.family.shared.firebase.LicenseRepository
import com.nirapod.family.shared.models.License
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val licenseRepo = remember { LicenseRepository() }
    val licenseKey = remember { "NIRAPOD-PREMIUM-2026" }

    var license by remember { mutableStateOf<License?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val res = licenseRepo.getLicense(licenseKey)
        license = res.getOrNull() ?: License(
            key = licenseKey,
            tier = "PREMIUM",
            deviceLimit = 5,
            expiry = System.currentTimeMillis() + 365L * 24 * 3600 * 1000,
            boundDeviceIds = listOf("DEV_CHILD_01", "DEV_CHILD_02"),
            isActive = true
        )
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("লাইসেন্স ও সাবস্ক্রিপশন", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "লাইসেন্স টিয়ার: ${license?.tier ?: "PREMIUM"}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "কী: ${license?.key ?: licenseKey}", fontSize = 13.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ডিভাইস স্লট ব্যবহৃত:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Text("${license?.boundDeviceIds?.size ?: 2} / ${license?.deviceLimit ?: 5}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("স্ট্যাটাস:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সক্রিয় (Active)", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
