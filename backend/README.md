# AutoCare Ledger Backend

Ktor-сервер для мобильного приложения AutoCare Ledger.

## Что уже есть

- SQLite-база данных;
- каталог автомобилей с маркой, моделью, поколением, рестайлингом, годами, комплектациями, цветами и URL изображения;
- регистрация и вход;
- PBKDF2-хеширование паролей;
- bearer-токены;
- хранение пользовательских автомобилей;
- хранение расходов и сервисных напоминаний.
- загрузка фото автомобиля через `POST /cars/photos`.

## Запуск

```powershell
.\gradlew.bat :backend:run
```

По умолчанию сервер стартует на:

```text
http://localhost:8080
```

Для Android Emulator приложение обращается к нему как:

```text
http://10.0.2.2:8080
```

Админ-интерфейс:

```text
http://localhost:8080/admin
```

## Основные endpoints

- `GET /health`
- `GET /admin`
- `GET /cars/catalog`
- `POST /cars/photos`
- `POST /receipts/fuel/parse`
- `POST /admin/catalog`
- `POST /auth/register`
- `POST /auth/login`
- `GET /garage/cars`
- `POST /garage/cars`
- `GET /garage/cars/{carId}/expenses`
- `POST /garage/cars/{carId}/expenses`
- `GET /garage/cars/{carId}/reminders`
- `POST /garage/cars/{carId}/reminders`
- `PUT /garage/reminders/{reminderId}`

Защищенные запросы используют заголовок:

```text
Authorization: Bearer <token>
```

## Парсинг топливного чека

`POST /receipts/fuel/parse` принимает QR-строку чека и/или позиции уже полученного фискального документа. Сам QR обычно содержит только реквизиты поиска чека, поэтому литры и тип топлива надежно определяются после получения деталей чека через ФНС/ОФД.

Для подключения официального Open API ФНС задайте переменные окружения backend:

```text
FNS_OPENAPI_MASTER_TOKEN=...
FNS_OPENAPI_AUTH_URL=...
FNS_OPENAPI_RECEIPT_URL=...
FNS_OPENAPI_RECEIPT_TEMPLATE=...
```

`FNS_OPENAPI_RECEIPT_TEMPLATE` должен быть SOAP/XML-шаблоном запроса по схеме, которую ФНС выдает после регистрации. Доступные плейсхолдеры: `{qr}`, `{dateTime}`, `{totalAmount}`, `{fn}`, `{fd}`, `{fiscalDocumentNumber}`, `{fp}`, `{fiscalSign}`, `{operationType}`. Если переменные не заданы, endpoint работает в локальном режиме: достает сумму/дату из QR и пытается разобрать литры, цену, тип топлива и адрес из переданного `receiptText` или `items`.

Минимальный пример с позициями чека:

```json
{
  "qr": "t=20260510T1530&s=2492.91&fn=9999999999999999&i=12345&fp=678901&n=1",
  "stationName": "АЗС Газпромнефть",
  "items": [
    {
      "name": "Бензин АИ-95-К5",
      "quantity": 42.11,
      "price": 59.2,
      "sum": 2492.91
    }
  ]
}
```

Ответ вернет `fuelType`, `liters`, `amount`, `pricePerLiter` и уверенность парсинга. Если отправить только QR без деталей чека, в ответе будет `needsReceiptDetails=true`.

## Как добавить свои фото для каталога

Вариант 1: через web-интерфейс.

1. Откройте:

```text
http://localhost:8080/admin
```

2. Зарегистрируйтесь или войдите.

3. Загрузите фото, проверьте предпросмотр и заполните форму модели.

4. Нажмите `Добавить в каталог`.

Вариант 2: вручную файлами.

1. Положите PNG/WebP/JPG в папку:

```text
backend/uploads/catalog
```

2. Запустите backend:

```powershell
.\gradlew.bat :backend:run
```

3. Фото будет доступно по URL:

```text
http://localhost:8080/uploads/catalog/my-car.png
```

Для Android Emulator используйте:

```text
http://10.0.2.2:8080/uploads/catalog/my-car.png
```

4. Укажите этот URL в сидировании каталога в `Application.kt` в нужной записи `CatalogSeed`.

Если каталог пустой, backend автоматически заполнит его тестовыми данными: 10 производителей, по 3 модели у каждого, по 3 комплектации и цвета на модель. `imageUrl` в сиде оставлен пустым, чтобы можно было добавить свои изображения позже. Если нужно принудительно очистить и заново заполнить каталог при запуске, запускайте сервер с `AUTOCARE_RESET_CATALOG=true`.

Позже можно сделать отдельную админ-панель для массовой загрузки марок, поколений, комплектаций и фото без ручного редактирования кода.

## Деплой на внешний сервер

Минимальный вариант для VPS на Ubuntu:

1. Установите JDK 17:

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless unzip
```

2. На своем компьютере соберите дистрибутив:

```powershell
.\gradlew.bat :backend:installDist
```

3. Скопируйте папку на сервер:

```text
backend/build/install/backend
```

Например в `/opt/autocare/backend`.

4. Запустите:

```bash
cd /opt/autocare/backend
AUTOCARE_PORT=8080 AUTOCARE_DB=/opt/autocare/autocare-ledger.db ./bin/backend
```

5. Откройте порт 8080:

```bash
sudo ufw allow 8080/tcp
```

6. Проверьте:

```text
http://SERVER_IP:8080/health
http://SERVER_IP:8080/admin
```

7. В Android-проекте поменяйте адрес backend в файле:

```text
app/src/main/java/ru/diploma/autocareledger/network/BackendConfig.kt
```

Например:

```kotlin
const val BASE_URL = "http://SERVER_IP:8080"
```

Для реального релиза лучше поставить Nginx и HTTPS, а потом использовать:

```kotlin
const val BASE_URL = "https://api.your-domain.ru"
```

## Про Drom и Auto.ru

Для дипломного проекта лучше не парсить Drom/Auto.ru напрямую из мобильного приложения. У них нет свободного публичного каталожного API для массового копирования фото, а автоматический парсинг может нарушать правила площадок. Правильный вариант: хранить собственный каталог на backend и наполнять его своими фото, разрешенными выгрузками или вручную подготовленными данными.
