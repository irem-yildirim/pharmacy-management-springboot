# 🗄️ DATABASE_STATE.md — Eczane Yönetim Sistemi Veritabanı Şeması

## ⚠️ AI AJAN DİREKTİFİ (SYSTEM PROMPT)

> Bu doküman, projenin **tek geçerli veritabanı referansıdır**.
> Entity sınıflarını yazarken bu dosyadaki tablo/alan tanımlarına **HARFİYEN** uyacaksın.
> Burada olmayan **hiçbir tablo veya alan UYDURMAYACAKSIN**.
> Mimari kurallar için → `ARCHITECTURE.md`

---

## 1. GENEL VERİTABANI KURALLARI

| Kural                    | Detay                                                                 |
|--------------------------|-----------------------------------------------------------------------|
| **DBMS**                 | MySQL 8.x                                                            |
| **ORM**                  | Spring Data JPA + Hibernate                                          |
| **DDL Stratejisi**       | `spring.jpa.hibernate.ddl-auto=update`                                |
| **Stok Yönetimi**        | Drug tablosunda stok alanı **YOKTUR**. Stok, `Purchase.remaining_quantity` toplamından hesaplanır. |
| **Fiyat Tarihi**         | Drug tablosundaki fiyat yalnızca **güncel satış fiyatıdır**. Geçmiş fiyatlar `SaleItem.unit_price` ve `Purchase.purchase_price` içinde **dondurulmuş** olarak saklanır. |
| **Soft Delete**          | Kayıtlar `DELETE` edilmez. `isActive = false` yapılır.                |
| **Optimistic Locking**   | Kritik entity'lerde `@Version` anotasyonu kullanılır.                 |

---

## 2. TABLO DETAYLARI

---

### 2.1 Category (Kategori — Meta Veri)

İlaç kategorilerini tutar (Ağrı Kesici, Antibiyotik, Vitamin, vb.).

| Alan       | Java Tipi   | DB Tipi         | Kısıtlar                     | Açıklama             |
|------------|-------------|-----------------|-------------------------------|----------------------|
| `id`       | `Long`      | `BIGINT`        | `@Id`, `@GeneratedValue(AUTO)` | Primary Key          |
| `name`     | `String`    | `VARCHAR(100)`  | `@Column(unique=true)`, `@NotBlank` | Kategori adı (UNIQUE)|
| `isActive` | `Boolean`   | `BOOLEAN`       | default `true`                | Soft Delete flag     |

**İlişkiler:** `Category` ← `Drug` (OneToMany, mappedBy)

---

### 2.2 Brand (Marka — Meta Veri)

İlaç üretici markalarını tutar (Bayer, Abdi İbrahim, vb.).

| Alan       | Java Tipi   | DB Tipi         | Kısıtlar                     | Açıklama             |
|------------|-------------|-----------------|-------------------------------|----------------------|
| `id`       | `Long`      | `BIGINT`        | `@Id`, `@GeneratedValue(AUTO)` | Primary Key          |
| `name`     | `String`    | `VARCHAR(100)`  | `@Column(unique=true)`, `@NotBlank` | Marka adı (UNIQUE)  |
| `isActive` | `Boolean`   | `BOOLEAN`       | default `true`                | Soft Delete flag     |

**İlişkiler:** `Brand` ← `Drug` (OneToMany, mappedBy)

---

### 2.3 PresType (Reçete Tipi — Meta Veri)

İlacın reçete sınıfını tanımlar. Satış akışını doğrudan etkiler.

| Alan        | Java Tipi   | DB Tipi         | Kısıtlar                     | Açıklama                              |
|-------------|-------------|-----------------|-------------------------------|---------------------------------------|
| `id`        | `Long`      | `BIGINT`        | `@Id`, `@GeneratedValue(AUTO)` | Primary Key                           |
| `name`      | `String`    | `VARCHAR(50)`   | `@NotBlank`                   | Reçete adı: `Normal`, `Kırmızı`, `Yeşil` |
| `riskLevel` | `Integer`   | `INT`           | `@Min(0)`                     | Risk seviyesi (0=Normal, 1=Kırmızı, 2=Yeşil) |

**İlişkiler:** `PresType` ← `Drug` (OneToMany, mappedBy)

> **İŞ KURALI:** `riskLevel >= 1` olan ilaçların satışında reçete/hasta bilgisi **zorunludur**.

---

### 2.4 Drug (İlaç Ana Kaydı)

Her ilacın **tek bir ana kaydı** bu tablodadır. Stok ve maliyet bilgisi bu tabloda **BULUNMAZ**.

| Alan                    | Java Tipi       | DB Tipi         | Kısıtlar                             | Açıklama                            |
|-------------------------|-----------------|-----------------|---------------------------------------|--------------------------------------|
| `barcode`               | `String`        | `VARCHAR(50)`   | `@Id` (Doğal PK, Auto Inc. değil)    | Barkod — **Primary Key**             |
| `name`                  | `String`        | `VARCHAR(200)`  | `@NotBlank`                           | İlaç adı                            |
| `category`              | `Category`      | FK → `category` | `@ManyToOne`, `@JoinColumn("category_id")` | İlaç kategorisi                |
| `brand`                 | `Brand`         | FK → `brand`    | `@ManyToOne`, `@JoinColumn("brand_id")`    | Üretici marka                  |
| `presType`              | `PresType`      | FK → `pres_type`| `@ManyToOne`, `@JoinColumn("pres_id")`     | Reçete tipi                    |
| `currentSellingPrice`   | `BigDecimal`    | `DECIMAL(10,2)` | `@NotNull`, `@Min(0)`                | O anki güncel satış fiyatı           |
| `minStockAlert`         | `Integer`       | `INT`           | `@Min(0)`, default `10`              | Dinamik stok uyarı eşiği            |
| `isActive`              | `Boolean`       | `BOOLEAN`       | default `true`                        | Soft Delete flag                     |
| `version`               | `Long`          | `BIGINT`        | `@Version`                            | **Optimistic Locking** sayacı        |

**İlişkiler:**
- `Drug` → `Category` (ManyToOne)
- `Drug` → `Brand` (ManyToOne)
- `Drug` → `PresType` (ManyToOne)
- `Drug` ← `Purchase` (OneToMany, mappedBy)

> ⛔ **KRİTİK:** Bu tabloda `stockQuantity` veya `costPrice` alanı **YOKTUR**.
> Toplam stok = `SELECT SUM(remaining_quantity) FROM purchase WHERE drug_barcode = ? AND remaining_quantity > 0`

---

### 2.5 Purchase (Stok Partisi / Alım Kaydı — FIFO'nun Kalbi)

Her ilaç alımı (stok girişi) ayrı bir parti olarak bu tabloya yazılır. FIFO satış mantığının temel veri kaynağıdır.

| Alan                 | Java Tipi     | DB Tipi         | Kısıtlar                             | Açıklama                                     |
|----------------------|---------------|-----------------|---------------------------------------|----------------------------------------------|
| `id`                 | `Long`        | `BIGINT`        | `@Id`, `@GeneratedValue(AUTO)`        | Primary Key                                  |
| `drug`               | `Drug`        | FK → `drug`     | `@ManyToOne`, `@JoinColumn("drug_barcode")` | Hangi ilaca ait parti                  |
| `originalQuantity`   | `Integer`     | `INT`           | `@NotNull`, `@Min(1)`                | Partide başlangıçta alınan miktar            |
| `remainingQuantity`  | `Integer`     | `INT`           | `@NotNull`, `@Min(0)`                | Satıldıkça düşen **anlık** miktar            |
| `purchasePrice`      | `BigDecimal`  | `DECIMAL(10,2)` | `@NotNull`                            | O günkü alım maliyeti (dondurulmuş fiyat)    |
| `expirationDate`     | `LocalDate`   | `DATE`          | `@NotNull`                            | Son Kullanma Tarihi (SKT)                    |
| `purchaseDate`       | `LocalDate`   | `DATE`          | `@NotNull`                            | Alım tarihi                                  |

**İlişkiler:**
- `Purchase` → `Drug` (ManyToOne)
- `Purchase` ← `SaleItem` (OneToMany, mappedBy)

> **FIFO KURALI:** Satış sırasında partiler `expirationDate ASC` sırasıyla çekilir.
> `remaining_quantity > 0` olan en eski SKT'li parti önce tüketilir.

---

### 2.6 User (Kullanıcı / Eczacı / Admin)

Sisteme giriş yapan kullanıcıları tutar.

| Alan       | Java Tipi   | DB Tipi          | Kısıtlar                             | Açıklama                         |
|------------|-------------|------------------|---------------------------------------|----------------------------------|
| `id`       | `Long`      | `BIGINT`         | `@Id`, `@GeneratedValue(AUTO)`        | Primary Key                      |
| `name`     | `String`    | `VARCHAR(100)`   | `@NotBlank`                           | Kullanıcı adı soyadı            |
| `username` | `String`    | `VARCHAR(50)`    | `@Column(unique=true)`, `@NotBlank`   | Giriş kullanıcı adı (UNIQUE)    |
| `password` | `String`    | `VARCHAR(255)`   | `@NotBlank`                           | BCrypt ile hashlenmiş şifre     |
| `role`     | `Role`      | `VARCHAR(20)`    | `@Enumerated(STRING)`, `@NotNull`     | `ADMIN` veya `PHARMACIST`        |
| `isActive` | `Boolean`   | `BOOLEAN`        | default `true`                        | Soft Delete flag                 |

**Role Enum:**
```java
public enum Role {
    ADMIN,
    PHARMACIST
}
```

---

### 2.7 Customer (Müşteri — Mini CRM)

Veresiye (açık hesap) takibi yapılan müşterileri tutar. Anonim satışlar için Customer **null** olabilir.

| Alan       | Java Tipi     | DB Tipi          | Kısıtlar                             | Açıklama                     |
|------------|---------------|------------------|---------------------------------------|------------------------------|
| `id`       | `Long`        | `BIGINT`         | `@Id`, `@GeneratedValue(AUTO)`        | Primary Key                  |
| `name`     | `String`      | `VARCHAR(100)`   | `@NotBlank`                           | Müşteri adı soyadı          |
| `phone`    | `String`      | `VARCHAR(20)`    |                                       | Telefon numarası             |
| `balance`  | `BigDecimal`  | `DECIMAL(10,2)`  | default `0.00`                        | Veresiye bakiyesi (borç)     |
| `isActive` | `Boolean`     | `BOOLEAN`        | default `true`                        | Soft Delete flag             |

**İlişkiler:** `Customer` ← `Sale` (OneToMany, mappedBy)

---

### 2.8 Sale (Satış / Fiş Başlığı)

Her satış işleminin ana kaydı.

| Alan                   | Java Tipi     | DB Tipi          | Kısıtlar                             | Açıklama                                 |
|------------------------|---------------|------------------|---------------------------------------|------------------------------------------|
| `id`                   | `Long`        | `BIGINT`         | `@Id`, `@GeneratedValue(AUTO)`        | Primary Key                              |
| `customer`             | `Customer`    | FK → `customer`  | `@ManyToOne`, `@JoinColumn("customer_id")`, **Nullable** | Müşteri (null = Misafir) |
| `totalAmount`          | `BigDecimal`  | `DECIMAL(10,2)`  | `@NotNull`                            | Fişin toplam tutarı                      |
| `saleDate`             | `LocalDateTime`| `DATETIME`      | `@NotNull`                            | Satış tarihi ve saati                    |
| `isPrescriptionLogged` | `Boolean`     | `BOOLEAN`        | default `false`                       | Reçete bilgisi girildi mi?               |

**İlişkiler:**
- `Sale` → `Customer` (ManyToOne, **nullable**)
- `Sale` ← `SaleItem` (OneToMany, mappedBy)

---

### 2.9 SaleItem (Satış Kalemi / Fiş Detay Satırı)

Her fişin alt kalemlerini tutar. Bir ilaç birden fazla partiden düşülebileceği için, aynı ilaç için **birden fazla SaleItem** yazılabilir.

| Alan          | Java Tipi     | DB Tipi          | Kısıtlar                             | Açıklama                                           |
|---------------|---------------|------------------|---------------------------------------|------------------------------------------------------|
| `id`          | `Long`        | `BIGINT`         | `@Id`, `@GeneratedValue(AUTO)`        | Primary Key                                          |
| `sale`        | `Sale`        | FK → `sale`      | `@ManyToOne`, `@JoinColumn("sale_id")`| Hangi fişe ait                                       |
| `purchase`    | `Purchase`    | FK → `purchase`  | `@ManyToOne`, `@JoinColumn("purchase_id")` | Ürün **hangi partiden** düşüldü               |
| `quantity`    | `Integer`     | `INT`            | `@NotNull`, `@Min(1)`                | Bu partiden satılan miktar                           |
| `unitPrice`   | `BigDecimal`  | `DECIMAL(10,2)`  | `@NotNull`                            | Satış anındaki **dondurulmuş** birim fiyat           |

**İlişkiler:**
- `SaleItem` → `Sale` (ManyToOne)
- `SaleItem` → `Purchase` (ManyToOne)

> **FIFO ÖRNEĞİ:** Müşteri 10 Parol alıyor. Parti-A'da 4, Parti-B'de 6 adet var.
> Sonuç: `SaleItem-1 (purchase=A, qty=4)` + `SaleItem-2 (purchase=B, qty=6)` → 2 ayrı satır yazılır.

---

## 3. ENTITY İLİŞKİ DİYAGRAMI (ER — Metin Tabanlı)

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│ Category │     │  Brand   │     │ PresType │
│----------│     │----------│     │----------│
│ id (PK)  │     │ id (PK)  │     │ id (PK)  │
│ name     │     │ name     │     │ name     │
│ isActive │     │ isActive │     │ riskLevel│
└────┬─────┘     └────┬─────┘     └────┬─────┘
     │ 1              │ 1              │ 1
     │                │                │
     │ *              │ *              │ *
┌────┴────────────────┴────────────────┴─────┐
│                   Drug                      │
│─────────────────────────────────────────────│
│ barcode (PK)                                │
│ name, currentSellingPrice, minStockAlert    │
│ isActive, version (@Version)                │
│ category_id (FK), brand_id (FK), pres_id(FK)│
└──────────────────┬──────────────────────────┘
                   │ 1
                   │
                   │ *
          ┌────────┴────────┐
          │    Purchase     │
          │─────────────────│
          │ id (PK)         │
          │ originalQuantity│
          │ remainingQuantity│        ┌──────────┐
          │ purchasePrice   │        │ Customer │
          │ expirationDate  │        │──────────│
          │ purchaseDate    │        │ id (PK)  │
          │ drug_barcode(FK)│        │ name     │
          └────────┬────────┘        │ phone    │
                   │ 1               │ balance  │
                   │                 │ isActive │
                   │                 └────┬─────┘
                   │                      │ 1 (nullable)
                   │                      │
                   │ *                    │ *
          ┌────────┴────────┐    ┌───────┴───────┐
          │    SaleItem     │    │     Sale      │
          │─────────────────│    │───────────────│
          │ id (PK)         │    │ id (PK)       │
          │ quantity        │◄───│ totalAmount   │
          │ unitPrice       │  * │ saleDate      │
          │ sale_id (FK)    │    │ isPrescLogged │
          │ purchase_id (FK)│    │ customer_id   │
          └─────────────────┘    └───────────────┘
                                   │         │
                              1..* │         │ 0..1
                            SaleItem      Customer
```

---

## 4. KRİTİK SORGULAR (Repository Metot İmzaları)

Aşağıdaki sorgular Entity ve Repository yazılırken **göz önünde bulundurulmalıdır**:

```java
// PurchaseRepository — FIFO sıralama
List<Purchase> findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
    String barcode, int minQty);

// PurchaseRepository — Toplam stok hesaplama
@Query("SELECT COALESCE(SUM(p.remainingQuantity), 0) FROM Purchase p WHERE p.drug.barcode = :barcode AND p.remainingQuantity > 0")
int sumRemainingByDrugBarcode(@Param("barcode") String barcode);

// PurchaseRepository — SKT uyarıları
List<Purchase> findByExpirationDateBeforeAndRemainingQuantityGreaterThan(LocalDate date, int minQty);

// DrugRepository — Aktif ilaçlar
List<Drug> findByIsActiveTrue();

// SaleItemRepository — Günlük ciro
@Query("SELECT COALESCE(SUM(si.unitPrice * si.quantity), 0) FROM SaleItem si WHERE si.sale.saleDate BETWEEN :start AND :end")
BigDecimal calculateDailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```

---

## 5. VERİTABANI YAPIM DURUMU (IMPLEMENTATION STATUS)

| Tablo      | Entity Yazıldı | Repository Yazıldı | Durum           |
|------------|:---------------:|:-------------------:|-----------------|
| Category   | ✅              | ✅                  | Tamamlandi       |
| Brand      | ✅              | ✅                  | Tamamlandi       |
| PresType   | ✅              | ✅                  | Tamamlandi       |
| Drug       | ✅              | ✅                  | Tamamlandi       |
| Purchase   | ✅              | ✅                  | Tamamlandi       |
| User       | ✅              | ✅                  | Tamamlandi       |
| Customer   | ✅              | ✅                  | Tamamlandi       |
| Sale       | ✅              | ✅                  | Tamamlandi       |
| SaleItem   | ✅              | ✅                  | Tamamlandi       |

> Bu tabloyu her modül tamamlandığında ✅ ile güncelleyeceğiz.

---

> **SON HATIRLATMA:** Drug tablosunda stok veya maliyet alanı **YOKTUR**.
> Stoklar `Purchase.remainingQuantity` toplamından, maliyetler `Purchase.purchasePrice` üzerinden hesaplanır.
> Mimari kurallar ve klasör yapısı için → `ARCHITECTURE.md`
