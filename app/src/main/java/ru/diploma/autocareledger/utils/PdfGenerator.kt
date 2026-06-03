package ru.diploma.autocareledger.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.ui.model.ExpenseCategory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateReport(
        context: Context,
        car: CarEntity,
        expenses: List<ExpenseEntity>,
        units: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = Color.BLACK
        }

        var yPos = 50f
        val margin = 50f
        val lineSpacing = 20f

        // Title
        canvas.drawText("Отчет по обслуживанию автомобиля", margin, yPos, titlePaint)
        yPos += 40f

        // Car Info
        canvas.drawText("Автомобиль: ${car.displayName}", margin, yPos, headerPaint)
        yPos += lineSpacing
        val mileageStr = "${UnitsConverter.getDisplayMileage(car.mileage, units)} ${if (units == "imperial") "mi" else "км"}"
        canvas.drawText("Текущий пробег: $mileageStr", margin, yPos, headerPaint)
        yPos += 40f

        // Table Headers
        val dateX = margin
        val catX = margin + 80f
        val titleX = margin + 200f
        val costX = margin + 450f

        canvas.drawText("Дата", dateX, yPos, headerPaint)
        canvas.drawText("Категория", catX, yPos, headerPaint)
        canvas.drawText("Описание", titleX, yPos, headerPaint)
        canvas.drawText("Сумма", costX, yPos, headerPaint)
        yPos += 10f
        canvas.drawLine(margin, yPos, 595f - margin, yPos, paint)
        yPos += lineSpacing

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        for (expense in expenses.sortedByDescending { it.dateMillis }) {
            if (yPos > 800f) {
                // New page
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }

            val dateStr = dateFormat.format(Date(expense.dateMillis))
            val catStr = getCategoryName(expense.category)
            val titleStr = expense.title.take(25) + if (expense.title.length > 25) "..." else ""
            val costStr = "%,.2f ₽".format(expense.amount)

            canvas.drawText(dateStr, dateX, yPos, textPaint)
            canvas.drawText(catStr, catX, yPos, textPaint)
            canvas.drawText(titleStr, titleX, yPos, textPaint)
            canvas.drawText(costStr, costX, yPos, textPaint)

            yPos += lineSpacing
        }

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "AutoCare_Report_${car.id}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    private fun getCategoryName(category: String): String {
        return when (category) {
            ExpenseCategory.Fuel.name -> "Заправка"
            ExpenseCategory.Maintenance.name -> "ТО"
            ExpenseCategory.Repair.name -> "Ремонт"
            ExpenseCategory.Parts.name -> "Запчасти"
            ExpenseCategory.Insurance.name -> "Страховка"
            ExpenseCategory.Parking.name -> "Парковка"
            ExpenseCategory.Wash.name -> "Мойка"
            else -> "Другое"
        }
    }
}
