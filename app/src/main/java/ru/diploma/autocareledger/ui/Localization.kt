package ru.diploma.autocareledger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "ru" }
val LocalAppUnits = staticCompositionLocalOf { "metric" }
val LocalAppCurrency = staticCompositionLocalOf { "RUB" }

@Composable
fun loc(ru: String, en: String): String {
    return if (LocalAppLanguage.current == "en") en else ru
}
