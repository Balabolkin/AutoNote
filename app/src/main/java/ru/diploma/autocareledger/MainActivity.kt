package ru.diploma.autocareledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yandex.mapkit.MapKitFactory
import ru.diploma.autocareledger.BuildConfig
import ru.diploma.autocareledger.ui.AutoCareApp
import ru.diploma.autocareledger.ui.theme.AutoNoteTheme
import ru.diploma.autocareledger.ui.theme.ThemePreference
import ru.diploma.autocareledger.viewmodel.GarageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.initialize(this)
        }
        enableEdgeToEdge()
        setContent {
            AutoCareRoot()
        }
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.getInstance().onStart()
        }
    }

    override fun onStop() {
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.getInstance().onStop()
        }
        super.onStop()
    }
}

@Composable
private fun AutoCareRoot() {
    val application = LocalContext.current.applicationContext as android.app.Application
    val viewModel: GarageViewModel = org.koin.androidx.compose.koinViewModel()
    val prefs = remember {
        application.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    }
    var themePreference by remember {
        mutableStateOf(
            runCatching { ThemePreference.valueOf(prefs.getString("theme", ThemePreference.System.name).orEmpty()) }
                .getOrDefault(ThemePreference.System)
        )
    }

    AutoNoteTheme(themePreference = themePreference) {
        AutoCareApp(
            viewModel = viewModel,
            themePreference = themePreference,
            onThemePreferenceChanged = {
                themePreference = it
                prefs.edit().putString("theme", it.name).apply()
            }
        )
    }
}
