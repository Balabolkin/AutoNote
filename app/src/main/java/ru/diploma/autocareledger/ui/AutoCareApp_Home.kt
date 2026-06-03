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
internal fun HomeScreen(
    uiState: GarageUiState,
    profileName: String,
    profileEmail: String,
    profileAvatarUri: String,
    onOpenProfile: () -> Unit,
    onCarSelected: (Long) -> Unit,
    onOpenReminders: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenGallery: () -> Unit,
    onAddFuel: () -> Unit,
    onAddOtherExpense: () -> Unit,
    onShareCar: (Long) -> Unit
) {
    val selectedCar = uiState.selectedCar
    val urgentReminders = upcomingReminders(uiState.allReminders, uiState.cars)
    val topReminder = urgentReminders.firstOrNull()
    val monthExpenses = uiState.expenses.filter { isCurrentMonth(it.dateMillis) }
    val monthTotal = monthExpenses.sumOf { it.amount }
    val monthFuel = monthExpenses
        .filter { it.category == ExpenseCategory.Fuel.name }
        .sumOf { it.amount }
    val monthFuelLiters = monthExpenses
        .filter { it.category == ExpenseCategory.Fuel.name }
        .sumOf { it.fuelLiters ?: 0.0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ProfileHeader(
                        name = profileName,
                        email = profileEmail,
                        avatarUri = profileAvatarUri,
                        carName = selectedCar?.displayName,
                        onClick = onOpenProfile,
                        modifier = Modifier.weight(1f)
                    )
                    ReminderHeaderButton(
                        urgentCount = urgentReminders.size,
                        activeCount = uiState.activeReminders,
                        onClick = onOpenReminders
                    )
                }
                ReminderTicker(
                    reminder = topReminder,
                    car = topReminder?.let { reminder -> uiState.cars.firstOrNull { it.id == reminder.carId } },
                    onClick = onOpenReminders
                )
            }
        }
        item {
            CarHeroPager(
                cars = uiState.cars,
                selectedCar = selectedCar,
                onCarSelected = onCarSelected,
                onShareCar = onShareCar
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        title = loc("За месяц", "This Month"),
                        value = formatMoney(monthTotal),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = loc("Топливо", "Fuel"),
                        value = formatMoney(monthFuel),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        title = loc("На 1 км", "Per 1 km"),
                        value = formatMoney(uiState.costPerKilometer),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = loc("Расход", "Consumption"),
                        value = formatConsumption(uiState.averageFuelConsumption),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        title = loc("Литры за месяц", "Liters/Month"),
                        value = formatLiters(monthFuelLiters),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = loc("Объем бака", "Tank Volume"),
                        value = selectedCar?.tankVolumeLiters?.takeIf { it > 0.0 }?.let { formatLiters(it) } ?: loc("не задан", "not set"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val isEnabled = selectedCar != null
                val aiButtonBrush = Brush.linearGradient(
                    colors = if (isEnabled) {
                        listOf(
                            Color(0xFF6366F1), // Neon Indigo
                            Color(0xFFEC4899), // Neon Pink
                            Color(0xFF3B82F6)  // Neon Blue
                        )
                    } else {
                        val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        listOf(disabledColor, disabledColor)
                    }
                )

                Button(
                    onClick = onOpenQrScanner,
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            brush = aiButtonBrush,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .then(
                            if (isEnabled) {
                                Modifier.shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    clip = false,
                                    ambientColor = Color(0xFF6366F1),
                                    spotColor = Color(0xFFEC4899)
                                )
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = loc("ИИ Камера (Авто-сканирование)", "AI Camera (Auto-Scan)"),
                            color = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAddFuel,
                        enabled = selectedCar != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.LocalGasStation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc("Заправка", "Fueling"))
                    }
                    Button(
                        onClick = onAddOtherExpense,
                        enabled = selectedCar != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc("Трата", "Expense"))
                    }
                }
            }
        }
        item {
            MinimalRecentExpenses(expenses = uiState.expenses.take(3))
        }
    }
}


@Composable
internal fun RestoreStatusScreen(
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (actionText != null && onAction != null) {
                    Button(onClick = onAction) {
                        Text(actionText)
                    }
                }
            }
        }
    }
}


@Composable
internal fun StatisticsScreen(uiState: GarageUiState) {
    val selectedCar = uiState.selectedCar
    if (selectedCar == null) {
        EmptyState(loc("Сначала добавьте автомобиль", "Add a vehicle first"))
        return
    }

    val lang = LocalAppLanguage.current
    var timeFilter by rememberSaveable { mutableStateOf("all") }
    var chartStyle by rememberSaveable { mutableStateOf("bars") }

    val carExpenses = remember(uiState.expenses, selectedCar.id) {
        uiState.expenses.filter { it.carId == selectedCar.id }
    }

    val filteredExpenses = remember(carExpenses, timeFilter) {
        if (timeFilter == "all") {
            carExpenses
        } else {
            val now = System.currentTimeMillis()
            val cutoff = when (timeFilter) {
                "year" -> now - 365L * 24 * 60 * 60 * 1000
                "half_year" -> now - 182L * 24 * 60 * 60 * 1000
                "month" -> now - 30L * 24 * 60 * 60 * 1000
                else -> 0L
            }
            carExpenses.filter { it.dateMillis >= cutoff }
        }
    }

    val totalSpent = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }
    val fuelExpenses = remember(filteredExpenses) { filteredExpenses.filter { it.category == ExpenseCategory.Fuel.name } }
    val totalFuelCost = remember(fuelExpenses) { fuelExpenses.sumOf { it.amount } }
    val totalFuelLiters = remember(fuelExpenses) { fuelExpenses.sumOf { it.fuelLiters ?: 0.0 } }
    val tankVolume = selectedCar.tankVolumeLiters

    val averageFuelPrice = remember(totalFuelCost, totalFuelLiters) {
        if (totalFuelLiters > 0.0) totalFuelCost / totalFuelLiters else 0.0
    }

    val averageFuelConsumption = remember(filteredExpenses) {
        calculateLocalAverageConsumption(filteredExpenses)
    }

    val mileageRecords = remember(filteredExpenses) {
        filteredExpenses.filter { it.mileage > 0 }.map { it.mileage }
    }
    val mileageDelta = remember(mileageRecords) {
        if (mileageRecords.size >= 2) {
            (mileageRecords.maxOrNull()!! - mileageRecords.minOrNull()!!).coerceAtLeast(0)
        } else {
            0
        }
    }

    val costPerKm = remember(totalSpent, mileageDelta, selectedCar.mileage) {
        if (mileageDelta > 0) {
            totalSpent / mileageDelta
        } else if (selectedCar.mileage > 0) {
            totalSpent / selectedCar.mileage
        } else {
            0.0
        }
    }

    val averageFuelCheck = remember(fuelExpenses, totalFuelCost) {
        if (fuelExpenses.isNotEmpty()) totalFuelCost / fuelExpenses.size else 0.0
    }

    val periodDays = remember(filteredExpenses) {
        if (filteredExpenses.size < 2) 1
        else {
            val minDate = filteredExpenses.minOf { it.dateMillis }
            val maxDate = filteredExpenses.maxOf { it.dateMillis }
            val diffDays = (maxDate - minDate) / (1000 * 60 * 60 * 24)
            diffDays.coerceAtLeast(1)
        }
    }

    val avgDaysBetweenFills = remember(fuelExpenses, periodDays) {
        if (fuelExpenses.size >= 2) periodDays.toDouble() / (fuelExpenses.size - 1) else 0.0
    }
    val avgMileageBetweenFills = remember(fuelExpenses, mileageDelta) {
        if (fuelExpenses.size >= 2 && mileageDelta > 0) mileageDelta.toDouble() / (fuelExpenses.size - 1) else 0.0
    }

    val now = System.currentTimeMillis()
    val sixMonthsAgo = now - 182L * 24 * 60 * 60 * 1000
    val recentExpenses = remember(carExpenses) {
        carExpenses.filter { it.dateMillis >= sixMonthsAgo }
    }
    val recentSpent = remember(recentExpenses) { recentExpenses.sumOf { it.amount } }
    val recentDays = remember(recentExpenses) {
        if (recentExpenses.size < 2) 182
        else {
            val minDate = recentExpenses.minOf { it.dateMillis }
            val maxDate = recentExpenses.maxOf { it.dateMillis }
            ((maxDate - minDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        }
    }

    val avgCostPerMonth = remember(recentSpent, recentDays) {
        recentSpent / recentDays * 30.4375
    }
    val projectedAnnualCost = remember(recentSpent, recentDays) {
        recentSpent / recentDays * 365.25
    }

    val periodLabel = when (timeFilter) {
        "year" -> loc("За последние 12 месяцев", "Last 12 Months")
        "half_year" -> loc("За последние 6 месяцев", "Last 6 Months")
        "month" -> loc("За последние 30 дней", "Last 30 Days")
        else -> loc("За все время", "All Time")
    }

    val monthlyExpenseData = remember(filteredExpenses, lang) {
        monthlyExpensePoints(filteredExpenses, lang)
    }
    val monthlyAvgPriceData = remember(fuelExpenses, lang) {
        monthlyAverageFuelPricePoints(fuelExpenses, lang)
    }
    val monthlyMileageData = remember(filteredExpenses, lang) {
        monthlyMileagePoints(filteredExpenses, lang)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = loc("Статистика", "Statistics"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = selectedCar.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = timeFilter == "all",
                    onClick = { timeFilter = "all" },
                    label = { Text(loc("Все время", "All Time")) }
                )
                FilterChip(
                    selected = timeFilter == "year",
                    onClick = { timeFilter = "year" },
                    label = { Text(loc("12 мес.", "12 Months")) }
                )
                FilterChip(
                    selected = timeFilter == "half_year",
                    onClick = { timeFilter = "half_year" },
                    label = { Text(loc("6 мес.", "6 Months")) }
                )
                FilterChip(
                    selected = timeFilter == "month",
                    onClick = { timeFilter = "month" },
                    label = { Text(loc("30 дней", "30 Days")) }
                )
            }
        }

        item {
            StatisticsHeroCard(
                totalSpent = totalSpent,
                periodLabel = periodLabel,
                filteredExpenses = filteredExpenses
            )
        }

        item {
            StatisticsMetricsGrid(
                fuelCost = totalFuelCost,
                fuelLiters = totalFuelLiters,
                averageFuelPrice = averageFuelPrice,
                averageFuelConsumption = averageFuelConsumption,
                costPerKm = costPerKm,
                averageFuelCheck = averageFuelCheck,
                periodMileage = mileageDelta,
                totalLogs = filteredExpenses.size
            )
        }

        item {
            ExpenseShareBreakdownCard(expenses = filteredExpenses)
        }

        item {
            AdvancedMetricsCard(
                avgDaysBetweenFills = avgDaysBetweenFills,
                avgMileageBetweenFills = avgMileageBetweenFills,
                avgCostPerMonth = avgCostPerMonth,
                projectedAnnualCost = projectedAnnualCost
            )
        }

        item {
            AiAnalyticsCard(
                car = selectedCar,
                avgFuelConsumption = averageFuelConsumption,
                totalSpent = totalSpent,
                expenses = carExpenses
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loc("Визуализация", "Visual Style"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chartStyle == "bars",
                        onClick = { chartStyle = "bars" },
                        label = { Text(loc("Столбцы", "Bars")) }
                    )
                    FilterChip(
                        selected = chartStyle == "line",
                        onClick = { chartStyle = "line" },
                        label = { Text(loc("Тренд", "Trend")) }
                    )
                }
            }
        }

        item {
            if (chartStyle == "line") {
                LineChartCard(
                    title = loc("Расходы по месяцам", "Monthly Expenses"),
                    points = monthlyExpenseData
                )
            } else {
                BarChartCard(
                    title = loc("Расходы по месяцам", "Monthly Expenses"),
                    points = monthlyExpenseData
                )
            }
        }

        item {
            if (chartStyle == "line") {
                LineChartCard(
                    title = loc("Средняя цена литра по месяцам", "Monthly Avg Price per Liter"),
                    points = monthlyAvgPriceData,
                    valueFormatter = { formatAverageFuelPrice(it) }
                )
            } else {
                BarChartCard(
                    title = loc("Средняя цена литра по месяцам", "Monthly Avg Price per Liter"),
                    points = monthlyAvgPriceData,
                    valueFormatter = { formatAverageFuelPrice(it) }
                )
            }
        }

        item {
            if (chartStyle == "line") {
                LineChartCard(
                    title = loc("Пробег по месяцам", "Monthly Mileage"),
                    points = monthlyMileageData,
                    valueFormatter = { formatMileage(it.toInt()) }
                )
            } else {
                BarChartCard(
                    title = loc("Пробег по месяцам", "Monthly Mileage"),
                    points = monthlyMileageData,
                    valueFormatter = { formatMileage(it.toInt()) }
                )
            }
        }


    }
}


