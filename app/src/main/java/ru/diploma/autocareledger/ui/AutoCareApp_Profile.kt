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
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Event
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ru.diploma.autocareledger.utils.PdfGenerator
import ru.diploma.autocareledger.utils.UnitsConverter
import android.content.Intent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileDialog(
    uiState: GarageUiState,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    preferredFuelType: String,
    onPreferredFuelTypeChanged: (String) -> Unit,
    onCarSelected: (Long) -> Unit,
    onOpenAddCar: () -> Unit,
    onMileageUpdated: (Int) -> Unit,
    onTankVolumeUpdated: (Double) -> Unit,
    onCarUpdated: (CarEntity) -> Unit,
    onCarArchived: (CarEntity, Boolean) -> Unit,
    onCarDeleted: (CarEntity) -> Unit,
    onProfileSaved: () -> Unit,
    onDismiss: () -> Unit,
    preferredLanguage: String,
    onLanguageChanged: (String) -> Unit,
    preferredUnits: String,
    onUnitsChanged: (String) -> Unit,
    preferredCurrency: String,
    onCurrencyChanged: (String) -> Unit,
    onLogout: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(loc("Профиль", "Profile")) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = loc("Закрыть", "Close"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                AccountScreen(
                    modifier = Modifier.weight(1f),
                    uiState = uiState,
                    themePreference = themePreference,
                    onThemePreferenceChanged = onThemePreferenceChanged,
                    preferredFuelType = preferredFuelType,
                    onPreferredFuelTypeChanged = onPreferredFuelTypeChanged,
                    onCarSelected = onCarSelected,
                    onOpenAddCar = onOpenAddCar,
                    onMileageUpdated = onMileageUpdated,
                    onTankVolumeUpdated = onTankVolumeUpdated,
                    onCarUpdated = onCarUpdated,
                    onCarArchived = onCarArchived,
                    onCarDeleted = onCarDeleted,
                    onProfileSaved = onProfileSaved,
                    preferredLanguage = preferredLanguage,
                    onLanguageChanged = onLanguageChanged,
                    preferredUnits = preferredUnits,
                    onUnitsChanged = onUnitsChanged,
                    preferredCurrency = preferredCurrency,
                    onCurrencyChanged = onCurrencyChanged,
                    onLogout = onLogout
                )
            }
        }
    }
}


@Composable
internal fun AccountScreen(
    modifier: Modifier = Modifier,
    uiState: GarageUiState,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    preferredFuelType: String,
    onPreferredFuelTypeChanged: (String) -> Unit,
    onCarSelected: (Long) -> Unit,
    onOpenAddCar: () -> Unit,
    onMileageUpdated: (Int) -> Unit,
    onTankVolumeUpdated: (Double) -> Unit,
    onCarUpdated: (CarEntity) -> Unit,
    onCarArchived: (CarEntity, Boolean) -> Unit,
    onCarDeleted: (CarEntity) -> Unit,
    onProfileSaved: () -> Unit,
    preferredLanguage: String,
    onLanguageChanged: (String) -> Unit,
    preferredUnits: String,
    onUnitsChanged: (String) -> Unit,
    preferredCurrency: String,
    onCurrencyChanged: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val accountPrefs = remember { context.getSharedPreferences("account_profile", Context.MODE_PRIVATE) }
    var name by rememberSaveable { mutableStateOf(accountPrefs.getString("name", "").orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(accountPrefs.getString("phone", "").orEmpty()) }
    var email by rememberSaveable { mutableStateOf(accountPrefs.getString("email", "").orEmpty()) }
    var avatarUri by rememberSaveable { mutableStateOf(accountPrefs.getString("avatar_uri", "").orEmpty()) }
    var tempAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarSourceDialog by remember { mutableStateOf(false) }

    val avatarPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
        avatarUri = uri?.toString().orEmpty()
    }

    val avatarCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            avatarUri = tempAvatarUri?.toString().orEmpty()
        }
    }
    val currentUnits = LocalAppUnits.current
    var mileage by rememberSaveable(uiState.selectedCar?.id, currentUnits) {
        val rawMileage = uiState.selectedCar?.mileage ?: 0
        val displayedMileage = if (currentUnits == "imperial") {
            Math.round(rawMileage * 0.621371).toInt()
        } else {
            rawMileage
        }
        mutableStateOf(if (rawMileage > 0) displayedMileage.toString() else "")
    }
    var tankVolume by rememberSaveable(uiState.selectedCar?.id, currentUnits) {
        val rawVolume = uiState.selectedCar?.tankVolumeLiters ?: 0.0
        val displayedVolume = if (currentUnits == "imperial") {
            rawVolume * 0.264172
        } else {
            rawVolume
        }
        mutableStateOf(if (rawVolume > 0.0) formatDecimalInput(displayedVolume) else "")
    }
    var editingCar by remember { mutableStateOf<CarEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<CarEntity?>(null) }
    var saved by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = loc("Профиль", "Profile"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showAvatarSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri.isNotBlank()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val firstLetter = name.trim().firstOrNull()?.uppercaseChar()?.toString()
                            ?: email.substringBefore("@").firstOrNull()?.uppercaseChar()?.toString()
                            ?: "A"
                        Text(
                            text = firstLetter,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loc("ИЗМЕНИТЬ", "EDIT"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                if (showAvatarSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showAvatarSourceDialog = false },
                        title = { Text(loc("Аватар профиля", "Profile Avatar")) },
                        text = { Text(loc("Сфотографировать на камеру или выбрать готовое фото из галереи?", "Take a new photo with the camera or choose an existing one from the gallery?")) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showAvatarSourceDialog = false
                                    avatarPhotoPicker.launch("image/*")
                                }
                            ) {
                                Text(loc("Галерея", "Gallery"))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showAvatarSourceDialog = false
                                    runCatching {
                                        val uri = getTmpFileUri(context)
                                        tempAvatarUri = uri
                                        avatarCameraLauncher.launch(uri)
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
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(loc("Имя", "Name")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(loc("Телефон", "Phone")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(loc("Email", "Email")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            accountPrefs.edit()
                                .putString("name", name)
                                .putString("phone", phone)
                                .putString("email", email)
                                .putString("avatar_uri", avatarUri)
                                .apply()
                            onProfileSaved()
                            saved = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(loc("Сохранить данные", "Save Profile Info"))
                    }
                    if (saved) {
                        Text(
                            text = loc("Данные сохранены", "Data saved successfully"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Тема", "Theme"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePreference.entries.forEach { option ->
                            val optionTitle = when(option) {
                                ThemePreference.System -> loc("Системная", "System")
                                ThemePreference.Light -> loc("Светлая", "Light")
                                ThemePreference.Dark -> loc("Темная", "Dark")
                            }
                            FilterChip(
                                selected = themePreference == option,
                                onClick = { onThemePreferenceChanged(option) },
                                label = { Text(optionTitle) }
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Язык", "Language"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferredLanguage == "system",
                            onClick = { onLanguageChanged("system") },
                            label = { Text(loc("Системный", "System")) }
                        )
                        FilterChip(
                            selected = preferredLanguage == "ru",
                            onClick = { onLanguageChanged("ru") },
                            label = { Text("Русский (RU)") }
                        )
                        FilterChip(
                            selected = preferredLanguage == "en",
                            onClick = { onLanguageChanged("en") },
                            label = { Text("English (EN)") }
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Система измерения", "Measurement System"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferredUnits == "metric",
                            onClick = { onUnitsChanged("metric") },
                            label = { Text(loc("Метрическая (км, л)", "Metric (km, L)")) }
                        )
                        FilterChip(
                            selected = preferredUnits == "imperial",
                            onClick = { onUnitsChanged("imperial") },
                            label = { Text(loc("Имперская (миль, гал.)", "Imperial (mi, gal)")) }
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Валюта", "Currency"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = loc(
                            "Выберите валюту для отображения расходов и статистики.",
                            "Select the currency for displaying expenses and statistics."
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    var showCurrencyDialog by remember { mutableStateOf(false) }
                    val currentCurrencyDetails = currenciesList.find { it.code == preferredCurrency } ?: currenciesList.first()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDialog = true }
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentCurrencyDetails.symbol,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentCurrencyDetails.code,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (LocalAppLanguage.current == "ru") currentCurrencyDetails.nameRu else currentCurrencyDetails.nameEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = loc("Выбрать", "Select"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (showCurrencyDialog) {
                        CurrencySelectionDialog(
                            currentCurrency = preferredCurrency,
                            onCurrencySelected = onCurrencyChanged,
                            onDismiss = { showCurrencyDialog = false }
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Топливо", "Fuel Type"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = loc(
                            "Этот тип будет показываться в облачках АЗС, когда цена есть в нашей базе.",
                            "This fuel type will be shown in gas station pins when the price is in our database."
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fuelTypeOptions.forEach { fuelType ->
                            FilterChip(
                                selected = preferredFuelType == fuelType,
                                onClick = { onPreferredFuelTypeChanged(fuelType) },
                                label = { Text(fuelType) }
                            )
                        }
                    }
                }
            }
        }

        uiState.selectedCar?.let { car ->
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = loc("Параметры автомобиля", "Vehicle Parameters"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = car.displayName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val originalMileage = car.mileage
                        val displayedOriginalMileage = if (currentUnits == "imperial") {
                            Math.round(originalMileage * 0.621371).toInt()
                        } else {
                            originalMileage
                        }
                        val isMileageError = mileage.isNotBlank() && (mileage.toIntOrNull() ?: 0) < displayedOriginalMileage
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = mileage,
                                onValueChange = { mileage = it.filter(Char::isDigit) },
                                label = { Text(if (currentUnits == "imperial") loc("Текущий пробег, миль", "Current Mileage, miles") else loc("Текущий пробег, км", "Current Mileage, km")) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = isMileageError,
                                supportingText = if (isMileageError) {
                                    { Text(loc("Не меньше $displayedOriginalMileage", "No less than $displayedOriginalMileage")) }
                                } else null,
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val entered = mileage.toIntOrNull()
                                    if (entered != null) {
                                        val km = if (currentUnits == "imperial") {
                                            Math.round(entered / 0.621371).toInt()
                                        } else {
                                            entered
                                        }
                                        onMileageUpdated(km)
                                    }
                                },
                                enabled = mileage.toIntOrNull() != null && !isMileageError,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text(loc("Обновить", "Update"))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = tankVolume,
                                onValueChange = { tankVolume = normalizeDecimalInput(it) },
                                label = { Text(if (currentUnits == "imperial") loc("Объем бака, галл.", "Tank Volume, gal") else loc("Объем бака, л", "Tank Volume, L")) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val entered = tankVolume.replace(',', '.').toDoubleOrNull()
                                    if (entered != null) {
                                        val liters = if (currentUnits == "imperial") {
                                            entered / 0.264172
                                        } else {
                                            entered
                                        }
                                        onTankVolumeUpdated(liters)
                                    }
                                },
                                enabled = tankVolume.replace(',', '.').toDoubleOrNull() != null,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text(loc("Сохранить", "Save"))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = loc("Экспорт данных", "Data Export"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = loc("Скачайте полную историю обслуживания автомобиля в формате PDF.", "Download the complete vehicle service history in PDF format."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val shareReportText = loc("Поделиться отчетом", "Share Report")
                        Button(
                            onClick = {
                                val expenses = uiState.expenses.filter { it.carId == car.id }
                                val file = PdfGenerator.generateReport(context, car, expenses, currentUnits)
                                if (file != null) {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, shareReportText))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(loc("Сгенерировать PDF", "Generate PDF"))
                        }
                    }
                }
            }
        }
        if (uiState.cars.isNotEmpty()) {
            item {
                Text(
                    text = loc("Машины", "Vehicles"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(uiState.cars, key = { it.id }) { car ->
                AccountCarRow(
                    car = car,
                    selected = car.id == uiState.selectedCar?.id,
                    onClick = { onCarSelected(car.id) },
                    onEdit = { editingCar = car },
                    onArchive = { onCarArchived(car, true) },
                    onDelete = { deleteCandidate = car }
                )
            }
        }
        if (uiState.archivedCars.isNotEmpty()) {
            item {
                Text(
                    text = loc("Архив", "Archive"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(uiState.archivedCars, key = { it.id }) { car ->
                AccountCarRow(
                    car = car,
                    selected = false,
                    onClick = { },
                    onEdit = { editingCar = car },
                    onArchive = { onCarArchived(car, false) },
                    onDelete = { deleteCandidate = car }
                )
            }
        }
        item {
            Button(
                onClick = onOpenAddCar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(loc("Добавить машину", "Add Vehicle"))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = loc("Аккаунт", "Account"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = loc(
                            "Выход из аккаунта очистит локальную базу данных. Перед выходом мы выполним синхронизацию на сервер.",
                            "Logging out will clear the local database. Before logging out, we will sync your data to the server."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    var showConfirmDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(loc("Выйти из аккаунта", "Log Out"))
                    }
                    
                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text(loc("Выйти из аккаунта?", "Log Out?")) },
                            text = { Text(loc("Все локальные данные будут очищены. Вы уверены?", "All local data will be cleared. Are you sure?")) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showConfirmDialog = false
                                        onLogout()
                                    }
                                ) {
                                    Text(loc("Выйти", "Log Out"))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text(loc("Отмена", "Cancel"))
                                }
                            }
                        )
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = { uriHandler.openUri("https://yandex.ru/legal/maps_termsofuse/") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(loc("Условия использования сервиса Яндекс.Карты", "Yandex.Maps Terms of Use"))
            }
        }
    }

    editingCar?.let { car ->
        CarEditDialog(
            car = car,
            onDismiss = { editingCar = null },
            onSave = {
                onCarUpdated(it)
                editingCar = null
            }
        )
    }

    deleteCandidate?.let { car ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(loc("Удалить автомобиль?", "Delete Vehicle?")) },
            text = { Text(loc("${car.displayName} и связанные расходы будут удалены из локальной базы и резервной копии.", "${car.displayName} and related expenses will be deleted from local storage and backup.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCarDeleted(car)
                        deleteCandidate = null
                    }
                ) {
                    Text(loc("Удалить", "Delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text(loc("Отмена", "Cancel"))
                }
            }
        )
    }
}


