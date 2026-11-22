package com.nirapod.chokroporno

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import java.security.SecureRandom

private const val PREFS = "family_safety_preferences"
private const val PIN_SALT = "pin_salt"
private const val PIN_HASH = "pin_hash"
private const val SESSION_LENGTH_MS = 5 * 60 * 1000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var unlocked by rememberSaveable { mutableStateOf(!prefs.contains(PIN_HASH)) }
                    if (unlocked) ConsentDemo(onLock = { unlocked = false })
                    else PinScreen(
                        hasPin = prefs.contains(PIN_HASH),
                        onSetPin = { pin ->
                            val salt = ByteArray(16).also(SecureRandom()::nextBytes)
                            prefs.edit().putString(PIN_SALT, salt.toHex()).putString(PIN_HASH, pin.digestWith(salt).toHex()).apply()
                            unlocked = true
                        },
                        onUnlock = { pin ->
                            val salt = prefs.getString(PIN_SALT, null)?.hexToBytes()
                            val expected = prefs.getString(PIN_HASH, null)?.hexToBytes()
                            if (salt != null && expected != null && MessageDigest.isEqual(expected, pin.digestWith(salt))) {
                                unlocked = true
                                true
                            } else false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinScreen(hasPin: Boolean, onSetPin: (String) -> Unit, onUnlock: (String) -> Boolean) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(if (hasPin) "Unlock family safety" else "Create your app PIN", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("This six-digit PIN is stored on this device. It is a convenience gate, not a replacement for your device lock.")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { value -> if (value.length <= 6 && value.all(Char::isDigit)) pin = value },
            label = { Text("Six-digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (pin.length != 6) error = "Enter exactly six digits."
            else if (hasPin) {
                if (!onUnlock(pin)) error = "That PIN did not match."
            } else onSetPin(pin)
        }, modifier = Modifier.fillMaxWidth()) { Text(if (hasPin) "Unlock" else "Save PIN") }
    }
}

@Composable
private fun ConsentDemo(onLock: () -> Unit) {
    var requestDialog by rememberSaveable { mutableStateOf(false) }
    var activeUntil by rememberSaveable { mutableLongStateOf(0L) }
    var now by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(activeUntil) {
        while (activeUntil > 0L && System.currentTimeMillis() < activeUntil) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000)
        }
        if (activeUntil > 0L && System.currentTimeMillis() >= activeUntil) activeUntil = 0L
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Family safety", style = MaterialTheme.typography.headlineMedium)
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("What this app does", style = MaterialTheme.typography.titleMedium)
            Text("This prototype does not collect or transmit location, camera, microphone, screen, or usage data. It has no background monitoring, and stays visible in the app launcher.")
            Text("Every future live feature must ask for approval on this device for that individual session. You can always deny or stop a session.")
        } }
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Live session consent demo", style = MaterialTheme.typography.titleMedium)
            Text("A request is only a simulation. Approving it does not start recording or sharing.")
            if (activeUntil > now) {
                Text("Demo request approved. It expires in ${((activeUntil - now + 999) / 1000)} seconds. No media is active.")
                OutlinedButton(onClick = { activeUntil = 0L }) { Text("Stop / revoke approval") }
            } else {
                Text("No session is active.")
                Button(onClick = { requestDialog = true }) { Text("Simulate a session request") }
            }
        } }
        Spacer(Modifier.weight(1f))
        Text("Uninstall is controlled by Android and the device owner, not hidden by this app.")
        TextButton(onClick = onLock) { Text("Lock app") }
    }

    if (requestDialog) AlertDialog(
        onDismissRequest = { requestDialog = false },
        title = { Text("Session approval required") },
        text = { Text("A parent has requested a demo live session. This prototype will not access your camera, microphone, screen, or location. Do you approve this request for five minutes? You may stop it at any time.") },
        confirmButton = { TextButton(onClick = { activeUntil = System.currentTimeMillis() + SESSION_LENGTH_MS; requestDialog = false }) { Text("Approve") } },
        dismissButton = { TextButton(onClick = { requestDialog = false }) { Text("Deny") } }
    )
}

private fun String.digestWith(salt: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(salt + toByteArray(Charsets.UTF_8))
private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
private fun String.hexToBytes(): ByteArray? = runCatching {
    if (length % 2 != 0) return null
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}.getOrNull()
