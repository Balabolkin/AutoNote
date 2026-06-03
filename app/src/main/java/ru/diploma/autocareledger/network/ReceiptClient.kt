package ru.diploma.autocareledger.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.diploma.autocareledger.BuildConfig
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class FuelReceiptParseResult(
    val totalAmount: Double? = null,
    val dateTime: String? = null,
    val stationName: String? = null,
    val stationAddress: String? = null,
    val fuelType: String? = null,
    val liters: Double? = null,
    val pricePerLiter: Double? = null,
    val needsReceiptDetails: Boolean = false,
    val category: String? = null,
    val expenseTitle: String? = null,
    val expenseNotes: String? = null
)

object ReceiptClient {
    private val YANDEX_API_KEY = BuildConfig.YANDEX_API_KEY
    private val YANDEX_FOLDER_ID = BuildConfig.YANDEX_FOLDER_ID

    private fun cleanJsonCommentsAndMarkdown(input: String): String {
        // 1. Extract block between first '{' and last '}'
        val startIndex = input.indexOf('{')
        val endIndex = input.lastIndexOf('}')
        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            return input
        }
        var json = input.substring(startIndex, endIndex + 1)
        
        // 2. Remove single-line comments: // ...
        json = json.replace(Regex("(?m)^\\s*//.*$"), "") // Comments on their own line
        json = json.replace(Regex("//.*$"), "") // Comments at the end of a line
        
        return json.trim()
    }

    suspend fun parseReceiptTextWithYandexGPT(text: String): FuelReceiptParseResult? {
        return withContext(Dispatchers.IO) {
            val connection = (URL("https://llm.api.cloud.yandex.net/foundationModels/v1/completion").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 25_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Api-Key $YANDEX_API_KEY")
                setRequestProperty("x-folder-id", YANDEX_FOLDER_ID)
            }
            
            val prompt = """
                Ты — эксперт по анализу чеков, счетов и документов, связанных с обслуживанием и расходами на автомобиль.
                Распознай и извлеки информацию из следующего OCR-текста на русском языке.
                Определи категорию расхода из следующего списка:
                - "Fuel" (если это чек заправки топливом АЗС)
                - "Maintenance" (техническое обслуживание ТО, замена масла, фильтров, диагностика)
                - "Repair" (ремонт автомобиля, кузовные работы, шиномонтаж, автоэлектрика)
                - "Parts" (покупка запчастей, шин, дисков, аксессуаров для авто)
                - "Insurance" (страховой полис ОСАГО, КАСКО)
                - "Wash" (мойка автомобиля, химчистка, полировка)
                - "Parking" (оплата парковки, платных дорог, штрафов)
                - "Other" (любые другие расходы на авто)

                Верни результат строго в формате JSON со следующими полями:
                {
                  "category": "Fuel",
                  "totalAmount": 1500.0,
                  "expenseTitle": "Замена масла",
                  "expenseNotes": "Фильтр масляный, масло синтетическое",
                  "stationName": "Газпромнефть",
                  "stationAddress": "ул. Ленина, 10",
                  "fuelType": "АИ-95",
                  "liters": 45.0,
                  "pricePerLiter": 55.5
                }

                Правила:
                1. Если поле не найдено, установи null.
                2. Не добавляй никаких других символов, пояснений или markdown-разметки (верни чистый JSON без markdown ```json).
                3. Категорически запрещено добавлять какие-либо комментарии (такие как // или /* */) внутрь JSON.
                4. Если категория отлична от "Fuel", то поля "stationName", "stationAddress", "fuelType", "liters", "pricePerLiter" установи в null.

                Текст чека:
                $text
            """.trimIndent()
            
            val requestBody = JSONObject().apply {
                put("modelUri", "gpt://$YANDEX_FOLDER_ID/yandexgpt/latest")
                put("completionOptions", JSONObject().apply {
                    put("stream", false)
                    put("temperature", 0.1)
                    put("maxTokens", "2000")
                })
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("text", prompt)
                    })
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }
            
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw Exception("HTTP $responseCode - $errorText")
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val responseJson = JSONObject(responseText)
            val alternatives = responseJson.getJSONObject("result").getJSONArray("alternatives")
            val candidateText = alternatives.getJSONObject(0).getJSONObject("message").getString("text")
                
            val cleanedText = cleanJsonCommentsAndMarkdown(candidateText)
            val resultJson = JSONObject(cleanedText)
            
            FuelReceiptParseResult(
                totalAmount = resultJson.optNullableDouble("totalAmount"),
                stationName = resultJson.optString("stationName").takeIf { it.isNotBlank() && it != "null" },
                stationAddress = resultJson.optString("stationAddress").takeIf { it.isNotBlank() && it != "null" },
                fuelType = resultJson.optString("fuelType").takeIf { it.isNotBlank() && it != "null" },
                liters = resultJson.optNullableDouble("liters"),
                pricePerLiter = resultJson.optNullableDouble("pricePerLiter"),
                category = resultJson.optString("category").takeIf { it.isNotBlank() && it != "null" },
                expenseTitle = resultJson.optString("expenseTitle").takeIf { it.isNotBlank() && it != "null" },
                expenseNotes = resultJson.optString("expenseNotes").takeIf { it.isNotBlank() && it != "null" }
            )
        }
    }

    suspend fun parseReceiptImageWithYandex(base64Image: String): FuelReceiptParseResult? {
        return withContext(Dispatchers.IO) {
            val connection = (URL("https://ocr.api.cloud.yandex.net/ocr/v1/recognizeText").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 25_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Api-Key $YANDEX_API_KEY")
                setRequestProperty("x-folder-id", YANDEX_FOLDER_ID)
            }

            val requestBody = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("languageCodes", org.json.JSONArray().apply { put("*") })
                put("model", "page")
                put("content", base64Image)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw Exception("HTTP $responseCode - $errorText")
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val responseJson = JSONObject(responseText)
            
            val resultObj = if (responseJson.has("result")) responseJson.getJSONObject("result") else responseJson
            val textObj = when {
                resultObj.has("textAnnotation") -> resultObj.getJSONObject("textAnnotation")
                resultObj.has("text_detection") -> resultObj.getJSONObject("text_detection")
                resultObj.has("textDetection") -> resultObj.getJSONObject("textDetection")
                else -> resultObj
            }
            
            val stringBuilder = StringBuilder()
            
            if (textObj.has("pages")) {
                val pages = textObj.getJSONArray("pages")
                for (p in 0 until pages.length()) {
                    val blocks = pages.getJSONObject(p).optJSONArray("blocks") ?: continue
                    for (b in 0 until blocks.length()) {
                        val lines = blocks.getJSONObject(b).optJSONArray("lines") ?: continue
                        for (l in 0 until lines.length()) {
                            val lineObj = lines.getJSONObject(l)
                            val lineText = when {
                                lineObj.has("text") -> lineObj.optString("text")
                                lineObj.has("words") -> {
                                    val words = lineObj.getJSONArray("words")
                                    val wordsList = mutableListOf<String>()
                                    for (w in 0 until words.length()) {
                                        val wordText = words.getJSONObject(w).optString("text")
                                        if (wordText.isNotBlank()) {
                                            wordsList.add(wordText)
                                        }
                                    }
                                    wordsList.joinToString(" ")
                                }
                                else -> ""
                            }
                            if (lineText.isNotBlank()) {
                                stringBuilder.append(lineText).append("\n")
                            }
                        }
                    }
                }
            } else if (textObj.has("blocks")) {
                val blocks = textObj.getJSONArray("blocks")
                for (b in 0 until blocks.length()) {
                    val lines = blocks.getJSONObject(b).optJSONArray("lines") ?: continue
                    for (l in 0 until lines.length()) {
                        val lineObj = lines.getJSONObject(l)
                        val lineText = when {
                            lineObj.has("text") -> lineObj.optString("text")
                            lineObj.has("words") -> {
                                val words = lineObj.getJSONArray("words")
                                val wordsList = mutableListOf<String>()
                                for (w in 0 until words.length()) {
                                    val wordText = words.getJSONObject(w).optString("text")
                                    if (wordText.isNotBlank()) {
                                        wordsList.add(wordText)
                                    }
                                }
                                wordsList.joinToString(" ")
                            }
                            else -> ""
                        }
                        if (lineText.isNotBlank()) {
                            stringBuilder.append(lineText).append("\n")
                        }
                    }
                }
            } else {
                throw Exception("Yandex Cloud OCR response did not contain pages or blocks field: $responseText")
            }
            
            val rawText = stringBuilder.toString()
            if (rawText.isBlank()) {
                throw Exception("Yandex Vision OCR did not recognize any text on the image.")
            }
            
            parseReceiptTextWithYandexGPT(rawText)
        }
    }

    fun parseFuelReceipt(token: String, qr: String): FuelReceiptParseResult {
        val connection = (URL("${BackendConfig.BASE_URL}/receipts/fuel/parse").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(JSONObject().put("qr", qr).toString())
        }
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Не удалось проверить чек")
        }
        val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val fuel = response.optJSONObject("fuel")
        return FuelReceiptParseResult(
            totalAmount = response.optNullableDouble("totalAmount") ?: fuel?.optNullableDouble("amount"),
            dateTime = response.optString("dateTime").takeIf(String::isNotBlank)
                ?: response.optJSONObject("qr")?.optString("dateTime")?.takeIf(String::isNotBlank),
            stationName = response.optString("stationName").takeIf(String::isNotBlank),
            stationAddress = response.optString("stationAddress").takeIf(String::isNotBlank),
            fuelType = fuel?.optString("fuelType")?.takeIf(String::isNotBlank),
            liters = fuel?.optNullableDouble("liters"),
            pricePerLiter = fuel?.optNullableDouble("pricePerLiter"),
            needsReceiptDetails = response.optBoolean("needsReceiptDetails", false)
        )
    }
}

private fun JSONObject.optNullableDouble(name: String): Double? =
    if (has(name) && !isNull(name)) optDouble(name).takeIf { !it.isNaN() } else null
