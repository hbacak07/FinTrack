# FinTrack

Android için kişisel finans yönetimi uygulaması. Multi Module mimariyle geliştirilmiştir.

---

## Ekran Görüntüleri

> *(Yakında — UI geliştirme devam ediyor)*

---

## Teknoloji Stack'i

| | |
|---|---|
| **Dil** | Kotlin 2.1.21 |
| **UI** | Jetpack Compose + Material 3 |
| **Mimari** | Clean Architecture · Multi-Module |
| **DI** | Koin 4.0 |
| **Async** | Coroutines + Flow |
| **Network** | Retrofit + OkHttp |
| **Yerel Depolama** | Room + DataStore |
| **Güvenlik** | EncryptedSharedPreferences + Android Keystore + Biometric |
| **Serializasyon** | Kotlinx Serialization |
| **Test** | JUnit 5 · MockK · MockWebServer · Turbine |
| **Build** | AGP 9.2.1 · Gradle Version Catalog · Convention Plugins |
| **Backend** | Ktor · PostgreSQL · JWT · Docker *(geliştiriliyor)* |

---

## Mimari

FinTrack, **Clean Architecture** prensiplerini **Multi Module** Gradle proje yapısıyla uygular. Her modülün tek bir sorumluluğu vardır ve modüller arası bağımlılık kuralları build seviyesinde zorlanır.

```
app
 ├── feature:auth
 ├── feature:home
 ├── feature:transactions
 └── feature:budget
        ↓
     domain          ← saf Kotlin, Android bağımlılığı yok
        ↑
      data
        ↓
  core:network
  core:security
  core:ui
  core:testing
```

**Bağımlılık kuralı:** `:domain` hiçbir şeye bağımlı değildir. Her şey `:domain`'e bağımlıdır. Data katmanı, domain interface'lerini implemente eder — asla tersine.

### Modüllere Genel Bakış

| Modül | Sorumluluk |
|---|---|
| `:app` | NavGraph, uygulama giriş noktası, Koin kurulumu |
| `:domain` | Entity'ler, use case'ler, repository interface'leri, Result tipi |
| `:data` | Repository implementasyonları, Room veritabanı, API çağrıları |
| `:feature:auth` | Giriş / kayıt ekranları |
| `:feature:home` | Dashboard, finansal özet |
| `:feature:transactions` | İşlem listesi, filtreleme, ekleme/düzenleme |
| `:feature:budget` | Bütçe oluşturma ve takip |
| `:core:network` | Retrofit client, interceptor'lar, hata haritalama |
| `:core:security` | Token depolama, Keystore, Biometric kimlik doğrulama |
| `:core:ui` | Design system, ortak Compose bileşenleri |
| `:core:testing` | Paylaşılan test yardımcıları, fake'ler, rule'lar |
| `build-logic` | Convention plugin'ler, merkezi Gradle konfigürasyonu |

---

## Özellikler

- **İşlem takibi** — kategorilere göre gelir, gider ve transfer kayıtları
- **Bütçe yönetimi** — aylık/haftalık bütçeler ve limit aşım uyarıları
- **Çoklu hesap desteği** — vadesiz, tasarruf, kredi kartı, nakit
- **Güvenli kimlik doğrulama** — refresh token rotasyonlu JWT
- **Biometric kilit** — hassas işlemler için parmak izi / yüz tanıma
- **Offline-first** — Single Source of Truth olarak Room, arka planda sync
- **Finansal dashboard** — aylık özet, en yüksek harcama kategorileri

---

## Güvenlik

Token depolama, **Android Keystore + EncryptedSharedPreferences** üzerine inşa edilmiştir.

```
Android Keystore (donanım destekli, TEE/StrongBox)
    └── MasterKey (AES256-GCM)
            └── EncryptedSharedPreferences
                    ├── access_token  → AES256-GCM şifreli
                    └── refresh_token → AES256-GCM şifreli
```

Şifreleme anahtarı, güvenli donanım enklavından asla düz metin olarak çıkmaz. Doğrudan dosya sistemi erişimi olsa bile token'lar okunamaz.

**Biometric kimlik doğrulama**, `BiometricPrompt` API'sini `callbackFlow` ile sarar ve ViewModel katmanına temiz bir `Flow<BiometricResult>` olarak sunar.

---

## Network Katmanı

Tüm API iletişimi katmanlı bir OkHttp client üzerinden geçer:

```
İstek
  → AuthInterceptor          (Bearer token'ı otomatik ekler)
  → HttpLoggingInterceptor
  → Retrofit
  → NetworkExceptionMapper   (HTTP hatalarını tipli DomainException'a dönüştürür)
→ Result<T>
```

`AuthInterceptor`, token kaynağını `TokenProvider` interface'i üzerinden çözer — `EncryptedSharedPreferences` veya herhangi bir güvenlik implementasyon detayı hakkında bilgisi yoktur. Bu bağlantı Koin tarafından çalışma zamanında kurulur.

---

## Hata Yönetimi

Tek bir `Result<T>` sealed interface, her katmanda akar:

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: DomainException) : Result<Nothing>
    data object Loading : Result<Nothing>
}
```

`DomainException`, ağ hatalarını, HTTP hatalarını, doğrulama hatalarını ve iş kuralı ihlallerini kapsayan sealed bir sınıftır — tamamen tipli, derleyici tarafından exhaustiveness kontrolü yapılır.

---

## Test

Testler, mimariyi yansıtacak şekilde katmanlandırılmıştır:

```
Unit testler    (JUnit 5 + MockK)      → domain use case'leri, mapper'lar, ViewModel'ler
Entegrasyon     (MockWebServer)        → Retrofit client, interceptor'lar, parsing
Instrumented    (Espresso + Compose)   → kritik UI akışları  [planlanıyor]
```

Network testleri, gerçek bir lokal TCP sunucusu olan **OkHttp MockWebServer** kullanır. Böylece gerçek bir backend olmadan, Retrofit client, interceptor'lar ve serialization gerçek şekilde test edilir.

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
val result = authApi.login(LoginRequest("user@example.com", "password"))
val recorded = mockWebServer.takeRequest()

assertEquals("/auth/login", recorded.path)
assertEquals("true", recorded.getHeader("No-Auth"))
```

---

## Build Sistemi

**Convention plugin'ler** (`build-logic` modülü), modüller arası Gradle tekrarını ortadan kaldırır. Her modül, türünü tek satırda bildirir:

```kotlin
// feature modülü
plugins { id("fintrack.android.feature") }

// saf Kotlin modülü
plugins { id("fintrack.kotlin.library") }
```

Tüm bağımlılık versiyonları `gradle/libs.versions.toml` (Gradle Version Catalog) üzerinden merkezi olarak yönetilir.

---

## Kurulum

**Gereksinimler:** Android Studio Quail (2026.1.1+) · JDK 17 · Android SDK 26+

```bash
git clone https://github.com/hbacak07/FinTrack.git
cd FinTrack
# Android Studio'da aç ve sync yap
```

**Unit testleri çalıştır:**

```bash
./gradlew test
```

**Belirli bir modülün testlerini çalıştır:**

```bash
./gradlew :domain:test
./gradlew :core:network:testDebugUnitTest
./gradlew :core:security:testDebugUnitTest
```

---

## Proje Durumu

| Modül | Durum |
|---|---|
| Proje kurulumu · Convention plugin'ler | ✅ Tamamlandı |
| Domain katmanı | ✅ Tamamlandı |
| Core Network | ✅ Tamamlandı |
| Core Security | ✅ Tamamlandı |
| Data katmanı | 🔄 Devam ediyor |
| Feature: Auth | ⬜ Planlanıyor |
| Feature: Home | ⬜ Planlanıyor |
| Feature: Transactions | ⬜ Planlanıyor |
| Feature: Budget | ⬜ Planlanıyor |
| CI/CD (GitHub Actions) | ⬜ Planlanıyor |
| Ktor backend | ⬜ Planlanıyor |

---

