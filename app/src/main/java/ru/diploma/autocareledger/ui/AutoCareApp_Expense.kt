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
internal fun AddExpenseForm(
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
    showScanButton: Boolean,
    onExpenseAdded: (ExpenseCategory, Double, Double?, Int, String, String, Double?, Double?, String?, String?, String?, String?, String?) -> Unit,
    pendingAiReceiptResult: FuelReceiptParseResult? = null,
    onClearPendingAiReceiptResult: () -> Unit = {}
) {
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val currentUnits = LocalAppUnits.current
    var selectedCategory by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    var amount by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.amount?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var fuelLiters by rememberSaveable(editingExpense, currentUnits) {
        val rawLiters = editingExpense?.fuelLiters
        val displayedLiters = if (rawLiters != null && currentUnits == "imperial") rawLiters * 0.264172 else rawLiters
        mutableStateOf(displayedLiters?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else "%.2f".format(Locale.US, it).replace('.', ',') } ?: "")
    }
    var mileage by rememberSaveable(editingExpense, currentUnits) {
        val rawMileage = editingExpense?.mileage ?: currentMileage
        val displayedMileage = if (currentUnits == "imperial") Math.round(rawMileage * 0.621371).toInt() else rawMileage
        mutableStateOf(displayedMileage.toString())
    }
    var title by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.title ?: "") }
    var notes by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.notes ?: "") }
    
    var workCost by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.workCost?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var partsCost by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.partsCost?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var shopName by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.shopName ?: "") }
    var partName by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.partName ?: "") }
    var partNumber by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.partNumber ?: "") }
    var partBrand by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.partBrand ?: "") }
    var assembly by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.assembly ?: "") }

    var showWorkCost by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.workCost != null) }
    var showPartsCost by rememberSaveable(editingExpense) { mutableStateOf(editingExpense?.partsCost != null) }
    var showShopName by rememberSaveable(editingExpense) { mutableStateOf(!editingExpense?.shopName.isNullOrBlank()) }
    var showAssembly by rememberSaveable(editingExpense) { mutableStateOf(!editingExpense?.assembly.isNullOrBlank()) }
    var showPartName by rememberSaveable(editingExpense) { mutableStateOf(!editingExpense?.partName.isNullOrBlank()) }
    var showPartBrand by rememberSaveable(editingExpense) { mutableStateOf(!editingExpense?.partBrand.isNullOrBlank()) }
    var showPartNumber by rememberSaveable(editingExpense) { mutableStateOf(!editingExpense?.partNumber.isNullOrBlank()) }

    var fieldMenuExpanded by remember { mutableStateOf(false) }

    var receiptFuelType by rememberSaveable { mutableStateOf<String?>(null) }
    var receiptStationAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var receiptParseHint by rememberSaveable { mutableStateOf<String?>(null) }
    var appliedReceiptQr by rememberSaveable { mutableStateOf<String?>(null) }
    var appliedReceiptText by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStation by remember { mutableStateOf<MapPlaceInfo?>(null) }
    var stationPickerOpen by rememberSaveable { mutableStateOf(false) }
    var servicePickerOpen by rememberSaveable { mutableStateOf(false) }
    var selectedFuelType by rememberSaveable { mutableStateOf(preferredFuelType) }

    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val searchManager = remember { SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE) }

    fun triggerGasStationSearch(name: String?, address: String?) {
        val searchTerms = listOfNotNull(
            name?.takeIf { it.isNotBlank() },
            address?.takeIf { it.isNotBlank() }
        )
        if (searchTerms.isEmpty()) return

        val combinedQuery = searchTerms.joinToString(" ")
        val finalQuery = if (!combinedQuery.contains("АЗС", ignoreCase = true) && !combinedQuery.contains("заправка", ignoreCase = true)) {
            "АЗС $combinedQuery"
        } else {
            combinedQuery
        }

        val searchCenter = getLastKnownLocationPoint(context) ?: Point(55.030204, 82.920430)

        runCatching {
            searchManager.submit(
                finalQuery,
                Geometry.fromPoint(searchCenter),
                SearchOptions().setSearchTypes(SearchType.BIZ.value),
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val geoObject = response.collection.children.firstOrNull()?.obj ?: return
                        val point = geoObject.geometry.firstOrNull()?.point ?: return
                        val place = geoObject.toMapPlaceInfo(
                            point = point,
                            allowDemoFuelPrices = false
                        )
                        selectedStation = place
                    }

                    override fun onSearchError(error: Error) {
                        // Silent fallback
                    }
                }
            )
        }
    }

    LaunchedEffect(pendingAiReceiptResult) {
        val result = pendingAiReceiptResult ?: return@LaunchedEffect
        val parsedCategory = result.category?.let { ExpenseCategory.fromName(it) } ?: ExpenseCategory.Fuel
        selectedCategory = parsedCategory
        
        val parsedLiters = result.liters
        val parsedPricePerLiter = result.pricePerLiter
        val parsedAmount = result.totalAmount
            ?: parsedLiters?.let { liters -> parsedPricePerLiter?.let { price -> liters * price } }

        amount = parsedAmount?.let(::formatDecimalInput) ?: amount
        
        if (parsedCategory == ExpenseCategory.Fuel) {
            val displayedLiters = if (parsedLiters != null && currentUnits == "imperial") {
                parsedLiters * 0.264172
            } else {
                parsedLiters
            }
            fuelLiters = displayedLiters?.let(::formatDecimalInput) ?: fuelLiters
            receiptFuelType = result.fuelType
            result.fuelType?.let { selectedFuelType = it }
            receiptStationAddress = result.stationAddress
            title = when {
                !result.stationName.isNullOrBlank() -> if (lang == "en") "Fueling ${result.stationName}" else "Заправка ${result.stationName}"
                !result.stationAddress.isNullOrBlank() -> if (lang == "en") "Fueling, ${result.stationAddress}" else "Заправка, ${result.stationAddress}"
                title.isBlank() || title == ExpenseCategory.Fuel.getTitle(lang) -> if (lang == "en") "Refueling by photo" else "Заправка по фото"
                else -> title
            }
            triggerGasStationSearch(result.stationName, result.stationAddress)
        } else {
            title = result.expenseTitle ?: parsedCategory.getTitle(lang)
            notes = result.expenseNotes.orEmpty()
        }
        
        receiptParseHint = if (lang == "en") "Data successfully recognized by AI from receipt/document photo! ✨" else "Данные успешно распознаны ИИ из фото чека/документа! ✨"
        onClearPendingAiReceiptResult()
    }

    LaunchedEffect(pendingReceiptQr) {
        val qr = pendingReceiptQr?.takeIf { it.isNotBlank() && it != appliedReceiptQr } ?: return@LaunchedEffect
        val localParsed = parseReceiptQr(qr)
        val parsed = runCatching {
            authToken.takeIf(String::isNotBlank)
                ?.let { token -> withContext(Dispatchers.IO) { ReceiptClient.parseFuelReceipt(token, qr) } }
        }.getOrNull()
        selectedCategory = ExpenseCategory.Fuel
        val parsedLiters = parsed?.liters ?: localParsed.liters
        val parsedPricePerLiter = parsed?.pricePerLiter ?: localParsed.pricePerLiter
        val parsedAmount = parsed?.totalAmount ?: localParsed.totalAmount
            ?: parsedLiters?.let { liters -> parsedPricePerLiter?.let { price -> liters * price } }
        amount = parsedAmount?.let(::formatDecimalInput) ?: amount
        val displayedLiters = if (parsedLiters != null && currentUnits == "imperial") {
            parsedLiters * 0.264172
        } else {
            parsedLiters
        }
        fuelLiters = displayedLiters?.let(::formatDecimalInput) ?: fuelLiters
        receiptFuelType = parsed?.fuelType ?: localParsed.fuelType
        (parsed?.fuelType ?: localParsed.fuelType)?.let { selectedFuelType = it }
        receiptStationAddress = parsed?.stationAddress ?: localParsed.stationAddress
        val stationName = parsed?.stationName ?: localParsed.stationName
        val stationAddress = parsed?.stationAddress ?: localParsed.stationAddress
        title = when {
            !stationName.isNullOrBlank() -> if (lang == "en") "Fueling $stationName" else "Заправка $stationName"
            !stationAddress.isNullOrBlank() -> if (lang == "en") "Fueling, $stationAddress" else "Заправка, $stationAddress"
            title.isBlank() || title == ExpenseCategory.Fuel.getTitle(lang) -> if (lang == "en") "Refueling by receipt" else "Заправка по чеку"
            else -> title
        }
        receiptParseHint = when {
            parsed?.needsReceiptDetails == true -> if (lang == "en") "Amount filled from QR. For liters and fuel type, switch to text scanning." else "Из QR подставлена сумма. Для литров и типа топлива переключитесь на сканирование текста."
            parsed?.liters != null || parsed?.fuelType != null || parsed?.stationAddress != null -> if (lang == "en") "Receipt data recognized and filled." else "Данные чека распознаны и подставлены."
            localParsed.totalAmount != null -> if (lang == "en") "Amount filled from QR. Remaining fields can be filled by text scanning." else "Из QR подставлена сумма. Остальные поля можно взять сканированием текста."
            else -> null
        }
        appliedReceiptQr = qr
        triggerGasStationSearch(stationName, stationAddress)
    }

    LaunchedEffect(pendingReceiptText) {
        val text = pendingReceiptText?.takeIf { it.isNotBlank() && it != appliedReceiptText } ?: return@LaunchedEffect

        var usedYandex = false
        var yandexError: String? = null
        val result = runCatching {
            withContext(Dispatchers.IO) {
                ReceiptClient.parseReceiptTextWithYandexGPT(text = text)
            }
        }
        val yandexResult = result.getOrNull()
        val parsed = if (yandexResult != null) {
            usedYandex = true
            ReceiptQrPreview(
                totalAmount = yandexResult.totalAmount,
                dateTime = yandexResult.dateTime,
                stationName = yandexResult.stationName,
                stationAddress = yandexResult.stationAddress,
                fuelType = yandexResult.fuelType,
                liters = yandexResult.liters,
                pricePerLiter = yandexResult.pricePerLiter
            )
        } else {
            yandexError = result.exceptionOrNull()?.localizedMessage
            parseReceiptText(text)
        }

        val parsedAmount = parsed.totalAmount
            ?: parsed.liters?.let { liters -> parsed.pricePerLiter?.let { price -> liters * price } }
        selectedCategory = ExpenseCategory.Fuel
        amount = parsedAmount?.let(::formatDecimalInput) ?: amount
        val displayedLiters = if (parsed.liters != null && currentUnits == "imperial") {
            parsed.liters * 0.264172
        } else {
            parsed.liters
        }
        fuelLiters = displayedLiters?.let(::formatDecimalInput) ?: fuelLiters
        receiptFuelType = parsed.fuelType
        parsed.fuelType?.let { selectedFuelType = it }
        receiptStationAddress = parsed.stationAddress
        title = when {
            !parsed.stationName.isNullOrBlank() -> if (lang == "en") "Fueling ${parsed.stationName}" else "Заправка ${parsed.stationName}"
            !parsed.stationAddress.isNullOrBlank() -> if (lang == "en") "Fueling, ${parsed.stationAddress}" else "Заправка, ${parsed.stationAddress}"
            title.isBlank() || title == ExpenseCategory.Fuel.getTitle(lang) -> if (lang == "en") "Refueling by receipt" else "Заправка по чеку"
            else -> title
        }
        receiptParseHint = if (usedYandex) {
            if (lang == "en") "Data successfully extracted and filled by AI ✨" else "Данные успешно извлечены и подставлены ИИ ✨"
        } else if (yandexError != null) {
            if (lang == "en") "AI Error: $yandexError. Local parser used." else "Ошибка ИИ: $yandexError. Использован локальный парсер."
        } else if (parsed.liters != null || parsed.pricePerLiter != null || parsed.fuelType != null) {
            if (lang == "en") "Receipt text recognized and filled." else "Текст чека распознан и подставлен."
        } else {
            if (lang == "en") "Text recognized, but fuel line should be checked manually." else "Текст распознан, но строку топлива нужно проверить вручную."
        }
        
        if (yandexError != null) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, if (lang == "en") "AI: $yandexError. Falling back to local parser." else "ИИ: $yandexError. Переход на локальный парсер.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
        
        appliedReceiptText = text
        triggerGasStationSearch(parsed.stationName, parsed.stationAddress)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            Text(
                text = if (editingExpense != null) loc("Редактировать расход", "Edit Expense") else loc("Новая трата", "New Expense"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            CategorySelector(
                selectedCategory = selectedCategory,
                onSelected = { selectedCategory = it }
            )
            if (showScanButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenQrScanner,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc("ИИ Камера", "AI Camera"))
                    }
                    Button(
                        onClick = onOpenGallery,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc("ИИ Галерея", "AI Gallery"))
                    }
                }
            }
            if (selectedCategory == ExpenseCategory.Fuel) {
                receiptParseHint?.let { hint ->
                    Text(
                        text = hint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = loc("Тип топлива", "Fuel type"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fuelTypeOptions.forEach { fuelType ->
                        FilterChip(
                            selected = selectedFuelType == fuelType,
                            onClick = { selectedFuelType = fuelType },
                            label = { Text(fuelType) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fuelLiters,
                        onValueChange = { fuelLiters = normalizeDecimalInput(it) },
                        label = { Text(if (currentUnits == "imperial") loc("Галлоны", "Gallons") else loc("Литры", "Liters")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    val pricePerUnit = calculatePricePerLiter(amount, fuelLiters)
                    OutlinedTextField(
                        value = if (pricePerUnit > 0.0) formatMoney(pricePerUnit) else "",
                        onValueChange = {},
                        label = { Text(if (currentUnits == "imperial") loc("Цена/гал", "Price/gal") else loc("Цена/л", "Price/L")) },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        singleLine = true
                    )
                }
                receiptStationAddress?.let { address ->
                    Text(
                        text = address,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(
                    onClick = { stationPickerOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedStation?.title ?: loc("Выбрать заправку на карте", "Select gas station on map"))
                }
            }
            val displayedCurrentMileage = if (currentUnits == "imperial") {
                Math.round(currentMileage * 0.621371).toInt()
            } else {
                currentMileage
            }
            val isMileageError = editingExpense == null && mileage.isNotBlank() && (mileage.toIntOrNull() ?: 0) < displayedCurrentMileage
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = normalizeDecimalInput(it) },
                    label = { Text(loc("Сумма", "Amount")) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it.filter(Char::isDigit) },
                    label = { Text(if (currentUnits == "imperial") loc("Пробег, миль", "Mileage, miles") else loc("Пробег, км", "Mileage, km")) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isMileageError,
                    supportingText = if (isMileageError) {
                        { Text(loc("Не меньше $displayedCurrentMileage", "No less than $displayedCurrentMileage")) }
                    } else null,
                    singleLine = true
                )
            }
            if (selectedCategory == ExpenseCategory.Repair || 
                selectedCategory == ExpenseCategory.Maintenance || 
                selectedCategory == ExpenseCategory.Parts) {
                
                val canAddField = !showWorkCost || !showPartsCost || !showShopName || !showAssembly || !showPartName || !showPartBrand || !showPartNumber
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loc("Детали ремонта / обслуживания", "Repair / Maintenance Details"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (canAddField) {
                        Box {
                            TextButton(
                                onClick = { fieldMenuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = loc("Добавить", "Add Field"),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            DropdownMenu(
                                expanded = fieldMenuExpanded,
                                onDismissRequest = { fieldMenuExpanded = false }
                            ) {
                                if (!showWorkCost) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Стоимость работ", "Work Cost")) },
                                        onClick = {
                                            showWorkCost = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showPartsCost) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Стоимость запчастей", "Parts Cost")) },
                                        onClick = {
                                            showPartsCost = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showShopName) {
                                    DropdownMenuItem(
                                        text = { Text(loc("СТО / Сервис", "Shop / Service Name")) },
                                        onClick = {
                                            showShopName = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showAssembly) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Узел / Группа", "Assembly / Group")) },
                                        onClick = {
                                            showAssembly = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showPartName) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Наименование детали", "Part Name")) },
                                        onClick = {
                                            showPartName = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showPartBrand) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Производитель детали", "Part Brand")) },
                                        onClick = {
                                            showPartBrand = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                                if (!showPartNumber) {
                                    DropdownMenuItem(
                                        text = { Text(loc("Артикул / Номер", "Part Number")) },
                                        onClick = {
                                            showPartNumber = true
                                            fieldMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (showWorkCost || showPartsCost) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (showWorkCost) {
                            OutlinedTextField(
                                value = workCost,
                                onValueChange = {
                                    workCost = normalizeDecimalInput(it)
                                    val w = workCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val p = partsCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    if (w > 0.0 || p > 0.0) {
                                        amount = formatDecimalInput(w + p)
                                    }
                                },
                                label = { Text(loc("Стоимость работ", "Work Cost")) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showWorkCost = false
                                        workCost = ""
                                        val p = partsCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                        amount = if (p > 0.0) formatDecimalInput(p) else amount
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                        if (showPartsCost) {
                            OutlinedTextField(
                                value = partsCost,
                                onValueChange = {
                                    partsCost = normalizeDecimalInput(it)
                                    val w = workCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val p = partsCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    if (w > 0.0 || p > 0.0) {
                                        amount = formatDecimalInput(w + p)
                                    }
                                },
                                label = { Text(loc("Стоимость запчастей", "Parts Cost")) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showPartsCost = false
                                        partsCost = ""
                                        val w = workCost.replace(',', '.').toDoubleOrNull() ?: 0.0
                                        amount = if (w > 0.0) formatDecimalInput(w) else amount
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
                
                if (showShopName || showAssembly) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (showShopName) {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text(loc("СТО / Сервис", "Shop / Service Name")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showShopName = false
                                        shopName = ""
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                        if (showAssembly) {
                            OutlinedTextField(
                                value = assembly,
                                onValueChange = { assembly = it },
                                label = { Text(loc("Узел / Группа", "Assembly / Group")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showAssembly = false
                                        assembly = ""
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
                
                if (showPartName) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedTextField(
                            value = partName,
                            onValueChange = { partName = it },
                            label = { Text(loc("Наименование детали", "Part Name")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    showPartName = false
                                    partName = ""
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = null)
                                }
                            }
                        )
                    }
                }
                
                if (showPartBrand || showPartNumber) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (showPartBrand) {
                            OutlinedTextField(
                                value = partBrand,
                                onValueChange = { partBrand = it },
                                label = { Text(loc("Производитель детали", "Part Brand")) },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showPartBrand = false
                                        partBrand = ""
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                        if (showPartNumber) {
                            OutlinedTextField(
                                value = partNumber,
                                onValueChange = { partNumber = it },
                                label = { Text(loc("Артикул / Номер", "Part Number")) },
                                modifier = Modifier.weight(2f),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showPartNumber = false
                                        partNumber = ""
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { servicePickerOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (shopName.isNotBlank()) loc("Сервис: $shopName", "Service: $shopName") else loc("Выбрать автосервис на карте", "Select auto service on map"))
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(loc("Название", "Title")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(loc("Комментарий", "Comment")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = {
                    val amountVal = amount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val litersVal = fuelLiters.replace(',', '.').toDoubleOrNull()
                    val mileageVal = mileage.toIntOrNull() ?: displayedCurrentMileage
                    val finalTitle = title.ifBlank { selectedCategory.getTitle(lang) }

                    val litersMetric = if (litersVal != null && currentUnits == "imperial") {
                        litersVal / 0.264172
                    } else {
                        litersVal
                    }
                    val mileageMetric = if (currentUnits == "imperial") {
                        Math.round(mileageVal / 0.621371).toInt()
                    } else {
                        mileageVal
                    }

                    onExpenseAdded(
                        selectedCategory,
                        amountVal,
                        litersMetric,
                        mileageMetric,
                        finalTitle,
                        notes,
                        workCost.replace(',', '.').toDoubleOrNull(),
                        partsCost.replace(',', '.').toDoubleOrNull(),
                        shopName.trim().takeIf { it.isNotEmpty() },
                        partName.trim().takeIf { it.isNotEmpty() },
                        partNumber.trim().takeIf { it.isNotEmpty() },
                        partBrand.trim().takeIf { it.isNotEmpty() },
                        assembly.trim().takeIf { it.isNotEmpty() }
                    )

                    if (selectedCategory == ExpenseCategory.Fuel && selectedStation != null && litersMetric != null && litersMetric > 0.0) {
                        val computedPrice = amountVal / litersMetric
                        val stationInfo = selectedStation
                        if (stationInfo != null && computedPrice > 0.0) {
                            scope.launch(Dispatchers.IO) {
                                GasStationClient.reportPrice(
                                    token = authToken,
                                    report = GasStationPriceReport(
                                        stationName = stationInfo.title,
                                        address = stationInfo.address.orEmpty(),
                                        latitude = stationInfo.point.latitude,
                                        longitude = stationInfo.point.longitude,
                                        fuelType = selectedFuelType,
                                        price = computedPrice
                                    )
                                )
                            }
                        }
                    }

                    amount = ""
                    fuelLiters = ""
                    title = ""
                    notes = ""
                    selectedStation = null
                    receiptFuelType = null
                    receiptStationAddress = null
                    receiptParseHint = null
                    appliedReceiptQr = null
                    appliedReceiptText = null
                },
                enabled = amount.replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true && !isMileageError,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (editingExpense != null) Icons.Outlined.Edit else Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingExpense != null) loc("Сохранить изменения", "Save changes") else loc("Добавить расход", "Add expense"))
            }
    }

    if (stationPickerOpen) {
        Dialog(
            onDismissRequest = { stationPickerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            GasStationPickerScreen(
                darkMapEnabled = darkMapEnabled,
                preferredFuelType = preferredFuelType,
                authToken = authToken,
                onStationSelected = { station ->
                    selectedStation = station
                    stationPickerOpen = false
                    if (title.isBlank() || title == ExpenseCategory.Fuel.getTitle(lang) || title == ExpenseCategory.Fuel.ruTitle || title == ExpenseCategory.Fuel.enTitle || title == "Заправка по чеку" || title == "Receipt fuel refill") {
                        title = if (lang == "en") "Refill at ${station.title}" else "Заправка ${station.title}"
                    }
                },
                onDismiss = { stationPickerOpen = false }
            )
        }
    }

    if (servicePickerOpen) {
        Dialog(
            onDismissRequest = { servicePickerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ServicePickerScreen(
                darkMapEnabled = darkMapEnabled,
                onServiceSelected = { service ->
                    shopName = service.title
                    showShopName = true
                    servicePickerOpen = false
                    if (title.isBlank() || title == ExpenseCategory.Repair.getTitle(lang) || title == ExpenseCategory.Maintenance.getTitle(lang) || title == ExpenseCategory.Parts.getTitle(lang)) {
                        title = if (lang == "en") "Service at ${service.title}" else "Сервис в ${service.title}"
                    }
                },
                onDismiss = { servicePickerOpen = false }
            )
        }
    }
}


@Composable
internal fun CategorySelector(
    selectedCategory: ExpenseCategory,
    onSelected: (ExpenseCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpenseCategory.entries.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onSelected(category) },
                        label = { Text(category.title) }
                    )
                }
            }
        }
    }
}


@Composable
internal fun ExpenseRow(
    expense: ExpenseEntity,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    onEditClick: ((ExpenseEntity) -> Unit)? = null,
    onDeleteClick: ((ExpenseEntity) -> Unit)? = null
) {
    val lang = LocalAppLanguage.current
    var showMenu by remember { mutableStateOf(false) }
    val category = ExpenseCategory.fromName(expense.category)
    val categoryColor = expenseCategoryColor(category)
    val categoryIcon = expenseCategoryIcon(category)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatMoney(expense.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val detailLine = buildString {
                    append(category.getTitle(lang))
                    expense.fuelLiters?.takeIf { it > 0.0 }?.let { append(" • ${formatLiters(it)}") }
                    append(" • ${formatMileage(expense.mileage)} • ${formatDate(expense.dateMillis)}")
                }
                Text(
                    text = detailLine,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (onEditClick != null && onDeleteClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = loc("Меню", "Menu"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(loc("Редактировать", "Edit")) },
                            onClick = {
                                showMenu = false
                                onEditClick(expense)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(loc("Удалить", "Delete"), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick(expense)
                            }
                        )
                    }
                }
            }
        }
    }
}


