# FinTrack

Android için kişisel finans yönetimi uygulaması. Multi Module mimariyle geliştirilmiştir, gerçek bir Ktor backend'e bağlıdır.

---

## Ekran Görüntüleri

> *(Yakında)*

---

## Teknoloji Stack'i

| | |
|---|---|
| **Dil** | Kotlin 2.2.10 |
| **UI** | Jetpack Compose + Material 3 |
| **Mimari** | Clean Architecture · Multi-Module · Offline-first |
| **DI** | Koin 4.0 |
| **Async** | Coroutines + Flow |
| **Network** | Retrofit + OkHttp + Kotlinx Serialization |
| **Yerel Depolama** | Room |
| **Güvenlik** | EncryptedSharedPreferences + Android Keystore + Biometric |
| **Test** | JUnit 5 · MockK · MockWebServer · Turbine |
| **Build** | AGP 9.2.1 · Gradle Version Catalog |
| **CI/CD** | GitHub Actions · ktlint · Detekt |
| **Backend** | Ktor 3 · Exposed · H2 · JWT · BCrypt |

---

## Mimari

FinTrack, **Clean Architecture** prensiplerini **Multi Module** Gradle proje yapısıyla uygular. Her modülün tek bir sorumluluğu vardır.

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
        
  backend            ← bağımsız Ktor modülü, ayrı çalışan süreç
```

**Bağımlılık kuralı:** `:domain` hiçbir şeye bağımlı değildir. Her şey `:domain`'e bağımlıdır. Data katmanı, domain interface'lerini implemente eder — asla tersine. `:backend`, Android modüllerinden tamamen bağımsızdır; aynı repoda durur ama kendi JVM sürecinde, ayrı bir uygulama olarak çalışır.

### Modüllere Genel Bakış

| Modül | Sorumluluk |
|---|---|
| `:app` | NavGraph, uygulama giriş noktası, Koin kurulumu |
| `:domain` | Entity'ler, use case'ler, repository interface'leri, Result tipi |
| `:data` | Repository implementasyonları, Room veritabanı, backend ile sync |
| `:feature:auth` | Giriş / kayıt ekranları |
| `:feature:home` | Dashboard, finansal özet |
| `:feature:transactions` | İşlem listesi, filtreleme, ekleme |
| `:feature:budget` | Bütçe oluşturma ve takip |
| `:core:network` | Retrofit client, interceptor'lar, hata haritalama |
| `:core:security` | Token depolama, Keystore, Biometric kimlik doğrulama |
| `:core:ui` | Design system, ortak Compose bileşenleri |
| `:core:testing` | Paylaşılan test yardımcıları, fake repository'ler, fixture'lar |
| `:backend` | Ktor HTTP API — auth, transaction/budget CRUD |

---

## Özellikler

- **İşlem takibi** — kategorilere göre gelir ve gider kayıtları
- **Bütçe yönetimi** — aylık bütçeler ve limit aşım uyarıları
- **JWT kimlik doğrulama** — gerçek backend ile register/login
- **Biometric kilit** — hassas işlemler için parmak izi / yüz tanıma
- **Offline-first** — Single Source of Truth olarak Room; backend'den pull sync
- **Finansal dashboard** — aylık özet, gelir/gider dağılımı, son işlemler

---

## Backend Mimarisi

`:backend`, Ktor üzerine yazılmış, öğrenme/demo amaçlı **sade tutulmuş** bir HTTP API'dir. Production'da olması gereken bazı özellikler (refresh token rotasyonu, e-posta doğrulama, rate limiting) bilinçli olarak kapsam dışı bırakılmıştır.

```
İstek
  → ContentNegotiation (JSON serialization)
  → Authentication (JWT doğrulama, korumalı route'lar için)
  → Routing (auth / transactions / budgets)
  → Repository (Exposed üzerinden SQL)
  → H2 (in-memory veritabanı)
```

**Veritabanı:** H2, in-memory modda çalışır — Docker kurulumu gerektirmez, anında ayağa kalkar. Bunun maliyeti: **backend her yeniden başlatıldığında tüm veri sıfırlanır.** Bu, gösteri/öğrenme projesi için kabul edilebilir bir trade-off'tur; production'da PostgreSQL'e geçiş, sadece `DatabaseFactory.kt`'deki bağlantı satırını değiştirmeyi gerektirir (Exposed sorgu kodu aynı kalır).

**Kimlik doğrulama:** Şifreler BCrypt ile hash'lenir (asla düz metin saklanmaz). Login/register sonrası üretilen JWT, 7 gün geçerlidir ve `Authorization: Bearer <token>` header'ı ile her korumalı isteğe eklenir.

### API Endpoint'leri

| Method | Path | Auth | Açıklama |
|---|---|---|---|
| POST | `/auth/register` | ✗ | Yeni kullanıcı kaydı, JWT döner |
| POST | `/auth/login` | ✗ | Giriş, JWT döner |
| GET | `/transactions` | ✓ | Giriş yapan kullanıcının tüm işlemleri |
| POST | `/transactions` | ✓ | Yeni işlem oluşturur |
| DELETE | `/transactions/{id}` | ✓ | İşlem siler |
| GET | `/budgets` | ✓ | Giriş yapan kullanıcının tüm bütçeleri |
| POST | `/budgets` | ✓ | Yeni bütçe oluşturur |

---

## Senkronizasyon Modeli

Android tarafı **offline-first** çalışır: UI her zaman Room'dan okur, backend'den asla doğrudan değil.

```
Home ekranı açılır
   → SyncTransactionsUseCase tetiklenir
        → backend'den GET /transactions ile tüm liste çekilir
        → Room'a upsert edilir (insertAll, REPLACE stratejisi)
   → Room'daki değişiklik otomatik olarak Flow ile UI'a yayılır
   → Kullanıcı yeni işlem eklerse: önce Room'a yazılır (isSynced=false),
     arkadan backend'e POST edilir, başarılı olursa isSynced=true işaretlenir
```

Bu, basit bir **pull sync** stratejisidir (production'da "son sync zamanından beri değişenler" gibi incremental bir yaklaşım gerekir). İnternet yokken eklenen işlemler `isSynced=false` kalır ve bir sonraki sync'te backend'e gönderilmeyi bekler.

---

## Güvenlik

Token depolama, **Android Keystore + EncryptedSharedPreferences** üzerine inşa edilmiştir.

```
Android Keystore (donanım destekli, TEE/StrongBox)
    └── MasterKey (AES256-GCM)
            └── EncryptedSharedPreferences
                    └── access_token → AES256-GCM şifreli
```

Şifreleme anahtarı, güvenli donanım enklavından asla düz metin olarak çıkmaz. Doğrudan dosya sistemi erişimi olsa bile token okunamaz.

**Biometric kimlik doğrulama**, `BiometricPrompt` API'sini `callbackFlow` ile sarar ve ViewModel katmanına temiz bir `Flow<BiometricResult>` olarak sunar.

---

## Network Katmanı

```
İstek
  → AuthInterceptor          (Bearer token'ı otomatik ekler)
  → HttpLoggingInterceptor
  → Retrofit
  → NetworkExceptionMapper   (HTTP hatalarını tipli DomainException'a dönüştürür)
→ Result<T>
```

`AuthInterceptor`, token kaynağını `TokenProvider` interface'i üzerinden çözer — `EncryptedSharedPreferences`'ın kendisi hakkında bilgisi yoktur (Dependency Inversion). Bu bağlantı Koin tarafından çalışma zamanında kurulur.

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

```
Unit testler       (JUnit 5 + MockK + Turbine)   → use case'ler, mapper'lar, ViewModel'ler
Entegrasyon        (MockWebServer)               → Retrofit client, interceptor'lar
Instrumented       (AndroidJUnit4, gerçek cihaz)  → Room veritabanı
```

Room testleri **instrumented** olarak çalışır çünkü SQLite motoru gerçek Android framework'üne ihtiyaç duyar — bu yüzden JUnit4 kullanır (`AndroidJUnit4` henüz JUnit5'i resmi desteklemiyor), JVM unit testlerinden ayrı bir kategoridir.

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
val result = authApi.login(LoginRequest("user@example.com", "password"))
val recorded = mockWebServer.takeRequest()

assertEquals("/auth/login", recorded.path)
assertEquals("true", recorded.getHeader("No-Auth"))
```

---

## CI/CD

GitHub Actions, her push/PR'da otomatik olarak:

1. **ktlint** — kod formatı kontrolü
2. **Detekt** — statik analiz (karmaşıklık, isimlendirme, kod kokuları)
3. **Unit testler** — tüm modüllerin JVM testleri

Detekt config'i (`config/detekt/detekt.yml`), projenin gerçek ihtiyaçlarına göre ince ayarlıdır: Composable fonksiyonlar uzunluk/parametre kurallarından muaftır, guard clause pattern'i kabul edilir, test dosyalarındaki backtick'li fonksiyon adları desteklenir.

Instrumented testler (emülatör gerektirdiği için) CI pipeline'ına dahil edilmemiştir — sadece lokal geliştirmede çalıştırılır.

---

## Kurulum ve Çalıştırma

**Gereksinimler:** Android Studio Quail (2026.1.1+) · JDK 17 · Android SDK 26+

```bash
git clone https://github.com/hbacak07/FinTrack.git
cd FinTrack
```

### 1. Backend'i başlat

```bash
./gradlew :backend:run
```

Sunucu `http://localhost:8080` adresinde ayağa kalkar. Konsolda şu satırı görene kadar bekle:

```
Responding at http://0.0.0.0:8080
```

> **Not:** Backend H2 in-memory veritabanı kullanır — her yeniden başlatmada veri sıfırlanır. Bu bilinçli bir tasarım kararıdır (Docker gerektirmeden anında çalışabilmesi için).

### 2. (Opsiyonel) Dummy veri ekle

Boş bir uygulama yerine gerçekçi veriyle test etmek için, proje köküne `seed-data.sh` script'ini ekleyip çalıştır:

```bash
chmod +x seed-data.sh
./seed-data.sh
```

Bu, `seed@fintrack.com` / `seed1234` kullanıcısını oluşturur (zaten varsa login olur), birkaç gelir/gider işlemi ve bütçe ekler. Backend'i her yeniden başlattığında bu script'i tekrar çalıştırman gerekir.

### 3. Android uygulamasını çalıştır

Android Studio'da projeyi aç, sync'in tamamlanmasını bekle, bir emülatör seç ve **Run** butonuna bas.

> Emülatör, `10.0.2.2` adresini host makinenin `localhost`'una yönlendirir — bu yüzden `core:network`'teki `baseUrl`, `http://10.0.2.2:8080/` olarak ayarlıdır. Gerçek bir cihazda test ediyorsan, bu adresi makinenin yerel ağ IP'siyle değiştirmen gerekir.

Uygulama açıldığında, `seed@fintrack.com` / `seed1234` ile (ya da yeni bir hesap oluşturarak) giriş yapabilirsin.

### Testleri çalıştır

```bash
# Tüm unit testler
./gradlew test

# Belirli bir modül
./gradlew :domain:test
./gradlew :feature:home:testDebugUnitTest

# Room instrumented testleri (emülatör/cihaz gerekir)
./gradlew :data:connectedDebugAndroidTest

# Kod kalitesi
./gradlew ktlintCheck
./gradlew detekt
```

---

## Proje Durumu

| Adım | Durum |
|---|---|
| Proje kurulumu, Gradle yapılandırması | ✅ Tamamlandı |
| Domain katmanı | ✅ Tamamlandı |
| Core Network | ✅ Tamamlandı |
| Core Security | ✅ Tamamlandı |
| Data katmanı (Room) | ✅ Tamamlandı |
| Core UI + Feature: Auth | ✅ Tamamlandı |
| Feature: Home | ✅ Tamamlandı |
| Feature: Transactions | ✅ Tamamlandı |
| Feature: Budget | ✅ Tamamlandı |
| Test katmanı (unit + instrumented) | ✅ Tamamlandı |
| CI/CD (GitHub Actions, ktlint, Detekt) | ✅ Tamamlandı |
| Ktor backend (JWT, Exposed, H2) | ✅ Tamamlandı |
| Backend ↔ Room senkronizasyonu | ✅ Tamamlandı |

### Bilinçli Olarak Kapsam Dışı Bırakılanlar

Bu proje bir öğrenme/portföy çalışması olduğu için, bazı production-grade özellikler kasıtlı olarak basitleştirildi:

- **Refresh token rotasyonu yok** — JWT 7 gün sabit geçerli, süresi dolunca yeniden login gerekir
- **PostgreSQL yerine H2 in-memory** — Docker'sız, anında çalışan bir geliştirme deneyimi için
- **Bottom navigation eksik** — Home, Transactions, Budget ekranları arasında geçiş için henüz bir navigasyon çubuğu yok; her ekran kendi feature modülünde bağımsız test edilebilir durumda
- **Incremental sync yok** — `syncTransactions()` her seferinde backend'deki tüm listeyi çeker, "son sync'ten beri değişenler" optimizasyonu yapılmıyor

---