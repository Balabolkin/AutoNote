package ru.diploma.autocareledger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import ru.diploma.autocareledger.network.AuthClient
import ru.diploma.autocareledger.network.AuthSession
import ru.diploma.autocareledger.network.BackendConfig
import java.util.Locale
import java.net.HttpURLConnection
import java.net.URL

data class CarSetupChoice(
    val brand: String,
    val model: String,
    val generation: String,
    val restyling: String,
    val trim: String,
    val year: Int,
    val plateNumber: String,
    val mileage: Int,
    val tankVolumeLiters: Double,
    val fuelType: String,
    val colorName: String,
    val colorHex: String,
    val photoUri: String?
)

private data class CarColorOption(
    val name: String,
    val hex: String,
    val imageUrl: String
)

private data class CarCatalogItem(
    val brand: String,
    val model: String,
    val generation: String,
    val restyling: String,
    val years: IntRange,
    val trims: List<String>,
    val fuelType: String,
    val colors: List<CarColorOption>
) {
    val variantLabel: String = "$generation • $restyling"
    fun imageFor(colorName: String): String =
        colors.firstOrNull { it.name == colorName }?.imageUrl ?: colors.firstOrNull()?.imageUrl.orEmpty()
}

private val carCatalog = listOf(
    CarCatalogItem(
        brand = "Toyota",
        model = "Camry",
        generation = "XV70",
        restyling = "рестайлинг",
        years = 2021..2024,
        trims = listOf("Classic", "Elegance", "GR Sport", "Executive Safety"),
        fuelType = "Бензин",
        colors = listOf(
            CarColorOption("Белый", "#F3F4F6", "https://www.pngkey.com/png/full/302-3021992_29-2012-toyota-camry-side-view.png"),
            CarColorOption("Серебристый", "#AEB4BA", "https://www.pngkey.com/png/full/269-2696512_toyota-camry-car-png.png"),
            CarColorOption("Черный", "#151515", "https://www.pngkey.com/png/full/237-2370193_toyota-camry-png.png")
        )
    ),
    CarCatalogItem(
        brand = "Hyundai",
        model = "Creta",
        generation = "SU2",
        restyling = "II поколение",
        years = 2021..2024,
        trims = listOf("Prime", "Lifestyle", "Prestige", "Smart"),
        fuelType = "Бензин",
        colors = listOf(
            CarColorOption("Серебристый", "#B7BDC3", "https://www.pngkey.com/png/detail/815-8158187_silver-hyundai-png-image-background-2020-hyundai-creta.png"),
            CarColorOption("Красный", "#B3262E", "https://www.pngkey.com/png/full/813-8134092_hyundai-kona-red.png"),
            CarColorOption("Черный", "#161616", "https://www.pngkey.com/png/full/80-803694_hyundai-car-png.png")
        )
    ),
    CarCatalogItem(
        brand = "Skoda",
        model = "Rapid",
        generation = "II",
        restyling = "лифтбек",
        years = 2020..2023,
        trims = listOf("Entry", "Active", "Ambition", "Style"),
        fuelType = "Бензин",
        colors = listOf(
            CarColorOption("Белый", "#F6F7F8", "https://www.pngkey.com/png/full/84-841180_skoda-rapid-white-side-view.png"),
            CarColorOption("Синий", "#244C8F", "https://www.pngkey.com/png/full/128-1286512_skoda-superb-blue.png"),
            CarColorOption("Черный", "#151515", "https://www.pngkey.com/png/full/338-3388651_skoda-octavia-black.png")
        )
    )
)

@Composable
fun FirstLaunchSetupScreen(
    onConfirmCar: (CarSetupChoice) -> Unit,
    onOpenLogin: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    var manualMode by rememberSaveable { mutableStateOf(false) }
    var catalog by remember { mutableStateOf(carCatalog) }

    LaunchedEffect(Unit) {
        val remoteCatalog = fetchRemoteCarCatalog()
        if (remoteCatalog.isNotEmpty()) {
            catalog = remoteCatalog
        }
    }

    OnboardingBackground(onLanguageChanged = onLanguageChanged) {
        if (manualMode) {
            ManualCarSetup(
                onConfirmCar = onConfirmCar,
                onBack = { manualMode = false }
            )
        } else {
            PresetCarSetup(
                catalog = catalog,
                onConfirmCar = onConfirmCar,
                onManualMode = { manualMode = true },
                onOpenLogin = onOpenLogin,
                showLoginAction = true
            )
        }
    }
}

@Composable
fun AddCarSetupScreen(
    onConfirmCar: (CarSetupChoice) -> Unit,
    onDismiss: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    var manualMode by rememberSaveable { mutableStateOf(false) }
    var catalog by remember { mutableStateOf(carCatalog) }

    LaunchedEffect(Unit) {
        val remoteCatalog = fetchRemoteCarCatalog()
        if (remoteCatalog.isNotEmpty()) {
            catalog = remoteCatalog
        }
    }

    OnboardingBackground(onLanguageChanged = onLanguageChanged) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (manualMode) {
                ManualCarSetup(
                    onConfirmCar = onConfirmCar,
                    onBack = { manualMode = false }
                )
            } else {
                PresetCarSetup(
                    catalog = catalog,
                    onConfirmCar = onConfirmCar,
                    onManualMode = { manualMode = true },
                    onOpenLogin = {},
                    showLoginAction = false
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    selectedCarName: String?,
    startWithLogin: Boolean,
    onBackToSetup: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onComplete: (AuthSession, Boolean) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    var loginMode by rememberSaveable { mutableStateOf(startWithLogin) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    OnboardingBackground(onLanguageChanged = onLanguageChanged) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToSetup) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(
                    text = if (loginMode) loc("Вход", "Login") else loc("Регистрация", "Registration"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedCarName ?: loc("Авто Блокнот", "AutoNote"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (loginMode) loc("Продолжите учет обслуживания в своем профиле", "Continue tracking services in your profile") else loc("Создайте профиль, чтобы вести историю затрат и напоминаний", "Create a profile to track expenses and reminders"),
                    color = Color(0xFFB8C4C0),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            OnboardingPanel {
                if (!loginMode) {
                    AuthField(
                        value = name,
                        onValueChange = { name = it },
                        label = loc("Имя", "Name"),
                        icon = Icons.Outlined.Person
                    )
                }
                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    label = loc("Email", "Email"),
                    icon = Icons.Outlined.AlternateEmail,
                    keyboardType = KeyboardType.Email
                )
                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    label = loc("Пароль", "Password"),
                    icon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    password = true
                )
                error?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFFB4AB),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = {
                        loading = true
                        error = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    if (loginMode) {
                                        AuthClient.login(email.trim(), password)
                                    } else {
                                        AuthClient.register(name.trim(), email.trim(), password)
                                    }
                                }
                            }
                            loading = false
                            result
                                .onSuccess { session -> onComplete(session, loginMode) }
                                .onFailure { throwable ->
                                    error = throwable.message ?: if (currentLang == "en") "Failed to login" else "Не удалось войти"
                                }
                        }
                    },
                    enabled = !loading && email.isNotBlank() && password.length >= 4 && (loginMode || name.isNotBlank()),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE6CC))
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            loading -> loc("Подключаем аккаунт...", "Connecting account...")
                            loginMode -> loc("Войти", "Login")
                            else -> loc("Создать аккаунт", "Create account")
                        }
                    )
                }
                TextButton(onClick = { loginMode = !loginMode }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (loginMode) loc("Создать новый аккаунт", "Create a new account") else loc("Уже есть аккаунт", "Already have an account"),
                        color = Color(0xFF9FE6CC)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetCarSetup(
    catalog: List<CarCatalogItem>,
    onConfirmCar: (CarSetupChoice) -> Unit,
    onManualMode: () -> Unit,
    onOpenLogin: () -> Unit,
    showLoginAction: Boolean
) {
    val brands = catalog.map { it.brand }.distinct()
    val currentUnits = LocalAppUnits.current
    var selectedBrand by rememberSaveable { mutableStateOf("") }
    var selectedModel by rememberSaveable { mutableStateOf("") }
    var selectedVariant by rememberSaveable { mutableStateOf("") }
    var selectedTrim by rememberSaveable { mutableStateOf("") }
    var selectedYear by rememberSaveable { mutableStateOf("") }
    var selectedColor by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }
    var tankVolume by rememberSaveable { mutableStateOf("") }
    var plateLetter by rememberSaveable { mutableStateOf("") }
    var plateDigits by rememberSaveable { mutableStateOf("") }
    var plateTail by rememberSaveable { mutableStateOf("") }
    var plateRegion by rememberSaveable { mutableStateOf("") }

    val models = catalog
        .filter { it.brand == selectedBrand }
        .map { it.model }
        .distinct()
    val variants = catalog.filter { it.brand == selectedBrand && it.model == selectedModel }
    val selectedCatalogItem = variants.firstOrNull { it.variantLabel == selectedVariant }
    val imageCatalogItem = selectedCatalogItem ?: variants.firstOrNull()
    val trimOptions = selectedCatalogItem?.trims.orEmpty()
    val yearOptions = selectedCatalogItem?.years?.map(Int::toString).orEmpty().reversed()
    val colorOptions = selectedCatalogItem?.colors.orEmpty()
    val hasSelectedModel = selectedModel.isNotBlank()
    val plateNumber = formatPlateNumber(plateLetter, plateDigits, plateTail, plateRegion)
    val selectedImageUrl = selectedCatalogItem?.imageFor(selectedColor) ?: imageCatalogItem?.colors?.firstOrNull()?.imageUrl
    val selectedColorOption = selectedCatalogItem?.colors?.firstOrNull { it.name == selectedColor }
    val canConfirm = selectedCatalogItem != null && selectedTrim.isNotBlank() && selectedYear.isNotBlank() && selectedColor.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .animateContentSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(if (hasSelectedModel) 22.dp else 40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = loc("Выберите автомобиль", "Select Car"),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = loc("Настроим гараж и подготовим учет расходов", "We'll set up your garage and prepare expense tracking"),
                color = Color(0xFFB8C4C0),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        AnimatedVisibility(visible = hasSelectedModel) {
            AsyncImage(
                model = selectedImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 16.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkDropdown(
                    label = loc("Марка", "Brand"),
                    value = selectedBrand,
                    options = brands,
                    onSelected = {
                        selectedBrand = it
                        selectedModel = ""
                        selectedVariant = ""
                        selectedTrim = ""
                        selectedYear = ""
                        selectedColor = ""
                    },
                    modifier = Modifier.weight(1f)
                )
                DarkDropdown(
                    label = loc("Модель", "Model"),
                    value = selectedModel,
                    options = models,
                    enabled = selectedBrand.isNotBlank(),
                    onSelected = {
                        selectedModel = it
                        selectedVariant = ""
                        selectedTrim = ""
                        selectedYear = ""
                        selectedColor = ""
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkDropdown(
                    label = loc("Поколение", "Generation"),
                    value = selectedVariant,
                    options = variants.map { it.variantLabel },
                    enabled = selectedModel.isNotBlank(),
                    onSelected = {
                        selectedVariant = it
                        val item = variants.first { variant -> variant.variantLabel == it }
                        selectedTrim = item.trims.first()
                        selectedYear = item.years.last.toString()
                        selectedColor = item.colors.firstOrNull()?.name.orEmpty()
                    },
                    modifier = Modifier.weight(1f)
                )
                DarkDropdown(
                    label = loc("Комплектация", "Trim"),
                    value = selectedTrim,
                    options = trimOptions,
                    enabled = selectedCatalogItem != null,
                    onSelected = { selectedTrim = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkDropdown(
                    label = loc("Год", "Year"),
                    value = selectedYear,
                    options = yearOptions,
                    enabled = selectedCatalogItem != null,
                    onSelected = { selectedYear = it },
                    modifier = Modifier.weight(1f)
                )
                DarkInput(
                    value = mileage,
                    onValueChange = { mileage = it.filter(Char::isDigit) },
                    label = if (currentUnits == "imperial") loc("Пробег, миль", "Mileage, miles") else loc("Пробег, км", "Mileage, km"),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
            DarkInput(
                value = tankVolume,
                onValueChange = { tankVolume = normalizeDecimalInput(it) },
                label = if (currentUnits == "imperial") loc("Объем бака, гал.", "Tank volume, gal") else loc("Объем бака, л", "Tank volume, L"),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal
            )
            ColorSelector(
                colors = colorOptions,
                selected = selectedColor,
                enabled = selectedCatalogItem != null,
                onSelected = { selectedColor = it.name }
            )
            LicensePlateInput(
                firstLetter = plateLetter,
                digits = plateDigits,
                tailLetters = plateTail,
                region = plateRegion,
                onFirstLetterChange = { plateLetter = normalizePlateLetters(it, 1) },
                onDigitsChange = { plateDigits = it.filter(Char::isDigit).take(3) },
                onTailLettersChange = { plateTail = normalizePlateLetters(it, 2) },
                onRegionChange = { plateRegion = it.filter(Char::isDigit).take(3) }
            )
            Button(
                onClick = {
                    val item = selectedCatalogItem ?: return@Button
                    val rawMileage = mileage.toIntOrNull() ?: 0
                    val finalMileage = if (currentUnits == "imperial") {
                        Math.round(rawMileage / 0.621371).toInt()
                    } else {
                        rawMileage
                    }
                    val rawVolume = tankVolume.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val finalVolume = if (currentUnits == "imperial") {
                        rawVolume / 0.264172
                    } else {
                        rawVolume
                    }
                    onConfirmCar(
                        CarSetupChoice(
                            brand = item.brand,
                            model = item.model,
                            generation = item.generation,
                            restyling = item.restyling,
                            trim = selectedTrim,
                            year = selectedYear.toIntOrNull() ?: item.years.last,
                            plateNumber = plateNumber,
                            mileage = finalMileage,
                            tankVolumeLiters = finalVolume,
                            fuelType = item.fuelType,
                            colorName = selectedColorOption?.name.orEmpty(),
                            colorHex = selectedColorOption?.hex.orEmpty(),
                            photoUri = selectedImageUrl
                        )
                    )
                },
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE6CC))
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(loc("Подтвердить автомобиль", "Confirm Vehicle"))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onManualMode, contentPadding = PaddingValues(horizontal = 0.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Color(0xFF9FE6CC))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(loc("Добавить свой", "Add Custom"), color = Color(0xFF9FE6CC))
                }
                if (showLoginAction) {
                    TextButton(onClick = onOpenLogin, contentPadding = PaddingValues(horizontal = 0.dp)) {
                        Text(loc("Войти в аккаунт", "Login to Account"), color = Color(0xFFB8C4C0))
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualCarSetup(
    onConfirmCar: (CarSetupChoice) -> Unit,
    onBack: () -> Unit
) {
    val currentUnits = LocalAppUnits.current
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var generation by rememberSaveable { mutableStateOf("") }
    var trim by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var plateLetter by rememberSaveable { mutableStateOf("") }
    var plateDigits by rememberSaveable { mutableStateOf("") }
    var plateTail by rememberSaveable { mutableStateOf("") }
    var plateRegion by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }
    var tankVolume by rememberSaveable { mutableStateOf("") }
    val defaultFuel = loc("Бензин", "Gasoline")
    var fuel by rememberSaveable { mutableStateOf(defaultFuel) }
    var colorName by rememberSaveable { mutableStateOf("") }
    var colorHex by rememberSaveable { mutableStateOf("#F3F4F6") }
    val context = LocalContext.current
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
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
        photoUri = uri?.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri = tempPhotoUri?.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = loc("Свой автомобиль", "Custom Car"),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = loc("Заполните данные вручную, если модели нет в списке", "Fill in details manually if your model is not listed"),
                color = Color(0xFFB8C4C0),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        OnboardingPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkInput(brand, { brand = it }, loc("Марка", "Brand"), Modifier.weight(1f))
                DarkInput(model, { model = it }, loc("Модель", "Model"), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkInput(generation, { generation = it }, loc("Поколение", "Generation"), Modifier.weight(1f))
                DarkInput(trim, { trim = it }, loc("Комплектация", "Trim"), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkInput(
                    value = year,
                    onValueChange = { year = it.filter(Char::isDigit).take(4) },
                    label = loc("Год", "Year"),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                DarkInput(
                    value = mileage,
                    onValueChange = { mileage = it.filter(Char::isDigit) },
                    label = if (currentUnits == "imperial") loc("Пробег, миль", "Mileage, miles") else loc("Пробег, км", "Mileage, km"),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
            DarkInput(
                value = tankVolume,
                onValueChange = { tankVolume = normalizeDecimalInput(it) },
                label = if (currentUnits == "imperial") loc("Объем бака, гал.", "Tank volume, gal") else loc("Объем бака, л", "Tank volume, L"),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal
            )
            ManualPhotoPicker(
                photoUri = photoUri,
                onPickPhoto = { showPhotoSourceDialog = true }
            )

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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkInput(colorName, { colorName = it }, loc("Цвет", "Color"), Modifier.weight(1f))
                DarkInput(colorHex, { colorHex = it.take(7) }, loc("HEX", "HEX"), Modifier.weight(1f))
            }
            LicensePlateInput(
                firstLetter = plateLetter,
                digits = plateDigits,
                tailLetters = plateTail,
                region = plateRegion,
                onFirstLetterChange = { plateLetter = normalizePlateLetters(it, 1) },
                onDigitsChange = { plateDigits = it.filter(Char::isDigit).take(3) },
                onTailLettersChange = { plateTail = normalizePlateLetters(it, 2) },
                onRegionChange = { plateRegion = it.filter(Char::isDigit).take(3) }
            )
            DarkInput(fuel, { fuel = it }, loc("Тип топлива", "Fuel type"), Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val rawMileage = mileage.toIntOrNull() ?: 0
                    val finalMileage = if (currentUnits == "imperial") {
                        Math.round(rawMileage / 0.621371).toInt()
                    } else {
                        rawMileage
                    }
                    val rawVolume = tankVolume.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val finalVolume = if (currentUnits == "imperial") {
                        rawVolume / 0.264172
                    } else {
                        rawVolume
                    }
                    onConfirmCar(
                        CarSetupChoice(
                            brand = brand,
                            model = model,
                            generation = generation,
                            restyling = "",
                            trim = trim,
                            year = year.toIntOrNull() ?: 2024,
                            plateNumber = formatPlateNumber(plateLetter, plateDigits, plateTail, plateRegion),
                            mileage = finalMileage,
                            tankVolumeLiters = finalVolume,
                            fuelType = fuel,
                            colorName = colorName,
                            colorHex = colorHex,
                            photoUri = photoUri
                        )
                    )
                },
                enabled = brand.isNotBlank() && model.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE6CC))
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(loc("Продолжить", "Continue"))
            }
        }
    }
}

@Composable
fun LanguageToggle(
    modifier: Modifier = Modifier,
    onLanguageChanged: (String) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ruActive = currentLang == "ru"
        val enActive = currentLang == "en"
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (ruActive) Color(0xFF9FE6CC).copy(alpha = 0.2f) else Color.Transparent)
                .clickable { onLanguageChanged("ru") }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "RU",
                color = if (ruActive) Color(0xFF9FE6CC) else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (enActive) Color(0xFF9FE6CC).copy(alpha = 0.2f) else Color.Transparent)
                .clickable { onLanguageChanged("en") }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "EN",
                color = if (enActive) Color(0xFF9FE6CC) else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun OnboardingBackground(
    onLanguageChanged: ((String) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF12191E),
                            Color(0xFF070A0D),
                            Color(0xFF0D1115)
                        )
                    )
                )
        )
        content()
        
        if (onLanguageChanged != null) {
            LanguageToggle(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp),
                onLanguageChanged = onLanguageChanged
            )
        }
    }
}

@Composable
private fun OnboardingPanel(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11171C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = { content() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val active = enabled && options.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && active,
        onExpandedChange = { if (active) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = active,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && active) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color(0xFF707B80),
                focusedLabelColor = Color(0xFF9FE6CC),
                unfocusedLabelColor = Color(0xFFB8C4C0),
                disabledLabelColor = Color(0xFF657077),
                focusedBorderColor = Color(0xFF9FE6CC),
                unfocusedBorderColor = Color(0xFF354049),
                disabledBorderColor = Color(0xFF252C31),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = active)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && active,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF151C22)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSelector(
    colors: List<CarColorOption>,
    selected: String,
    enabled: Boolean,
    onSelected: (CarColorOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = loc("Цвет автомобиля", "Car Color"),
            color = if (enabled) Color(0xFFB8C4C0) else Color(0xFF657077),
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { option ->
                val isSelected = selected == option.name
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF9FE6CC) else Color(0xFF354049),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = enabled) { onSelected(option) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(parseHexColor(option.hex))
                            .border(1.dp, Color(0xFF2C3338), RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = option.name,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualPhotoPicker(
    photoUri: String?,
    onPickPhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B1014)),
                contentScale = ContentScale.Fit
            )
        }
        TextButton(
            onClick = onPickPhoto,
            contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color(0xFF9FE6CC))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (photoUri == null) loc("Загрузить фото автомобиля", "Upload vehicle photo") else loc("Заменить фото", "Replace photo"),
                color = Color(0xFF9FE6CC)
            )
        }
    }
}

@Composable
private fun LicensePlateInput(
    firstLetter: String,
    digits: String,
    tailLetters: String,
    region: String,
    onFirstLetterChange: (String) -> Unit,
    onDigitsChange: (String) -> Unit,
    onTailLettersChange: (String) -> Unit,
    onRegionChange: (String) -> Unit
) {
    val firstLetterFocus = remember { FocusRequester() }
    val digitsFocus = remember { FocusRequester() }
    val tailLettersFocus = remember { FocusRequester() }
    val regionFocus = remember { FocusRequester() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = loc("Госномер", "License Plate"),
            color = Color(0xFFB8C4C0),
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ) {
                PlateField(
                    value = firstLetter,
                    onValueChange = {
                        val normalized = normalizePlateLetters(it, 1)
                        onFirstLetterChange(normalized)
                        if (normalized.length == 1) digitsFocus.requestFocus()
                    },
                    placeholder = "A",
                    modifier = Modifier
                        .width(34.dp)
                        .focusRequester(firstLetterFocus)
                )
                PlateField(
                    value = digits,
                    onValueChange = {
                        val normalized = it.filter(Char::isDigit).take(3)
                        onDigitsChange(normalized)
                        if (normalized.length == 3) tailLettersFocus.requestFocus()
                    },
                    placeholder = "000",
                    modifier = Modifier
                        .width(86.dp)
                        .focusRequester(digitsFocus),
                    keyboardType = KeyboardType.Number
                )
                PlateField(
                    value = tailLetters,
                    onValueChange = {
                        val normalized = normalizePlateLetters(it, 2)
                        onTailLettersChange(normalized)
                        if (normalized.length == 2) regionFocus.requestFocus()
                    },
                    placeholder = "AA",
                    modifier = Modifier
                        .width(58.dp)
                        .focusRequester(tailLettersFocus)
                )
            }
            Column(
                modifier = Modifier
                    .width(74.dp)
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PlateField(
                    value = region,
                    onValueChange = { onRegionChange(it.filter(Char::isDigit).take(3)) },
                    placeholder = "78",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(regionFocus),
                    keyboardType = KeyboardType.Number,
                    fontSize = 24
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "RUS",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    RussianFlag()
                }
            }
        }
    }
}

@Composable
private fun PlateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    fontSize: Int = 34
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(Color.Black),
        textStyle = TextStyle(
            color = Color.Black,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        ),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFFD2D2D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize.sp,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun RussianFlag() {
    Column(
        modifier = Modifier
            .width(28.dp)
            .height(18.dp)
            .border(1.dp, Color(0xFF6E6E6E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1F5FAE))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE53935))
        )
    }
}

@Composable
private fun DarkInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = darkTextFieldColors(),
        modifier = modifier
    )
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF9FE6CC)) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = darkTextFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun normalizePlateLetters(value: String, limit: Int): String {
    val allowedLetters = "ABEKMHOPCTYXАВЕКМНОРСТУХ"
    return value
        .uppercase(Locale.getDefault())
        .filter { it in allowedLetters }
        .take(limit)
}

private fun formatPlateNumber(
    firstLetter: String,
    digits: String,
    tailLetters: String,
    region: String
): String {
    val mainNumber = listOf(firstLetter, digits, tailLetters)
        .filter(String::isNotBlank)
        .joinToString("")
    return listOf(mainNumber, region)
        .filter(String::isNotBlank)
        .joinToString(" ")
}

private fun parseHexColor(value: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(value)) }
        .getOrDefault(Color(0xFFB7BDC3))

private suspend fun fetchRemoteCarCatalog(): List<CarCatalogItem> = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL("${BackendConfig.BASE_URL}/cars/catalog").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 1_500
            readTimeout = 2_500
        }
        if (connection.responseCode !in 200..299) {
            return@runCatching emptyList()
        }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        parseRemoteCatalog(body)
    }.getOrDefault(emptyList())
}

private fun parseRemoteCatalog(body: String): List<CarCatalogItem> {
    val rows = JSONArray(body)
    val grouped = linkedMapOf<String, MutableList<String>>()
    val metadata = mutableMapOf<String, CarCatalogItem>()

    for (index in 0 until rows.length()) {
        val item = rows.getJSONObject(index)
        val key = listOf(
            item.getString("brand"),
            item.getString("model"),
            item.getString("generation"),
            item.getString("restyling"),
            item.getInt("yearFrom"),
            item.getInt("yearTo"),
            item.getString("fuelType"),
            item.optString("generationImageGroup", ""),
        ).joinToString("|")

        grouped.getOrPut(key) { mutableListOf() }.add(item.getString("trim"))
        metadata.getOrPut(key) {
            CarCatalogItem(
                brand = item.getString("brand"),
                model = item.getString("model"),
                generation = item.getString("generation"),
                restyling = item.getString("restyling"),
                years = item.getInt("yearFrom")..item.getInt("yearTo"),
                trims = emptyList(),
                fuelType = item.getString("fuelType"),
                colors = emptyList()
            )
        }
    }

    return grouped.mapNotNull { (key, trims) ->
        metadata[key]?.copy(
            trims = trims.distinct(),
            colors = collectRemoteColors(rows, key)
        )
    }
}

private fun collectRemoteColors(rows: JSONArray, key: String): List<CarColorOption> {
    val colors = linkedMapOf<String, CarColorOption>()
    for (index in 0 until rows.length()) {
        val item = rows.getJSONObject(index)
        val rowKey = listOf(
            item.getString("brand"),
            item.getString("model"),
            item.getString("generation"),
            item.getString("restyling"),
            item.getInt("yearFrom"),
            item.getInt("yearTo"),
            item.getString("fuelType"),
            item.optString("generationImageGroup", ""),
        ).joinToString("|")
        if (rowKey == key) {
            val colorName = item.optString("colorName", "Белый")
            colors[colorName] = CarColorOption(
                name = colorName,
                hex = item.optString("colorHex", "#F3F4F6"),
                imageUrl = item.getString("imageUrl")
            )
        }
    }
    return colors.values.toList()
}


@Composable
private fun darkTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF9FE6CC),
    unfocusedLabelColor = Color(0xFFB8C4C0),
    cursorColor = Color(0xFF9FE6CC),
    focusedBorderColor = Color(0xFF9FE6CC),
    unfocusedBorderColor = Color(0xFF354049),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
