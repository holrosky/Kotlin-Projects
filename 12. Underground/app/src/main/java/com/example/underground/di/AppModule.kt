package com.example.underground.di

import android.app.Activity
import com.example.underground.data.api.StationApi
import com.example.underground.data.api.StationStorageApi
import com.example.underground.data.db.AppDatabase
import com.example.underground.data.preference.PreferenceManager
import com.example.underground.data.preference.SharedPreferenceManager
import com.example.underground.data.repository.StationRepository
import com.example.underground.data.repository.StationRepositoryImpl
import com.example.underground.presenter.stations.StationsContract
import com.example.underground.presenter.stations.StationsFragment
import com.example.underground.presenter.stations.StationsPresenter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single { Dispatchers.IO }

    // Database
    single { AppDatabase.build(androidApplication()) }
    single { get<AppDatabase>().stationDao() }

    // Preference
    single { androidContext().getSharedPreferences("preference", Activity.MODE_PRIVATE) }
    single<PreferenceManager> { SharedPreferenceManager(get()) }

    // Api
    single<StationApi> { StationStorageApi(Firebase.storage) }

    // Repository
    single<StationRepository> { StationRepositoryImpl(get(), get(), get(), get()) }

    // Presenter
    scope<StationsFragment> {
        scoped<StationsContract.Presenter> { StationsPresenter(getSource()!!, get()) }
    }
}