package ru.diploma.autocareledger.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import ru.diploma.autocareledger.data.repository.GarageRepository

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: GarageRepository by inject()
    private val accountPrefs: android.content.SharedPreferences by inject(named("account_profile"))

    override suspend fun doWork(): Result {
        val token = accountPrefs.getString("token", null)
        if (token.isNullOrBlank()) {
            return Result.failure()
        }

        return try {
            repository.syncToServer(applicationContext, token)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
