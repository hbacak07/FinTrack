package com.hbacakk.fintrack.core.security.di

import com.hbacakk.fintrack.core.network.interceptor.TokenProvider
import com.hbacakk.fintrack.core.security.token.SecureTokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Security modülünün Koin kaydı.
 *
 * Kritik nokta: TokenProvider interface'i istenince,
 * Koin SecureTokenStorage'ı döner. Bu, Dependency Inversion'ın
 * Koin tarafında nasıl "bağlandığını" gösteriyor.
 */
val securityModule = module {

    // SecureTokenStorage'ı hem kendi tipi hem de TokenProvider olarak kaydet
    single { SecureTokenStorage(androidContext()) }

    // network modülündeki AuthInterceptor, "TokenProvider" istediğinde
    // Koin'in SecureTokenStorage'ı döndürmesi için bu binding gerekli
    single<TokenProvider> { get<SecureTokenStorage>() }
}