package ru.diploma.autocareledger.backend

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.security.SecureRandom
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun main() {
    embeddedServer(
        Netty,
        host = System.getenv("AUTOCARE_HOST") ?: "0.0.0.0",
        port = System.getenv("AUTOCARE_PORT")?.toIntOrNull() ?: 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val databasePath = System.getenv("AUTOCARE_DB") ?: "autocare-ledger.db"
    DatabaseFactory.init(databasePath)

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Некорректный запрос"))
        }
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "time" to Instant.now().toString()))
        }

        get("/admin") {
            val page = object {}.javaClass.getResource("/admin/index.html")
                ?.readText()
                ?: "<h1>Admin page not found</h1>"
            call.respondText(page, ContentType.Text.Html)
        }

        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val response = AccountService.register(request)
                call.respond(HttpStatusCode.Created, response)
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val response = AccountService.login(request)
                if (response == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Неверный email или пароль"))
                } else {
                    call.respond(response)
                }
            }
        }

        get("/cars/catalog") {
            call.respond(CarCatalogService.list())
        }

        post("/admin/catalog") {
            call.requireUserId() ?: return@post
            val request = call.receive<CreateCatalogEntryRequest>()
            call.respond(HttpStatusCode.Created, CarCatalogService.add(request))
        }

        put("/admin/catalog/{catalogId}") {
            call.requireUserId() ?: return@put
            val catalogId = call.parameters["catalogId"]?.toLongOrNull()
            if (catalogId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id записи каталога"))
                return@put
            }
            val request = call.receive<CreateCatalogEntryRequest>()
            val updated = CarCatalogService.update(catalogId, request)
            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Запись каталога не найдена"))
            } else {
                call.respond(updated)
            }
        }

        delete("/admin/catalog/{catalogId}") {
            call.requireUserId() ?: return@delete
            val catalogId = call.parameters["catalogId"]?.toLongOrNull()
            if (catalogId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id записи каталога"))
                return@delete
            }
            val deleted = CarCatalogService.delete(catalogId)
            if (!deleted) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Запись каталога не найдена"))
            } else {
                call.respond(mapOf("deleted" to true))
            }
        }

        post("/cars/photos") {
            val userId = call.requireUserId() ?: return@post
            call.respond(HttpStatusCode.Created, PhotoService.upload(userId, call.receiveMultipart()))
        }

        post("/receipts/fuel/parse") {
            call.requireUserId() ?: return@post
            val request = call.receive<ParseFuelReceiptRequest>()
            call.respond(ReceiptParser.parse(request))
        }

        get("/sync/snapshot") {
            val userId = call.requireUserId() ?: return@get
            call.respond(SyncService.getSnapshot(userId))
        }

        put("/sync/snapshot") {
            val userId = call.requireUserId() ?: return@put
            val request = call.receive<SyncSnapshotDto>()
            call.respond(SyncService.saveSnapshot(userId, request))
        }

        staticFiles("/uploads", File("uploads"))

        route("/garage") {
            get("/snapshot") {
                val userId = call.requireUserId() ?: return@get
                call.respond(SyncService.getSnapshot(userId))
            }

            put("/snapshot") {
                val userId = call.requireUserId() ?: return@put
                val request = call.receive<SyncSnapshotDto>()
                call.respond(SyncService.saveSnapshot(userId, request))
            }

            get("/cars") {
                val userId = call.requireUserId() ?: return@get
                if (call.request.queryParameters["backup"] == "1") {
                    call.respond(SyncService.getSnapshot(userId))
                    return@get
                }
                call.respond(GarageService.listCars(userId))
            }

            put("/cars") {
                val userId = call.requireUserId() ?: return@put
                if (call.request.queryParameters["backup"] != "1") {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный запрос"))
                    return@put
                }
                val request = call.receive<SyncSnapshotDto>()
                call.respond(SyncService.saveSnapshot(userId, request))
            }

            post("/cars") {
                val userId = call.requireUserId() ?: return@post
                if (call.request.queryParameters["backup"] == "1") {
                    val request = call.receive<SyncSnapshotDto>()
                    call.respond(SyncService.saveSnapshot(userId, request))
                    return@post
                }
                val request = call.receive<CreateUserCarRequest>()
                call.respond(HttpStatusCode.Created, GarageService.addCar(userId, request))
            }

            get("/cars/{carId}/expenses") {
                val userId = call.requireUserId() ?: return@get
                val carId = call.parameters["carId"]?.toLongOrNull()
                if (carId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id автомобиля"))
                    return@get
                }
                call.respond(RecordsService.listExpenses(userId, carId))
            }

            post("/cars/{carId}/expenses") {
                val userId = call.requireUserId() ?: return@post
                val carId = call.parameters["carId"]?.toLongOrNull()
                if (carId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id автомобиля"))
                    return@post
                }
                val request = call.receive<CreateExpenseRequest>()
                call.respond(HttpStatusCode.Created, RecordsService.addExpense(userId, carId, request))
            }

            get("/cars/{carId}/reminders") {
                val userId = call.requireUserId() ?: return@get
                val carId = call.parameters["carId"]?.toLongOrNull()
                if (carId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id автомобиля"))
                    return@get
                }
                call.respond(RecordsService.listReminders(userId, carId))
            }

            post("/cars/{carId}/reminders") {
                val userId = call.requireUserId() ?: return@post
                val carId = call.parameters["carId"]?.toLongOrNull()
                if (carId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id автомобиля"))
                    return@post
                }
                val request = call.receive<CreateReminderRequest>()
                call.respond(HttpStatusCode.Created, RecordsService.addReminder(userId, carId, request))
            }

            put("/reminders/{reminderId}") {
                val userId = call.requireUserId() ?: return@put
                val reminderId = call.parameters["reminderId"]?.toLongOrNull()
                if (reminderId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id напоминания"))
                    return@put
                }
                val request = call.receive<UpdateReminderRequest>()
                val updated = RecordsService.updateReminder(userId, reminderId, request)
                if (updated == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Напоминание не найдено"))
                } else {
                    call.respond(updated)
                }
            }
        }

        route("/gas-stations") {
            get("/prices") {
                val prices = transaction {
                    GasStationPrices.selectAll()
                        .orderBy(GasStationPrices.reportedAt to SortOrder.DESC)
                        .map {
                            GasStationPriceDto(
                                id = it[GasStationPrices.id].value,
                                stationName = it[GasStationPrices.stationName],
                                address = it[GasStationPrices.address],
                                latitude = it[GasStationPrices.latitude],
                                longitude = it[GasStationPrices.longitude],
                                fuelType = it[GasStationPrices.fuelType],
                                price = it[GasStationPrices.price],
                                reportedAt = it[GasStationPrices.reportedAt]
                            )
                        }
                }
                call.respond(prices)
            }

            post("/prices") {
                call.requireUserId() ?: return@post
                val request = call.receive<ReportGasStationPriceRequest>()
                val priceId = transaction {
                    val existing = GasStationPrices.selectAll().where {
                        (GasStationPrices.latitude eq request.latitude) and
                        (GasStationPrices.longitude eq request.longitude) and
                        (GasStationPrices.fuelType eq request.fuelType)
                    }.firstOrNull()

                    if (existing != null) {
                        GasStationPrices.update({ GasStationPrices.id eq existing[GasStationPrices.id] }) {
                            it[stationName] = request.stationName.trim()
                            it[address] = request.address.trim()
                            it[price] = request.price
                            it[reportedAt] = System.currentTimeMillis()
                        }
                        existing[GasStationPrices.id].value
                    } else {
                        GasStationPrices.insertAndGetId {
                            it[stationName] = request.stationName.trim()
                            it[address] = request.address.trim()
                            it[latitude] = request.latitude
                            it[longitude] = request.longitude
                            it[fuelType] = request.fuelType.trim()
                            it[price] = request.price
                            it[reportedAt] = System.currentTimeMillis()
                        }.value
                    }
                }
                call.respond(HttpStatusCode.Created, mapOf("id" to priceId))
            }
        }

        // --- PUBLIC AND USER SHARING STATISTICS API ---
        route("/api") {
            route("/public") {
                get("/stats") {
                    val snapshots = transaction {
                        UserBackupSnapshots.selectAll().map { it[UserBackupSnapshots.payload] }
                    }
                    var totalCars = 0
                    var totalExpenses = 0.0
                    var totalMileage = 0.0
                    val brandPopularity = mutableMapOf<String, Int>()
                    val expensesByCategory = mutableMapOf<String, Double>()
                    val fuelTypeDistribution = mutableMapOf<String, Int>()
                    var carMileageCount = 0

                    snapshots.forEach { jsonStr ->
                        runCatching {
                            val snap = Json.decodeFromString<SyncSnapshotDto>(jsonStr)
                            snap.cars.forEach { car ->
                                totalCars++
                                val brand = car.brand.trim()
                                if (brand.isNotEmpty()) {
                                    brandPopularity[brand] = brandPopularity.getOrDefault(brand, 0) + 1
                                }
                                val fuel = car.fuelType.trim()
                                if (fuel.isNotEmpty()) {
                                    fuelTypeDistribution[fuel] = fuelTypeDistribution.getOrDefault(fuel, 0) + 1
                                }
                                if (car.mileage > 0) {
                                    totalMileage += car.mileage
                                    carMileageCount++
                                }
                            }
                            snap.expenses.forEach { exp ->
                                totalExpenses += exp.amount
                                val cat = normalizeCategory(exp.category)
                                expensesByCategory[cat] = expensesByCategory.getOrDefault(cat, 0.0) + exp.amount
                            }
                        }
                    }

                    val avgMileage = if (carMileageCount > 0) totalMileage / carMileageCount else 0.0
                    val avgExpensePerKm = if (totalMileage > 0.0) totalExpenses / totalMileage else 0.0

                    val fuelPrices = transaction {
                        GasStationPrices.selectAll().map {
                            it[GasStationPrices.fuelType] to it[GasStationPrices.price]
                        }
                    }.groupBy { it.first }
                     .map { (fuelType, pairs) ->
                         val avgPrice = pairs.map { it.second }.average()
                         PublicFuelPriceDto(fuelType, if (avgPrice.isNaN()) 0.0 else avgPrice)
                     }

                    call.respond(
                        PublicStatsDto(
                            totalCars = totalCars,
                            totalExpenses = totalExpenses,
                            averageMileage = avgMileage,
                            brandPopularity = brandPopularity,
                            expensesByCategory = expensesByCategory,
                            fuelTypeDistribution = fuelTypeDistribution,
                            averageExpensePerKm = avgExpensePerKm,
                            fuelPrices = fuelPrices
                        )
                    )
                }

                get("/shared-cars") {
                    val sharedList = transaction {
                        SharedCars.selectAll().mapNotNull { row ->
                            val userIdVal = row[SharedCars.userId].value
                            val carIdVal = row[SharedCars.carId]
                            val tokenVal = row[SharedCars.token]

                            val ownerName = Users.selectAll().where { Users.id eq userIdVal }
                                .firstOrNull()?.get(Users.name) ?: "Пользователь"

                            val snapshotJson = UserBackupSnapshots.selectAll().where { UserBackupSnapshots.userId eq userIdVal }
                                .firstOrNull()?.get(UserBackupSnapshots.payload) ?: return@mapNotNull null

                            val snapshot = runCatching { Json.decodeFromString<SyncSnapshotDto>(snapshotJson) }.getOrNull() ?: return@mapNotNull null
                            val car = snapshot.cars.firstOrNull { it.id == carIdVal } ?: return@mapNotNull null

                            val carExpenses = snapshot.expenses.filter { it.carId == carIdVal }
                            val totalExpenses = carExpenses.sumOf { it.amount }

                            PublicCarDto(
                                brand = car.brand,
                                model = car.model,
                                generation = car.generation,
                                restyling = car.restyling,
                                trim = car.trim,
                                year = car.year,
                                mileage = car.mileage,
                                colorHex = car.colorHex,
                                colorName = car.colorName,
                                imageUrl = car.photoUri ?: "",
                                ownerName = ownerName,
                                token = tokenVal,
                                totalExpenses = totalExpenses,
                                recordsCount = carExpenses.size
                            )
                        }
                    }
                    call.respond(sharedList)
                }

                get("/shared-cars/{token}") {
                    val tokenParam = call.parameters["token"]
                    if (tokenParam.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный токен"))
                        return@get
                    }
                    val detail = transaction {
                        val row = SharedCars.selectAll().where { SharedCars.token eq tokenParam }.firstOrNull()
                            ?: return@transaction null

                        val userIdVal = row[SharedCars.userId].value
                        val carIdVal = row[SharedCars.carId]

                        val ownerName = Users.selectAll().where { Users.id eq userIdVal }
                            .firstOrNull()?.get(Users.name) ?: "Пользователь"

                        val snapshotJson = UserBackupSnapshots.selectAll().where { UserBackupSnapshots.userId eq userIdVal }
                            .firstOrNull()?.get(UserBackupSnapshots.payload) ?: return@transaction null

                        val snapshot = runCatching { Json.decodeFromString<SyncSnapshotDto>(snapshotJson) }.getOrNull() ?: return@transaction null
                        val car = snapshot.cars.firstOrNull { it.id == carIdVal } ?: return@transaction null

                        val carExpenses = snapshot.expenses.filter { it.carId == carIdVal }
                        val totalExpenses = carExpenses.sumOf { it.amount }

                        val publicCar = PublicCarDto(
                            brand = car.brand,
                            model = car.model,
                            generation = car.generation,
                            restyling = car.restyling,
                            trim = car.trim,
                            year = car.year,
                            mileage = car.mileage,
                            colorHex = car.colorHex,
                            colorName = car.colorName,
                            imageUrl = car.photoUri ?: "",
                            ownerName = ownerName,
                            token = tokenParam,
                            totalExpenses = totalExpenses,
                            recordsCount = carExpenses.size
                        )

                        val publicExpenses = carExpenses.sortedByDescending { it.dateMillis }.map { exp ->
                            PublicExpenseDto(
                                category = normalizeCategory(exp.category),
                                amount = exp.amount,
                                mileage = exp.mileage,
                                dateMillis = exp.dateMillis,
                                title = exp.title,
                                notes = exp.notes
                            )
                        }

                        PublicCarDetailsDto(
                            car = publicCar,
                            expenses = publicExpenses
                        )
                    }

                    if (detail == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Общий автомобиль не найден"))
                    } else {
                        call.respond(detail)
                    }
                }

                get("/buyer-guide/options") {
                    val snapshots = transaction {
                        UserBackupSnapshots.selectAll().map { it[UserBackupSnapshots.payload] }
                    }
                    val brandModels = mutableMapOf<String, MutableSet<String>>()
                    snapshots.forEach { jsonStr ->
                        runCatching {
                            val snap = Json.decodeFromString<SyncSnapshotDto>(jsonStr)
                            snap.cars.forEach { car ->
                                val brand = car.brand.trim().takeIf { it.isNotEmpty() } ?: return@forEach
                                val model = car.model.trim().takeIf { it.isNotEmpty() } ?: return@forEach
                                brandModels.getOrPut(brand) { mutableSetOf() }.add(model)
                            }
                        }
                    }
                    val options = brandModels.entries.sortedBy { it.key }.map { (brand, models) ->
                        BrandModelOptionDto(brand, models.sorted().toList())
                    }
                    call.respond(options)
                }

                get("/buyer-guide/stats/{brand}/{model}") {
                    val brandParam = call.parameters["brand"]?.trim()
                    val modelParam = call.parameters["model"]?.trim()
                    if (brandParam.isNullOrBlank() || modelParam.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Укажите марку и модель"))
                        return@get
                    }

                    val snapshots = transaction {
                        UserBackupSnapshots.selectAll().map { it[UserBackupSnapshots.payload] }
                    }

                    val matchingCars = mutableListOf<SyncCarDto>()
                    val matchingCarIds = mutableSetOf<Long>()
                    val matchingExpenses = mutableListOf<SyncExpenseDto>()

                    snapshots.forEach { jsonStr ->
                        runCatching {
                            val snap = Json.decodeFromString<SyncSnapshotDto>(jsonStr)
                            snap.cars.forEach { car ->
                                if (car.brand.equals(brandParam, ignoreCase = true) && 
                                    car.model.equals(modelParam, ignoreCase = true)) {
                                    matchingCars.add(car)
                                    matchingCarIds.add(car.id)
                                }
                            }
                            snap.expenses.forEach { exp ->
                                if (matchingCarIds.contains(exp.carId)) {
                                    matchingExpenses.add(exp)
                                }
                            }
                        }
                    }

                    if (matchingCars.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Нет данных для указанной модели"))
                        return@get
                    }

                    val carCount = matchingCars.size
                    val totalExpenses = matchingExpenses.sumOf { it.amount }
                    val averageMileage = matchingCars.map { it.mileage }.average().let { if (it.isNaN()) 0.0 else it }
                    val totalMileage = matchingCars.sumOf { it.mileage.toDouble() }
                    val averageExpensePerKm = if (totalMileage > 0.0) totalExpenses / totalMileage else 0.0

                    // Calculate maintenance expenses (ТО, Ремонт, Запчасти, Шины)
                    val maintenanceCategories = setOf("ремонт", "запчасти", "сервис", "то", "шины")
                    val maintenanceExpenses = matchingExpenses.filter {
                        normalizeCategory(it.category).lowercase() in maintenanceCategories
                    }
                    val totalMaintenanceCost = maintenanceExpenses.sumOf { it.amount }
                    val maintenanceRatio = if (totalExpenses > 0.0) totalMaintenanceCost / totalExpenses else 0.0
                    val averageMaintenanceCost = totalMaintenanceCost / carCount

                    // Category breakdown
                    val categoryBreakdown = matchingExpenses.groupBy {
                        normalizeCategory(it.category)
                    }.mapValues { (_, list) -> list.sumOf { it.amount } }

                    // Common repairs logic
                    val repairStats = maintenanceExpenses.mapNotNull { exp ->
                        val cleanPart = extractPartName(exp.title, exp.partName)
                        if (cleanPart.isNotEmpty()) {
                            cleanPart to exp
                        } else {
                            null
                        }
                    }.groupBy { it.first }
                     .map { (partName, pairs) ->
                         val list = pairs.map { it.second }
                         val count = list.size
                         val avgMileage = list.map { it.mileage }.filter { it > 0 }.average().let { if (it.isNaN()) 0 else it.toInt() }
                         val avgCost = list.map { it.amount }.average().let { if (it.isNaN()) 0.0 else it }
                         BuyerRepairStatDto(partName, count, avgMileage, avgCost)
                     }
                     .sortedByDescending { it.count }
                     .take(8)

                    call.respond(
                        BuyerStatsDto(
                            brand = brandParam,
                            model = modelParam,
                            carCount = carCount,
                            averageMileage = averageMileage,
                            averageTotalExpense = totalExpenses / carCount,
                            averageExpensePerKm = averageExpensePerKm,
                            maintenanceRatio = maintenanceRatio,
                            averageMaintenanceCost = averageMaintenanceCost,
                            categoryBreakdown = categoryBreakdown,
                            commonRepairs = repairStats
                        )
                    )
                }
            }

            route("/user") {
                get("/shared-cars") {
                    val userId = call.requireUserId() ?: return@get
                    val snapshotJson = transaction {
                        UserBackupSnapshots.selectAll().where { UserBackupSnapshots.userId eq userId }
                            .firstOrNull()?.get(UserBackupSnapshots.payload)
                    }
                    if (snapshotJson == null) {
                        call.respond(emptyList<UserCarShareStatusDto>())
                        return@get
                    }
                    val snapshot = runCatching { Json.decodeFromString<SyncSnapshotDto>(snapshotJson) }.getOrNull()
                    if (snapshot == null) {
                        call.respond(emptyList<UserCarShareStatusDto>())
                        return@get
                    }

                    val sharedCarIds = transaction {
                        SharedCars.selectAll().where { SharedCars.userId eq userId }
                            .associate { it[SharedCars.carId] to it[SharedCars.token] }
                    }

                    val userCars = snapshot.cars.map { car ->
                        val token = sharedCarIds[car.id]
                        UserCarShareStatusDto(
                            id = car.id,
                            brand = car.brand,
                            model = car.model,
                            year = car.year,
                            isShared = token != null,
                            token = token
                        )
                    }
                    call.respond(userCars)
                }

                post("/shared-cars/toggle") {
                    val userId = call.requireUserId() ?: return@post
                    val request = call.receive<ToggleShareRequest>()
                    val result = transaction {
                        val existing = SharedCars.selectAll().where {
                            (SharedCars.userId eq userId) and (SharedCars.carId eq request.carId)
                        }.firstOrNull()

                        if (request.shared) {
                            if (existing != null) {
                                ToggleShareResponse(request.carId, true, existing[SharedCars.token])
                            } else {
                                val tokenVal = java.util.UUID.randomUUID().toString().replace("-", "")
                                SharedCars.insert {
                                    it[SharedCars.userId] = userId
                                    it[SharedCars.carId] = request.carId
                                    it[token] = tokenVal
                                    it[createdAt] = System.currentTimeMillis()
                                }
                                ToggleShareResponse(request.carId, true, tokenVal)
                            }
                        } else {
                            SharedCars.deleteWhere {
                                (SharedCars.userId eq userId) and (SharedCars.carId eq request.carId)
                            }
                            ToggleShareResponse(request.carId, false, null)
                        }
                    }
                    call.respond(result)
                }
            }
        }

        staticResources("/", "web")
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.requireUserId(): Long? {
    val token = request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
    val userId = token?.let(AccountService::findUserIdByToken)
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse("Требуется авторизация"))
    }
    return userId
}

private object DatabaseFactory {
    fun init(databasePath: String) {
        Database.connect("jdbc:sqlite:$databasePath", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(
                Users,
                Sessions,
                CarCatalog,
                UserCars,
                Expenses,
                Reminders,
                UserBackupSnapshots,
                GasStationPrices,
                SharedCars
            )
            val conn = this.connection.connection as java.sql.Connection
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN work_cost REAL") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN parts_cost REAL") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN shop_name VARCHAR(200)") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN part_name VARCHAR(200)") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN part_number VARCHAR(100)") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN part_brand VARCHAR(100)") } }
            runCatching { conn.createStatement().use { it.execute("ALTER TABLE expenses ADD COLUMN assembly VARCHAR(100)") } }

            val resetCatalog = System.getenv("AUTOCARE_RESET_CATALOG")?.toBooleanStrictOrNull() ?: false
            if (resetCatalog || CarCatalog.selectAll().empty()) {
                CatalogSeed.replaceCatalog()
            }
        }
    }
}

private object Users : LongIdTable("users") {
    val name = varchar("name", 120)
    val email = varchar("email", 180).uniqueIndex()
    val passwordHash = varchar("password_hash", 260)
    val createdAt = long("created_at")
}

private object Sessions : LongIdTable("sessions") {
    val userId = reference("user_id", Users)
    val token = varchar("token", 120).uniqueIndex()
    val createdAt = long("created_at")
}

private object CarCatalog : LongIdTable("car_catalog") {
    val brand = varchar("brand", 80)
    val model = varchar("model", 100)
    val generation = varchar("generation", 80)
    val restyling = varchar("restyling", 100)
    val yearFrom = integer("year_from")
    val yearTo = integer("year_to")
    val trim = varchar("trim", 120)
    val fuelType = varchar("fuel_type", 60)
    val colorName = varchar("color_name", 80)
    val colorHex = varchar("color_hex", 20)
    val imageUrl = varchar("image_url", 600)
    val generationImageGroup = varchar("generation_image_group", 180)
}

private object CatalogSeed {
    fun replaceCatalog() {
        CarCatalog.deleteAll()
        models().forEach { model ->
            model.trims.forEachIndexed { index, trim ->
                val color = model.colors[index % model.colors.size]
                CarCatalog.insert {
                    it[CarCatalog.brand] = model.brand
                    it[CarCatalog.model] = model.model
                    it[CarCatalog.generation] = model.generation
                    it[CarCatalog.restyling] = model.restyling
                    it[CarCatalog.yearFrom] = model.yearFrom
                    it[CarCatalog.yearTo] = model.yearTo
                    it[CarCatalog.trim] = trim
                    it[CarCatalog.fuelType] = model.fuelType
                    it[CarCatalog.colorName] = color.name
                    it[CarCatalog.colorHex] = color.hex
                    it[CarCatalog.imageUrl] = ""
                    it[CarCatalog.generationImageGroup] = model.imageGroup
                }
            }
        }
    }

    private fun models() = listOf(
        SeedModel(
            brand = "Toyota",
            model = "Camry",
            generation = "XV70",
            restyling = "рестайлинг",
            yearFrom = 2021,
            yearTo = 2024,
            trims = listOf("Стандарт Плюс", "Elegance Safety", "GR Sport"),
            colors = listOf(
                SeedColor("Белый перламутр", "#F2F3F0"),
                SeedColor("Черный металлик", "#111214"),
                SeedColor("Серебристый металлик", "#AEB4BA")
            )
        ),
        SeedModel(
            brand = "Toyota",
            model = "Corolla",
            generation = "E210",
            restyling = "XII поколение",
            yearFrom = 2019,
            yearTo = 2024,
            trims = listOf("Стандарт", "Классик", "Престиж"),
            colors = listOf(
                SeedColor("Белый", "#F4F5F2"),
                SeedColor("Темно-синий металлик", "#1B2F52"),
                SeedColor("Красный металлик", "#9E1D2B")
            )
        ),
        SeedModel(
            brand = "Toyota",
            model = "RAV4",
            generation = "XA50",
            restyling = "V поколение",
            yearFrom = 2019,
            yearTo = 2024,
            trims = listOf("Comfort", "Prestige", "Adventure"),
            colors = listOf(
                SeedColor("Белый перламутр", "#F3F1EA"),
                SeedColor("Темно-серый металлик", "#4A4E52"),
                SeedColor("Красный металлик", "#A8242F")
            )
        ),
        SeedModel(
            brand = "Honda",
            model = "Civic",
            generation = "FE",
            restyling = "XI поколение",
            yearFrom = 2021,
            yearTo = 2025,
            trims = listOf("LX", "Sport", "Touring"),
            colors = listOf(
                SeedColor("Platinum White Pearl", "#F3F4F2"),
                SeedColor("Rallye Red", "#B4212B"),
                SeedColor("Crystal Black Pearl", "#101113")
            )
        ),
        SeedModel(
            brand = "Honda",
            model = "Accord",
            generation = "CV",
            restyling = "X поколение, рестайлинг",
            yearFrom = 2021,
            yearTo = 2022,
            trims = listOf("Sport", "EX-L", "Touring"),
            colors = listOf(
                SeedColor("Modern Steel Metallic", "#5B6268"),
                SeedColor("Lunar Silver Metallic", "#B8BDC1"),
                SeedColor("Still Night Pearl", "#1D4F8F")
            )
        ),
        SeedModel(
            brand = "Honda",
            model = "CR-V",
            generation = "RS",
            restyling = "VI поколение",
            yearFrom = 2022,
            yearTo = 2025,
            trims = listOf("EX", "Sport Hybrid", "Sport Touring Hybrid"),
            colors = listOf(
                SeedColor("Radiant Red Metallic", "#A9232C"),
                SeedColor("Urban Gray Pearl", "#626A70"),
                SeedColor("Canyon River Blue", "#273D5F")
            )
        ),
        SeedModel(
            brand = "Hyundai",
            model = "Solaris",
            generation = "HC",
            restyling = "II поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2022,
            trims = listOf("Active Plus", "Comfort", "Elegance"),
            colors = listOf(
                SeedColor("Белый", "#F5F6F3"),
                SeedColor("Серебристый", "#B4BAC0"),
                SeedColor("Красный", "#A5222D")
            )
        ),
        SeedModel(
            brand = "Hyundai",
            model = "Creta",
            generation = "SU2",
            restyling = "II поколение",
            yearFrom = 2021,
            yearTo = 2024,
            trims = listOf("Prime", "Lifestyle", "Prestige"),
            colors = listOf(
                SeedColor("Atlas White", "#F4F5F1"),
                SeedColor("Dragon Red", "#9E2430"),
                SeedColor("Abyss Black", "#111214")
            )
        ),
        SeedModel(
            brand = "Hyundai",
            model = "Tucson",
            generation = "NX4",
            restyling = "IV поколение",
            yearFrom = 2021,
            yearTo = 2024,
            trims = listOf("Family", "Lifestyle", "Prestige"),
            colors = listOf(
                SeedColor("Shimmering Silver", "#B5B9BD"),
                SeedColor("Amazon Gray", "#59635F"),
                SeedColor("Deep Sea", "#203A4E")
            )
        ),
        SeedModel(
            brand = "Kia",
            model = "Rio",
            generation = "FB",
            restyling = "IV поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2023,
            trims = listOf("Classic", "Luxe", "Prestige"),
            colors = listOf(
                SeedColor("Clear White", "#F5F5F1"),
                SeedColor("Silky Silver", "#B9BDC1"),
                SeedColor("Fiery Red", "#B0202B")
            )
        ),
        SeedModel(
            brand = "Kia",
            model = "Sportage",
            generation = "NQ5",
            restyling = "V поколение",
            yearFrom = 2021,
            yearTo = 2025,
            trims = listOf("Comfort", "Prestige", "GT-Line"),
            colors = listOf(
                SeedColor("Snow White Pearl", "#F2F3F0"),
                SeedColor("Experience Green", "#3C4D45"),
                SeedColor("Black Pearl", "#111214")
            )
        ),
        SeedModel(
            brand = "Kia",
            model = "K5",
            generation = "DL3",
            restyling = "III поколение",
            yearFrom = 2020,
            yearTo = 2024,
            trims = listOf("Luxe", "Prestige", "GT Line+"),
            colors = listOf(
                SeedColor("Snow White Pearl", "#F2F3F0"),
                SeedColor("Gravity Blue", "#1E3049"),
                SeedColor("Runway Red", "#A8202C")
            )
        ),
        SeedModel(
            brand = "Skoda",
            model = "Octavia",
            generation = "A8",
            restyling = "IV поколение",
            yearFrom = 2020,
            yearTo = 2024,
            trims = listOf("Active Plus", "Ambition Plus", "Style Plus"),
            colors = listOf(
                SeedColor("Candy White", "#F7F7F2"),
                SeedColor("Quartz Grey Metallic", "#666C72"),
                SeedColor("Race Blue Metallic", "#1F4E8C")
            )
        ),
        SeedModel(
            brand = "Skoda",
            model = "Rapid",
            generation = "NH3",
            restyling = "II поколение, лифтбек",
            yearFrom = 2020,
            yearTo = 2023,
            trims = listOf("Entry", "Active", "Ambition"),
            colors = listOf(
                SeedColor("Candy White", "#F7F7F2"),
                SeedColor("Brilliant Silver", "#B8BDC2"),
                SeedColor("Deep Black Pearl", "#101113")
            )
        ),
        SeedModel(
            brand = "Skoda",
            model = "Kodiaq",
            generation = "NS7",
            restyling = "I поколение, рестайлинг",
            yearFrom = 2021,
            yearTo = 2024,
            trims = listOf("Ambition Plus", "Style Plus", "SportLine"),
            colors = listOf(
                SeedColor("Moon White Metallic", "#F2F2EE"),
                SeedColor("Graphite Grey Metallic", "#555B60"),
                SeedColor("Velvet Red Metallic", "#7D1E2B")
            )
        ),
        SeedModel(
            brand = "Volkswagen",
            model = "Polo",
            generation = "CK",
            restyling = "VI поколение, лифтбек",
            yearFrom = 2020,
            yearTo = 2024,
            trims = listOf("Origin", "Respect", "Status"),
            colors = listOf(
                SeedColor("Pure White", "#F5F5F2"),
                SeedColor("Reflex Silver", "#B6BABE"),
                SeedColor("Deep Black Pearl", "#101113")
            )
        ),
        SeedModel(
            brand = "Volkswagen",
            model = "Tiguan",
            generation = "AD1",
            restyling = "II поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2024,
            trims = listOf("Respect", "Status", "R-Line"),
            colors = listOf(
                SeedColor("Oryx White Pearl", "#F1F2ED"),
                SeedColor("Dolphin Grey Metallic", "#666D72"),
                SeedColor("Nightshade Blue Metallic", "#1C324F")
            )
        ),
        SeedModel(
            brand = "Volkswagen",
            model = "Passat",
            generation = "B8",
            restyling = "рестайлинг",
            yearFrom = 2019,
            yearTo = 2022,
            trims = listOf("Business", "Elegance", "R-Line"),
            colors = listOf(
                SeedColor("Pure White", "#F5F5F2"),
                SeedColor("Manganese Grey Metallic", "#53595E"),
                SeedColor("Aquamarine Blue Metallic", "#24506C")
            )
        ),
        SeedModel(
            brand = "BMW",
            model = "3 Series",
            generation = "G20",
            restyling = "VII поколение",
            yearFrom = 2018,
            yearTo = 2022,
            trims = listOf("Sport Line", "Luxury Line", "M Sport"),
            colors = listOf(
                SeedColor("Alpine White", "#F4F4F0"),
                SeedColor("Black Sapphire Metallic", "#111317"),
                SeedColor("Portimao Blue Metallic", "#1D4E89")
            )
        ),
        SeedModel(
            brand = "BMW",
            model = "5 Series",
            generation = "G30",
            restyling = "VII поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2023,
            trims = listOf("Business", "Sport Line", "M Sport"),
            colors = listOf(
                SeedColor("Mineral White Metallic", "#EEEDE8"),
                SeedColor("Carbon Black Metallic", "#101927"),
                SeedColor("Phytonic Blue Metallic", "#1D425D")
            )
        ),
        SeedModel(
            brand = "BMW",
            model = "X5",
            generation = "G05",
            restyling = "IV поколение",
            yearFrom = 2018,
            yearTo = 2023,
            trims = listOf("xLine", "M Sport", "M50i"),
            colors = listOf(
                SeedColor("Alpine White", "#F4F4F0"),
                SeedColor("Arctic Grey Metallic", "#5C6266"),
                SeedColor("Manhattan Green Metallic", "#4C5548")
            )
        ),
        SeedModel(
            brand = "Mercedes-Benz",
            model = "C-Class",
            generation = "W206",
            restyling = "V поколение",
            yearFrom = 2021,
            yearTo = 2025,
            trims = listOf("Avantgarde", "AMG Line", "Sport"),
            colors = listOf(
                SeedColor("Polar White", "#F5F5F1"),
                SeedColor("Obsidian Black Metallic", "#111214"),
                SeedColor("Selenite Grey Metallic", "#62686D")
            )
        ),
        SeedModel(
            brand = "Mercedes-Benz",
            model = "E-Class",
            generation = "W213",
            restyling = "V поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2023,
            trims = listOf("Avantgarde", "Exclusive", "AMG Line"),
            colors = listOf(
                SeedColor("Polar White", "#F5F5F1"),
                SeedColor("Graphite Grey Metallic", "#555B60"),
                SeedColor("Nautic Blue Metallic", "#1D344E")
            )
        ),
        SeedModel(
            brand = "Mercedes-Benz",
            model = "GLC",
            generation = "X254",
            restyling = "II поколение",
            yearFrom = 2022,
            yearTo = 2025,
            trims = listOf("Premium", "AMG Line", "Exclusive"),
            colors = listOf(
                SeedColor("High-Tech Silver", "#B8BDC0"),
                SeedColor("Spectral Blue Metallic", "#203E63"),
                SeedColor("MANUFAKTUR Diamond White", "#F4F3EE")
            )
        ),
        SeedModel(
            brand = "Audi",
            model = "A4",
            generation = "B9",
            restyling = "рестайлинг",
            yearFrom = 2019,
            yearTo = 2023,
            trims = listOf("Design", "Sport", "S line"),
            colors = listOf(
                SeedColor("Ibis White", "#F6F6F2"),
                SeedColor("Mythos Black Metallic", "#101113"),
                SeedColor("Navarra Blue Metallic", "#1E3C64")
            )
        ),
        SeedModel(
            brand = "Audi",
            model = "A6",
            generation = "C8",
            restyling = "V поколение",
            yearFrom = 2018,
            yearTo = 2024,
            trims = listOf("Base", "Sport", "Business"),
            colors = listOf(
                SeedColor("Glacier White Metallic", "#F2F3EE"),
                SeedColor("Chronos Grey Metallic", "#5B6268"),
                SeedColor("Firmament Blue Metallic", "#1C304A")
            )
        ),
        SeedModel(
            brand = "Audi",
            model = "Q5",
            generation = "FY",
            restyling = "II поколение, рестайлинг",
            yearFrom = 2020,
            yearTo = 2024,
            trims = listOf("Base", "Advanced", "S line"),
            colors = listOf(
                SeedColor("Glacier White Metallic", "#F2F3EE"),
                SeedColor("District Green Metallic", "#465347"),
                SeedColor("Ultra Blue Metallic", "#174D8C")
            )
        ),
        SeedModel(
            brand = "Nissan",
            model = "Qashqai",
            generation = "J12",
            restyling = "III поколение",
            yearFrom = 2021,
            yearTo = 2024,
            trims = listOf("Visia", "Acenta", "Tekna"),
            colors = listOf(
                SeedColor("Pearl White", "#F2F2ED"),
                SeedColor("Magnetic Blue", "#1E4969"),
                SeedColor("Ceramic Grey", "#737A7E")
            )
        ),
        SeedModel(
            brand = "Nissan",
            model = "X-Trail",
            generation = "T33",
            restyling = "IV поколение",
            yearFrom = 2021,
            yearTo = 2025,
            trims = listOf("SE", "LE", "Tekna"),
            colors = listOf(
                SeedColor("Pearl White", "#F2F2ED"),
                SeedColor("Champagne Silver", "#B7B0A3"),
                SeedColor("Scarlet Ember", "#8F2430")
            )
        ),
        SeedModel(
            brand = "Nissan",
            model = "Sentra",
            generation = "B18",
            restyling = "VIII поколение",
            yearFrom = 2019,
            yearTo = 2024,
            trims = listOf("S", "SV", "SR"),
            colors = listOf(
                SeedColor("Aspen White", "#F4F4F0"),
                SeedColor("Super Black", "#111214"),
                SeedColor("Electric Blue Metallic", "#1D5D9C")
            )
        )
    )

    private data class SeedModel(
        val brand: String,
        val model: String,
        val generation: String,
        val restyling: String,
        val yearFrom: Int,
        val yearTo: Int,
        val trims: List<String>,
        val fuelType: String = "Бензин",
        val colors: List<SeedColor>
    ) {
        val imageGroup: String = "$brand-$model-$generation-$restyling"
    }

    private data class SeedColor(val name: String, val hex: String)
}

private object UserCars : LongIdTable("user_cars") {
    val userId = reference("user_id", Users)
    val brand = varchar("brand", 80)
    val model = varchar("model", 100)
    val generation = varchar("generation", 80).nullable()
    val restyling = varchar("restyling", 100).nullable()
    val trim = varchar("trim", 120).nullable()
    val year = integer("year")
    val plateNumber = varchar("plate_number", 20)
    val mileage = integer("mileage")
    val fuelType = varchar("fuel_type", 60)
    val colorName = varchar("color_name", 80).nullable()
    val colorHex = varchar("color_hex", 20).nullable()
    val imageUrl = varchar("image_url", 600).nullable()
    val createdAt = long("created_at")
}

private object Expenses : LongIdTable("expenses") {
    val userId = reference("user_id", Users)
    val carId = reference("car_id", UserCars)
    val category = varchar("category", 80)
    val amount = double("amount")
    val mileage = integer("mileage")
    val dateMillis = long("date_millis")
    val title = varchar("title", 160)
    val notes = text("notes")
    val workCost = double("work_cost").nullable()
    val partsCost = double("parts_cost").nullable()
    val shopName = varchar("shop_name", 200).nullable()
    val partName = varchar("part_name", 200).nullable()
    val partNumber = varchar("part_number", 100).nullable()
    val partBrand = varchar("part_brand", 100).nullable()
    val assembly = varchar("assembly", 100).nullable()
}

private object Reminders : LongIdTable("reminders") {
    val userId = reference("user_id", Users)
    val carId = reference("car_id", UserCars)
    val title = varchar("title", 160)
    val dueMileage = integer("due_mileage").nullable()
    val dueDateMillis = long("due_date_millis").nullable()
    val isCompleted = bool("is_completed")
}

private object UserBackupSnapshots : LongIdTable("user_backup_snapshots") {
    val userId = reference("user_id", Users).uniqueIndex()
    val payload = text("payload")
    val updatedAt = long("updated_at")
}

private object GasStationPrices : LongIdTable("gas_station_prices") {
    val stationName = varchar("station_name", 120)
    val address = varchar("address", 200)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val fuelType = varchar("fuel_type", 60)
    val price = double("price")
    val reportedAt = long("reported_at")
}

private object SharedCars : LongIdTable("shared_cars") {
    val userId = reference("user_id", Users)
    val carId = long("car_id")
    val token = varchar("token", 120).uniqueIndex()
    val createdAt = long("created_at")
}

private object AccountService {
    fun register(request: RegisterRequest): AuthResponse = transaction {
        val normalizedEmail = request.email.trim().lowercase()
        val existing = Users.selectAll().where { Users.email eq normalizedEmail }.firstOrNull()
        require(existing == null) { "Пользователь с таким email уже существует" }

        val userId = Users.insertAndGetId {
            it[name] = request.name.trim()
            it[email] = normalizedEmail
            it[passwordHash] = Passwords.hash(request.password)
            it[createdAt] = System.currentTimeMillis()
        }.value
        val token = createSession(userId)
        AuthResponse(token = token, user = UserDto(userId, request.name.trim(), normalizedEmail))
    }

    fun login(request: LoginRequest): AuthResponse? = transaction {
        val normalizedEmail = request.email.trim().lowercase()
        val row = Users.selectAll().where { Users.email eq normalizedEmail }.firstOrNull() ?: return@transaction null
        if (!Passwords.verify(request.password, row[Users.passwordHash])) {
            return@transaction null
        }
        val userId = row[Users.id].value
        AuthResponse(
            token = createSession(userId),
            user = UserDto(userId, row[Users.name], row[Users.email])
        )
    }

    fun findUserIdByToken(token: String): Long? = transaction {
        Sessions.selectAll().where { Sessions.token eq token }
            .firstOrNull()
            ?.get(Sessions.userId)
            ?.value
    }

    private fun createSession(userId: Long): String {
        val token = UUID.randomUUID().toString().replace("-", "")
        Sessions.insert {
            it[Sessions.userId] = userId
            it[Sessions.token] = token
            it[createdAt] = System.currentTimeMillis()
        }
        return token
    }
}

private object CarCatalogService {
    fun list(): List<CarCatalogDto> = transaction {
        CarCatalog.selectAll()
            .orderBy(
                CarCatalog.brand to SortOrder.ASC,
                CarCatalog.model to SortOrder.ASC,
                CarCatalog.trim to SortOrder.ASC
            )
            .map {
                CarCatalogDto(
                    id = it[CarCatalog.id].value,
                    brand = it[CarCatalog.brand],
                    model = it[CarCatalog.model],
                    generation = it[CarCatalog.generation],
                    restyling = it[CarCatalog.restyling],
                    yearFrom = it[CarCatalog.yearFrom],
                    yearTo = it[CarCatalog.yearTo],
                    trim = it[CarCatalog.trim],
                    fuelType = it[CarCatalog.fuelType],
                    colorName = it[CarCatalog.colorName],
                    colorHex = it[CarCatalog.colorHex],
                    imageUrl = it[CarCatalog.imageUrl],
                    generationImageGroup = it[CarCatalog.generationImageGroup]
                )
            }
    }

    fun add(request: CreateCatalogEntryRequest): CarCatalogDto = transaction {
        val normalized = request.withInheritedImage()
        val id = CarCatalog.insertAndGetId {
            fillCatalogEntry(it, normalized)
        }.value
        syncImageForModelColor(id, normalized)
        findById(id) ?: error("Созданная запись каталога не найдена")
    }

    fun update(catalogId: Long, request: CreateCatalogEntryRequest): CarCatalogDto? = transaction {
        val normalized = request.withInheritedImage(excludeId = catalogId)
        val updated = CarCatalog.update({ CarCatalog.id eq catalogId }) {
            fillCatalogEntry(it, normalized)
        }
        syncImageForModelColor(catalogId, normalized)
        if (updated == 0) null else findById(catalogId)
    }

    fun delete(catalogId: Long): Boolean = transaction {
        CarCatalog.deleteWhere { CarCatalog.id eq catalogId } > 0
    }

    private fun findById(catalogId: Long): CarCatalogDto? =
        CarCatalog.selectAll().where { CarCatalog.id eq catalogId }.firstOrNull()?.let(::toDto)

    private fun toDto(row: org.jetbrains.exposed.sql.ResultRow) = CarCatalogDto(
        id = row[CarCatalog.id].value,
        brand = row[CarCatalog.brand],
        model = row[CarCatalog.model],
        generation = row[CarCatalog.generation],
        restyling = row[CarCatalog.restyling],
        yearFrom = row[CarCatalog.yearFrom],
        yearTo = row[CarCatalog.yearTo],
        trim = row[CarCatalog.trim],
        fuelType = row[CarCatalog.fuelType],
        colorName = row[CarCatalog.colorName],
        colorHex = row[CarCatalog.colorHex],
        imageUrl = row[CarCatalog.imageUrl],
        generationImageGroup = row[CarCatalog.generationImageGroup]
    )

    private fun fillCatalogEntry(
        statement: org.jetbrains.exposed.sql.statements.UpdateBuilder<*>,
        request: CreateCatalogEntryRequest
    ) {
        statement[CarCatalog.brand] = request.brand.trim()
        statement[CarCatalog.model] = request.model.trim()
        statement[CarCatalog.generation] = request.generation.trim()
        statement[CarCatalog.restyling] = request.restyling.trim()
        statement[CarCatalog.yearFrom] = request.yearFrom
        statement[CarCatalog.yearTo] = request.yearTo
        statement[CarCatalog.trim] = request.trim.trim()
        statement[CarCatalog.fuelType] = request.fuelType.trim()
        statement[CarCatalog.colorName] = request.colorName.trim()
        statement[CarCatalog.colorHex] = request.colorHex.trim()
        statement[CarCatalog.imageUrl] = request.imageUrl.trim()
        statement[CarCatalog.generationImageGroup] = request.generationImageGroup?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "${request.brand}-${request.model}-${request.generation}-${request.restyling}"
    }

    private fun CreateCatalogEntryRequest.withInheritedImage(excludeId: Long? = null): CreateCatalogEntryRequest {
        val explicitImage = imageUrl.trim()
        if (explicitImage.isNotBlank()) return this
        val group = generationImageGroup?.trim()?.takeIf(String::isNotBlank)
            ?: "$brand-$model-$generation-$restyling"
        val inherited = CarCatalog.selectAll().where {
            (CarCatalog.generationImageGroup eq group) and
                (CarCatalog.colorName eq colorName.trim())
        }.firstOrNull { excludeId == null || it[CarCatalog.id].value != excludeId }
            ?.get(CarCatalog.imageUrl)
            .orEmpty()
        return copy(imageUrl = inherited)
    }

    private fun syncImageForModelColor(catalogId: Long, request: CreateCatalogEntryRequest) {
        val image = request.imageUrl.trim()
        if (image.isBlank()) return
        val group = request.generationImageGroup?.trim()?.takeIf(String::isNotBlank)
            ?: "${request.brand}-${request.model}-${request.generation}-${request.restyling}"
        CarCatalog.update({
            (CarCatalog.generationImageGroup eq group) and
                (CarCatalog.colorName eq request.colorName.trim())
        }) {
            it[imageUrl] = image
            it[colorHex] = request.colorHex.trim()
        }
    }
}

private object ReceiptParser {
    private val fuelPatterns = listOf(
        "АИ-100" to Regex("""(?iu)\b(?:АИ|AI)\s*-?\s*100\b|G[-\s]?Drive\s*100"""),
        "АИ-98" to Regex("""(?iu)\b(?:АИ|AI)\s*-?\s*98\b|G[-\s]?Drive\s*98"""),
        "АИ-95" to Regex("""(?iu)\b(?:АИ|AI)\s*-?\s*95\b|G[-\s]?Drive\s*95|Pulsar\s*95"""),
        "АИ-92" to Regex("""(?iu)\b(?:АИ|AI)\s*-?\s*92\b|G[-\s]?Drive\s*92|Pulsar\s*92"""),
        "ДТ" to Regex("""(?iu)\bДТ\b|дизель|diesel"""),
        "Газ" to Regex("""(?iu)\b(?:СУГ|LPG|газ)\b""")
    )
    private val numberPattern = Regex("""\d+(?:[,.]\d+)?""")

    fun parse(request: ParseFuelReceiptRequest): FuelReceiptParseResponse {
        val qr = request.qr?.let(::parseQr)
        val fnsReceipt = if (request.items.isEmpty() && request.receiptText.isNullOrBlank()) {
            request.qr?.let(FnsReceiptClient::fetchReceipt)
        } else {
            null
        }
        val receiptText = request.receiptText ?: fnsReceipt?.receiptText
        val receiptItems = request.items.ifEmpty { fnsReceipt?.items.orEmpty() }
        val structuredCandidates = receiptItems.mapNotNull(::parseItem)
        val textCandidates = receiptText
            ?.lineSequence()
            ?.mapNotNull(::parseLine)
            ?.toList()
            .orEmpty()
        val candidates = (structuredCandidates + textCandidates)
            .distinctBy { listOf(it.itemName, it.fuelType, it.liters, it.amount, it.pricePerLiter) }
            .sortedWith(
                compareByDescending<FuelPurchaseCandidate> { it.confidence }
                    .thenByDescending { it.amount ?: 0.0 }
            )
        val best = candidates.firstOrNull()
        return FuelReceiptParseResponse(
            qr = qr,
            totalAmount = qr?.totalAmount ?: request.totalAmount ?: best?.amount,
            dateTime = qr?.dateTime ?: request.dateTime,
            stationName = request.stationName ?: detectStationName(receiptText),
            stationAddress = detectStationAddress(receiptText),
            fnsStatus = fnsReceipt?.status ?: FnsReceiptClient.configurationStatus(),
            fuel = best,
            candidates = candidates,
            needsReceiptDetails = request.items.isEmpty() && request.receiptText.isNullOrBlank() && best == null
        )
    }

    private fun parseQr(raw: String): ReceiptQrDto {
        val query = raw.substringAfter('?', raw)
        val fields = query.split('&')
            .mapNotNull { part ->
                val key = part.substringBefore('=', "").takeIf(String::isNotBlank) ?: return@mapNotNull null
                val value = part.substringAfter('=', "")
                URLDecoder.decode(key, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
            }
            .toMap()
        return ReceiptQrDto(
            raw = raw,
            dateTime = fields["t"],
            totalAmount = fields["s"]?.replace(',', '.')?.toDoubleOrNull(),
            fiscalDriveNumber = fields["fn"],
            fiscalDocumentNumber = fields["i"],
            fiscalSign = fields["fp"],
            operationType = fields["n"]
        )
    }

    private fun parseItem(item: ReceiptItemInput): FuelPurchaseCandidate? {
        val fuelType = detectFuelType(item.name) ?: return null
        val liters = item.quantity?.takeIf { it > 0.0 }
        val amount = item.sum ?: liters?.let { quantity -> item.price?.let { quantity * it } }
        val pricePerLiter = item.price ?: amount?.let { total -> liters?.takeIf { it > 0.0 }?.let { total / it } }
        return FuelPurchaseCandidate(
            fuelType = fuelType,
            liters = liters,
            amount = amount,
            pricePerLiter = pricePerLiter,
            itemName = item.name.trim(),
            confidence = confidence(liters, amount, pricePerLiter, structured = true)
        )
    }

    private fun parseLine(line: String): FuelPurchaseCandidate? {
        val fuelType = detectFuelType(line) ?: return null
        val normalized = fuelPatterns.fold(line) { value, (_, pattern) -> pattern.replace(value, " ") }
        val numbers = numberPattern.findAll(normalized)
            .mapNotNull { it.value.replace(',', '.').toDoubleOrNull() }
            .filter { it > 0.0 }
            .toList()
        val estimate = estimateFuelNumbers(numbers) ?: return FuelPurchaseCandidate(
            fuelType = fuelType,
            liters = null,
            amount = null,
            pricePerLiter = null,
            itemName = line.trim(),
            confidence = 0.35
        )
        return FuelPurchaseCandidate(
            fuelType = fuelType,
            liters = estimate.liters,
            amount = estimate.amount,
            pricePerLiter = estimate.pricePerLiter,
            itemName = line.trim(),
            confidence = confidence(estimate.liters, estimate.amount, estimate.pricePerLiter, structured = false)
        )
    }

    private fun detectFuelType(value: String): String? =
        fuelPatterns.firstOrNull { (_, pattern) -> pattern.containsMatchIn(value) }?.first

    private fun estimateFuelNumbers(numbers: List<Double>): FuelNumbers? {
        if (numbers.size < 2) return null
        val candidates = mutableListOf<Pair<Double, FuelNumbers>>()
        for (liters in numbers.filter { it in 1.0..300.0 }) {
            for (price in numbers.filter { it in 20.0..300.0 && it != liters }) {
                val expectedAmount = liters * price
                val amount = numbers
                    .filter { it >= 100.0 }
                    .minByOrNull { kotlin.math.abs(it - expectedAmount) }
                if (amount != null) {
                    val diff = kotlin.math.abs(amount - expectedAmount)
                    val tolerance = maxOf(2.0, amount * 0.03)
                    if (diff <= tolerance) {
                        candidates += diff to FuelNumbers(liters, amount, price)
                    }
                }
            }
        }
        candidates.minByOrNull { it.first }?.let { return it.second }

        val amount = numbers.maxOrNull()
        val liters = numbers.firstOrNull { it in 1.0..300.0 && it != amount }
        val price = amount?.let { total -> liters?.takeIf { it > 0.0 }?.let { total / it } }
            ?.takeIf { it in 20.0..300.0 }
        return if (amount != null && liters != null) FuelNumbers(liters, amount, price) else null
    }

    private fun confidence(liters: Double?, amount: Double?, pricePerLiter: Double?, structured: Boolean): Double {
        var score = if (structured) 0.55 else 0.4
        if (liters != null) score += 0.2
        if (amount != null) score += 0.15
        if (pricePerLiter != null) score += 0.1
        return score.coerceAtMost(1.0)
    }

    private fun detectStationName(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val stationPattern = Regex("""(?iu)(АЗС|ЛУКОЙЛ|LUKOIL|Газпромнефть|GPN|Роснефть|Татнефть|Башнефть|Shell|BP|Нефтьмагистраль).*""")
        return text.lineSequence()
            .map(String::trim)
            .firstOrNull { stationPattern.containsMatchIn(it) }
    }

    private fun detectStationAddress(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val addressPattern = Regex("""(?iu)(область|край|республика|г\.\s*|город\s+).*(ул\.|улица|проспект|пр-т|шоссе|д\.)""")
        return text.lineSequence()
            .map(String::trim)
            .firstOrNull { addressPattern.containsMatchIn(it) }
    }

    private data class FuelNumbers(
        val liters: Double,
        val amount: Double,
        val pricePerLiter: Double?
    )
}

private object FnsReceiptClient {
    private val masterToken = System.getenv("FNS_OPENAPI_MASTER_TOKEN").orEmpty()
    private val authUrl = System.getenv("FNS_OPENAPI_AUTH_URL").orEmpty()
    private val receiptUrl = System.getenv("FNS_OPENAPI_RECEIPT_URL").orEmpty()
    private val receiptTemplate = System.getenv("FNS_OPENAPI_RECEIPT_TEMPLATE").orEmpty()
    private var cachedToken: String? = null
    private var cachedTokenExpiresAt: Long = 0L

    fun configurationStatus(): String? = when {
        masterToken.isBlank() || authUrl.isBlank() || receiptUrl.isBlank() ->
            "FNS_DISABLED: задайте FNS_OPENAPI_MASTER_TOKEN, FNS_OPENAPI_AUTH_URL и FNS_OPENAPI_RECEIPT_URL"
        receiptTemplate.isBlank() ->
            "FNS_TEMPLATE_MISSING: задайте FNS_OPENAPI_RECEIPT_TEMPLATE по схеме, выданной ФНС"
        else -> null
    }

    fun fetchReceipt(qr: String): FnsReceiptFetchResult? {
        val status = configurationStatus()
        if (status != null) return FnsReceiptFetchResult(status = status)
        val qrFields = parseQrFields(qr)
        if (qrFields.isEmpty()) return FnsReceiptFetchResult(status = "FNS_SKIPPED: QR не содержит фискальные реквизиты")
        return runCatching {
            val temporaryToken = temporaryToken()
            val requestBody = buildReceiptRequest(qrFields)
            val responseText = postXml(receiptUrl, requestBody, temporaryToken)
            FnsReceiptFetchResult(
                receiptText = responseText,
                items = extractItems(responseText),
                status = "FNS_OK"
            )
        }.getOrElse { error ->
            FnsReceiptFetchResult(status = "FNS_ERROR: ${error.message ?: error::class.simpleName}")
        }
    }

    private fun temporaryToken(): String {
        val now = System.currentTimeMillis()
        cachedToken?.takeIf { now < cachedTokenExpiresAt - 60_000 }?.let { return it }
        val authRequest = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="urn://x-artefacts-gnivc-ru/ais3/kkt/AuthService/types/1.0">
              <soapenv:Header/>
              <soapenv:Body>
                <tns:GetMessageRequest>
                  <tns:Message>
                    <tns:AuthRequest>
                      <tns:AuthAppInfo>
                        <tns:MasterToken>${masterToken.escapeXml()}</tns:MasterToken>
                      </tns:AuthAppInfo>
                    </tns:AuthRequest>
                  </tns:Message>
                </tns:GetMessageRequest>
              </soapenv:Body>
            </soapenv:Envelope>
        """.trimIndent()
        val response = postXml(authUrl, authRequest, token = null)
        val token = Regex("""<[^:>]*:?Token>(.*?)</[^:>]*:?Token>""").find(response)?.groupValues?.get(1)
            ?: throw IllegalStateException("ФНС не вернула временный токен")
        val expireTime = Regex("""<[^:>]*:?ExpireTime>(.*?)</[^:>]*:?ExpireTime>""").find(response)?.groupValues?.get(1)
        cachedToken = token
        cachedTokenExpiresAt = expireTime?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: (System.currentTimeMillis() + 50 * 60 * 1000)
        return token
    }

    private fun buildReceiptRequest(fields: Map<String, String>): String {
        val placeholders = mapOf(
            "qr" to fields.entries.joinToString("&") { "${it.key}=${it.value}" },
            "dateTime" to fields["t"].orEmpty(),
            "totalAmount" to fields["s"].orEmpty(),
            "fn" to fields["fn"].orEmpty(),
            "fd" to fields["i"].orEmpty(),
            "fiscalDocumentNumber" to fields["i"].orEmpty(),
            "fp" to fields["fp"].orEmpty(),
            "fiscalSign" to fields["fp"].orEmpty(),
            "operationType" to fields["n"].orEmpty()
        )
        return placeholders.entries.fold(receiptTemplate) { body, (key, value) ->
            body.replace("{$key}", value.escapeXml())
        }
    }

    private fun postXml(url: String, body: String, token: String?): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 25_000
            doOutput = true
            setRequestProperty("Content-Type", "text/xml; charset=utf-8")
            setRequestProperty("Accept", "text/xml, application/xml, application/json, text/plain")
            token?.let { setRequestProperty("FNS-OpenApi-Token", it) }
        }
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
        val responseText = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("HTTP ${connection.responseCode}: $responseText")
        }
        return responseText
    }

    private fun extractItems(raw: String): List<ReceiptItemInput> {
        val objectPattern = Regex("""(?is)\{[^{}]*(?:"name"|"Name"|"Наименование")[^{}]*}""")
        return objectPattern.findAll(raw)
            .mapNotNull { match ->
                val block = match.value
                val name = findJsonString(block, "name") ?: findJsonString(block, "Name") ?: findJsonString(block, "Наименование")
                if (name.isNullOrBlank()) return@mapNotNull null
                ReceiptItemInput(
                    name = name,
                    quantity = findJsonNumber(block, "quantity") ?: findJsonNumber(block, "Quantity"),
                    price = findJsonNumber(block, "price") ?: findJsonNumber(block, "Price"),
                    sum = findJsonNumber(block, "sum") ?: findJsonNumber(block, "Sum")
                )
            }
            .toList()
    }

    private fun findJsonString(block: String, key: String): String? =
        Regex(""""${Regex.escape(key)}"\\s*:\\s*"([^"]*)"""").find(block)?.groupValues?.get(1)

    private fun findJsonNumber(block: String, key: String): Double? =
        Regex(""""${Regex.escape(key)}"\\s*:\\s*(\\d+(?:[,.]\\d+)?)""")
            .find(block)
            ?.groupValues
            ?.get(1)
            ?.replace(',', '.')
            ?.toDoubleOrNull()

    private fun parseQrFields(raw: String): Map<String, String> {
        val query = raw.substringAfter('?', raw)
        return query.split('&')
            .mapNotNull { part ->
                val key = part.substringBefore('=', "").takeIf(String::isNotBlank) ?: return@mapNotNull null
                val value = part.substringAfter('=', "")
                URLDecoder.decode(key, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
            }
            .toMap()
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}

private data class FnsReceiptFetchResult(
    val receiptText: String? = null,
    val items: List<ReceiptItemInput> = emptyList(),
    val status: String
)

private object PhotoService {
    suspend fun upload(userId: Long, multipart: io.ktor.http.content.MultiPartData): PhotoUploadResponse {
        val uploadDir = File("uploads/$userId").apply { mkdirs() }
        var uploadedUrl: String? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "photo") {
                val extension = part.originalFileName
                    ?.substringAfterLast('.', "")
                    ?.lowercase()
                    ?.takeIf { it in setOf("jpg", "jpeg", "png", "webp") }
                    ?: "jpg"
                val fileName = "${UUID.randomUUID()}.$extension"
                val target = File(uploadDir, fileName)
                part.provider().toInputStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                uploadedUrl = "/uploads/$userId/$fileName"
            }
            part.dispose()
        }

        return PhotoUploadResponse(
            url = uploadedUrl ?: throw IllegalArgumentException("Файл photo не найден")
        )
    }
}

private object GarageService {
    fun listCars(userId: Long): List<UserCarDto> = transaction {
        UserCars.selectAll().where { UserCars.userId eq userId }
            .map(::toUserCarDto)
    }

    fun addCar(userId: Long, request: CreateUserCarRequest): UserCarDto = transaction {
        val carId = UserCars.insertAndGetId {
            it[UserCars.userId] = userId
            it[brand] = request.brand.trim()
            it[model] = request.model.trim()
            it[generation] = request.generation?.trim()
            it[restyling] = request.restyling?.trim()
            it[trim] = request.trim?.trim()
            it[year] = request.year
            it[plateNumber] = request.plateNumber.trim()
            it[mileage] = request.mileage
            it[fuelType] = request.fuelType.trim()
            it[colorName] = request.colorName?.trim()
            it[colorHex] = request.colorHex?.trim()
            it[imageUrl] = request.imageUrl?.trim()
            it[createdAt] = System.currentTimeMillis()
        }.value
        UserCars.selectAll().where { UserCars.id eq carId }.first().let(::toUserCarDto)
    }

    fun ownsCar(userId: Long, carId: Long): Boolean = transaction {
        UserCars.selectAll()
            .where { (UserCars.id eq carId) and (UserCars.userId eq userId) }
            .count() > 0
    }

    private fun toUserCarDto(row: org.jetbrains.exposed.sql.ResultRow) = UserCarDto(
        id = row[UserCars.id].value,
        brand = row[UserCars.brand],
        model = row[UserCars.model],
        generation = row[UserCars.generation],
        restyling = row[UserCars.restyling],
        trim = row[UserCars.trim],
        year = row[UserCars.year],
        plateNumber = row[UserCars.plateNumber],
        mileage = row[UserCars.mileage],
        fuelType = row[UserCars.fuelType],
        colorName = row[UserCars.colorName],
        colorHex = row[UserCars.colorHex],
        imageUrl = row[UserCars.imageUrl]
    )
}

private object RecordsService {
    fun listExpenses(userId: Long, carId: Long): List<ExpenseDto> = transaction {
        if (!GarageService.ownsCar(userId, carId)) return@transaction emptyList()
        Expenses.selectAll()
            .where { (Expenses.userId eq userId) and (Expenses.carId eq carId) }
            .orderBy(Expenses.dateMillis to SortOrder.DESC)
            .map(::toExpenseDto)
    }

    fun addExpense(userId: Long, carId: Long, request: CreateExpenseRequest): ExpenseDto = transaction {
        require(GarageService.ownsCar(userId, carId)) { "Автомобиль не найден" }
        val expenseId = Expenses.insertAndGetId {
            it[Expenses.userId] = userId
            it[Expenses.carId] = carId
            it[category] = request.category.trim()
            it[amount] = request.amount
            it[mileage] = request.mileage
            it[dateMillis] = request.dateMillis ?: System.currentTimeMillis()
            it[title] = request.title.trim()
            it[notes] = request.notes.trim()
            it[workCost] = request.workCost
            it[partsCost] = request.partsCost
            it[shopName] = request.shopName?.trim()
            it[partName] = request.partName?.trim()
            it[partNumber] = request.partNumber?.trim()
            it[partBrand] = request.partBrand?.trim()
            it[assembly] = request.assembly?.trim()
        }.value
        Expenses.selectAll().where { Expenses.id eq expenseId }.first().let(::toExpenseDto)
    }

    fun listReminders(userId: Long, carId: Long): List<ReminderDto> = transaction {
        if (!GarageService.ownsCar(userId, carId)) return@transaction emptyList()
        Reminders.selectAll()
            .where { (Reminders.userId eq userId) and (Reminders.carId eq carId) }
            .orderBy(Reminders.isCompleted to SortOrder.ASC)
            .map(::toReminderDto)
    }

    fun addReminder(userId: Long, carId: Long, request: CreateReminderRequest): ReminderDto = transaction {
        require(GarageService.ownsCar(userId, carId)) { "Автомобиль не найден" }
        val reminderId = Reminders.insertAndGetId {
            it[Reminders.userId] = userId
            it[Reminders.carId] = carId
            it[title] = request.title.trim()
            it[dueMileage] = request.dueMileage
            it[dueDateMillis] = request.dueDateMillis
            it[isCompleted] = false
        }.value
        Reminders.selectAll().where { Reminders.id eq reminderId }.first().let(::toReminderDto)
    }

    fun updateReminder(userId: Long, reminderId: Long, request: UpdateReminderRequest): ReminderDto? = transaction {
        val row = Reminders.selectAll()
            .where { (Reminders.id eq reminderId) and (Reminders.userId eq userId) }
            .firstOrNull() ?: return@transaction null
        Reminders.update({ Reminders.id eq row[Reminders.id] }) {
            it[isCompleted] = request.isCompleted
        }
        Reminders.selectAll().where { Reminders.id eq reminderId }.first().let(::toReminderDto)
    }

    private fun toExpenseDto(row: org.jetbrains.exposed.sql.ResultRow) = ExpenseDto(
        id = row[Expenses.id].value,
        carId = row[Expenses.carId].value,
        category = row[Expenses.category],
        amount = row[Expenses.amount],
        mileage = row[Expenses.mileage],
        dateMillis = row[Expenses.dateMillis],
        title = row[Expenses.title],
        notes = row[Expenses.notes],
        workCost = row[Expenses.workCost],
        partsCost = row[Expenses.partsCost],
        shopName = row[Expenses.shopName],
        partName = row[Expenses.partName],
        partNumber = row[Expenses.partNumber],
        partBrand = row[Expenses.partBrand],
        assembly = row[Expenses.assembly]
    )

    private fun toReminderDto(row: org.jetbrains.exposed.sql.ResultRow) = ReminderDto(
        id = row[Reminders.id].value,
        carId = row[Reminders.carId].value,
        title = row[Reminders.title],
        dueMileage = row[Reminders.dueMileage],
        dueDateMillis = row[Reminders.dueDateMillis],
        isCompleted = row[Reminders.isCompleted]
    )
}

private object SyncService {
    fun getSnapshot(userId: Long): SyncSnapshotDto = transaction {
        val row = UserBackupSnapshots.selectAll()
            .where { UserBackupSnapshots.userId eq userId }
            .firstOrNull()
            ?: return@transaction SyncSnapshotDto()
        Json.decodeFromString(SyncSnapshotDto.serializer(), row[UserBackupSnapshots.payload])
    }

    fun saveSnapshot(userId: Long, snapshot: SyncSnapshotDto): SyncStatusDto = transaction {
        val now = System.currentTimeMillis()
        val existing = UserBackupSnapshots.selectAll()
            .where { UserBackupSnapshots.userId eq userId }
            .firstOrNull()
        var normalizedSnapshot = snapshot.copy(updatedAt = now)
        if (existing != null && snapshot.hasNoGarageData()) {
            val existingSnapshot = Json.decodeFromString(
                SyncSnapshotDto.serializer(),
                existing[UserBackupSnapshots.payload]
            )
            if (!existingSnapshot.hasNoGarageData()) {
                return@transaction SyncStatusDto(
                    synced = false,
                    updatedAt = existing[UserBackupSnapshots.updatedAt],
                    cars = existingSnapshot.cars.size,
                    expenses = existingSnapshot.expenses.size,
                    reminders = existingSnapshot.reminders.size
                )
            }
        }
        if (existing != null && snapshot.reminders.isEmpty()) {
            val existingSnapshot = Json.decodeFromString(
                SyncSnapshotDto.serializer(),
                existing[UserBackupSnapshots.payload]
            )
            if (existingSnapshot.reminders.isNotEmpty() && !snapshot.hasNoGarageData()) {
                normalizedSnapshot = normalizedSnapshot.copy(reminders = existingSnapshot.reminders)
            }
        }
        val payload = Json.encodeToString(SyncSnapshotDto.serializer(), normalizedSnapshot)
        if (existing == null) {
            UserBackupSnapshots.insert {
                it[UserBackupSnapshots.userId] = userId
                it[UserBackupSnapshots.payload] = payload
                it[updatedAt] = now
            }
        } else {
            UserBackupSnapshots.update({ UserBackupSnapshots.userId eq userId }) {
                it[UserBackupSnapshots.payload] = payload
                it[updatedAt] = now
            }
        }
        SyncStatusDto(
            synced = true,
            updatedAt = now,
            cars = normalizedSnapshot.cars.size,
            expenses = normalizedSnapshot.expenses.size,
            reminders = normalizedSnapshot.reminders.size
        )
    }

    private fun SyncSnapshotDto.hasNoGarageData(): Boolean =
        cars.isEmpty() && expenses.isEmpty() && reminders.isEmpty()
}

private object Passwords {
    private const val Iterations = 120_000
    private const val KeyLength = 256
    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(16).also(random::nextBytes)
        val derived = derive(password, salt)
        return listOf(
            Iterations.toString(),
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(derived)
        ).joinToString(":")
    }

    fun verify(password: String, encoded: String): Boolean {
        val parts = encoded.split(":")
        if (parts.size != 3) return false
        val salt = Base64.getDecoder().decode(parts[1])
        val expected = Base64.getDecoder().decode(parts[2])
        val actual = derive(password, salt)
        return expected.contentEquals(actual)
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, Iterations, KeyLength)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: UserDto)

@Serializable
data class UserDto(val id: Long, val name: String, val email: String)

@Serializable
data class CarCatalogDto(
    val id: Long,
    val brand: String,
    val model: String,
    val generation: String,
    val restyling: String,
    val yearFrom: Int,
    val yearTo: Int,
    val trim: String,
    val fuelType: String,
    val colorName: String,
    val colorHex: String,
    val imageUrl: String,
    val generationImageGroup: String
)

@Serializable
data class PhotoUploadResponse(val url: String)

@Serializable
data class CreateCatalogEntryRequest(
    val brand: String,
    val model: String,
    val generation: String,
    val restyling: String,
    val yearFrom: Int,
    val yearTo: Int,
    val trim: String,
    val fuelType: String,
    val colorName: String,
    val colorHex: String,
    val imageUrl: String,
    val generationImageGroup: String? = null
)

@Serializable
data class ParseFuelReceiptRequest(
    val qr: String? = null,
    val receiptText: String? = null,
    val items: List<ReceiptItemInput> = emptyList(),
    val totalAmount: Double? = null,
    val dateTime: String? = null,
    val stationName: String? = null
)

@Serializable
data class ReceiptItemInput(
    val name: String,
    val quantity: Double? = null,
    val price: Double? = null,
    val sum: Double? = null
)

@Serializable
data class FuelReceiptParseResponse(
    val qr: ReceiptQrDto? = null,
    val totalAmount: Double? = null,
    val dateTime: String? = null,
    val stationName: String? = null,
    val stationAddress: String? = null,
    val fnsStatus: String? = null,
    val fuel: FuelPurchaseCandidate? = null,
    val candidates: List<FuelPurchaseCandidate> = emptyList(),
    val needsReceiptDetails: Boolean = false
)

@Serializable
data class ReceiptQrDto(
    val raw: String,
    val dateTime: String? = null,
    val totalAmount: Double? = null,
    val fiscalDriveNumber: String? = null,
    val fiscalDocumentNumber: String? = null,
    val fiscalSign: String? = null,
    val operationType: String? = null
)

@Serializable
data class FuelPurchaseCandidate(
    val fuelType: String,
    val liters: Double? = null,
    val amount: Double? = null,
    val pricePerLiter: Double? = null,
    val itemName: String,
    val confidence: Double
)

@Serializable
data class CreateUserCarRequest(
    val brand: String,
    val model: String,
    val generation: String? = null,
    val restyling: String? = null,
    val trim: String? = null,
    val year: Int,
    val plateNumber: String,
    val mileage: Int,
    val tankVolumeLiters: Double = 0.0,
    val fuelType: String,
    val colorName: String? = null,
    val colorHex: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class UserCarDto(
    val id: Long,
    val brand: String,
    val model: String,
    val generation: String?,
    val restyling: String?,
    val trim: String?,
    val year: Int,
    val plateNumber: String,
    val mileage: Int,
    val tankVolumeLiters: Double = 0.0,
    val fuelType: String,
    val colorName: String?,
    val colorHex: String?,
    val imageUrl: String?
)

@Serializable
data class CreateExpenseRequest(
    val category: String,
    val amount: Double,
    val mileage: Int,
    val dateMillis: Long? = null,
    val title: String,
    val notes: String = "",
    val workCost: Double? = null,
    val partsCost: Double? = null,
    val shopName: String? = null,
    val partName: String? = null,
    val partNumber: String? = null,
    val partBrand: String? = null,
    val assembly: String? = null
)

@Serializable
data class ExpenseDto(
    val id: Long,
    val carId: Long,
    val category: String,
    val amount: Double,
    val fuelLiters: Double? = null,
    val mileage: Int,
    val dateMillis: Long,
    val title: String,
    val notes: String,
    val workCost: Double? = null,
    val partsCost: Double? = null,
    val shopName: String? = null,
    val partName: String? = null,
    val partNumber: String? = null,
    val partBrand: String? = null,
    val assembly: String? = null
)

@Serializable
data class CreateReminderRequest(
    val title: String,
    val category: String = "Service",
    val dueMileage: Int? = null,
    val dueDateMillis: Long? = null,
    val repeatMileageInterval: Int? = null
)

@Serializable
data class UpdateReminderRequest(val isCompleted: Boolean)

@Serializable
data class ReminderDto(
    val id: Long,
    val carId: Long,
    val title: String,
    val category: String = "Service",
    val dueMileage: Int?,
    val dueDateMillis: Long?,
    val repeatMileageInterval: Int? = null,
    val isCompleted: Boolean
)

@Serializable
data class SyncSnapshotDto(
    val updatedAt: Long = 0,
    val profile: SyncProfileDto? = null,
    val cars: List<SyncCarDto> = emptyList(),
    val expenses: List<SyncExpenseDto> = emptyList(),
    val reminders: List<SyncReminderDto> = emptyList()
)

@Serializable
data class SyncProfileDto(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val themePreference: String = "System",
    val preferredFuelType: String = "АИ-95"
)

@Serializable
data class SyncCarDto(
    val id: Long,
    val brand: String,
    val model: String,
    val generation: String = "",
    val restyling: String = "",
    val trim: String = "",
    val year: Int,
    val plateNumber: String,
    val mileage: Int,
    val tankVolumeLiters: Double = 0.0,
    val fuelType: String,
    val colorName: String = "",
    val colorHex: String = "",
    val photoUri: String? = null,
    val isArchived: Boolean = false
)

@Serializable
data class SyncExpenseDto(
    val id: Long,
    val carId: Long,
    val category: String,
    val amount: Double,
    val fuelLiters: Double? = null,
    val mileage: Int,
    val dateMillis: Long,
    val title: String,
    val notes: String,
    val workCost: Double? = null,
    val partsCost: Double? = null,
    val shopName: String? = null,
    val partName: String? = null,
    val partNumber: String? = null,
    val partBrand: String? = null,
    val assembly: String? = null
)

@Serializable
data class SyncReminderDto(
    val id: Long,
    val carId: Long,
    val title: String,
    val category: String = "Service",
    val dueMileage: Int? = null,
    val dueDateMillis: Long? = null,
    val repeatMileageInterval: Int? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class SyncStatusDto(
    val synced: Boolean,
    val updatedAt: Long,
    val cars: Int,
    val expenses: Int,
    val reminders: Int
)

@Serializable
data class GasStationPriceDto(
    val id: Long,
    val stationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fuelType: String,
    val price: Double,
    val reportedAt: Long
)

@Serializable
data class ReportGasStationPriceRequest(
    val stationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fuelType: String,
    val price: Double
)

@Serializable
data class ToggleShareRequest(val carId: Long, val shared: Boolean)

@Serializable
data class ToggleShareResponse(val carId: Long, val shared: Boolean, val token: String? = null)

@Serializable
data class PublicCarDto(
    val brand: String,
    val model: String,
    val generation: String,
    val restyling: String,
    val trim: String,
    val year: Int,
    val mileage: Int,
    val colorHex: String,
    val colorName: String,
    val imageUrl: String,
    val ownerName: String,
    val token: String,
    val totalExpenses: Double,
    val recordsCount: Int
)

@Serializable
data class PublicCarDetailsDto(
    val car: PublicCarDto,
    val expenses: List<PublicExpenseDto>
)

@Serializable
data class PublicExpenseDto(
    val category: String,
    val amount: Double,
    val mileage: Int,
    val dateMillis: Long,
    val title: String,
    val notes: String
)

@Serializable
data class UserCarShareStatusDto(
    val id: Long,
    val brand: String,
    val model: String,
    val year: Int,
    val isShared: Boolean,
    val token: String?
)

@Serializable
data class PublicStatsDto(
    val totalCars: Int,
    val totalExpenses: Double,
    val averageMileage: Double,
    val brandPopularity: Map<String, Int>,
    val expensesByCategory: Map<String, Double>,
    val fuelTypeDistribution: Map<String, Int>,
    val averageExpensePerKm: Double,
    val fuelPrices: List<PublicFuelPriceDto>
)

@Serializable
data class PublicFuelPriceDto(
    val fuelType: String,
    val averagePrice: Double
)

@Serializable
data class BrandModelOptionDto(
    val brand: String,
    val models: List<String>
)

@Serializable
data class BuyerStatsDto(
    val brand: String,
    val model: String,
    val carCount: Int,
    val averageMileage: Double,
    val averageTotalExpense: Double,
    val averageExpensePerKm: Double,
    val maintenanceRatio: Double,
    val averageMaintenanceCost: Double,
    val categoryBreakdown: Map<String, Double>,
    val commonRepairs: List<BuyerRepairStatDto>
)

@Serializable
data class BuyerRepairStatDto(
    val partName: String,
    val count: Int,
    val averageMileage: Int,
    val averageCost: Double
)

private fun extractPartName(title: String, partName: String?): String {
    val cleanPartName = partName?.trim()?.lowercase()
    if (!cleanPartName.isNullOrBlank()) {
        return when {
            "масло" in cleanPartName && ("двс" in cleanPartName || "двигател" in cleanPartName) -> "Масло ДВС"
            "колодк" in cleanPartName -> "Тормозные колодки"
            "фильтр" in cleanPartName && "масл" in cleanPartName -> "Масляный фильтр"
            "фильтр" in cleanPartName && "воздух" in cleanPartName -> "Воздушный фильтр"
            "фильтр" in cleanPartName && "салон" in cleanPartName -> "Салонный фильтр"
            "свеч" in cleanPartName -> "Свечи зажигания"
            "грм" in cleanPartName -> "Ремень/цепь ГРМ"
            "амортизатор" in cleanPartName -> "Амортизаторы"
            "аккумулятор" in cleanPartName || "акб" in cleanPartName -> "Аккумулятор"
            "диск" in cleanPartName && "тормозн" in cleanPartName -> "Тормозные диски"
            "шина" in cleanPartName || "покрышк" in cleanPartName -> "Шины"
            else -> partName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    val t = title.lowercase()
    return when {
        "масло" in t && ("двс" in t || "двиг" in t || "мотор" in t) -> "Масло ДВС"
        "масло" in t && "коробк" in t -> "Масло КПП"
        "колодк" in t -> "Тормозные колодки"
        "фильтр" in t && "масл" in t -> "Масляный фильтр"
        "фильтр" in t && "воздух" in t -> "Воздушный фильтр"
        "фильтр" in t && "салон" in t -> "Салонный фильтр"
        "фильтр" in t -> "Фильтры"
        "свеч" in t -> "Свечи зажигания"
        "грм" in t -> "Ремень/цепь ГРМ"
        "амортизатор" in t || "стойк" in t -> "Амортизаторы"
        "аккумулятор" in t || "акб" in t -> "Аккумулятор"
        "диск" in t && "тормозн" in t -> "Тормозные диски"
        "шина" in t || "резин" in t || "колес" in t -> "Шины"
        "жидкост" in t && "тормозн" in t -> "Тормозная жидкость"
        "антифриз" in t -> "Антифриз"
        "щетк" in t || "дворник" in t -> "Дворники"
        else -> "Общий ремонт / ТО"
    }
}

private fun normalizeCategory(category: String): String {
    return when (category.trim().lowercase()) {
        "fuel", "топливо" -> "Топливо"
        "maintenance", "то" -> "ТО"
        "repair", "ремонт" -> "Ремонт"
        "parts", "spare parts", "запчасти" -> "Запчасти"
        "insurance", "страховка", "страхование" -> "Страхование"
        "wash", "car wash", "мойка" -> "Мойка"
        "parking", "парковка" -> "Парковка"
        "other", "прочее" -> "Прочее"
        "service", "сервис" -> "Сервис"
        "tires", "шины" -> "Шины"
        else -> category.trim().takeIf { it.isNotEmpty() } ?: "Другое"
    }
}

