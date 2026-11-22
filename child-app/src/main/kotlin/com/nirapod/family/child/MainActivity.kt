package com.nirapod.family.child

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nirapod.family.child.ui.hidden.HiddenStatusScreen
import com.nirapod.family.child.ui.setup.SetupWizardScreen
import com.nirapod.family.child.ui.splash.SplashScreen
import com.nirapod.family.child.ui.welcome.WelcomeLicenseScreen
import com.nirapod.family.shared.util.Constants

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkColors = darkColorScheme(
                primary = Color(0xFF2563EB),
                secondary = Color(0xFF10B981),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B),
                onPrimary = Color.White,
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC)
            )

            MaterialTheme(colorScheme = darkColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChildAppNavigation(this)
                }
            }
        }
    }
}

enum class ChildScreen {
    SPLASH,
    WELCOME_LICENSE,
    SETUP_WIZARD,
    HIDDEN_STATUS
}

@Composable
fun ChildAppNavigation(context: Context) {
    val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    val hasLicense = prefs.getString(Constants.KEY_LICENSE_KEY, null) != null
    val isSetupDone = prefs.getBoolean(Constants.KEY_IS_SETUP_DONE, false)

    var currentScreen by remember {
        mutableStateOf(
            when {
                !hasLicense -> ChildScreen.SPLASH
                !isSetupDone -> ChildScreen.SETUP_WIZARD
                else -> ChildScreen.HIDDEN_STATUS
            }
        )
    }

    when (currentScreen) {
        ChildScreen.SPLASH -> {
            SplashScreen {
                currentScreen = if (hasLicense) {
                    if (isSetupDone) ChildScreen.HIDDEN_STATUS else ChildScreen.SETUP_WIZARD
                } else {
                    ChildScreen.WELCOME_LICENSE
                }
            }
        }
        ChildScreen.WELCOME_LICENSE -> {
            WelcomeLicenseScreen(
                context = context,
                onLicenseActivated = {
                    currentScreen = ChildScreen.SETUP_WIZARD
                }
            )
        }
        ChildScreen.SETUP_WIZARD -> {
            SetupWizardScreen(
                context = context,
                onSetupComplete = {
                    prefs.edit().putBoolean(Constants.KEY_IS_SETUP_DONE, true).apply()
                    currentScreen = ChildScreen.HIDDEN_STATUS
                }
            )
        }
        ChildScreen.HIDDEN_STATUS -> {
            HiddenStatusScreen(context = context)
        }
    }
}
