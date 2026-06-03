package ru.diploma.autocareledger.ui

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.geometry.center
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.border
import java.io.File
import ru.diploma.autocareledger.network.FuelReceiptParseResult
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.googlecode.tesseract.android.TessBaseAPI
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map as YandexMap
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SearchType
import com.yandex.runtime.Error
import com.yandex.runtime.ui_view.ViewProvider
import ru.diploma.autocareledger.BuildConfig
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity
import ru.diploma.autocareledger.network.ReceiptClient
import ru.diploma.autocareledger.network.GasStationClient
import ru.diploma.autocareledger.network.GasStationPriceItem
import ru.diploma.autocareledger.network.GasStationPriceReport
import ru.diploma.autocareledger.ui.model.ExpenseCategory
import ru.diploma.autocareledger.ui.theme.ThemePreference
import ru.diploma.autocareledger.viewmodel.GarageUiState
import ru.diploma.autocareledger.viewmodel.GarageViewModel
import java.net.URLDecoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

internal enum class AppTab(val title: String, val icon: ImageVector) {
    Home("Главная", Icons.Outlined.DirectionsCar),
    Map("Карта", Icons.Outlined.Map),
    Stats("Статистика", Icons.Outlined.BarChart),
    Records("Журнал", Icons.AutoMirrored.Outlined.ReceiptLong)
}

internal enum class EntryStep {
    Setup,
    Registration,
    Login,
    Main
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AutoCareApp(
    viewModel: GarageViewModel,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    val appSettings = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var preferredFuelType by rememberSaveable {
        mutableStateOf(appSettings.getString("preferred_fuel_type", "АИ-95").orEmpty().ifBlank { "АИ-95" })
    }
    var preferredLanguage by rememberSaveable {
        mutableStateOf(appSettings.getString("preferred_language", "system").orEmpty().ifBlank { "system" })
    }
    var preferredUnits by rememberSaveable {
        mutableStateOf(appSettings.getString("preferred_units", "metric").orEmpty().ifBlank { "metric" })
    }
    var preferredCurrency by rememberSaveable {
        mutableStateOf(appSettings.getString("preferred_currency", "RUB").orEmpty().ifBlank { "RUB" })
    }
    val resolvedLanguage = remember(preferredLanguage) {
        if (preferredLanguage == "system") {
            val sysLang = java.util.Locale.getDefault().language
            if (sysLang.startsWith("ru", ignoreCase = true)) "ru" else "en"
        } else {
            preferredLanguage
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }
    var entryStep by rememberSaveable { mutableStateOf(EntryStep.Setup) }
    var onboardingCarName by rememberSaveable { mutableStateOf<String?>(null) }
    var scannerOpen by rememberSaveable { mutableStateOf(false) }
    var pendingAiReceiptResult by remember { mutableStateOf<FuelReceiptParseResult?>(null) }
    var pendingOnboardingCar by remember { mutableStateOf<CarSetupChoice?>(null) }
    var isGlobalProcessing by rememberSaveable { mutableStateOf(false) }
    var expenseDialogCategory by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var isLoggingOut by rememberSaveable { mutableStateOf(false) }
    
    var globalProcessingStep by remember { mutableStateOf("") }

    val globalGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isGlobalProcessing = true
            globalProcessingStep = if (preferredLanguage == "en") "Compressing image... 🖼️" else "Сжимаем изображение... 🖼️"
            scope.launch {
                try {
                    val base64 = withContext(Dispatchers.IO) {
                        uriToBase64(context, uri)
                    }
                    if (base64 == null) {
                        throw Exception(if (preferredLanguage == "en") "Failed to compress image" else "Не удалось сжать изображение")
                    }
                    
                    globalProcessingStep = if (preferredLanguage == "en") "Analyzing receipt with AI... 🧠✨" else "Анализируем чек с помощью ИИ... 🧠✨"
                    val result = withContext(Dispatchers.IO) {
                        ReceiptClient.parseReceiptImageWithYandex(base64Image = base64)
                    }
                    
                    if (result != null) {
                        pendingAiReceiptResult = result
                        val parsedCategory = result.category?.let { ExpenseCategory.fromName(it) } ?: ExpenseCategory.Fuel
                        expenseDialogCategory = parsedCategory
                    } else {
                        throw Exception(if (preferredLanguage == "en") "AI returned empty result. Check the document photo." else "ИИ вернул пустой результат. Проверьте фото документа.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val msg = e.localizedMessage ?: (if (preferredLanguage == "en") "Unknown error" else "Неизвестная ошибка")
                    android.widget.Toast.makeText(context, if (preferredLanguage == "en") "AI Error: $msg" else "Ошибка ИИ: $msg", android.widget.Toast.LENGTH_LONG).show()
                } finally {
                    isGlobalProcessing = false
                }
            }
        }
    }
    var addCarOpen by rememberSaveable { mutableStateOf(false) }
    var remindersOpen by rememberSaveable { mutableStateOf(false) }
    var profileOpen by rememberSaveable { mutableStateOf(false) }
    val accountPrefs = remember { context.getSharedPreferences("account_profile", Context.MODE_PRIVATE) }
    var profileName by rememberSaveable {
        mutableStateOf(accountPrefs.getString("name", "").orEmpty())
    }
    var profileEmail by rememberSaveable {
        mutableStateOf(accountPrefs.getString("email", "").orEmpty())
    }
    var profileAvatarUri by rememberSaveable {
        mutableStateOf(accountPrefs.getString("avatar_uri", "").orEmpty())
    }
    var authToken by rememberSaveable {
        mutableStateOf(accountPrefs.getString("auth_token", "").orEmpty())
    }
    var authTokenHandled by rememberSaveable { mutableStateOf(false) }
    val systemInDarkTheme = isSystemInDarkTheme()
    val mapDarkMode = when (themePreference) {
        ThemePreference.System -> systemInDarkTheme
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }

    fun refreshProfileHeader() {
        profileName = accountPrefs.getString("name", "").orEmpty()
        profileEmail = accountPrefs.getString("email", "").orEmpty()
        profileAvatarUri = accountPrefs.getString("avatar_uri", "").orEmpty()
    }

    LaunchedEffect(authToken) {
        if (authTokenHandled) return@LaunchedEffect
        val token = authToken.takeIf(String::isNotBlank) ?: return@LaunchedEffect
        authTokenHandled = true
        entryStep = EntryStep.Main
        viewModel.setAuthToken(
            token = token,
            restoreFromServer = uiState.cars.isEmpty() || uiState.allReminders.isEmpty(),
            syncAfterTokenSet = uiState.cars.isNotEmpty() && uiState.allReminders.isNotEmpty()
        )
    }

    LaunchedEffect(uiState.restoreCompleted, uiState.restoredGarageData) {
        if (uiState.restoreCompleted) {
            refreshProfileHeader()
        }
    }

    DisposableEffect(authToken, context) {
        if (authToken.isBlank()) {
            return@DisposableEffect onDispose { }
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModel.syncNow()
            }
        }
        runCatching { connectivityManager.registerNetworkCallback(networkRequest, networkCallback) }
        onDispose {
            runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides resolvedLanguage,
        LocalAppUnits provides preferredUnits,
        LocalAppCurrency provides preferredCurrency
    ) {
        if (uiState.cars.isEmpty() && entryStep != EntryStep.Main) {
            when (entryStep) {
                EntryStep.Setup -> FirstLaunchSetupScreen(
                    onConfirmCar = { choice ->
                        pendingOnboardingCar = choice
                        onboardingCarName = "${choice.brand} ${choice.model}"
                        entryStep = EntryStep.Registration
                    },
                    onOpenLogin = { entryStep = EntryStep.Login },
                    onLanguageChanged = { lang ->
                        preferredLanguage = lang
                        appSettings.edit().putString("preferred_language", lang).apply()
                    }
                )
                EntryStep.Registration,
                EntryStep.Login -> RegistrationScreen(
                    selectedCarName = onboardingCarName,
                    startWithLogin = entryStep == EntryStep.Login,
                    onBackToSetup = { entryStep = EntryStep.Setup },
                    onLanguageChanged = { lang ->
                        preferredLanguage = lang
                        appSettings.edit().putString("preferred_language", lang).apply()
                    },
                    onComplete = { session, loginMode ->
                        authTokenHandled = true
                        authToken = session.token
                        accountPrefs.edit()
                            .putString("auth_token", session.token)
                            .putString("name", session.name)
                            .putString("email", session.email)
                            .apply()
                        refreshProfileHeader()
                        viewModel.setAuthToken(
                            token = session.token,
                            restoreFromServer = loginMode,
                            syncAfterTokenSet = loginMode
                        )
                        pendingOnboardingCar?.let { choice ->
                            viewModel.addCar(
                                choice.brand,
                                choice.model,
                                choice.generation,
                                choice.restyling,
                                choice.trim,
                                choice.year,
                                choice.plateNumber,
                                choice.mileage,
                                choice.tankVolumeLiters,
                                choice.fuelType,
                                choice.colorName,
                                choice.colorHex,
                                choice.photoUri
                            )
                        }
                        pendingOnboardingCar = null
                        entryStep = EntryStep.Main
                        selectedTab = AppTab.Home
                    }
                )
                EntryStep.Main -> Unit
            }
        } else if (authToken.isBlank() && uiState.cars.isNotEmpty()) {
            RegistrationScreen(
                selectedCarName = uiState.selectedCar?.displayName,
                startWithLogin = true,
                onBackToSetup = { },
                onLanguageChanged = { lang ->
                    preferredLanguage = lang
                    appSettings.edit().putString("preferred_language", lang).apply()
                },
                onComplete = { session, _ ->
                    authTokenHandled = true
                    authToken = session.token
                    accountPrefs.edit()
                        .putString("auth_token", session.token)
                        .putString("name", session.name)
                        .putString("email", session.email)
                        .apply()
                    refreshProfileHeader()
                    viewModel.setAuthToken(
                        token = session.token,
                        restoreFromServer = false,
                        syncAfterTokenSet = true
                    )
                    entryStep = EntryStep.Main
                    selectedTab = AppTab.Home
                }
            )
        } else if (
            authToken.isNotBlank() &&
            entryStep == EntryStep.Main &&
            uiState.cars.isEmpty() &&
            uiState.restoreInProgress
        ) {
            RestoreStatusScreen(
                title = loc("Загружаем данные аккаунта", "Loading account data"),
                message = loc("Восстанавливаем машины, расходы и напоминания с сервера.", "Restoring vehicles, expenses, and reminders from server.")
            )
        } else if (
            authToken.isNotBlank() &&
            entryStep == EntryStep.Main &&
            uiState.cars.isEmpty() &&
            uiState.restoreFailed
        ) {
            RestoreStatusScreen(
                title = loc("Не удалось загрузить данные", "Failed to load data"),
                message = loc("Проверьте интернет и попробуйте еще раз.", "Please check your internet connection and try again."),
                actionText = loc("Повторить", "Retry"),
                onAction = {
                    viewModel.setAuthToken(
                        token = authToken,
                        restoreFromServer = true,
                        syncAfterTokenSet = false
                    )
                }
            )
        } else if (
            authToken.isNotBlank() &&
            entryStep == EntryStep.Main &&
            uiState.cars.isEmpty() &&
            uiState.restoreCompleted &&
            !uiState.restoreInProgress &&
            !uiState.restoreFailed &&
            !uiState.restoredGarageData
        ) {
            AddCarSetupScreen(
                onConfirmCar = { choice ->
                    viewModel.addCar(
                        choice.brand,
                        choice.model,
                        choice.generation,
                        choice.restyling,
                        choice.trim,
                        choice.year,
                        choice.plateNumber,
                        choice.mileage,
                        choice.tankVolumeLiters,
                        choice.fuelType,
                        choice.colorName,
                        choice.colorHex,
                        choice.photoUri
                    )
                    selectedTab = AppTab.Home
                },
                onDismiss = { },
                onLanguageChanged = { lang ->
                    preferredLanguage = lang
                    appSettings.edit().putString("preferred_language", lang).apply()
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = {
                                    val tabText = when (tab) {
                                        AppTab.Home -> loc("Главная", "Home")
                                        AppTab.Map -> loc("Карта", "Map")
                                        AppTab.Stats -> loc("Статистика", "Stats")
                                        AppTab.Records -> loc("Журнал", "Journal")
                                    }
                                    Text(tabText)
                                }
                            )
                        }
                    }
                }
            ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = selectedTab, label = "tab_crossfade") { targetTab ->
                when (targetTab) {
                    AppTab.Home -> HomeScreen(
                        uiState = uiState,
                        profileName = profileName,
                        profileEmail = profileEmail,
                        profileAvatarUri = profileAvatarUri,
                        onOpenProfile = { profileOpen = true },
                        onCarSelected = viewModel::selectCar,
                        onOpenReminders = { remindersOpen = true },
                        onOpenQrScanner = { scannerOpen = true },
                        onOpenGallery = { globalGalleryLauncher.launch("image/*") },
                        onAddFuel = { expenseDialogCategory = ExpenseCategory.Fuel },
                        onAddOtherExpense = { expenseDialogCategory = ExpenseCategory.Fuel },
                        onShareCar = { carId ->
                            viewModel.shareCar(
                                carId = carId,
                                onShareUrlReady = { url ->
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, url)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                onError = { errorMsg ->
                                    android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                    AppTab.Map -> MapScreen(
                        darkMapEnabled = mapDarkMode,
                        preferredFuelType = preferredFuelType,
                        authToken = authToken
                    )
                    AppTab.Stats -> StatisticsScreen(uiState = uiState)
                    AppTab.Records -> RecordsScreen(
                        uiState = uiState,
                        onEditClick = { editingExpense = it },
                        onDeleteClick = { viewModel.deleteExpense(it) }
                    )
                }
            }
        }
    }
}

    if (scannerOpen) {
        ReceiptScannerDialog(
            onReceiptParsed = { result ->
                pendingAiReceiptResult = result
                val parsedCategory = result.category?.let { ExpenseCategory.fromName(it) } ?: ExpenseCategory.Fuel
                expenseDialogCategory = parsedCategory
                scannerOpen = false
            },
            onDismiss = { scannerOpen = false }
        )
    }

    if (addCarOpen) {
        AddCarSetupScreen(
            onConfirmCar = { choice ->
                viewModel.addCar(
                    choice.brand,
                    choice.model,
                    choice.generation,
                    choice.restyling,
                    choice.trim,
                    choice.year,
                    choice.plateNumber,
                    choice.mileage,
                    choice.tankVolumeLiters,
                    choice.fuelType,
                    choice.colorName,
                    choice.colorHex,
                    choice.photoUri
                )
                addCarOpen = false
            },
            onDismiss = { addCarOpen = false },
            onLanguageChanged = { lang ->
                preferredLanguage = lang
                appSettings.edit().putString("preferred_language", lang).apply()
            }
        )
    }

    if (remindersOpen) {
        RemindersDialog(
            uiState = uiState,
            onDismiss = { remindersOpen = false },
            onReminderAdded = viewModel::addReminder,
            onReminderChecked = viewModel::setReminderCompleted,
            onReminderUpdated = viewModel::updateReminder,
            onReminderDeleted = viewModel::deleteReminder
        )
    }

    if (profileOpen) {
        ProfileDialog(
            uiState = uiState,
            themePreference = themePreference,
            onThemePreferenceChanged = {
                onThemePreferenceChanged(it)
                viewModel.syncNow()
            },
            preferredFuelType = preferredFuelType,
            onPreferredFuelTypeChanged = {
                preferredFuelType = it
                appSettings.edit().putString("preferred_fuel_type", it).apply()
                viewModel.syncNow()
            },
            onCarSelected = viewModel::selectCar,
            onOpenAddCar = {
                profileOpen = false
                addCarOpen = true
            },
            onMileageUpdated = viewModel::updateSelectedCarMileage,
            onTankVolumeUpdated = viewModel::updateSelectedCarTankVolume,
            onCarUpdated = viewModel::updateCar,
            onCarArchived = viewModel::archiveCar,
            onCarDeleted = viewModel::deleteCar,
            onProfileSaved = {
                refreshProfileHeader()
                viewModel.syncNow()
            },
            onDismiss = { profileOpen = false },
            preferredLanguage = preferredLanguage,
            onLanguageChanged = { lang ->
                preferredLanguage = lang
                appSettings.edit().putString("preferred_language", lang).apply()
            },
            preferredUnits = preferredUnits,
            onUnitsChanged = { units ->
                preferredUnits = units
                appSettings.edit().putString("preferred_units", units).apply()
            },
            preferredCurrency = preferredCurrency,
            onCurrencyChanged = { currency ->
                preferredCurrency = currency
                appSettings.edit().putString("preferred_currency", currency).apply()
                viewModel.syncNow()
            },
            onLogout = {
                profileOpen = false
                isLoggingOut = true
                viewModel.logout {
                    authToken = ""
                    authTokenHandled = false
                    entryStep = EntryStep.Setup
                    selectedTab = AppTab.Home
                    isLoggingOut = false
                }
            }
        )
    }

    if (expenseDialogCategory != null || editingExpense != null) {
        val category = expenseDialogCategory ?: ExpenseCategory.fromName(editingExpense!!.category)
        val selectedCar = uiState.selectedCar
        if (selectedCar != null) {
            ExpenseDialog(
                title = if (editingExpense != null) loc("Редактировать трату", "Edit Expense") else if (category == ExpenseCategory.Fuel) loc("Заправка", "Fueling") else loc("Новая трата", "New Expense"),
                currentMileage = selectedCar.mileage,
                initialCategory = category,
                editingExpense = editingExpense,
                darkMapEnabled = mapDarkMode,
                preferredFuelType = preferredFuelType,
                pendingReceiptQr = uiState.pendingReceiptQr,
                pendingReceiptText = uiState.pendingReceiptText,
                authToken = authToken,
                onOpenQrScanner = {
                    expenseDialogCategory = null
                    editingExpense = null
                    scannerOpen = true
                },
                onOpenGallery = {
                    expenseDialogCategory = null
                    editingExpense = null
                    globalGalleryLauncher.launch("image/*")
                },
                onDismiss = { 
                    expenseDialogCategory = null 
                    editingExpense = null
                },
                onExpenseAdded = { expenseCategory, amount, fuelLiters, mileage, title, notes, workCost, partsCost, shopName, partName, partNumber, partBrand, assembly ->
                    if (editingExpense != null) {
                        viewModel.updateExpense(editingExpense!!, expenseCategory, amount, fuelLiters, mileage, title, notes, workCost, partsCost, shopName, partName, partNumber, partBrand, assembly)
                    } else {
                        viewModel.addExpense(expenseCategory, amount, fuelLiters, mileage, title, notes, workCost, partsCost, shopName, partName, partNumber, partBrand, assembly)
                    }
                    viewModel.clearPendingReceiptQr()
                    expenseDialogCategory = null
                    editingExpense = null
                },
                pendingAiReceiptResult = pendingAiReceiptResult,
                onClearPendingAiReceiptResult = { pendingAiReceiptResult = null }
            )
        }
    }

    AnimatedVisibility(
        visible = isGlobalProcessing,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                PremiumScanningLoader()
                
                Text(
                    text = globalProcessingStep,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Text(
                    text = loc("Обработка может занять несколько секунд...", "Processing may take a few seconds..."),
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    AnimatedVisibility(
        visible = isLoggingOut,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
                
                Text(
                    text = loc("Синхронизация и выход...", "Syncing & logging out..."),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Text(
                    text = loc("Сохраняем данные на сервере...", "Saving data to the server..."),
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
}



@Composable
internal fun ProfileHeader(
    name: String,
    email: String,
    avatarUri: String?,
    carName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = name.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@").takeIf { it.isNotBlank() }
        ?: loc("Профиль", "Profile")
    val avatarLetter = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(54.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(99.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUri.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(99.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = avatarLetter,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = carName ?: loc("Добавьте автомобиль в профиле", "Add vehicle in profile"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ReminderHeaderButton(
    urgentCount: Int,
    activeCount: Int,
    onClick: () -> Unit
) {
    val highlighted = urgentCount > 0
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                },
                shape = RoundedCornerShape(99.dp)
            )
    ) {
        BadgedBox(
            badge = {
                if (activeCount > 0) {
                    Badge(
                        containerColor = if (highlighted) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ) {
                        Text(activeCount.coerceAtMost(99).toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Event,
                contentDescription = loc("Планы", "Plans"),
                tint = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CarHeroPager(
    cars: List<CarEntity>,
    selectedCar: CarEntity?,
    onCarSelected: (Long) -> Unit,
    onShareCar: (Long) -> Unit
) {
    if (cars.isEmpty()) {
        CarHero(car = null, onShareClick = {})
        return
    }
    val carIds = cars.map { it.id }
    val selectedIndex = cars.indexOfFirst { it.id == selectedCar?.id }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = selectedIndex, pageCount = { cars.size })

    LaunchedEffect(selectedIndex, carIds) {
        if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedIndex && selectedIndex in cars.indices) {
            pagerState.scrollToPage(selectedIndex)
        }
    }
    LaunchedEffect(pagerState, carIds) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                cars.getOrNull(page)?.let { car ->
                    onCarSelected(car.id)
                }
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1)
            )
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val absoluteOffset = kotlin.math.abs(pageOffset)
            val scale = 1f - (absoluteOffset.coerceIn(0f, 1f) * 0.15f)
            val alpha = 1f - (absoluteOffset.coerceIn(0f, 1f) * 0.4f)
            
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
            ) {
                CarHero(car = cars[page], onShareClick = { onShareCar(cars[page].id) })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            cars.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (index == pagerState.currentPage) 18.dp else 7.dp)
                        .height(7.dp)
                        .background(
                            color = if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(99.dp)
                        )
                )
            }
        }
    }
}

@Composable
internal fun CarHero(car: CarEntity?, onShareClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        if (!car?.photoUri.isNullOrBlank()) {
            AsyncImage(
                model = car?.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = car?.displayName ?: loc("Автомобиль не выбран", "No vehicle selected"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                car?.let {
                    Text(
                        text = "${it.year} • ${formatMileage(it.mileage)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (car != null) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = loc("Поделиться", "Share"),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
internal fun MinimalRecentExpenses(expenses: List<ExpenseEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = loc("Последние операции", "Recent Transactions"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (expenses.isEmpty()) {
            Text(
                text = loc("Пока нет записей", "No records yet"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            expenses.forEach { expense ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expense.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatDate(expense.dateMillis),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = formatMoney(expense.amount),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

internal enum class MapCategory(val ruTitle: String, val enTitle: String, val query: String) {
    Gas("АЗС", "Gas", "заправка"),
    Wash("Мойки", "Car Wash", "автомойка"),
    Service("Сервисы", "Services", "автосервис"),
    Tires("Шиномонтаж", "Tire Shop", "шиномонтаж"),
    Parking("Парковки", "Parking", "парковка");
    val title: String @Composable get() = loc(ruTitle, enTitle)
}

internal val fuelTypeOptions = listOf("АИ-92", "АИ-95", "АИ-95 Prime", "АИ-98 Prime", "ДТ")

internal data class DemoFuelPrice(
    val fuelType: String,
    val price: String
) {
    fun asText(): String = "$fuelType: $price"
}

internal data class MapPlaceInfo(
    val title: String,
    val category: String?,
    val address: String?,
    val workingHours: String?,
    val phone: String?,
    val fuelPrices: List<DemoFuelPrice>,
    val point: Point
) {
    fun bubbleTitle(preferredFuelType: String): String {
        val prefix = when {
            category?.contains("АЗС", ignoreCase = true) == true || category?.contains("заправ", ignoreCase = true) == true || category?.contains("Gas", ignoreCase = true) == true -> "⛽ "
            category?.contains("мойка", ignoreCase = true) == true || category?.contains("wash", ignoreCase = true) == true -> "🧼 "
            category?.contains("сервис", ignoreCase = true) == true || category?.contains("ремонт", ignoreCase = true) == true || category?.contains("service", ignoreCase = true) == true -> "🔧 "
            else -> "📍 "
        }
        val price = preferredFuelPrice(preferredFuelType)?.price ?: return "$prefix$title"
        return "$prefix$title · $price"
    }

    fun preferredFuelPrice(preferredFuelType: String): DemoFuelPrice? {
        return fuelPrices.firstOrNull { it.fuelType.equals(preferredFuelType, ignoreCase = true) }
            ?: fuelPrices.firstOrNull()
    }
}








internal fun runMapSearch(
    context: Context,
    searchManager: SearchManager,
    mapObjects: MapObjectCollection,
    geometry: Geometry,
    query: String,
    darkMapEnabled: Boolean,
    preferredFuelType: String,
    reportedPrices: List<GasStationPriceItem> = emptyList(),
    onPlaceSelected: (MapPlaceInfo) -> Unit
) {
    runCatching {
        searchManager.submit(
            query,
            geometry,
            SearchOptions().setSearchTypes(SearchType.BIZ.value),
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    mapObjects.clear()
                    response.collection.children.take(20).forEach { item ->
                        val geoObject = item.obj ?: return@forEach
                        val point = geoObject.geometry.firstOrNull()?.point ?: return@forEach
                        val place = geoObject.toMapPlaceInfo(
                            point = point,
                            allowDemoFuelPrices = query == MapCategory.Gas.query,
                            reportedPrices = reportedPrices
                        )
                        val placemark = mapObjects.addPlacemark(point)
                        placemark.setView(
                            ViewProvider(createPlaceBubbleView(context, place.bubbleTitle(preferredFuelType), darkMapEnabled)),
                            IconStyle().setAnchor(PointF(0.5f, 1.0f)).setScale(1.0f)
                        )
                        placemark.zIndex = 10f
                        placemark.userData = place
                        placemark.addTapListener(
                            MapObjectTapListener { mapObject, _ ->
                                (mapObject.userData as? MapPlaceInfo)?.let(onPlaceSelected)
                                true
                            }
                        )
                    }
                }

                override fun onSearchError(error: Error) = Unit
            }
        )
    }
}

internal fun com.yandex.mapkit.GeoObject.toMapPlaceInfo(
    point: Point,
    allowDemoFuelPrices: Boolean,
    reportedPrices: List<GasStationPriceItem> = emptyList()
): MapPlaceInfo {
    val business = runCatching {
        metadataContainer.getItem(BusinessObjectMetadata::class.java)
    }.getOrNull()
    val title = business?.name
        ?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
        ?: "Место"
    val category = business?.categories
        ?.firstOrNull()
        ?.name
        ?.takeIf { it.isNotBlank() }
    val address = business?.address?.formattedAddress
        ?.takeIf { it.isNotBlank() }
        ?: descriptionText?.takeIf { it.isNotBlank() }
    val workingHours = business?.workingHours?.text
        ?.takeIf { it.isNotBlank() }
    val phone = business?.phones
        ?.firstOrNull()
        ?.formattedNumber
        ?.takeIf { it.isNotBlank() }

    val matchingPrices = reportedPrices.filter {
        kotlin.math.abs(it.latitude - point.latitude) < 0.0009 &&
        kotlin.math.abs(it.longitude - point.longitude) < 0.0009
    }

    val fuelPrices = if (matchingPrices.isNotEmpty()) {
        matchingPrices.map {
            DemoFuelPrice(
                fuelType = it.fuelType,
                price = "${formatDecimalInput(it.price)} ₽"
            )
        }
    } else if (allowDemoFuelPrices) {
        demoPrimeFuelPrices(title)
    } else {
        emptyList()
    }

    return MapPlaceInfo(
        title = title,
        category = category,
        address = address,
        workingHours = workingHours,
        phone = phone,
        fuelPrices = fuelPrices,
        point = point
    )
}

internal fun demoPrimeFuelPrices(stationTitle: String): List<DemoFuelPrice> {
    // DEMO ONLY: Prime fuel prices are copied from the user's screenshot to demonstrate
    // how our own receipt-based price database will be shown later. Do not treat as live data.
    if (!stationTitle.contains("Прайм", ignoreCase = true) &&
        !stationTitle.contains("Prime", ignoreCase = true)
    ) {
        return emptyList()
    }
    return listOf(
        DemoFuelPrice("АИ-92", "62,20 ₽"),
        DemoFuelPrice("АИ-95", "66,20 ₽"),
        DemoFuelPrice("АИ-95 Prime", "67,20 ₽"),
        DemoFuelPrice("АИ-98 Prime", "86,00 ₽"),
        DemoFuelPrice("ДТ", "80,10 ₽")
    )
}

internal fun createPlaceBubbleView(
    context: Context,
    title: String,
    darkMapEnabled: Boolean
): TextView {
    val density = context.resources.displayMetrics.density
    val backgroundColor = if (darkMapEnabled) 0xEE1E293B.toInt() else 0xF5F8FAFC.toInt()
    val strokeColor = if (darkMapEnabled) 0xFF818CF8.toInt() else 0xFF4F46E5.toInt()
    val textColor = if (darkMapEnabled) 0xFFF1F5F9.toInt() else 0xFF0F172A.toInt()
    val horizontalPadding = (12 * density).toInt()
    val verticalPadding = (7 * density).toInt()

    return TextView(context).apply {
        text = title
        setTextColor(textColor)
        textSize = 12f
        setTypeface(null, android.graphics.Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        maxWidth = (200 * density).toInt()
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20 * density
            setColor(backgroundColor)
            setStroke((1.5f * density).toInt().coerceAtLeast(1), strokeColor)
        }
        elevation = 6f * density
    }
}

internal fun visibleRegionToSearchGeometry(visibleRegion: VisibleRegion): Geometry {
    val points = listOf(
        visibleRegion.topLeft,
        visibleRegion.topRight,
        visibleRegion.bottomLeft,
        visibleRegion.bottomRight
    )
    val minLatitude = points.minOf { it.latitude }
    val maxLatitude = points.maxOf { it.latitude }
    val minLongitude = points.minOf { it.longitude }
    val maxLongitude = points.maxOf { it.longitude }
    return Geometry.fromBoundingBox(
        BoundingBox(
            Point(minLatitude, minLongitude),
            Point(maxLatitude, maxLongitude)
        )
    )
}

internal fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
internal fun getLastKnownLocationPoint(context: Context): Point? {
    if (!hasLocationPermission(context)) return null
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    ).mapNotNull { provider ->
        runCatching {
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.getLastKnownLocation(provider)
            } else {
                null
            }
        }.getOrNull()
    }.maxByOrNull { it.time }?.toMapPoint()
}

internal fun Location.toMapPoint(): Point = Point(latitude, longitude)



@Composable
internal fun Dashboard(uiState: GarageUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = loc("Всего затрат", "Total Expenses"),
                value = formatMoney(uiState.totalExpenses),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = loc("На 1 км", "Per 1 km"),
                value = formatMoney(uiState.costPerKilometer),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = loc("Записей", "Records"),
                value = uiState.expenses.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = loc("Напоминаний", "Reminders"),
                value = uiState.activeReminders.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun MetricTile(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AccountCarRow(
    car: CarEntity,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedMileage = formatMileage(car.mileage)
    val formattedLiters = car.tankVolumeLiters.takeIf { it > 0.0 }?.let { formatLiters(it) }
    val plateText = car.plateNumber.ifBlank { loc("номер не указан", "no plate") }
    val archivedText = loc("архив", "archive")
    val tankText = loc("бак", "tank")
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(car.displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = buildString {
                        append("${car.year} • $plateText • $formattedMileage")
                        if (formattedLiters != null) append(" • $tankText $formattedLiters")
                        if (car.isArchived) append(" • $archivedText")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(loc("Изменить", "Edit"))
                }
                TextButton(onClick = onArchive, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (car.isArchived) loc("Вернуть", "Restore") else loc("В архив", "Archive"))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
internal fun CarEditDialog(
    car: CarEntity,
    onDismiss: () -> Unit,
    onSave: (CarEntity) -> Unit
) {
    val currentUnits = LocalAppUnits.current
    var brand by rememberSaveable(car.id) { mutableStateOf(car.brand) }
    var model by rememberSaveable(car.id) { mutableStateOf(car.model) }
    var generation by rememberSaveable(car.id) { mutableStateOf(car.generation) }
    var restyling by rememberSaveable(car.id) { mutableStateOf(car.restyling) }
    var trim by rememberSaveable(car.id) { mutableStateOf(car.trim) }
    var year by rememberSaveable(car.id) { mutableStateOf(car.year.toString()) }
    var plate by rememberSaveable(car.id) { mutableStateOf(car.plateNumber) }
    var mileage by rememberSaveable(car.id, currentUnits) {
        val displayedMileage = if (currentUnits == "imperial") {
            Math.round(car.mileage * 0.621371).toInt()
        } else {
            car.mileage
        }
        mutableStateOf(displayedMileage.toString())
    }
    var tank by rememberSaveable(car.id, currentUnits) {
        val displayedVolume = if (currentUnits == "imperial") {
            car.tankVolumeLiters * 0.264172
        } else {
            car.tankVolumeLiters
        }
        mutableStateOf(if (car.tankVolumeLiters > 0.0) formatDecimalInput(displayedVolume) else "")
    }
    var fuelType by rememberSaveable(car.id) { mutableStateOf(car.fuelType) }
    var colorName by rememberSaveable(car.id) { mutableStateOf(car.colorName) }
    var colorHex by rememberSaveable(car.id) { mutableStateOf(car.colorHex) }
    var photoUri by rememberSaveable(car.id) { mutableStateOf(car.photoUri.orEmpty()) }
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        photoUri = uri?.toString().orEmpty()
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri = tempPhotoUri?.toString().orEmpty()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(loc("Редактировать автомобиль", "Edit Vehicle"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = loc("Закрыть", "Close"))
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(brand, { brand = it }, label = { Text(loc("Марка", "Brand")) }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(model, { model = it }, label = { Text(loc("Модель", "Model")) }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(generation, { generation = it }, label = { Text(loc("Поколение", "Generation")) }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(restyling, { restyling = it }, label = { Text(loc("Рестайлинг", "Restyling")) }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(trim, { trim = it }, label = { Text(loc("Комплектация", "Trim")) }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(year, { year = it.filter(Char::isDigit).take(4) }, label = { Text(loc("Год", "Year")) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    }
                }
                val originalMileage = car.mileage
                val displayedOriginalMileage = if (currentUnits == "imperial") {
                    Math.round(originalMileage * 0.621371).toInt()
                } else {
                    originalMileage
                }
                val isMileageError = mileage.isNotBlank() && (mileage.toIntOrNull() ?: 0) < displayedOriginalMileage
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = mileage,
                            onValueChange = { mileage = it.filter(Char::isDigit) },
                            label = { Text(if (currentUnits == "imperial") loc("Пробег, миль", "Mileage, miles") else loc("Пробег, км", "Mileage, km")) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isMileageError,
                            supportingText = if (isMileageError) {
                                { Text(loc("Не меньше $displayedOriginalMileage", "No less than $displayedOriginalMileage")) }
                            } else null,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tank,
                            onValueChange = { tank = normalizeDecimalInput(it) },
                            label = { Text(if (currentUnits == "imperial") loc("Объем бака, галл.", "Tank Volume, gal") else loc("Объем бака, л", "Tank Volume, L")) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(plate, { plate = it.uppercase(Locale.getDefault()) }, label = { Text(loc("Госномер", "Plate Number")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(fuelType, { fuelType = it }, label = { Text(loc("Тип топлива", "Fuel Type")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(colorName, { colorName = it }, label = { Text(loc("Цвет", "Color")) }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(colorHex, { colorHex = it.take(7) }, label = { Text("HEX") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = loc("Фото автомобиля", "Vehicle Photo"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (photoUri.isNotBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        }
                        TextButton(
                            onClick = { showPhotoSourceDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (photoUri.isBlank()) loc("Загрузить фото", "Upload Photo") else loc("Заменить фото", "Replace Photo")
                            )
                        }
                    }

                    if (showPhotoSourceDialog) {
                        AlertDialog(
                            onDismissRequest = { showPhotoSourceDialog = false },
                            title = { Text(loc("Выберите источник", "Select Source")) },
                            text = { Text(loc("Сфотографировать на камеру или выбрать готовое фото из галереи?", "Take a new photo with the camera or choose an existing one from the gallery?")) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showPhotoSourceDialog = false
                                        photoPicker.launch("image/*")
                                    }
                                ) {
                                    Text(loc("Галерея", "Gallery"))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showPhotoSourceDialog = false
                                        runCatching {
                                            val uri = getTmpFileUri(context)
                                            tempPhotoUri = uri
                                            cameraLauncher.launch(uri)
                                        }.onFailure { e ->
                                            e.printStackTrace()
                                            android.widget.Toast.makeText(context, e.localizedMessage ?: "Error opening camera", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text(loc("Камера", "Camera"))
                                }
                            }
                        )
                    }
                }
                item {
                    Button(
                        onClick = {
                            val enteredMileage = mileage.toIntOrNull() ?: car.mileage
                            val savedMileage = if (currentUnits == "imperial") {
                                Math.round(enteredMileage / 0.621371).toInt()
                            } else {
                                enteredMileage
                            }
                            val enteredTank = tank.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val savedTank = if (currentUnits == "imperial") {
                                enteredTank / 0.264172
                            } else {
                                enteredTank
                            }
                            onSave(
                                car.copy(
                                    brand = brand.trim(),
                                    model = model.trim(),
                                    generation = generation.trim(),
                                    restyling = restyling.trim(),
                                    trim = trim.trim(),
                                    year = year.toIntOrNull() ?: car.year,
                                    plateNumber = plate.trim(),
                                    mileage = savedMileage,
                                    tankVolumeLiters = savedTank,
                                    fuelType = fuelType.trim(),
                                    colorName = colorName.trim(),
                                    colorHex = colorHex.trim(),
                                    photoUri = photoUri.ifBlank { null }
                                )
                            )
                        },
                        enabled = brand.isNotBlank() && model.isNotBlank() && !isMileageError,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(loc("Сохранить", "Save"))
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddCarForm(
    onCarAdded: (String, String, Int, String, Int, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    val currentUnits = LocalAppUnits.current
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }
    var fuelType by rememberSaveable(lang) { mutableStateOf(if (lang == "en") "Gasoline" else "Бензин") }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = loc("Добавить автомобиль", "Add vehicle"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(loc("Марка", "Brand")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(loc("Модель", "Model")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter(Char::isDigit).take(4) },
                    label = { Text(loc("Год", "Year")) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it.filter(Char::isDigit) },
                    label = { Text(if (currentUnits == "imperial") loc("Пробег, миль", "Mileage, miles") else loc("Пробег, км", "Mileage, km")) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it.uppercase(Locale.getDefault()) },
                label = { Text(loc("Госномер", "License plate")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = fuelType,
                onValueChange = { fuelType = it },
                label = { Text(loc("Тип топлива", "Fuel type")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    val parsedYear = year.toIntOrNull() ?: 2020
                    val parsedMileage = mileage.toIntOrNull() ?: 0
                    onCarAdded(brand, model, parsedYear, plate, parsedMileage, fuelType)
                    brand = ""
                    model = ""
                    year = ""
                    plate = ""
                    mileage = ""
                    fuelType = if (lang == "en") "Gasoline" else "Бензин"
                },
                enabled = brand.isNotBlank() && model.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(loc("Сохранить автомобиль", "Save vehicle"))
            }
        }
    }
}

internal data class ChartPoint(val label: String, val value: Double)

@Composable
internal fun StatisticsHeroCard(
    totalSpent: Double,
    periodLabel: String,
    filteredExpenses: List<ExpenseEntity>
) {
    val totalDays = remember(filteredExpenses) {
        if (filteredExpenses.size < 2) 1
        else {
            val minDate = filteredExpenses.minOf { it.dateMillis }
            val maxDate = filteredExpenses.maxOf { it.dateMillis }
            val diffDays = (maxDate - minDate) / (1000 * 60 * 60 * 24)
            diffDays.coerceAtLeast(1)
        }
    }
    
    val avgPerDay = totalSpent / totalDays
    
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = loc("ОБЩИЕ ЗАТРАТЫ", "TOTAL EXPENSES"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Text(
                    text = formatMoney(totalSpent),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatMoney(avgPerDay)} / ${loc("день", "day")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
internal fun StatisticsMetricsGrid(
    fuelCost: Double,
    fuelLiters: Double,
    averageFuelPrice: Double,
    averageFuelConsumption: Double,
    costPerKm: Double,
    averageFuelCheck: Double,
    periodMileage: Int,
    totalLogs: Int
) {
    val units = LocalAppUnits.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PremiumMetricTile(
                title = loc("Затраты на топливо", "Fuel Expenses"),
                value = formatMoney(fuelCost),
                icon = Icons.Outlined.LocalGasStation,
                iconColor = Color(0xFFE57373),
                modifier = Modifier.weight(1f)
            )
            PremiumMetricTile(
                title = loc("Стоимость 1 км", "Cost per 1 km"),
                value = if (costPerKm > 0.0) "${formatMoney(costPerKm)}/${loc("км", "km")}" else loc("нет данных", "no data"),
                icon = Icons.Outlined.Build,
                iconColor = Color(0xFF7986CB),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PremiumMetricTile(
                title = if (units == "imperial") loc("Объем (галлоны)", "Volume (Gallons)") else loc("Объем (литры)", "Volume (Liters)"),
                value = formatLiters(fuelLiters),
                icon = Icons.Outlined.DirectionsCar,
                iconColor = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
            PremiumMetricTile(
                title = loc("Средний чек АЗС", "Average Fill Cost"),
                value = formatMoney(averageFuelCheck),
                icon = Icons.Outlined.DirectionsCar,
                iconColor = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PremiumMetricTile(
                title = loc("Средний расход", "Avg Consumption"),
                value = formatConsumption(averageFuelConsumption),
                icon = Icons.Outlined.Settings,
                iconColor = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )
            PremiumMetricTile(
                title = loc("Средняя цена литра", "Avg Price/Liter"),
                value = formatAverageFuelPrice(averageFuelPrice),
                icon = Icons.Outlined.LocalGasStation,
                iconColor = Color(0xFF4DB6AC),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PremiumMetricTile(
                title = loc("Пробег за период", "Period Mileage"),
                value = formatMileage(periodMileage),
                icon = Icons.Outlined.DirectionsCar,
                iconColor = Color(0xFFAB47BC),
                modifier = Modifier.weight(1f)
            )
            PremiumMetricTile(
                title = loc("Всего записей", "Total Records"),
                value = "$totalLogs ${loc("зап.", "logs")}",
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                iconColor = Color(0xFFFF7043),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun PremiumMetricTile(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = iconColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun ExpenseShareBreakdownCard(expenses: List<ExpenseEntity>) {
    val lang = LocalAppLanguage.current
    val totalAmount = expenses.sumOf { it.amount }
    
    val categoryShares = remember(expenses, totalAmount) {
        if (totalAmount <= 0.0) emptyList()
        else {
            ExpenseCategory.entries.map { category ->
                val amount = expenses.filter { it.category == category.name }.sumOf { it.amount }
                val percentage = amount / totalAmount
                CategoryShareItem(category, amount, percentage)
            }
            .filter { it.amount > 0.0 }
            .sortedByDescending { it.amount }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = loc("Распределение расходов", "Expense Share Breakdown"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (categoryShares.isEmpty()) {
                Text(
                    text = loc("Нет данных о расходах за выбранный период", "No expense data for the selected period"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    categoryShares.forEach { item ->
                        val weight = item.percentage.toFloat().coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(14.dp)
                                .background(expenseCategoryColor(item.category))
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoryShares.forEach { item ->
                        val categoryColor = expenseCategoryColor(item.category)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color = categoryColor, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                                Text(
                                    text = item.category.getTitle(lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatMoney(item.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${Math.round(item.percentage * 100)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal data class CategoryShareItem(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Double
)

@Composable
internal fun AdvancedMetricsCard(
    avgDaysBetweenFills: Double,
    avgMileageBetweenFills: Double,
    avgCostPerMonth: Double,
    projectedAnnualCost: Double
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = loc("Расширенная аналитика и прогнозы", "Advanced Analytics & Forecasts"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = loc("ЧАСТОТА И ИНТЕРВАЛЫ", "FREQUENCY & INTERVALS"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricItem(
                        title = loc("Дней между АЗС", "Days between Fills"),
                        value = if (avgDaysBetweenFills > 0.0) "${formatOneDecimal(avgDaysBetweenFills)} ${loc("дн.", "days")}" else loc("нет данных", "no data"),
                        modifier = Modifier.weight(1f)
                    )
                    DetailMetricItem(
                        title = loc("Пробег между АЗС", "Mileage between Fills"),
                        value = if (avgMileageBetweenFills > 0.0) formatMileage(avgMileageBetweenFills.toInt()) else loc("нет данных", "no data"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            DottedDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = loc("ПРОГНОЗЫ РАСХОДОВ (НА ОСНОВЕ ПОСЛ. 6 МЕС.)", "RECENT SPEND FORECASTS (6M BASE)"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricItem(loc("Среднее за месяц", "Monthly Avg"), formatMoney(avgCostPerMonth), Modifier.weight(1f))
                    DetailMetricItem(loc("Прогноз на 1 год", "Annual Forecast"), formatMoney(projectedAnnualCost), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun AiAnalyticsCard(
    car: CarEntity,
    avgFuelConsumption: Double,
    totalSpent: Double,
    expenses: List<ExpenseEntity>
) {
    val lang = LocalAppLanguage.current
    val units = LocalAppUnits.current
    val recommendations = remember(car, avgFuelConsumption, totalSpent, expenses, lang, units) {
        fun locStr(ru: String, en: String): String = if (lang == "en") en else ru
        fun formatMileageStr(value: Int): String {
            return if (units == "imperial") {
                val miles = Math.round(value * 0.621371).toInt()
                if (lang == "en") "${NumberFormat.getIntegerInstance(Locale.US).format(miles)} mi" else "${NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(miles)} миль"
            } else {
                if (lang == "en") "${NumberFormat.getIntegerInstance(Locale.US).format(value)} km" else "${NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(value)} км"
            }
        }
        val list = mutableListOf<String>()
        val carName = car.displayName.lowercase()
        
        if (carName.contains("qashqai") || carName.contains("cvt")) {
            list.add(
                locStr(
                    "На автомобилях Nissan Qashqai с вариатором (CVT) крайне рекомендуется менять трансмиссионную жидкость (NS-2/NS-3) каждые 60 000 км для предотвращения износа конусов и ремня.",
                    "On Nissan Qashqai vehicles with CVT, it is highly recommended to replace the transmission fluid (NS-2/NS-3) every 60,000 km to prevent cone and belt wear."
                )
            )
        }
        
        if (avgFuelConsumption > 11.5) {
            list.add(
                locStr(
                    "Средний расход топлива составляет ${formatOneDecimal(avgFuelConsumption)} л/100 км, что превышает норму. Рекомендуется провести чистку дроссельной заслонки, проверить свечи зажигания и давление в шинах (снижение на 0.5 бар повышает расход на 5%).",
                    "Your average fuel consumption is ${formatOneDecimal(avgFuelConsumption)} L/100km, which is slightly high. Consider cleaning the throttle body, checking spark plugs, and verifying tire pressure (a 0.5 bar drop increases fuel consumption by 5%)."
                )
            )
        }
        
        if (car.mileage > 150000) {
            list.add(
                locStr(
                    "Пробег автомобиля составляет ${formatMileageStr(car.mileage)}. Рекомендуется уделить внимание состоянию цепи/ремня ГРМ, подвески (особенно сайлентблоков) и проверить катализатор на предмет разрушения.",
                    "Your vehicle has covered ${formatMileageStr(car.mileage)}. It is recommended to check the condition of the timing belt/chain, suspension components (especially silent blocks), and monitor the catalytic converter."
                )
            )
        }
        
        val partsCost = expenses.sumOf { it.partsCost ?: 0.0 }
        val workCost = expenses.sumOf { it.workCost ?: 0.0 }
        if (partsCost > 0.0 && workCost > 0.0 && workCost / partsCost > 1.2) {
            list.add(
                locStr(
                    "Затраты на работу механиков существенно превышают стоимость деталей. Рекомендуется совмещать мелкие ремонтные работы или выбирать СТО с фиксированными пакетными предложениями.",
                    "Labor services significantly exceed the cost of purchased parts. We recommend combining minor repair tasks together or choosing service centers with bundled flat-rate packages."
                )
            )
        }
        
        if (list.isEmpty()) {
            list.add(
                locStr(
                    "Своевременная замена моторного масла (каждые 8 000–10 000 км) существенно снижает риск закоксовки поршневых колец и увеличивает ресурс двигателя на 150 000+ км.",
                    "Timely engine oil changes (every 8,000–10,000 km) significantly reduce the risk of carbon build-up on piston rings and increase engine life by 150,000+ km."
                )
            )
            list.add(
                locStr(
                    "Проверяйте уровень охлаждающей и тормозной жидкостей раз в месяц. Падение уровня тормозной жидкости часто сигнализирует о предельном износе тормозных колодок.",
                    "Check coolant and brake fluid levels monthly. A drop in brake fluid often signals extreme wear of the brake pads."
                )
            )
        }
        
        list
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = loc("Интеллектуальный отчет AI", "AI Smart Insights"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI ANALYTICS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recommendations.forEach { recommendation ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailMetricItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun BarChartCard(
    title: String,
    points: List<ChartPoint>,
    valueFormatter: @Composable (Double) -> String = { formatMoney(it) }
) {
    val lang = LocalAppLanguage.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (points.isEmpty()) {
                Text(
                    text = loc("Недостаточно данных для графика", "Insufficient data for chart"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val max = points.maxOf { it.value }.coerceAtLeast(1.0)
                val suffix = if (lang == "en") "k" else "к"
                
                fun formatShort(value: Double): String {
                    val lower = title.lowercase()
                    return if (lower.contains("расход") || lower.contains("consumption")) {
                        String.format(Locale.US, "%.1f", value)
                    } else if (lower.contains("цена") || lower.contains("price")) {
                        String.format(Locale.US, "%.1f", value)
                    } else {
                        if (value >= 1000.0) {
                            val thousands = value / 1000.0
                            if (thousands >= 100.0) {
                                "${Math.round(thousands)}$suffix"
                            } else {
                                String.format(Locale.US, "%.1f", thousands) + suffix
                            }
                        } else {
                            if (value % 1.0 == 0.0) {
                                value.toInt().toString()
                            } else {
                                String.format(Locale.US, "%.1f", value)
                            }
                        }
                    }
                }
                
                val scrollState = rememberScrollState()
                val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
                LaunchedEffect(points) {
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        points.forEach { point ->
                            val isPeak = point.value == max && points.size > 1
                            Column(
                                modifier = Modifier
                                    .width(44.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Value label
                                Text(
                                    text = formatShort(point.value),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Spacer container for the vertical bar
                                Box(
                                    modifier = Modifier
                                        .width(44.dp)
                                        .weight(1f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val barHeightFraction = ((point.value / max).toFloat().coerceIn(0.06f, 1.0f)) * animationProgress.value
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(barHeightFraction.coerceAtLeast(0.01f))
                                            .background(
                                                brush = if (isPeak) {
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.tertiary
                                                        )
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                                            MaterialTheme.colorScheme.secondary
                                                        )
                                                    )
                                                },
                                                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Label (Month and Year on separate lines)
                                val yearLabel = point.label.substringBefore(" ")
                                val monthLabel = point.label.substringAfter(" ")
                                Text(
                                    text = monthLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = yearLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LineChartCard(
    title: String,
    points: List<ChartPoint>,
    valueFormatter: @Composable (Double) -> String = { formatMoney(it) }
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val displayFormatter = @Composable { v: Double -> valueFormatter(v) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (points.isEmpty()) {
                Text(
                    text = loc("Недостаточно данных для графика", "Insufficient data for chart"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val max = points.maxOf { it.value }
                val formattedMax = displayFormatter(max)
                val formattedMin = displayFormatter(0.0)
                
                val scrollState = rememberScrollState()
                val minWidth = 320.dp
                val dynamicWidth = (points.size * 44 + (points.size - 1) * 10).dp.coerceAtLeast(minWidth)
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = loc("Макс: ", "Max: ") + formattedMax,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = loc("Мин: 0", "Min: 0"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier.width(dynamicWidth),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                LineChart(
                                    points = points,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 16.dp, bottom = 16.dp, start = 22.dp, end = 22.dp)
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                points.forEach { point ->
                                    val yearLabel = point.label.substringBefore(" ")
                                    val monthLabel = point.label.substringAfter(" ")
                                    Column(
                                        modifier = Modifier.width(44.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = monthLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = yearLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LineChart(
    points: List<ChartPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    
    val maxVal = points.maxOf { it.value }.coerceAtLeast(1.0)
    val minVal = 0.0

    val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val count = points.size
        if (count < 2) {
            val y = height - (points[0].value / maxVal * height).toFloat()
            drawCircle(
                color = color,
                radius = 6.dp.toPx(),
                center = Offset(width / 2, y)
            )
            return@Canvas
        }
        
        val stepX = width / (count - 1)
        val path = Path()
        val connectionPoints = mutableListOf<Offset>()
        
        for (i in 0 until count) {
            val x = i * stepX
            val targetRatio = ((points[i].value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
            val ratio = targetRatio * animationProgress.value
            val y = height - (ratio * height)
            val offset = Offset(x, y)
            connectionPoints.add(offset)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prev = connectionPoints[i - 1]
                val controlX1 = prev.x + (offset.x - prev.x) / 2f
                val controlY1 = prev.y
                val controlX2 = prev.x + (offset.x - prev.x) / 2f
                val controlY2 = offset.y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, offset.x, offset.y)
            }
        }
        
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.25f),
                    color.copy(alpha = 0.0f)
                )
            )
        )
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
        
        connectionPoints.forEach { offset ->
            drawCircle(
                color = color,
                radius = 5.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = offset
            )
        }
    }
}

internal fun calculateLocalAverageConsumption(expenses: List<ExpenseEntity>): Double {
    val fuelRecords = expenses
        .filter { it.category == ExpenseCategory.Fuel.name && (it.fuelLiters ?: 0.0) > 0.0 && it.mileage > 0 }
        .sortedBy { it.mileage }
    if (fuelRecords.size < 2) return 0.0
    val distance = (fuelRecords.last().mileage - fuelRecords.first().mileage).coerceAtLeast(0)
    if (distance == 0) return 0.0
    val litersAfterFirst = fuelRecords.drop(1).sumOf { it.fuelLiters ?: 0.0 }
    return litersAfterFirst / distance * 100.0
}

@Composable
internal fun FuelRecordStats(fuelExpenses: List<ExpenseEntity>, tankVolume: Double) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(loc("Заправки", "Refuelings"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val records = fuelExpenses
                .filter { (it.fuelLiters ?: 0.0) > 0.0 }
                .sortedByDescending { it.dateMillis }
                .take(6)
            if (records.isEmpty()) {
                Text(loc("Добавьте литры в заправках, чтобы увидеть детализацию.", "Add liters in refuelings to see detailed stats."), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                records.forEach { expense ->
                    val liters = expense.fuelLiters ?: 0.0
                    val fillPercent = if (tankVolume > 0.0) liters / tankVolume * 100.0 else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${formatDate(expense.dateMillis)} • ${formatMileage(expense.mileage)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = buildString {
                                append(formatLiters(liters))
                                if (fillPercent > 0.0) append(" • ${formatOneDecimal(fillPercent)}%")
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RecordsScreen(
    uiState: GarageUiState,
    onEditClick: (ExpenseEntity) -> Unit,
    onDeleteClick: (ExpenseEntity) -> Unit
) {
    val selectedCar = uiState.selectedCar
    if (selectedCar == null) {
        EmptyState(loc("Сначала добавьте автомобиль", "Add a vehicle first"))
        return
    }

    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }
    var currentSort by rememberSaveable { mutableStateOf("date_desc") }
    var showSortMenu by remember { mutableStateOf(false) }
    var activeDetailExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val currentFilter = selectedCategoryFilter
    val filteredExpenses = remember(uiState.expenses, searchQuery, currentFilter, currentSort) {
        uiState.expenses
            .filter { expense ->
                expense.carId == selectedCar.id && (
                    expense.title.contains(searchQuery, ignoreCase = true) ||
                    expense.notes.contains(searchQuery, ignoreCase = true) ||
                    (expense.shopName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (expense.partName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (expense.assembly?.contains(searchQuery, ignoreCase = true) == true)
                ) && (currentFilter == null || expense.category == currentFilter.name)
            }
            .sortedWith { a, b ->
                when (currentSort) {
                    "date_desc" -> b.dateMillis.compareTo(a.dateMillis)
                    "date_asc" -> a.dateMillis.compareTo(b.dateMillis)
                    "amount_desc" -> b.amount.compareTo(a.amount)
                    "amount_asc" -> a.amount.compareTo(b.amount)
                    else -> b.dateMillis.compareTo(a.dateMillis)
                }
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(loc("Поиск по расходам...", "Search expenses...")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        item {
            // Horizontally scrolling Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text(loc("Все", "All")) }
                )
                
                ExpenseCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat.title) }
                    )
                }
            }
        }

        item {
            // Sorting & Found Count Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${loc("Найдено", "Found")}: ${filteredExpenses.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Box {
                    TextButton(
                        onClick = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Outlined.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val sortLabel = when (currentSort) {
                            "date_desc" -> loc("По дате (новые)", "Date (Newest)")
                            "date_asc" -> loc("По дате (старые)", "Date (Oldest)")
                            "amount_desc" -> loc("По сумме (убыв.)", "Amount (Desc)")
                            "amount_asc" -> loc("По сумме (возр.)", "Amount (Asc)")
                            else -> loc("Сортировка", "Sort")
                        }
                        Text(
                            text = sortLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(loc("По дате: сначала новые", "Date: Newest first")) },
                            onClick = {
                                currentSort = "date_desc"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(loc("По дате: сначала старые", "Date: Oldest first")) },
                            onClick = {
                                currentSort = "date_asc"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(loc("По сумме: от больших к меньшим", "Amount: Highest first")) },
                            onClick = {
                                currentSort = "amount_desc"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(loc("По сумме: от меньших к большим", "Amount: Lowest first")) },
                            onClick = {
                                currentSort = "amount_asc"
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        item {
            CategoryStats(expenses = filteredExpenses)
        }

        item {
            Text(
                text = loc("История расходов", "Expense History"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (filteredExpenses.isEmpty()) {
            item { EmptyState(loc("Совпадений не найдено", "No matches found")) }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    modifier = Modifier.animateItem(),
                    onCardClick = {
                        activeDetailExpense = expense
                    },
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }

    activeDetailExpense?.let { expense ->
        ExpenseDetailDialog(
            expense = expense,
            onEditClick = { onEditClick(expense) },
            onDismiss = { activeDetailExpense = null }
        )
    }
}

@Composable
internal fun ExpenseDialog(
    title: String,
    currentMileage: Int,
    initialCategory: ExpenseCategory,
    editingExpense: ExpenseEntity?,
    darkMapEnabled: Boolean,
    preferredFuelType: String,
    pendingReceiptQr: String?,
    pendingReceiptText: String?,
    authToken: String,
    onOpenQrScanner: () -> Unit,
    onOpenGallery: () -> Unit,
    onDismiss: () -> Unit,
    onExpenseAdded: (ExpenseCategory, Double, Double?, Int, String, String, Double?, Double?, String?, String?, String?, String?, String?) -> Unit,
    pendingAiReceiptResult: FuelReceiptParseResult? = null,
    onClearPendingAiReceiptResult: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                    }
                }
            }
            item {
                Text(
                    text = loc(
                        "Заполните только нужные поля. Запись сразу попадет в журнал расходов.",
                        "Fill in only the necessary fields. The entry will be added to the expense log immediately."
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                AddExpenseForm(
                    currentMileage = currentMileage,
                    initialCategory = initialCategory,
                    editingExpense = editingExpense,
                    darkMapEnabled = darkMapEnabled,
                    preferredFuelType = preferredFuelType,
                    pendingReceiptQr = pendingReceiptQr,
                    pendingReceiptText = pendingReceiptText,
                    authToken = authToken,
                    onOpenQrScanner = onOpenQrScanner,
                    onOpenGallery = onOpenGallery,
                    showScanButton = true,
                    onExpenseAdded = onExpenseAdded,
                    pendingAiReceiptResult = pendingAiReceiptResult,
                    onClearPendingAiReceiptResult = onClearPendingAiReceiptResult
                )
            }
        }
    }
}



@Composable
internal fun CategoryStats(expenses: List<ExpenseEntity>) {
    val totals = expenses
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    if (totals.isEmpty()) {
        return
    }

    val max = totals.maxOf { it.second }.coerceAtLeast(1.0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = loc("Структура затрат", "Cost Structure"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        totals.forEach { (categoryName, total) ->
            val category = ExpenseCategory.fromName(categoryName)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.title,
                    modifier = Modifier.width(92.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((total / max).toFloat().coerceIn(0.05f, 1f))
                            .height(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
                Text(
                    text = formatMoney(total),
                    modifier = Modifier.width(96.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

internal fun expenseCategoryColor(category: ExpenseCategory): Color = when (category) {
    ExpenseCategory.Fuel -> Color(0xFFE57373) // Red-Orange
    ExpenseCategory.Maintenance -> Color(0xFF64B5F6) // Light Blue
    ExpenseCategory.Repair -> Color(0xFFBA68C8) // Purple
    ExpenseCategory.Parts -> Color(0xFFFFB74D) // Orange
    ExpenseCategory.Insurance -> Color(0xFF4DD0E1) // Cyan
    ExpenseCategory.Wash -> Color(0xFF81C784) // Green
    ExpenseCategory.Parking -> Color(0xFF90A4AE) // Blue-Grey
    ExpenseCategory.Other -> Color(0xFFA1887F) // Brown
}

internal fun expenseCategoryIcon(category: ExpenseCategory): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    ExpenseCategory.Fuel -> Icons.Outlined.LocalGasStation
    ExpenseCategory.Maintenance -> Icons.Outlined.Build
    ExpenseCategory.Repair -> Icons.Outlined.Settings
    ExpenseCategory.Parts -> Icons.Outlined.Inventory2
    ExpenseCategory.Insurance -> Icons.Outlined.Shield
    ExpenseCategory.Wash -> Icons.Outlined.DirectionsCar
    ExpenseCategory.Parking -> Icons.Outlined.LocationOn
    ExpenseCategory.Other -> Icons.Outlined.Build
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseDetailDialog(
    expense: ExpenseEntity,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val category = ExpenseCategory.fromName(expense.category)
    val categoryColor = expenseCategoryColor(category)
    val categoryIcon = expenseCategoryIcon(category)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = categoryColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = category.getTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatMoney(expense.amount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                DottedDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(loc("Дата", "Date"), formatDate(expense.dateMillis))
                    DetailRow(loc("Пробег", "Mileage"), formatMileage(expense.mileage))
                    
                    expense.fuelLiters?.takeIf { it > 0.0 }?.let { liters ->
                        DetailRow(loc("Объем топлива", "Fuel Volume"), formatLiters(liters))
                        val pricePerLiter = expense.amount / liters
                        DetailRow(loc("Цена за литр", "Price per liter"), formatMoney(pricePerLiter))
                    }

                    expense.shopName?.takeIf { it.isNotBlank() }?.let { shop ->
                        DetailRow(loc("СТО / АЗС", "Shop / Gas Station"), shop)
                    }

                    expense.assembly?.takeIf { it.isNotBlank() }?.let { assembly ->
                        DetailRow(loc("Узел автомобиля", "Car Assembly"), assembly)
                    }

                    expense.partName?.takeIf { it.isNotBlank() }?.let { part ->
                        DetailRow(loc("Деталь", "Part Name"), part)
                    }

                    expense.partBrand?.takeIf { it.isNotBlank() }?.let { brand ->
                        DetailRow(loc("Производитель детали", "Part Brand"), brand)
                    }

                    expense.partNumber?.takeIf { it.isNotBlank() }?.let { num ->
                        DetailRow(loc("Артикул детали", "Part Number"), num)
                    }

                    if (expense.workCost != null || expense.partsCost != null) {
                        DottedDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        expense.workCost?.let {
                            DetailRow(loc("Стоимость работы", "Work Cost"), formatMoney(it))
                        }
                        expense.partsCost?.let {
                            DetailRow(loc("Стоимость деталей", "Parts Cost"), formatMoney(it))
                        }
                    }
                }

                if (expense.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = loc("Заметки:", "Notes:"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = expense.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(loc("Закрыть", "Close"))
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onEditClick()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(loc("Изменить", "Edit"))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun DottedDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}



internal data class ReminderCategoryOption(val key: String, val ruTitle: String, val enTitle: String) {
    val title: String @Composable get() = loc(ruTitle, enTitle)
    fun getTitle(lang: String): String = if (lang == "en") enTitle else ruTitle
}

internal val reminderCategoryOptions = listOf(
    ReminderCategoryOption("Service", "ТО", "Service"),
    ReminderCategoryOption("Fluids", "Жидкости", "Fluids"),
    ReminderCategoryOption("Tires", "Шины", "Tires"),
    ReminderCategoryOption("Insurance", "Документы", "Documents"),
    ReminderCategoryOption("Repair", "Ремонт", "Repair"),
    ReminderCategoryOption("Other", "Другое", "Other")
)

@Composable
internal fun RemindersScreen(
    uiState: GarageUiState,
    onReminderAdded: (String, String, Int?, Long?, Int?, Int?) -> Unit,
    onReminderChecked: (ReminderEntity, Boolean) -> Unit,
    onReminderUpdated: (ReminderEntity) -> Unit,
    onReminderDeleted: (ReminderEntity) -> Unit
) {
    val selectedCar = uiState.selectedCar
    if (selectedCar == null) {
        EmptyState(loc("Сначала добавьте автомобиль", "Add a vehicle first"))
        return
    }

    var selectedSetupPreset by remember { mutableStateOf<MaintenanceCardPreset?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = loc("Регулярное обслуживание", "Regular Maintenance"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(maintenancePresets) { preset ->
            val activeReminder = uiState.allReminders.firstOrNull { reminder ->
                reminder.carId == selectedCar.id && !reminder.isCompleted && 
                (reminder.title.equals(preset.titleRu, ignoreCase = true) || 
                 reminder.title.equals(preset.titleEn, ignoreCase = true))
            }
            
            MaintenanceTaskCard(
                preset = preset,
                activeReminder = activeReminder,
                car = selectedCar,
                onConfigureClick = {
                    selectedSetupPreset = preset
                },
                onCompletedClick = {
                    if (activeReminder != null) {
                        onReminderChecked(activeReminder, true)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = loc("Другие напоминания", "Custom Reminders"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            AddReminderForm(
                onReminderAdded = onReminderAdded
            )
        }

        val customReminders = uiState.allReminders.filter { reminder ->
            reminder.carId == selectedCar.id && 
            maintenancePresets.none { preset ->
                reminder.title.equals(preset.titleRu, ignoreCase = true) || 
                reminder.title.equals(preset.titleEn, ignoreCase = true)
            }
        }

        if (customReminders.isEmpty()) {
            item {
                EmptyState(loc("Дополнительных напоминаний пока нет.", "No custom reminders yet."))
            }
        } else {
            items(customReminders, key = { it.id }) { reminder ->
                ReminderRow(
                    reminder = reminder,
                    modifier = Modifier.animateItem(),
                    car = selectedCar,
                    onChecked = { checked -> onReminderChecked(reminder, checked) }
                )
            }
        }
    }

    selectedSetupPreset?.let { preset ->
        val activeReminder = uiState.allReminders.firstOrNull { reminder ->
            reminder.carId == selectedCar.id && !reminder.isCompleted && 
            (reminder.title.equals(preset.titleRu, ignoreCase = true) || 
             reminder.title.equals(preset.titleEn, ignoreCase = true))
        }
        
        MaintenanceSetupDialog(
            preset = preset,
            activeReminder = activeReminder,
            currentMileage = selectedCar.mileage,
            onSave = { mileage, months ->
                if (activeReminder != null) {
                    onReminderUpdated(
                        activeReminder.copy(
                            dueMileage = mileage?.let { selectedCar.mileage + it },
                            dueDateMillis = months?.let { m ->
                                val cal = java.util.Calendar.getInstance()
                                cal.add(java.util.Calendar.MONTH, m)
                                cal.timeInMillis
                            },
                            repeatMileageInterval = mileage,
                            repeatIntervalMonths = months
                        )
                    )
                } else {
                    onReminderAdded(
                        preset.titleRu,
                        preset.category,
                        mileage?.let { selectedCar.mileage + it },
                        months?.let { m ->
                            val cal = java.util.Calendar.getInstance()
                            cal.add(java.util.Calendar.MONTH, m)
                            cal.timeInMillis
                        },
                        mileage,
                        months
                    )
                }
            },
            onDeactivate = if (activeReminder != null) {
                { onReminderDeleted(activeReminder) }
            } else null,
            onDismiss = { selectedSetupPreset = null }
        )
    }
}

internal data class MaintenanceCardPreset(
    val titleRu: String,
    val titleEn: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBackground: androidx.compose.ui.graphics.Color,
    val defaultIntervalMileage: Int?,
    val defaultIntervalMonths: Int?
)

internal val maintenancePresets = listOf(
    MaintenanceCardPreset("Плановое ТО", "Scheduled Maintenance", "Service", Icons.Outlined.Build, Color(0xFF7986CB), 15000, 12),
    MaintenanceCardPreset("Замена моторного масла", "Engine Oil Change", "Service", Icons.Outlined.LocalGasStation, Color(0xFFE57373), 10000, 12),
    MaintenanceCardPreset("Замена салонного фильтра", "Cabin Filter Change", "Service", Icons.Outlined.DirectionsCar, Color(0xFF64B5F6), 15000, 12),
    MaintenanceCardPreset("Замена воздушного фильтра", "Air Filter Change", "Service", Icons.Outlined.DirectionsCar, Color(0xFF81C784), 15000, 12),
    MaintenanceCardPreset("Замена тормозных колодок", "Brake Pads Replacement", "Repair", Icons.Outlined.Settings, Color(0xFFFFB74D), 30000, null),
    MaintenanceCardPreset("Замена тормозной жидкости", "Brake Fluid Change", "Fluids", Icons.Outlined.LocalGasStation, Color(0xFF4DB6AC), 40000, 24),
    MaintenanceCardPreset("Замена антифриза (ОЖ)", "Coolant Change", "Fluids", Icons.Outlined.LocalGasStation, Color(0xFF9575CD), 60000, 36),
    MaintenanceCardPreset("Замена ремня ГРМ", "Timing Belt Replacement", "Repair", Icons.Outlined.Build, Color(0xFFBA68C8), 80000, 60),
    MaintenanceCardPreset("Страховка ОСАГО", "Insurance Renewal", "Insurance", Icons.Outlined.Shield, Color(0xFF4DD0E1), null, 12),
    MaintenanceCardPreset("Техосмотр", "Technical Inspection", "Insurance", Icons.Outlined.Shield, Color(0xFFA1887F), null, 12),
    MaintenanceCardPreset("Сезонный шиномонтаж", "Seasonal Tire Swap", "Tires", Icons.Outlined.Settings, Color(0xFFD4E157), null, 6)
)

@Composable
internal fun MaintenanceTaskCard(
    preset: MaintenanceCardPreset,
    activeReminder: ReminderEntity?,
    car: CarEntity?,
    onConfigureClick: () -> Unit,
    onCompletedClick: () -> Unit
) {
    val isConfigure = activeReminder != null
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfigure) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigureClick() }
            .border(
                width = 1.dp,
                color = if (isConfigure) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isConfigure) preset.iconBackground else Color.LightGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = if (isConfigure) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loc(preset.titleRu, preset.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isConfigure) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (isConfigure && activeReminder != null) {
                    val statusText = reminderDueText(activeReminder, car)
                    val isOverdue = car?.mileage?.let { activeReminder.dueMileage?.let { due -> due <= it } } == true || 
                            activeReminder.dueDateMillis?.let { it < System.currentTimeMillis() } == true
                    
                    Text(
                        text = statusText,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    val repeatIntervals = buildList {
                        activeReminder.repeatMileageInterval?.takeIf { it > 0 }?.let {
                            add("${loc("каждые", "every")} ${formatMileage(it)}")
                        }
                        activeReminder.repeatIntervalMonths?.takeIf { it > 0 }?.let {
                            add("${loc("каждые", "every")} $it ${loc("мес.", "months")}")
                        }
                    }
                    if (repeatIntervals.isNotEmpty()) {
                        Text(
                            text = repeatIntervals.joinToString(" & "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = loc("Не настроено. Нажмите, чтобы включить.", "Not configured. Tap to activate."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            if (isConfigure) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onCompletedClick()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = loc("Выполнено", "Done"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun MaintenanceSetupDialog(
    preset: MaintenanceCardPreset,
    activeReminder: ReminderEntity?,
    currentMileage: Int,
    onSave: (Int?, Int?) -> Unit,
    onDeactivate: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val currentUnits = LocalAppUnits.current
    var isMileageEnabled by rememberSaveable { mutableStateOf(activeReminder?.dueMileage != null || (activeReminder == null && preset.defaultIntervalMileage != null)) }
    var isTimeEnabled by rememberSaveable { mutableStateOf(activeReminder?.dueDateMillis != null || (activeReminder == null && preset.defaultIntervalMonths != null)) }
    
    var mileageInterval by rememberSaveable { 
        val defaultVal = preset.defaultIntervalMileage ?: 10000
        val displayVal = if (currentUnits == "imperial") Math.round(defaultVal * 0.621371).toInt() else defaultVal
        mutableStateOf(activeReminder?.repeatMileageInterval?.let { if (currentUnits == "imperial") Math.round(it * 0.621371).toInt() else it }?.toString() ?: displayVal.toString()) 
    }
    var timeIntervalMonths by rememberSaveable { 
        mutableStateOf(activeReminder?.repeatIntervalMonths?.toString() ?: (preset.defaultIntervalMonths ?: 12).toString()) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = loc(preset.titleRu, preset.titleEn),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = loc("Настройте периодичность напоминаний для этой задачи.", "Configure the frequency of reminders for this task."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isMileageEnabled,
                                onCheckedChange = { isMileageEnabled = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loc("По пробегу", "By Mileage"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (isMileageEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mileageInterval,
                                onValueChange = { mileageInterval = it.filter(Char::isDigit) },
                                label = { Text(if (currentUnits == "imperial") loc("Каждые X миль", "Every X miles") else loc("Каждые X км", "Every X km")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isTimeEnabled,
                                onCheckedChange = { isTimeEnabled = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loc("По времени", "By Time"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (isTimeEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = timeIntervalMonths,
                                onValueChange = { timeIntervalMonths = it.filter(Char::isDigit) },
                                label = { Text(loc("Каждые X месяцев", "Every X months")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activeReminder != null && onDeactivate != null) {
                    TextButton(
                        onClick = {
                            onDeactivate()
                            onDismiss()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(loc("Отключить", "Deactivate"))
                    }
                }
                
                Button(
                    onClick = {
                        val enteredMileage = if (isMileageEnabled) mileageInterval.toIntOrNull() else null
                        val savedMileage = if (enteredMileage != null && currentUnits == "imperial") {
                            Math.round(enteredMileage / 0.621371).toInt()
                        } else {
                            enteredMileage
                        }
                        val savedMonths = if (isTimeEnabled) timeIntervalMonths.toIntOrNull() else null
                        onSave(savedMileage, savedMonths)
                        onDismiss()
                    },
                    enabled = isMileageEnabled || isTimeEnabled
                ) {
                    Text(loc("Сохранить", "Save"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(loc("Отмена", "Cancel"))
            }
        }
    )
}

@Composable
internal fun AddReminderForm(
    onReminderAdded: (String, String, Int?, Long?, Int?, Int?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(reminderCategoryOptions.first().key) }
    val currentUnits = LocalAppUnits.current
    var isTimeBased by rememberSaveable { mutableStateOf(false) }
    
    var dueMileage by rememberSaveable { mutableStateOf("") }
    var repeatMileage by rememberSaveable { mutableStateOf("") }
    
    var dueDays by rememberSaveable { mutableStateOf("") }
    var repeatMonths by rememberSaveable { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = loc("Новое ручное напоминание", "New Custom Reminder"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(loc("Название события", "Event Name")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminderCategoryOptions.forEach { option ->
                    FilterChip(
                        selected = category == option.key,
                        onClick = { category = option.key },
                        label = { Text(option.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = loc("Тип напоминания:", "Reminder Type:"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isTimeBased,
                    onClick = { isTimeBased = false },
                    label = { Text(loc("По пробегу", "By Mileage")) }
                )
                FilterChip(
                    selected = isTimeBased,
                    onClick = { isTimeBased = true },
                    label = { Text(loc("По времени", "By Time")) }
                )
            }

            if (!isTimeBased) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dueMileage,
                        onValueChange = { dueMileage = it.filter(Char::isDigit) },
                        label = { Text(if (currentUnits == "imperial") loc("Срок на миль", "Due at miles") else loc("Срок на км", "Due at km")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = repeatMileage,
                        onValueChange = { repeatMileage = it.filter(Char::isDigit) },
                        label = { Text(if (currentUnits == "imperial") loc("Повтор, миль", "Repeat, miles") else loc("Повтор, км", "Repeat, km")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dueDays,
                        onValueChange = { dueDays = it.filter(Char::isDigit) },
                        label = { Text(loc("Через дней", "In days")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = repeatMonths,
                        onValueChange = { repeatMonths = it.filter(Char::isDigit) },
                        label = { Text(loc("Повтор каждые мес.", "Repeat months")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            Button(
                onClick = {
                    val date = if (isTimeBased) {
                        dueDays.toIntOrNull()?.let { days ->
                            System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L
                        }
                    } else null

                    val enteredDue = if (!isTimeBased) dueMileage.toIntOrNull() else null
                    val savedDue = if (enteredDue != null && currentUnits == "imperial") {
                        Math.round(enteredDue / 0.621371).toInt()
                    } else {
                        enteredDue
                    }

                    val enteredRepeat = if (!isTimeBased) repeatMileage.toIntOrNull() else null
                    val savedRepeat = if (enteredRepeat != null && currentUnits == "imperial") {
                        Math.round(enteredRepeat / 0.621371).toInt()
                    } else {
                        enteredRepeat
                    }

                    val repeatMonthsVal = if (isTimeBased) repeatMonths.toIntOrNull() else null

                    onReminderAdded(title, category, savedDue, date, savedRepeat, repeatMonthsVal)
                    
                    title = ""
                    dueMileage = ""
                    repeatMileage = ""
                    dueDays = ""
                    repeatMonths = ""
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(loc("Добавить напоминание", "Add reminder"))
            }
        }
    }
}

@Composable
internal fun ReminderRow(reminder: ReminderEntity, car: CarEntity?, modifier: Modifier = Modifier, onChecked: (Boolean) -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onChecked
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminderCategoryTitle(reminder.category),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val repeatIntervals = buildList {
                    reminder.repeatMileageInterval?.takeIf { it > 0 }?.let {
                        add("${loc("повтор каждые", "repeat every")} ${formatMileage(it)}")
                    }
                    reminder.repeatIntervalMonths?.takeIf { it > 0 }?.let {
                        add("${loc("повтор каждые", "repeat every")} $it ${loc("мес.", "months")}")
                    }
                }
                Text(
                    text = listOfNotNull(
                        car?.displayName,
                        reminderDueText(reminder, car),
                        repeatIntervals.takeIf { it.isNotEmpty() }?.joinToString(" & ")
                    ).joinToString(" • "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun ReceiptScannerDialog(
    onReceiptParsed: (FuelReceiptParseResult) -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // CameraX elements
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    // Premium loader states
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            processingStep = if (lang == "en") "Compressing image... 🖼️" else "Сжимаем изображение... 🖼️"
            scope.launch {
                try {
                    val base64 = withContext(Dispatchers.IO) {
                        uriToBase64(context, uri)
                    }
                    if (base64 == null) {
                        throw Exception(if (lang == "en") "Failed to compress image" else "Не удалось сжать изображение")
                    }
                    
                    processingStep = if (lang == "en") "Analyzing receipt with AI... 🧠✨" else "Анализируем чек с помощью ИИ... 🧠✨"
                    val result = withContext(Dispatchers.IO) {
                        ReceiptClient.parseReceiptImageWithYandex(base64Image = base64)
                    }
                    
                    if (result != null) {
                        onReceiptParsed(result)
                    } else {
                        throw Exception(if (lang == "en") "AI returned an empty result. Check document photo." else "ИИ вернул пустой результат. Проверьте фото документа.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val defaultMsg = if (lang == "en") "Unknown error" else "Неизвестная ошибка"
                    val msg = e.localizedMessage ?: defaultMsg
                    val toastPrefix = if (lang == "en") "AI Error: " else "Ошибка ИИ: "
                    android.widget.Toast.makeText(context, "$toastPrefix$msg", android.widget.Toast.LENGTH_LONG).show()
                    isProcessing = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // Camera Preview
                AndroidView(
                    factory = { viewContext ->
                        val previewView = PreviewView(viewContext).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(viewContext))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = loc("Нужен доступ к камере", "Camera access needed"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = loc(
                            "Разрешите доступ к камере, чтобы фотографировать чеки для ИИ-распознавания.",
                            "Please grant camera access to take photos of receipts for AI recognition."
                        ),
                        color = Color(0xFFB8C4C0),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text(loc("Разрешить", "Grant"))
                    }
                }
            }

            // Top Bar with Close button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0x99000000))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF81C784)
                    )
                    Text(
                        text = loc("ИИ-сканирование документов", "AI Document Scanning"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss, enabled = !isProcessing) {
                    Icon(Icons.Outlined.Close, contentDescription = loc("Закрыть", "Close"), tint = Color.White)
                }
            }

            // Camera overlay (guide frame to position the receipt)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 120.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .align(Alignment.Center)
            ) {
                Text(
                    text = loc("Поместите документ или чек в рамку", "Place document or receipt inside the frame"),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Bottom Controller: Camera trigger button and Gallery button
            if (!isProcessing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xCC000000))
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Button
                    androidx.compose.material3.IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = loc("Выбрать из галереи", "Choose from gallery"),
                            tint = Color.White
                        )
                    }

                    // Main Shutter Button
                    if (hasCameraPermission) {
                        Button(
                            onClick = {
                                val capture = imageCapture ?: return@Button
                                isProcessing = true
                                processingStep = if (lang == "en") "Capturing document... 📸" else "Снимаем документ... 📸"
                                
                                val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                
                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            scope.launch {
                                                try {
                                                    processingStep = if (lang == "en") "Compressing image... 🖼️" else "Сжимаем изображение... 🖼️"
                                                    val base64 = withContext(Dispatchers.IO) {
                                                        fileToBase64(photoFile)
                                                    }
                                                    if (base64 == null) {
                                                        throw Exception(if (lang == "en") "Failed to compress image" else "Не удалось сжать изображение")
                                                    }
                                                    
                                                    processingStep = if (lang == "en") "Analyzing receipt with AI... 🧠✨" else "Анализируем чек с помощью ИИ... 🧠✨"
                                                    val result = withContext(Dispatchers.IO) {
                                                        ReceiptClient.parseReceiptImageWithYandex(base64Image = base64)
                                                    }
                                                    
                                                    if (result != null) {
                                                        onReceiptParsed(result)
                                                    } else {
                                                        throw Exception(if (lang == "en") "AI returned an empty result. Check document photo." else "ИИ вернул пустой результат. Проверьте фото документа.")
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    val defaultMsg = if (lang == "en") "Unknown error" else "Неизвестная ошибка"
                                                    val msg = e.localizedMessage ?: defaultMsg
                                                    val toastPrefix = if (lang == "en") "AI Error: " else "Ошибка ИИ: "
                                                    android.widget.Toast.makeText(context, "$toastPrefix$msg", android.widget.Toast.LENGTH_LONG).show()
                                                    isProcessing = false
                                                } finally {
                                                    try { photoFile.delete() } catch (ignored: Exception) {}
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                            val defaultMsg = if (lang == "en") "Unknown camera error" else "Неизвестная ошибка камеры"
                                            val msg = exception.localizedMessage ?: defaultMsg
                                            val toastPrefix = if (lang == "en") "Camera error: " else "Ошибка камеры: "
                                            android.widget.Toast.makeText(context, "$toastPrefix$msg", android.widget.Toast.LENGTH_LONG).show()
                                            isProcessing = false
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = loc("Сфотографировать", "Take Photo"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    } else {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(loc("Разрешить камеру", "Allow Camera"))
                        }
                    }
                }
            }

            // Premium loader screen overlay
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        PremiumScanningLoader()
                        
                        Text(
                            text = processingStep,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Text(
                            text = loc("Обработка может занять несколько секунд...", "Processing may take a few seconds..."),
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

internal fun fileToBase64(file: File): String? {
    return try {
        val originalBitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath) ?: return null
        
        val maxDimension = 1024
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toDouble() / height.toDouble()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1.0) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        originalBitmap.recycle()
        android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
internal fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun reminderDueText(reminder: ReminderEntity, car: CarEntity? = null): String {
    val parts = buildList {
        reminder.dueMileage?.let { dueMileage ->
            val remaining = car?.let { dueMileage - it.mileage }
            add(
                if (remaining != null) {
                    if (remaining >= 0) {
                        "${loc("до", "up to")} ${formatMileage(dueMileage)}, ${loc("осталось", "remaining")} ${formatMileage(remaining)}"
                    } else {
                        "${loc("до", "up to")} ${formatMileage(dueMileage)}, ${loc("просрочено на", "overdue by")} ${formatMileage(-remaining)}"
                    }
                } else {
                    "${loc("до", "up to")} ${formatMileage(dueMileage)}"
                }
            )
        }
        reminder.dueDateMillis?.let { add("${loc("до", "up to")} ${formatDate(it)}") }
    }
    return if (parts.isEmpty()) loc("без срока", "no deadline") else parts.joinToString(" • ")
}

internal fun upcomingReminders(reminders: List<ReminderEntity>, cars: List<CarEntity>): List<ReminderEntity> {
    val now = System.currentTimeMillis()
    val soonDateLimit = now + 14L * 24L * 60L * 60L * 1000L
    return reminders
        .filterNot { it.isCompleted }
        .filter { reminder ->
            val car = cars.firstOrNull { it.id == reminder.carId }
            val dateIsSoon = reminder.dueDateMillis?.let { it <= soonDateLimit } == true
            val mileageIsSoon = car?.let { carEntity ->
                reminder.dueMileage?.let { it <= carEntity.mileage + 1000 } == true
            } == true
            dateIsSoon || mileageIsSoon
        }
        .sortedWith(
            compareBy<ReminderEntity> { reminder ->
                reminder.dueDateMillis ?: Long.MAX_VALUE
            }.thenBy { reminder ->
                reminder.dueMileage ?: Int.MAX_VALUE
            }
        )
}

@Composable
internal fun reminderTickerText(reminder: ReminderEntity, car: CarEntity?): String {
    val now = System.currentTimeMillis()
    val overdueByDate = reminder.dueDateMillis?.let { it < now } == true
    val overdueByMileage = car?.mileage?.let { current ->
        reminder.dueMileage?.let { it <= current } == true
    } == true
    val prefix = if (overdueByDate || overdueByMileage) loc("Просрочено", "Overdue") else loc("Скоро", "Soon")
    return listOfNotNull(
        "$prefix: ${reminder.title}",
        car?.displayName,
        reminderDueText(reminder, car)
    ).joinToString(" • ")
}

@Composable
internal fun reminderCategoryTitle(category: String): String =
    reminderCategoryOptions.firstOrNull { it.key == category }?.title ?: loc("Другое", "Other")

internal fun isCurrentMonth(value: Long): Boolean {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = value }
    return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == date.get(Calendar.MONTH)
}

internal fun monthlyExpensePoints(expenses: List<ExpenseEntity>, lang: String): List<ChartPoint> =
    expenses
        .groupBy { monthKey(it.dateMillis, lang) }
        .map { (key, rows) ->
            val earliestDate = rows.minOfOrNull { it.dateMillis } ?: 0L
            earliestDate to ChartPoint(key, rows.sumOf { it.amount })
        }
        .sortedByDescending { it.first }
        .map { it.second }

internal fun monthlyFuelLiterPoints(expenses: List<ExpenseEntity>, lang: String): List<ChartPoint> =
    expenses
        .groupBy { monthKey(it.dateMillis, lang) }
        .map { (key, rows) ->
            val earliestDate = rows.minOfOrNull { it.dateMillis } ?: 0L
            earliestDate to ChartPoint(key, rows.sumOf { it.fuelLiters ?: 0.0 })
        }
        .filter { it.second.value > 0.0 }
        .sortedByDescending { it.first }
        .map { it.second }

internal fun monthlyAverageFuelPricePoints(expenses: List<ExpenseEntity>, lang: String): List<ChartPoint> =
    expenses
        .filter { it.category == ExpenseCategory.Fuel.name && (it.fuelLiters ?: 0.0) > 0.0 }
        .groupBy { monthKey(it.dateMillis, lang) }
        .map { (key, rows) ->
            val earliestDate = rows.minOfOrNull { it.dateMillis } ?: 0L
            val cost = rows.sumOf { it.amount }
            val liters = rows.sumOf { it.fuelLiters ?: 0.0 }
            val avgPrice = if (liters > 0.0) cost / liters else 0.0
            earliestDate to ChartPoint(key, avgPrice)
        }
        .filter { it.second.value > 0.0 }
        .sortedByDescending { it.first }
        .map { it.second }

internal fun monthlyMileagePoints(expenses: List<ExpenseEntity>, lang: String): List<ChartPoint> {
    val sorted = expenses.filter { it.mileage > 0 }.sortedBy { it.dateMillis }
    if (sorted.size < 2) return emptyList()
    
    val deltas = mutableListOf<Pair<ExpenseEntity, Double>>()
    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val curr = sorted[i]
        val diff = (curr.mileage - prev.mileage).coerceAtLeast(0)
        if (diff > 0) {
            deltas.add(curr to diff.toDouble())
        }
    }
    
    if (deltas.isEmpty()) return emptyList()
    
    return deltas
        .groupBy { monthKey(it.first.dateMillis, lang) }
        .map { (key, rows) ->
            val earliestDate = rows.minOf { it.first.dateMillis }
            earliestDate to ChartPoint(key, rows.sumOf { it.second })
        }
        .sortedByDescending { it.first }
        .map { it.second }
}

internal fun categoryExpensePoints(expenses: List<ExpenseEntity>, lang: String): List<ChartPoint> =
    expenses
        .groupBy { it.category }
        .map { (categoryName, rows) ->
            ChartPoint(ExpenseCategory.fromName(categoryName).getTitle(lang), rows.sumOf { it.amount })
        }
        .filter { it.value > 0.0 }
        .sortedByDescending { it.value }

internal fun monthKey(value: Long, lang: String): String {
    val date = Calendar.getInstance().apply { timeInMillis = value }
    val locale = if (lang == "en") Locale.ENGLISH else Locale("ru", "RU")
    val month = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale).orEmpty()
    return "${date.get(Calendar.YEAR)} ${month.replaceFirstChar { it.titlecase(locale) }}"
}

internal fun calculatePricePerLiter(amount: String, liters: String): Double {
    val parsedAmount = amount.replace(',', '.').toDoubleOrNull() ?: return 0.0
    val parsedLiters = liters.replace(',', '.').toDoubleOrNull() ?: return 0.0
    return if (parsedLiters > 0.0) parsedAmount / parsedLiters else 0.0
}

internal data class ReceiptQrPreview(
    val totalAmount: Double?,
    val dateTime: String?,
    val stationName: String? = null,
    val stationAddress: String? = null,
    val fuelType: String? = null,
    val liters: Double? = null,
    val pricePerLiter: Double? = null
)

internal fun parseReceiptQr(qr: String): ReceiptQrPreview {
    val fields = qr
        .substringAfter('?', qr)
        .split('&')
        .mapNotNull { part ->
            val key = part.substringBefore('=', "").takeIf(String::isNotBlank) ?: return@mapNotNull null
            val value = part.substringAfter('=', "")
            URLDecoder.decode(key, Charsets.UTF_8.name()) to URLDecoder.decode(value, Charsets.UTF_8.name())
        }
        .toMap()
    val textPreview = parseReceiptText(qr)
    return ReceiptQrPreview(
        totalAmount = fields["s"]?.replace(',', '.')?.toDoubleOrNull() ?: textPreview.totalAmount,
        dateTime = fields["t"] ?: textPreview.dateTime,
        stationName = textPreview.stationName,
        stationAddress = textPreview.stationAddress,
        fuelType = textPreview.fuelType,
        liters = textPreview.liters,
        pricePerLiter = textPreview.pricePerLiter
    )
}

internal fun parseReceiptText(text: String): ReceiptQrPreview {
    val fuelTypePattern = Regex("""(?iu)\b(?:АИ|AI)\s*-?\s*(?:92|95|98|100)(?:\s*(?:Prime|Прайм))?\b|\bДТ\b|дизель|diesel""")
    val numberPattern = Regex("""\d+(?:[,.]\d+)?""")
    val lines = text.lineSequence().map(String::trim).filter(String::isNotBlank).toList()
    val joinedText = lines.joinToString(" ")
    val fuelLineIndex = lines.indexOfFirst { fuelTypePattern.containsMatchIn(it) }
    val fuelWindow = if (fuelLineIndex >= 0) {
        lines.drop(fuelLineIndex).take(3).joinToString(" ")
    } else {
        joinedText
    }
    val fuelType = fuelTypePattern.find(fuelWindow)?.value
        ?.replace(Regex("""(?iu)\s+"""), " ")
        ?.replace("AI", "АИ", ignoreCase = true)
    val explicitLiters = Regex("""(?iu)(\d+(?:[,.]\d+)?)\s*(?:л|литр|литра|литров)\b""")
        .find(fuelWindow)
        ?.groupValues
        ?.get(1)
        ?.replace(',', '.')
        ?.toDoubleOrNull()
    val explicitPrice = Regex("""(?iu)(?:цена|стоимость\s*л|руб\s*/\s*л)\D{0,12}(\d+(?:[,.]\d+)?)""")
        .find(fuelWindow)
        ?.groupValues
        ?.get(1)
        ?.replace(',', '.')
        ?.toDoubleOrNull()
    val numbers = fuelType?.let {
        numberPattern.findAll(fuelTypePattern.replace(fuelWindow, " "))
            .mapNotNull { it.value.replace(',', '.').toDoubleOrNull() }
            .filter { it > 0.0 }
            .toList()
    }.orEmpty()
    val estimate = estimateFuelNumbers(numbers)
    val liters = explicitLiters ?: estimate?.liters
    val pricePerLiter = explicitPrice ?: estimate?.pricePerLiter
    val amount = estimate?.amount ?: liters?.let { l -> pricePerLiter?.let { p -> l * p } }
    val stationName = lines.firstOrNull {
        Regex("""(?iu)\bАЗС\b|ЛУКОЙЛ|Газпромнефть|Роснефть|Татнефть|Башнефть|Прайм|Газойл|Азойл""").containsMatchIn(it)
    }
    val address = Regex("""(?iu)((?:[^.]{0,35})(?:область|край|республика|г\.\s*|город\s+)[^|]{0,160}?(?:ул\.|улица|проспект|пр-т|шоссе)[^|]{0,80}?(?:д\.?\s*\S+)?)""")
        .find(joinedText)
        ?.groupValues
        ?.get(1)
        ?.trim()
    return ReceiptQrPreview(
        totalAmount = amount,
        dateTime = null,
        stationName = stationName,
        stationAddress = address,
        fuelType = fuelType,
        liters = liters,
        pricePerLiter = pricePerLiter
    )
}

internal fun parsedReceiptLooksUseful(text: String): Boolean {
    val parsed = parseReceiptText(text)
    return parsed.liters != null || parsed.pricePerLiter != null || parsed.fuelType != null || parsed.stationAddress != null
}

internal data class ParsedFuelNumbers(
    val liters: Double,
    val amount: Double,
    val pricePerLiter: Double?
)

internal fun estimateFuelNumbers(numbers: List<Double>): ParsedFuelNumbers? {
    if (numbers.size < 2) return null
    val candidates = mutableListOf<Pair<Double, ParsedFuelNumbers>>()
    for (liters in numbers.filter { it in 1.0..300.0 }) {
        for (price in numbers.filter { it in 20.0..300.0 && it != liters }) {
            val expectedAmount = liters * price
            val amount = numbers.filter { it >= 100.0 }.minByOrNull { kotlin.math.abs(it - expectedAmount) }
            if (amount != null) {
                val diff = kotlin.math.abs(amount - expectedAmount)
                if (diff <= maxOf(2.0, amount * 0.03)) {
                    candidates += diff to ParsedFuelNumbers(liters, amount, price)
                }
            }
        }
    }
    candidates.minByOrNull { it.first }?.let { return it.second }
    val amount = numbers.maxOrNull()
    val liters = numbers.firstOrNull { it in 1.0..300.0 && it != amount }
    val price = amount?.let { total -> liters?.takeIf { it > 0.0 }?.let { total / it } }
        ?.takeIf { it in 20.0..300.0 }
    return if (amount != null && liters != null) ParsedFuelNumbers(liters, amount, price) else null
}

internal fun normalizeDecimalInput(value: String): String =
    value.filter { it.isDigit() || it == ',' || it == '.' }
        .replace('.', ',')
        .let { cleaned ->
            val firstComma = cleaned.indexOf(',')
            if (firstComma == -1) cleaned
            else cleaned.take(firstComma + 1) + cleaned.drop(firstComma + 1).replace(",", "")
        }

internal fun formatDecimalInput(value: Double): String =
    "%.2f".format(Locale.US, value).replace('.', ',')

internal fun formatOneDecimal(value: Double): String =
    "%.1f".format(Locale.US, value).replace('.', ',')

@Composable
internal fun formatLiters(value: Double): String {
    return if (LocalAppUnits.current == "imperial") {
        val gallons = value * 0.264172
        if (LocalAppLanguage.current == "en") "${formatOneDecimal(gallons)} gal" else "${formatOneDecimal(gallons)} гал."
    } else {
        if (LocalAppLanguage.current == "en") "${formatOneDecimal(value)} L" else "${formatOneDecimal(value)} л"
    }
}

@Composable
internal fun formatConsumption(value: Double): String {
    if (value <= 0.0) {
        return loc("нет данных", "no data")
    }
    return if (LocalAppUnits.current == "imperial") {
        val mpg = 235.214583 / value
        if (LocalAppLanguage.current == "en") "${formatOneDecimal(mpg)} MPG" else "${formatOneDecimal(mpg)} MPG"
    } else {
        if (LocalAppLanguage.current == "en") "${formatOneDecimal(value)} L/100 km" else "${formatOneDecimal(value)} л/100 км"
    }
}

data class CurrencyDetails(
    val code: String,
    val symbol: String,
    val nameRu: String,
    val nameEn: String,
    val isLeftSymbol: Boolean = false
)

val currenciesList = listOf(
    CurrencyDetails("RUB", "₽", "Российский рубль", "Russian Ruble", false),
    CurrencyDetails("BYN", "Br", "Белорусский рубль", "Belarusian Ruble", false),
    CurrencyDetails("KZT", "₸", "Казахстанский тенге", "Kazakhstani Tenge", false),
    CurrencyDetails("KGS", "сом", "Киргизский сом", "Kyrgyzstani Som", false),
    CurrencyDetails("AMD", "֏", "Армянский драм", "Armenian Dram", false),
    CurrencyDetails("AZN", "₼", "Азербайджанский манат", "Azerbaijani Manat", false),
    CurrencyDetails("UZS", "сум", "Узбекский сум", "Uzbekistani Som", false),
    CurrencyDetails("TJS", "смн.", "Таджикский сомони", "Tajikistani Somoni", false),
    CurrencyDetails("MDL", "L", "Молдавский лей", "Moldovan Leu", false),
    CurrencyDetails("UAH", "₴", "Украинская гривна", "Ukrainian Hryvnia", false),
    CurrencyDetails("GEL", "₾", "Грузинский лари", "Georgian Lari", false),
    CurrencyDetails("TMT", "m", "Туркменский манат", "Turkmenistani Manat", false),
    
    CurrencyDetails("USD", "$", "Доллар США", "US Dollar", true),
    CurrencyDetails("EUR", "€", "Евро", "Euro", true),
    CurrencyDetails("GBP", "£", "Фунт стерлингов", "British Pound", true),
    CurrencyDetails("CNY", "¥", "Китайский юань", "Chinese Yuan", true),
    CurrencyDetails("JPY", "¥", "Японская иена", "Japanese Yen", true)
)

@Composable
internal fun CurrencySelectionDialog(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = loc("Выбор валюты", "Select Currency"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currenciesList) { currency ->
                        val isSelected = currency.code == currentCurrency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCurrencySelected(currency.code)
                                    onDismiss()
                                }
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currency.symbol,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (LocalAppLanguage.current == "ru") currency.nameRu else currency.nameEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(loc("Закрыть", "Close"))
            }
        }
    )
}

@Composable
internal fun formatMoney(value: Double): String {
    val currencyCode = LocalAppCurrency.current
    val currency = currenciesList.find { it.code == currencyCode } ?: currenciesList.first()
    val isRu = LocalAppLanguage.current == "ru"
    val formattedValue = String.format(Locale.US, "%,.2f", value).let {
        if (isRu) {
            it.replace(",", " ").replace(".", ",")
        } else {
            it
        }
    }
    return if (currency.isLeftSymbol) {
        "${currency.symbol}$formattedValue"
    } else {
        "$formattedValue ${currency.symbol}"
    }
}

@Composable
internal fun formatAverageFuelPrice(value: Double): String {
    val currentUnits = LocalAppUnits.current
    val currentLang = LocalAppLanguage.current
    val convertedPrice = if (currentUnits == "imperial") value / 0.264172 else value
    val moneyStr = formatMoney(convertedPrice)
    return if (currentUnits == "imperial") {
        if (currentLang == "en") "$moneyStr/gal" else "$moneyStr/гал."
    } else {
        if (currentLang == "en") "$moneyStr/L" else "$moneyStr/л"
    }
}

@Composable
internal fun formatMileage(value: Int): String {
    return if (LocalAppUnits.current == "imperial") {
        val miles = Math.round(value * 0.621371).toInt()
        if (LocalAppLanguage.current == "en") "${NumberFormat.getIntegerInstance(Locale.US).format(miles)} mi" else "${NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(miles)} миль"
    } else {
        if (LocalAppLanguage.current == "en") "${NumberFormat.getIntegerInstance(Locale.US).format(value)} km" else "${NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(value)} км"
    }
}

@Composable
internal fun formatDate(value: Long): String =
    SimpleDateFormat("dd.MM.yyyy", if (LocalAppLanguage.current == "en") Locale.US else Locale("ru", "RU")).format(Date(value))

internal fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        
        val maxDimension = 1024
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toDouble() / height.toDouble()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1.0) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        originalBitmap.recycle()
        android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun PremiumScanningLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    
    // Pulse animation for rings
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale1"
    )
    
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha2"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale2"
    )

    // Scanning line animation (moving up and down)
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Pulsing Rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = size.center
            val maxRadius = size.minDimension / 2
            
            // Ring 1
            drawCircle(
                color = Color(0xFF00FFCC).copy(alpha = pulseAlpha1),
                radius = maxRadius * pulseScale1,
                center = centerPoint,
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Ring 2
            drawCircle(
                color = Color(0xFF00FFCC).copy(alpha = pulseAlpha2),
                radius = maxRadius * pulseScale2,
                center = centerPoint,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Central Icon / Document shape
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF00FFCC).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Document lines inside the receipt
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Mimic lines of a receipt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.width(30.dp).height(4.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(12.dp).height(4.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                }
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.width(20.dp).height(4.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(16.dp).height(4.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                }
                Box(modifier = Modifier.width(28.dp).height(4.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.width(36.dp).height(4.dp).background(Color(0xFF00FFCC).copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
            }

            // Laser scan overlay line moving across the container
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineY = size.height * scanProgress
                // Draw a glowing gradient scan line
                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00FFCC).copy(alpha = 0f),
                        Color(0xFF00FFCC),
                        Color(0xFF00FFCC).copy(alpha = 0f)
                    )
                )
                // Draw the main line
                drawLine(
                    brush = gradientBrush,
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 3.dp.toPx()
                )
                
                // Draw a subtle vertical glow halo behind the line
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FFCC).copy(alpha = 0.15f),
                            Color(0xFF00FFCC).copy(alpha = 0f)
                        )
                    ),
                    topLeft = Offset(0f, lineY - 15.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(size.width, 15.dp.toPx())
                )
            }
        }
    }
}
