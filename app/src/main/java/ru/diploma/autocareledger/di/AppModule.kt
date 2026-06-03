package ru.diploma.autocareledger.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.diploma.autocareledger.data.local.AutoCareDatabase
import ru.diploma.autocareledger.data.repository.GarageRepository
import ru.diploma.autocareledger.viewmodel.GarageViewModel

val appModule = module {
    // Database
    single { AutoCareDatabase.create(androidContext()) }
    single { get<AutoCareDatabase>().garageDao() }

    // SharedPreferences for Repository
    single(org.koin.core.qualifier.named("account_profile")) {
        androidContext().getSharedPreferences("account_profile", Context.MODE_PRIVATE)
    }
    single(org.koin.core.qualifier.named("app_settings")) {
        androidContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    // Repository
    single {
        GarageRepository(
            dao = get(),
            accountPrefs = get(org.koin.core.qualifier.named("account_profile")),
            appSettings = get(org.koin.core.qualifier.named("app_settings"))
        )
    }

    // ViewModel
    viewModel {
        GarageViewModel(
            repository = get(),
            context = androidContext()
        )
    }
}
