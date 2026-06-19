package com.hbacakk.fintrack.feature.auth.di

import com.hbacakk.fintrack.domain.usecase.auth.LoginUseCase
import com.hbacakk.fintrack.feature.auth.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Auth feature'ının Koin modülü.
 *
 * viewModel { }: Koin'in ViewModel'lere özel DSL fonksiyonu.
 * Bu, ViewModel'in Android lifecycle'ına bağlı olarak
 * doğru şekilde scope'lanmasını sağlar — Activity/Fragment
 * yeniden oluşturulduğunda ViewModel hayatta kalır.
 */
val authModule = module {

    // UseCase — AuthRepository, data modülünden Koin'e otomatik bağlanacak
    factory { LoginUseCase(get()) }

    viewModel { LoginViewModel(get()) }
}