package ru.diploma.autocareledger

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import ru.diploma.autocareledger.di.appModule
import ru.diploma.autocareledger.worker.ReminderWorker

class AutoCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@AutoCareApplication)
            modules(appModule)
        }

        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        }

        // Schedule periodic reminder check
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminder_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}
