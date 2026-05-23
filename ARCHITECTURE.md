# 🏗️ ARCHITECTURE.md — Eczane Yönetim Sistemi (Pharmacy Management System)

## ⚠️ AI AJAN DİREKTİFİ (SYSTEM PROMPT)

> Bu doküman, projenin mimari anayasasıdır.
> Kod yazarken bu dosyadaki her maddeye **HARFİYEN** uyacaksın.
> Kendi başına tablo, alan veya özellik **UYDURMAYACAKSIN**.
> Veritabanı şeması için **DATABASE_STATE.md** dosyasına başvuracaksın.

---

## 1. TEKNOLOJİ YIĞINI (TECH STACK)

| Katman        | Teknoloji                                        |
|---------------|--------------------------------------------------|
| **Dil**       | Java 25                                          |
| **Framework** | Spring Boot 3.x, Spring Web MVC                  |
| **ORM**       | Spring Data JPA, Hibernate                       |
| **Veritabanı**| MySQL 8.x (`ddl-auto=update`)                    |
| **Güvenlik**  | Spring Security, `BCryptPasswordEncoder`          |
| **Frontend**  | Thymeleaf, HTML5, Tailwind CSS / Bootstrap 5, Vanilla JS / AJAX |
| **Test**      | JUnit 5, Mockito                                 |

---

## 2. AKADEMİK MİMARİ KURALLAR (6 ANAYASA MADDESİ)

### KURAL 1 — Katı Katmanlı Mimari (Strict Layering)

```
Controller  →  Service  →  Repository
```

- **Controller** sınıfları KESİNLİKLE `Repository` çağıramaz.
- **Controller** sınıfları yalnızca `Service` sınıflarını enjekte eder.
- Tüm iş kuralları **Service** katmanında izole edilir.
- **Repository** sınıfları yalnızca `JpaRepository` arayüzlerini extend eder; iş mantığı barındırmaz.

> 🚫 **YASAK:** `controller` paketi içinden `repository` paketine doğrudan erişim.
> ✅ **DOĞRU:** `Controller → Service → Repository` akışı.

---

### KURAL 2 — DTO Kullanımı (ZORUNLU)

- Veritabanı **Entity** sınıfları (`Drug`, `User`, `Purchase` vb.) **ASLA** Thymeleaf şablonlarına veya JSON API yanıtlarına doğrudan taşınmaz.
- Controller, formdan **Entity kabul edemez**, formdan **Entity döndüremez**.
- Veri alışverişi iki yönlü DTO'larla yapılır:
  - **`dto/request/`**: İstemciden gelen veriler (`DrugCreateRequest`, `SaleRequest`, vb.)
  - **`dto/response/`**: İstemciye dönen veriler (`InventoryResponse`, `DashboardStatsResponse`, vb.)
- Entity ↔ DTO dönüşümü **Service katmanında** gerçekleştirilir.

> 🚫 **YASAK:** `model.addAttribute("drug", drugEntity);`
> ✅ **DOĞRU:** `model.addAttribute("drug", drugResponseDto);`

---

### KURAL 3 — Strategy Design Pattern (ZORUNLU)

- Projede **uzun if-else / switch blokları YASAKTIR**.
- İlaçların **Son Kullanma Tarihi (SKT)** değerlendirmesi **Strategy Pattern** ile yapılacaktır.

```
ExpiryStrategy (Interface)
 ├── ExpiredStrategy   → daysRemaining <= 0
 ├── CriticalStrategy  → daysRemaining <= 30
 └── OkStrategy        → daysRemaining > 30
```

- Her strateji sınıfı `strategy/` paketinde bulunur.
- `ExpiryService`, strateji seçimini yapar ve ilgili stratejiyi çalıştırır.

---

### KURAL 4 — Validasyon ve Merkezi Hata Yönetimi

#### Validasyon

- Gelen DTO alanları **Hibernate Validator** ile doğrulanır:
  - `@NotBlank`, `@NotNull`, `@Min`, `@Size`, `@Email`, vb.
- Controller metotlarında `@Valid` veya `@Validated` kullanılır.

#### Global Exception Handling

- Kod içinde **dağınık try-catch YASAKTIR**.
- Özel Exception sınıfları oluşturulur:

| Exception Sınıfı             | Tetiklenme Durumu                          |
|------------------------------|--------------------------------------------|
| `DrugNotFoundException`      | Barkodla ilaç bulunamadığında              |
| `InsufficientStockException` | Satışta yeterli stok olmadığında           |
| `CustomerNotFoundException`  | Müşteri ID bulunamadığında                 |
| `DuplicateEntryException`    | UNIQUE kısıtlama ihlallerinde             |
| `OptimisticLockException`    | Eşzamanlı güncelleme çakışmasında          |

- Tüm exception'lar tek bir `@ControllerAdvice` sınıfında (`GlobalExceptionHandler`) yakalanır.
- Kullanıcıya **Toast / Alert** formatında temiz hata mesajı döndürülür.

> 🚫 **YASAK:** Service veya Controller içinde `try { ... } catch (Exception e) { ... }`
> ✅ **DOĞRU:** `throw new InsufficientStockException("Parol için yeterli stok yok.");`

---

### KURAL 5 — Optimistic Locking (Eşzamanlılık Koruması)

- İki eczacının aynı ilacı aynı anda satmasını engellemek için **kritik Entity'lerde** `@Version` anotasyonu kullanılır.
- `Drug` ve `Purchase` entity'lerinde `@Version private Long version;` alanı bulunur.
- Çakışma durumunda `OptimisticLockException` fırlatılır ve `GlobalExceptionHandler` tarafından yakalanır.

---

## 3. İŞ KURALLARI ÖZETİ (BUSINESS LOGIC)

### 3.1 FIFO Satış Mantığı (`SaleService`)

1. Satış sırasında ilaca ait `remaining_quantity > 0` olan **Purchase (Parti)** kayıtları `expiration_date ASC` sırasıyla çekilir.
2. Talep edilen miktar partilerden sırayla düşülür. Bir parti tükenirse sonrakine geçilir.
3. Her parti düşümü için **ayrı bir `SaleItem` kaydı** yazılır (`purchase_id` ile bağlı).
4. Toplam stok yetersizse → `InsufficientStockException` → **Transactional Rollback**.
5. Sepetteki ilaç **Kırmızı veya Yeşil reçeteli** ise → satış öncesi reçete/hasta bilgisi zorunludur.

### 3.2 Soft Delete (`DrugService`)

- Silme işlemi `DELETE` değildir. `isActive = false` yapılır.
- Geçmiş satış ve finans verileri korunur.

### 3.3 SKT Değerlendirme (`ExpiryService`)

- Strategy Pattern ile `daysRemaining` hesaplanır ve uygun strateji döndürülür (`EXPIRED`, `CRITICAL`, `OK`).

### 3.4 Finans Hesaplama (`FinanceService`)

- Ciro: `SaleItem.unit_price × SaleItem.quantity` toplamı (Drug tablosundaki güncel fiyat **KULLANILMAZ**).
- Kâr: `(SaleItem.unit_price - Purchase.purchase_price) × SaleItem.quantity` — parti bazlı.

---

## 4. PROJE KLASÖR HİYERARŞİSİ

```
src/main/java/com/pharmacy/
 ├── config/            # SecurityConfig, WebMvcConfig
 ├── controller/        # Web Controllers (Thymeleaf) & REST Controllers (API)
 ├── dto/
 │   ├── request/       # DrugCreateRequest, SaleRequest, vb.
 │   └── response/      # InventoryResponse, DashboardStatsResponse, vb.
 ├── entity/            # Drug, User, Purchase, Sale, SaleItem, Category, Brand, PresType, Customer
 ├── exception/         # GlobalExceptionHandler, DrugNotFoundException, InsufficientStockException, vb.
 ├── repository/        # JpaRepository arayüzleri
 ├── service/           # İş kuralları (@Service)
 └── strategy/          # ExpiryStrategy interface + implementasyonları

src/main/resources/
 ├── static/
 │   ├── css/
 │   ├── js/
 │   └── img/
 ├── templates/
 │   ├── fragments/     # navbar.html, sidebar.html, footer.html, toast.html
 │   ├── dashboard/     # index.html (Bento Box Dashboard)
 │   ├── inventory/     # list.html (Accordion görünüm), form.html
 │   ├── pos/           # sale.html (POS/Kasa ekranı)
 │   └── settings/      # category.html, brand.html, vb.
 └── application.yml    # DB config, ddl-auto=update
```

---

## 5. UI/UX TASARIM PRENSİPLERİ

| Bileşen                  | Açıklama                                                                                          |
|--------------------------|---------------------------------------------------------------------------------------------------|
| **Tema**                 | Tailwind CSS / Bootstrap 5 — bol whitespace, açık gri arka plan, net navigasyon.                  |
| **Toast Bildirimler**    | Başarı/Hata mesajları ekranın sağ üstünde 3sn Toast olarak belirir.                               |
| **Dashboard**            | Bento Box kartları: Bugünkü Ciro, Kritik SKT Partileri, Düşük Stok İlaçları. Tek tıkla SKT imha. |
| **POS Ekranı**           | Sol: AJAX barkod/isim arama. Sağ: Dinamik sepet. Varsayılan müşteri = "Misafir".                 |
| **Envanter Accordion**   | Tabloda ilaç başına tek satır (toplam stok). Tıklayınca alttan parti/SKT detayları açılır.        |
| **Fragments**            | `templates/fragments/` altında header, sidebar, footer, toast parçaları.                          |

---

## 6. MODÜLER GELİŞTİRME PROTOKOLÜ

Sistem aşağıdaki sırayla **modül modül** inşa edilir. Tümünü tek seferde yazmak **YASAKTIR**.

| Adım | Modül                                 | Kapsam                                          |
|------|---------------------------------------|------------------------------------------------|
| 1    | Entity & Repository                  | Tüm `@Entity` sınıfları ve `JpaRepository`'ler |
| 2    | Service & Strategy Pattern           | İş kuralları, FIFO, SKT stratejileri            |
| 3    | DTO, Controller & Exception Handling | Request/Response DTO, Controller, GlobalHandler |
| 4    | Thymeleaf UI                         | Dashboard, POS, Envanter, Ayarlar sayfaları     |

---

## 7. NAMING CONVENTIONS (İSİMLENDİRME KURALLARI)

| Tür             | Format               | Örnek                         |
|-----------------|----------------------|-------------------------------|
| Entity          | PascalCase           | `Drug`, `SaleItem`            |
| Repository      | PascalCase + Repo    | `DrugRepository`              |
| Service         | PascalCase + Service | `SaleService`                 |
| Controller      | PascalCase + Ctrl    | `SaleController`              |
| Request DTO     | PascalCase + Request | `DrugCreateRequest`           |
| Response DTO    | PascalCase + Response| `InventoryResponse`           |
| Exception       | PascalCase + Exception | `InsufficientStockException`|
| Strategy        | PascalCase + Strategy| `ExpiredStrategy`             |
| Thymeleaf dosya | kebab-case           | `drug-list.html`              |
| Paket           | lowercase            | `com.pharmacy.service`        |

---

### KURAL 6 — Dil, Yorum ve Format Kuralları (STRICT FORMATTING)
- **Dil:** Projedeki her şey (değişkenler, metot isimleri, commit mesajları, hata mesajları, UI metinleri ve yorum satırları) KESİNLİKLE ve SADECE **İngilizce** olacaktır.
- **Yorum Satırları:** Yapay zekanın her metot ve değişken üzerine gereksiz Javadoc/yorum satırı eklemesi YASAKTIR. Sadece çok karmaşık iş algoritmalarında (örn: FIFO hesaplaması) İngilizce tek satırlık (//) teknik açıklamalar yapılabilir. Boilerplate kodlar (Entity, Getter/Setter, basit CRUD) asla yorum içermeyecektir.
- **Emoji Yasağı:** Kodun içinde, konsol çıktılarında, hata mesajlarında, Thymeleaf arayüzünde (UI) veya commit mesajlarında kullanıcı açıkça talep etmedikçe **HİÇBİR EMOJİ KULLANILMAYACAKTIR**.

> **SON HATIRLATMA:** Bu dosya projenin mimari sözleşmesidir. Hiçbir kural esnetilemez.
> Veritabanı detayları için → `DATABASE_STATE.md`
