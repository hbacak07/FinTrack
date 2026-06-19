package com.hbacakk.fintrack

import android.app.Application
import com.hbacakk.fintrack.core.network.di.networkModule
import com.hbacakk.fintrack.core.security.di.securityModule
import com.hbacakk.fintrack.data.di.dataModule
import com.hbacakk.fintrack.feature.auth.di.authModule
import com.hbacakk.fintrack.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Uygulamanın giriş noktası.
 *
 * Koin'i burada başlatıyoruz — tüm modüller burada
 * tek bir listede toplanır. Yeni bir feature modülü
 * eklediğinde, onun Koin modülünü buraya eklemen gerekir.
 */
class FinTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FinTrackApplication)
            modules(
                networkModule,
                securityModule,
                dataModule,
                authModule,
                homeModule
            )
        }
    }
}