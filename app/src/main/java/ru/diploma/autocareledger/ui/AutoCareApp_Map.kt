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


@Composable
internal fun MapScreen(
    darkMapEnabled: Boolean,
    preferredFuelType: String,
    authToken: String
) {
    var selectedCategory by rememberSaveable { mutableStateOf(MapCategory.Gas) }
    var reportedPrices by remember { mutableStateOf<List<GasStationPriceItem>>(emptyList()) }
    LaunchedEffect(authToken) {
        if (authToken.isNotBlank()) {
            val prices = withContext(Dispatchers.IO) {
                GasStationClient.fetchPrices(authToken)
            }
            reportedPrices = prices
        }
    }
    var currentMapCenter by remember { mutableStateOf(Point(55.030204, 82.920430)) }
    var userPoint by remember { mutableStateOf<Point?>(null) }
    var selectedPlace by remember { mutableStateOf<MapPlaceInfo?>(null) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val yandexButtonBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    val yandexButtonTextColor = MaterialTheme.colorScheme.onSurface
    var hasLocationPermission by remember {
        mutableStateOf(hasLocationPermission(context))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    fun openSelectedCategoryInYandexMaps() {
        val query = Uri.encode(selectedCategory.query)
        val longitude = currentMapCenter.longitude
        val latitude = currentMapCenter.latitude
        uriHandler.openUri("https://yandex.ru/maps/?text=$query&ll=$longitude%2C$latitude&z=12")
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            getLastKnownLocationPoint(context)?.let { point ->
                userPoint = point
                currentMapCenter = point
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val point = location.toMapPoint()
                    userPoint = point
                    currentMapCenter = point
                }
            }
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            providers.forEach { provider ->
                runCatching {
                    if (locationManager?.isProviderEnabled(provider) == true) {
                        locationManager.requestLocationUpdates(
                            provider,
                            30_000L,
                            25f,
                            listener,
                            Looper.getMainLooper()
                        )
                    }
                }
            }
            onDispose {
                runCatching { locationManager?.removeUpdates(listener) }
            }
        }
    }

    if (BuildConfig.MAPKIT_API_KEY.isBlank()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = loc("Карта", "Map"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Text(
                    text = loc("Добавьте MAPKIT_API_KEY в local.properties, чтобы включить Yandex Maps.", "Add MAPKIT_API_KEY to local.properties to enable Yandex Maps."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = loc("MAPKIT_API_KEY=ваш_ключ_yandex_mapkit", "MAPKIT_API_KEY=your_yandex_mapkit_key"),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        YandexMapView(
            category = selectedCategory,
            userPoint = userPoint,
            darkMapEnabled = darkMapEnabled,
            preferredFuelType = preferredFuelType,
            reportedPrices = reportedPrices,
            onPlaceSelected = { selectedPlace = it },
            onCenterChanged = { currentMapCenter = it },
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapCategory.entries.forEach { category ->
                MapCategoryPill(
                    category = category,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }
        selectedPlace?.let { place ->
            MapPlaceCard(
                place = place,
                preferredFuelType = preferredFuelType,
                onDismiss = { selectedPlace = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, bottom = 68.dp)
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 12.dp, bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(percent = 50))
                .background(yandexButtonBackground, RoundedCornerShape(percent = 50))
                .clickable(onClick = ::openSelectedCategoryInYandexMaps)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = loc("Открыть в Яндекс.Картах", "Open in Yandex Maps"),
                style = MaterialTheme.typography.bodySmall,
                color = yandexButtonTextColor,
                maxLines = 1
            )
        }
    }
}


@Composable
internal fun ServicePickerScreen(
    darkMapEnabled: Boolean,
    onServiceSelected: (MapPlaceInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlace by remember { mutableStateOf<MapPlaceInfo?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            YandexMapView(
                category = MapCategory.Service,
                userPoint = null,
                darkMapEnabled = darkMapEnabled,
                preferredFuelType = "",
                reportedPrices = emptyList(),
                onPlaceSelected = { selectedPlace = it },
                onCenterChanged = {},
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .shadow(1.dp, RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f), RoundedCornerShape(percent = 50))
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loc("Выберите автосервис", "Select Auto Service"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                }
            }
            selectedPlace?.let { place ->
                MapPlaceCard(
                    place = place,
                    preferredFuelType = "",
                    actionText = loc("Выбрать", "Select"),
                    onAction = { onServiceSelected(place) },
                    onDismiss = { selectedPlace = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(12.dp)
                )
            }
        }
    }
}


@Composable
internal fun GasStationPickerScreen(
    darkMapEnabled: Boolean,
    preferredFuelType: String,
    authToken: String,
    onStationSelected: (MapPlaceInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlace by remember { mutableStateOf<MapPlaceInfo?>(null) }
    var reportedPrices by remember { mutableStateOf<List<GasStationPriceItem>>(emptyList()) }
    LaunchedEffect(authToken) {
        if (authToken.isNotBlank()) {
            val prices = withContext(Dispatchers.IO) {
                GasStationClient.fetchPrices(authToken)
            }
            reportedPrices = prices
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            YandexMapView(
                category = MapCategory.Gas,
                userPoint = null,
                darkMapEnabled = darkMapEnabled,
                preferredFuelType = preferredFuelType,
                reportedPrices = reportedPrices,
                onPlaceSelected = { selectedPlace = it },
                onCenterChanged = {},
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .shadow(1.dp, RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f), RoundedCornerShape(percent = 50))
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loc("Выберите АЗС", "Select Gas Station"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                }
            }
            selectedPlace?.let { place ->
                MapPlaceCard(
                    place = place,
                    preferredFuelType = preferredFuelType,
                    actionText = loc("Выбрать", "Select"),
                    onAction = { onStationSelected(place) },
                    onDismiss = { selectedPlace = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(12.dp)
                )
            }
        }
    }
}


@Composable
internal fun MapPlaceCard(
    place: MapPlaceInfo,
    preferredFuelType: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp, start = 18.dp, end = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = place.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    place.category?.let {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                place.address?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                place.workingHours?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (place.fuelPrices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        place.fuelPrices.forEach { price ->
                            FuelPricePill(
                                price = price,
                                selected = price.fuelType.equals(preferredFuelType, ignoreCase = true),
                                modifier = Modifier.width(128.dp)
                            )
                        }
                    }
                    Text(
                        text = loc("Демонстрация: цены из будущей базы чеков", "Demo: prices from future receipt database"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                } else if (place.category?.contains("АЗС", ignoreCase = true) == true ||
                    place.category?.contains("заправ", ignoreCase = true) == true ||
                    place.category?.contains("Gas", ignoreCase = true) == true
                ) {
                    Text(
                        text = loc("Цены пока не добавлены из чеков", "Prices not added from receipts yet"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
internal fun MapCategoryPill(
    category: MapCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .shadow(1.dp, RoundedCornerShape(percent = 50))
            .background(backgroundColor, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.title,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}


@Composable
internal fun FuelPricePill(
    price: DemoFuelPrice,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val borderStroke = if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }
    val labelColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = price.fuelType,
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = price.price,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}


@Composable
internal fun YandexMapView(
    category: MapCategory,
    userPoint: Point?,
    darkMapEnabled: Boolean,
    preferredFuelType: String,
    reportedPrices: List<GasStationPriceItem> = emptyList(),
    onPlaceSelected: (MapPlaceInfo) -> Unit,
    onCenterChanged: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fallbackPoint = remember { Point(55.030204, 82.920430) }
    val searchManager = remember { SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE) }
    var searchCenter by remember { mutableStateOf(userPoint ?: fallbackPoint) }
    var searchGeometry by remember { mutableStateOf(Geometry.fromPoint(userPoint ?: fallbackPoint)) }
    var centeredOnUser by remember { mutableStateOf(false) }
    var mapObjects by remember { mutableStateOf<MapObjectCollection?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val cameraListener = remember {
        object : CameraListener {
            override fun onCameraPositionChanged(
                map: YandexMap,
                cameraPosition: CameraPosition,
                cameraUpdateReason: CameraUpdateReason,
                finished: Boolean
            ) {
                if (finished) {
                    searchCenter = cameraPosition.target
                    searchGeometry = runCatching {
                        visibleRegionToSearchGeometry(map.getVisibleRegion())
                    }.getOrDefault(Geometry.fromPoint(cameraPosition.target))
                    onCenterChanged(cameraPosition.target)
                }
            }
        }
    }

    DisposableEffect(mapViewRef) {
        val mapView = mapViewRef
        val map = mapView?.mapWindow?.map
        map?.addCameraListener(cameraListener)
        mapView?.onStart()
        onDispose {
            map?.removeCameraListener(cameraListener)
            mapView?.onStop()
        }
    }

    LaunchedEffect(darkMapEnabled, mapViewRef) {
        mapViewRef?.mapWindow?.map?.setNightModeEnabled(darkMapEnabled)
    }

    LaunchedEffect(userPoint, mapViewRef) {
        val point = userPoint ?: return@LaunchedEffect
        val mapView = mapViewRef ?: return@LaunchedEffect
        if (!centeredOnUser) {
            centeredOnUser = true
            searchCenter = point
            searchGeometry = Geometry.fromPoint(point)
            onCenterChanged(point)
            mapView.mapWindow.map.move(
                CameraPosition(point, 14.5f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0.45f),
                null
            )
        }
    }

    LaunchedEffect(category, searchGeometry, mapObjects, reportedPrices) {
        val objects = mapObjects ?: return@LaunchedEffect
        runMapSearch(
            context = context,
            searchManager = searchManager,
            mapObjects = objects,
            geometry = searchGeometry,
            query = category.query,
            darkMapEnabled = darkMapEnabled,
            preferredFuelType = preferredFuelType,
            reportedPrices = reportedPrices,
            onPlaceSelected = onPlaceSelected
        )
    }

    AndroidView(
        factory = { viewContext ->
            val startPoint = userPoint ?: fallbackPoint
            MapView(viewContext).also { mapView ->
                mapViewRef = mapView
                mapObjects = mapView.mapWindow.map.mapObjects
                mapView.mapWindow.map.setNightModeEnabled(darkMapEnabled)
                searchCenter = startPoint
                searchGeometry = Geometry.fromPoint(startPoint)
                onCenterChanged(startPoint)
                mapView.mapWindow.map.move(CameraPosition(startPoint, 13.0f, 0.0f, 0.0f))
            }
        },
        modifier = modifier
    )
}


