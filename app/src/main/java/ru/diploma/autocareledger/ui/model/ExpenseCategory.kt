package ru.diploma.autocareledger.ui.model

import androidx.compose.runtime.Composable
import ru.diploma.autocareledger.ui.loc

enum class ExpenseCategory(val ruTitle: String, val enTitle: String) {
    Fuel("Топливо", "Fuel"),
    Maintenance("ТО", "Maintenance"),
    Repair("Ремонт", "Repair"),
    Parts("Запчасти", "Spare Parts"),
    Insurance("Страховка", "Insurance"),
    Wash("Мойка", "Car Wash"),
    Parking("Парковка", "Parking"),
    Other("Прочее", "Other");

    val title: String
        @Composable
        get() = loc(ruTitle, enTitle)

    fun getTitle(lang: String): String = if (lang == "en") enTitle else ruTitle

    companion object {
        fun fromName(name: String): ExpenseCategory =
            entries.firstOrNull { it.name == name } ?: Other
    }
}
