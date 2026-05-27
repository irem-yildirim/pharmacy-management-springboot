# 📝 Eczane Yönetim Sistemi - Soru & Cevap Günlüğü

Bu dosya, proje incelemesi boyunca sorduğunuz soruları ve teknik açıklamalarını kısa ve öz bir şekilde takip edebilmeniz için oluşturulmuştur.

---

### 📌 `.mvn` Klasörü Nedir? Neden Var?
* **Özet:** Maven Wrapper (Maven Sarmalayıcısı) dosyalarını barındırır.
* **Detay:** Bilgisayarda Maven kurulu olmasa bile projenin derlenmesini sağlar. Herkesin birebir aynı Maven sürümünü kullanmasını garantiler. Teknik olarak çalışması için **zorunlu değildir** (bilgisayarda global Maven varsa çalışır) fakat taşınabilirlik ve ortak çalışma için **şiddetle tavsiye edilir**.

---

### 📌 `.vscode` Klasörü Nedir? Neden Var?
* **Özet:** VS Code editörünün projeye özel Java yapılandırma ayarlarını tutar.
* **Detay:** VS Code eklentilerinin (örn. Java dili desteği) derleme ve boş referans (`NullPointerException`) analizi yaparken nasıl davranacağını belirler. Projenin çalışması için **kesinlikle zorunlu değildir**, sadece VS Code editörünü kullanan yazılımcı için geliştirme kolaylığı sağlar.

---

### 📌 `com.pharmacy.advice` Paketi Neden "advice" Olarak Adlandırıldı?
* **Özet:** AOP (Aspect-Oriented Programming) mimarisindeki araya girme ("Advice") teriminden gelir.
* **Detay:** Hataları tek bir merkezden yakalayıp yönetmek (Global Exception Handling) için tasarlanmıştır. Controller katmanında oluşabilecek tüm istisnai durumları dinler ve araya girerek akışı yönlendirir.

---

### 📌 `CustomerNotFoundException` İçeriği Neden Boş? Hata Mesajları Nerede Tanımlanıyor?
* **Özet:** Hata mesajlarını sabit tutmak yerine dinamik (değişken) kılmak için içi boştur.
* **Detay:** `extends RuntimeException` sayesinde Java'nın hata özelliklerini miras alır. Hata fırlatılırken (`throw new CustomerNotFoundException("...")`) parantez içine yazılan dinamik mesaj `super(message)` ile üst sınıfa iletilir ve `GlobalExceptionHandler` tarafından yakalanıp kullanıcıya sunulur.

---

### 📌 `GlobalExceptionHandler` Sınıfının Kod Kod Analizi
* **Özet:** Projenin merkezi hata yönetim (yangın söndürme) istasyonudur.
* **Detay:**
  * `@ControllerAdvice`: Tüm Controller'ların etrafına koruma kalkanı koyar.
  * `@ExceptionHandler(...)`: İlgili hata türü fırlatıldığında havada yakalayacak metodu tanımlar.
  * `Customer/DrugNotFoundException` -> HTTP 404 (Bulunamadı) döner.
  * `InsufficientStock/RestrictedSaleException` -> HTTP 400 (Hatalı İstek) döner.
  * `DuplicateEntryException` / `OptimisticLockException` -> HTTP 409 (Çakışma) döner.
  * `MethodArgumentNotValidException` -> Form/Veri doğrulama hatalarını (Örn: Boş isim, negatif fiyat) tek tek listeleyerek HTTP 400 döner.
  * `Exception.class` -> Öngörülemeyen genel sunucu hatalarını yakalayıp HTTP 500 döner.

---

### 📌 `com.pharmacy.config` Klasörü Nedir ve Köşesindeki Küçük Çark Simgesi Neden Var?
* **Özet:** Projenin ayarlarını (güvenlik, ilk veri yükleme, web ayarları) yapan sınıfları barındırır. Köşesindeki çark ise sadece editörün (VS Code) görsel bir simgesidir.
* **Detay:**
  * **Klasörün Amacı:** Spring Boot'ta uygulamanın davranışını değiştiren veya ek özellikleri devreye sokan yapılandırma sınıfları (Security, CORS, ilk veri seed etme vb.) `@Configuration` anotasyonuyla bu klasör altında toplanır.
  * **Çark Simgesi:** Kullandığınız editörün (VS Code veya IntelliJ) ikon teması (örneğin Material Icon Theme), klasör adının `config` olduğunu algılayıp bunun bir "ayar/yapılandırma" klasörü olduğunu görsel olarak belirtmek için otomatik olarak bir **çark (gear) simgesi** yerleştirir. Kodun çalışmasıyla bir ilgisi yoktur, tamamen görsel bir kolaylıktır.

---

### 📌 `PharmacyUserDetails.java` Sınıfı Satır Satır Analizi
* **Özet:** Spring Security'nin sisteme giriş yapan kullanıcıyı tanıması ve arka planda oturum bilgilerini (kullanıcı adı, rolü, adı-soyadı, ID'si) saklaması için yazılmış özel bir adaptör sınıfıdır.
* **Detay:**
  * `extends org.springframework.security.core.userdetails.User`: Spring Security'nin kendi hazır `User` sınıfından kalıtım alır.
  * `fullName` ve `userId`: Spring'in standart kullanıcı sınıfında ad-soyad ve ID alanları yoktur. Biz bu iki alanı ekleyerek, giriş yapan eczacının adını ekranda göstermek veya veritabanı ID'sini satış işlemlerinde kullanmak üzere özelleştirdik.
  * `super(user.getUsername(), ...)`: Bizim veritabanımızdaki kullanıcı bilgilerini (kullanıcı adı, şifre, aktiflik durumu) Spring Security'nin anlayacağı formata çevirerek üst sınıfa (parent) aktarır.
  * `ROLE_` öneki: Veritabanımızdaki `Role` enum değerlerini (ADMIN, PHARMACIST) Spring'in yetkilendirme mekanizmasına uygun şekilde `"ROLE_ADMIN"`, `"ROLE_PHARMACIST"` şeklinde kaydeder.

---

### 📌 `SecurityConfig.java` Sınıfının Blok Blok Analizi
* **Özet:** Uygulamanın güvenlik kapısıdır. Hangi sayfalara kimlerin erişebileceğini ve sisteme giriş/çıkış mantığını belirler.
* **Detay:**
  * `passwordEncoder()`: Kullanıcı şifrelerini veritabanında `BCrypt` algoritması ile karmaşık (hashlenmiş) hale getirerek saklar.
  * `userDetailsService()`: Giriş yapılmaya çalışıldığında kullanıcı adına göre veritabanı sorgusu yapar ve eşleşen kullanıcıyı `PharmacyUserDetails` olarak döner.
  * `filterChain()` Yetki Dağılımı:
    * Statik dosyalar (CSS/JS) ve `/login` sayfası herkese açıktır.
    * `/api/users/**` -> Sadece `ADMIN` yetkisine açık.
    * Finans, Dashboard, Satın Alım ve Ayarlar -> Sadece `ADMIN` ve `PHARMACIST` yetkilerine açık.
    * Müşteri yönetimi -> Kasiyerler (`CASHIER`) dahil tüm rollere açık.
  * `successHandler()` Yönlendirmesi: Giriş yapan kişi `CASHIER` ise onu doğrudan POS Kasa ekranına (`/pos`), diğer rolleri (`ADMIN`, `PHARMACIST`) ise yönetim paneline (`/dashboard`) yönlendirir.
  * `csrf().ignoringRequestMatchers("/api/**")`: Ön yüzdeki JavaScript (AJAX) isteklerinin API uçlarına kolayca veri gönderebilmesi için `/api/**` yollarında CSRF korumasını kapatır.

---

### 📌 `com.pharmacy.controller` Klasörünün Rolü Nedir? `ui` ve `api` Farkı Nedir?
* **Özet:** Controller, kullanıcının (tarayıcının) isteklerini karşılayıp işlenmesi için Service katmanına gönderen ve sonucu kullanıcıya geri sunan kapı yöneticisidir.
* **Detay:**
  * **ui Klasörü (Thymeleaf):** Tarayıcıya doğrudan HTML sayfaları döndürür. Ekranda gördüğünüz sayfaların (Dashboard, POS, Ayarlar vb.) yüklenmesini sağlar.
  * **api Klasörü (REST API):** Sayfa yenilemeden arka planda hızlı veri alışverişi yapmak için JSON formatında saf veri döner. AJAX/Fetch istekleri ile çalışır.
  * **İçindeki Dosyalar:** 
    * `UIController` -> Sayfa taslaklarını sunar.
    * `GlobalUIControllerAdvice` -> Oturum açan kullanıcının adını/rolünü tüm sayfalara otomatik giydirir.
    * `api/` altındakiler (`Drug`, `Sale`, `Purchase`, `Customer`, `Dashboard`, `User`, `Brand`, `Category`) -> İlaç barkod arama, FIFO satışı onaylama, stok ekleme, müşteri borç hesabı gibi arka plan işlerini JSON verileriyle çözen API uçlarıdır.
---

### 📌 `GlobalUIControllerAdvice.java` Neden Var ve "Global" Mantığı Nedir?
* **Özet:** Açık olan aktif sayfa bilgisini (`currentUri`) tüm arayüz şablonlarına (Thymeleaf) otomatik olarak gönderen merkezi bir yardımcıdır.
* **Detay:**
  * `@ControllerAdvice` sayesinde, `UIController` içindeki hangi GET isteği (sayfa) çalışırsa çalışsın araya girer ve tarayıcının o an bulunduğu sayfa adresini modele ekler.
  * Bu sayede yan menü (`sidebar.html`) gibi ortak bileşenler, kullanıcının o an hangi sekmede (Dashboard, POS, Finans vb.) olduğunu bilir ve ilgili menü butonunu otomatik olarak aktif (vurgulu) hale verir. Her sayfaya tek tek bu ayarı yazma yükünü ortadan kaldırır.

---


### 📌 `BrandController.java` Sınıfının Satır Satır Analizi (REST API Nedir?)
* **Özet:** İlaç üretici markalarını (Bayer, Pfizer vb.) ön yüze JSON saf veri formatında sunan, kaydeden ve silen REST API kapısıdır.
* **Detay:**
  * `@RestController` & `@RequestMapping("/api/brands")`: Bu sınıfa atılan HTTP isteklerinin sonucunu doğrudan JSON saf verisine çevirerek `/api/brands` yolundan sunar.
  * `@Tag` & `@Operation`: API'yi test etmek için kullanılan **Swagger** dokümantasyon arayüzünde bu metodun ne iş yaptığını anlatan başlıklar ekler.
  * `getAllActive() (GET)` -> Silinmemiş aktif markaları `BrandResponse` DTO listesine dönüştürerek HTTP 200 OK koduyla listeler (Kural 2 - DTO standardına uyum).
  * `save() (POST)` -> Gelen JSON verisini `@Valid` ile doğrulayıp yeni marka olarak kaydeder ve HTTP 201 Created döner.
  * `delete() (DELETE)` -> URL'den gelen ID'yi (`@PathVariable`) alarak markayı silinmiş (`isActive = false`) işaretler ve HTTP 204 No Content döner (Soft Delete mantığı).

---

### 📌 Repository Katmanı Nedir? Neden Sınıf Değil de `public interface` Olarak Tanımlanır?
* **Özet:** Veritabanına veri kaydetme, güncelleme, silme ve arama (SQL sorguları atma) işlerini yapan kapıdır. Spring Data JPA sayesinde gövdesiz arayüz (interface) olarak tanımlanır ve tüm veritabanı sorguları otomatik olarak Spring tarafından üretilir.
* **Detay:**
  * **Neden Interface (Arayüz):** Normalde veritabanı bağlantısı kurmak, SQL bağlantısı açıp kapamak ve sorguları tek tek yazmak yüzlerce satır sürer. `JpaRepository<Entity, IdTipi>` arayüzünü extend (miras) ettiğimizde, Spring Boot arka planda bu metodların tüm gövdelerini (SQL kodlarını) kendisi otomatik yazar. Bizim tek bir satır dahi sınıf kodu yazmamıza gerek kalmaz.

---

### 📌 `PresTypeRepository` İçi Neden Tamamen Boş?
* **Özet:** Spring Data JPA'nın sağladığı hazır CRUD (Ekle, Oku, Güncelle, Sil) metotları bu sınıf için tamamen yeterli olduğundan ekstra bir kod yazmaya gerek kalmamıştır.
* **Detay:**
  * `JpaRepository`'yi miras aldığımız için; `save()`, `findById()`, `findAll()`, `deleteById()` gibi temel veritabanı metotları zaten arka planda hazır olarak gelir. Reçete Tipleri tablosunda (`PresType`) karmaşık aramalar veya özel ciro hesaplamaları gerekmediğinden, içi boş bırakılarak bu hazır metotların kullanılması yeterli olmuştur.

---

### 📌 `PurchaseRepository` ve `SaleItemRepository` Kodlarının Detaylı Analizi
* **Özet:** FIFO stok satışı, ciro ve maliyet hesaplama gibi projeye özel karmaşık iş kurallarını çözmek için içlerinde özel SQL/JPQL sorguları barındırırlar.
* **Detay:**

#### 1. `PurchaseRepository` (Stok Partileri Sorgu Merkezi)
* `findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(...)`: Barkodu verilen ilaca ait, kalan stoğu 0'dan büyük olan partileri **Son Kullanma Tarihi en yakın olandan en uzak olana doğru (ASC)** sıralayarak getirir. **FIFO (İlk Giren İlk Çıkar) satış mantığının kalbidir.**
* `sumRemainingByDrugBarcode(...)`: Bir ilacın tüm partilerindeki kalan stokları toplayarak **anlık toplam stoğunu** döner. (Bu sayede Drug tablosunda gereksiz bir stok kolonu tutulmaz).
* `calculateExpiredLoss()`: Tarihi geçmiş ve satılamadan elde kalmış ilaçların toplam alış maliyeti zararını (`kalan stok * alış fiyatı`) hesaplar.

#### 2. `SaleItemRepository` (Finansal Hesaplama Merkezi)
* `calculateDailyRevenue(...)` & `calculateTotalRevenue()`: Satılan her ürünün **satış anındaki dondurulmuş birim fiyatı ile adedini çarparak** günlük veya toplam **Ciro (Hasılat)** miktarını hesaplar.
* `calculateDailyCost(...)` & `calculateTotalCost()`: Satılan her ürünün **kendi alındığı stok partisindeki (Purchase) alış maliyeti ile adedini çarparak** günlük veya toplam **Maliyet (Gider)** miktarını hesaplar.
  * *Neden Özel:* Bu sayede ilaçların fiyatı zamanla değişse bile, hangi ürünün hangi maliyetli partiden satıldığı nokta atışı bilindiği için **net kâr kuruşu kuruşuna** doğru hesaplanır.


---

### 📌 Tüm API Controller Sınıflarının ve Metotlarının Özet Tablosu
* **Özet:** Uygulamanın arka plandaki tüm JSON veri alışverişini yöneten API uçlarının toplu özetidir.
* **Detay:**

| Sınıf / API Yolu | HTTP Metodu | Metot Adı | Ne İşe Yarar? (İş Mantığı) |
| :--- | :--- | :--- | :--- |
| **`CategoryController`** <br> `/api/categories` | **GET** <br> **POST** <br> **DELETE** | `getAllActive` <br> `save` <br> `delete` | Aktif kategorileri listeler. <br> Yeni ilaç kategorisi oluşturur. <br> Kategoriyi pasife alır (Soft Delete). |
| **`CustomerController`** <br> `/api/customers` | **GET** <br> **GET** <br> **POST** | `getAllActive` <br> `searchCustomers` <br> `createCustomer` | Kayıtlı tüm aktif müşterileri ve borç bakiyelerini listeler. <br> İsme veya telefona göre dinamik arama yapar (POS için). <br> Yeni müşteri hesabı (CRM) açar. |
| **`DashboardController`** <br> `/api/dashboard` | **GET** | `getStats` | Bento Box paneli için bugünkü ciro, kâr ve SKT imha zararını çeker. |
| **`DrugController`** <br> `/api/drugs` | **GET** <br> **GET** <br> **POST** <br> **DELETE** <br> **PUT** <br> **POST** | `getAllActive` <br> `getByBarcode` <br> `save` <br> `softDelete` <br> `update` <br> `disposeExpired` | Tüm ilaçları toplam stokları ve partileriyle listeler. <br> Barkodla tek bir ilacın detayını çeker. <br> Kataloğa yeni ilaç kaydeder. <br> İlacı satışa kapatır/pasife alır (Soft Delete). <br> İlacın satış fiyatını veya min. stok uyarısını günceller. <br> İlacın tarihi geçmiş partilerini imha eder (Zararı hesaplar). |
| **`PurchaseController`** <br> `/api/purchases` | **POST** | `createPurchase` | Eczaneye yeni ilaç alımı yapar (FIFO stok partisi ekler). |
| **`SaleController`** <br> `/api/sales` | **GET** <br> **POST** | `getAllSales` <br> `createSale` | Yapılan tüm satış geçmişini listeler (veya müşteri bazlı filtreler). <br> POS sepetini onaylayıp FIFO ile stok düşer, reçete/müşteri kontrolü yapar. |
| **`UserController`** <br> `/api/users` | **GET** <br> **POST** <br> **GET** | `getAll` <br> `create` <br> `getPerformance` | Tüm personel hesaplarını (şifreler gizli şekilde) listeler. <br> Sisteme yeni eczacı/kasiyer tanımlar (şifreyi BCrypt ile şifreler). <br> Giriş yapan personelin kendi toplam cirosunu ve performansını çeker. |


### 📌 DTO (Data Transfer Object) Nedir? Neden Entity'leri Doğrudan Kullanmayız?
* **Özet:** Arayüz (tarayıcı) ile arka plan servisleri arasında güvenli, hafif ve kontrollü veri taşıyan maske nesnelerdir.
* **Detay:**
  * **Gizlilik ve Güvenlik:** Veritabanındaki `User` nesnesini doğrudan dönmek şifreleri (`password`) sızdırır. `UserResponse` DTO'su şifre alanını barındırmayarak veriyi güvenliğe alır.
  * **Sonsuz Döngü Koruması:** İlişkili tablolarda (Örn: İlaç -> Satın Alım -> İlaç) oluşan sonsuz JSON döngülerini engeller, veriyi düzleştirir.
  * **Gevşek Bağlılık (Decoupling):** Veritabanındaki tablo veya kolon isimleri değişse bile API çıkışlarının bozulmasını engeller.
  * **Request (İstek) DTO:** Kullanıcının formdan sadece girmesine izin verilen alanları sınırlandırır ve `@Valid` ile doğrular (Örn: `DrugCreateRequest`).
  * **Response (Yanıt) DTO:** Kullanıcının sadece görmesi gereken alanları JSON olarak ön yüze döner (Örn: `DrugResponse`).

---

### 📌 `com.pharmacy.model` Klasörü (JPA Entities) Nedir ve Sınıfları Nelerdir?
* **Özet:** Veritabanındaki MySQL tablolarının Java'daki birebir haritasıdır. Sınıfın üstündeki `@Entity` anotasyonu sayesinde her bir sınıf bir tabloya, her bir nesne ise o tablodaki bir satıra karşılık gelir.
* **Detay:**

1. **`Category` (İlaç Kategorisi):** İlaçları Ağrı Kesici, Antibiyotik gibi gruplara ayırır. Özellikleri: `id`, `name`, `isActive`.
2. **`Brand` (İlaç Üreticisi):** İlacı üreten firmayı (Bayer, Pfizer vb.) temsil eder. Özellikleri: `id`, `name`, `isActive`.
3. **`PresType` (Reçete Tipi):** İlacın reçete sınıfını ve risk seviyesini tanımlayarak satış kurallarını belirler. Özellikleri: `id`, `name`, `riskLevel` (1-4).
4. **`Drug` (İlaç Ana Bilgisi):** İlacın barkod ve temel kimlik kartıdır. **Stok ve maliyet bilgisi barındırmaz!** Özellikleri: `barcode` (PK), `name`, `category` (FK), `brand` (FK), `presType` (FK), `currentSellingPrice`, `minStockAlert`, `isActive`, `version` (Eşzamanlılık Kilidi).
5. **`Purchase` (Stok Alım Partisi - FIFO'nun Temeli):** Alınan her stok partisinin fiyatını ve son kullanma tarihini (SKT) tutar. Özellikleri: `id`, `drug` (FK), `originalQuantity`, `remainingQuantity`, `purchasePrice` (Giriş Maliyeti), `expirationDate` (SKT), `purchaseDate`.
6. **`User` (Personel):** Sisteme giriş yapan çalışanları (Admin, Eczacı, Kasiyer) temsil eder. Özellikleri: `id`, `name`, `username`, `password` (BCrypt'li), `role` (ADMIN, PHARMACIST, CASHIER), `isActive`.
7. **`Customer` (Müşteri - CRM):** Veresiye alışveriş yapan ve borç bakiyesi takip edilen hastaları temsil eder. Özellikleri: `id`, `name`, `phone`, `balance` (Veresiye borcu bakiyesi), `isActive`.
8. **`Sale` (Satış Fişi Başlığı):** Yapılan her bir kasa satış işleminin genel fiş faturasını saklar. Özellikleri: `id`, `customer` (FK), `user` (FK - Satan personel), `totalAmount`, `saleDate`, `isPrescriptionLogged`.
9. **`SaleItem` (Satış Kalem Detayı):** Satış fişindeki her bir ürünü ve hangi stok partisinden düşüldüğünü gösteren detay satırıdır. Özellikleri: `id`, `sale` (FK), `purchase` (FK - Hangi partiden satıldı), `quantity`, `unitPrice` (Dondurulmuş satış fiyatı).

---

### 📌 `@ManyToOne` ve `@JoinColumn` Anotasyonları Nedir? Ne İşe Yarar?
* **Özet:** Veritabanındaki Foreign Key (Yabancı Anahtar) ve SQL JOIN (Tablo Birleştirme) mantığının Java nesnelerine otomatik olarak eşlenmesini (ORM) sağlar.
* **Detay:**
  * **`@ManyToOne` (Çoka-Tek İlişkisi):** Şu anki sınıftan **çok sayıda** kaydın, karşı taraftaki **tek bir** nesneye bağlanabileceğini söyler.
    * *Örnek:* **Birçok farklı ilaç** (`Many` Drug) -> **Tek bir kategoriye** (`One` Category - örn: Ağrı Kesiciler) bağlıdır.
  * **`@JoinColumn(name = "category_id")`:** Veritabanındaki tablonun içine `category_id` adında bir Foreign Key (bağlantı kolonu) kurulacağını belirtir.
  * **Faydası:** Bizi karmaşık `INNER JOIN` SQL sorguları yazmaktan kurtarır. Java kodunda doğrudan `drug.getCategory().getName()` yazarak ilişkili tüm verilere tek tıkla erişmemizi sağlar.

---

### 📌 `com.pharmacy.service` Klasörü Nedir? Köşesindeki Sarı Çark Ne Anlama Gelir?
* **Özet:** Uygulamanın asıl karar alma, hesaplama ve kuralları yürütme merkezidir (Beyni/İş Mantığı). Köşesindeki sarı çark simgesi ise sadece editörün (VS Code) bu klasörün "servis" olduğunu belirtmek için koyduğu görsel bir ikondur.
* **Detay:**
  * **Aşçı Rolü (Mutfak):** Controller (Garson) istekleri alır, Repository (Kiler) verileri saklar. Service katmanı ise verileri alıp işler, FIFO satışı gibi iş kurallarını işletir ve karar verir.
  * **İşlem Güvencesi (`@Transactional`):** Satış esnasında stoğun düşmesi, veresiyenin kaydedilmesi ve fişin kesilmesi gibi bir dizi adımın tek seferde bitmesini garanti eder. Biri bile hata verirse tüm adımları iptal eder (Rollback), veritabanında veri tutarsızlığını engeller.
  * **Kod Tekrarını Önleme:** İş kuralları servis altında tek merkezde toplandığı için, aynı kodları farklı yerlerde tekrar tekrar yazmak yerine her yerden tek satırla çağırarak kullanabiliriz.

---

### 📌 Strategy Design Pattern Nedir? Eczane Projesinde Nasıl Kullanılmıştır?
* **Özet:** Bir iş kuralının farklı yollarını (algoritmalarını) ayrı sınıflara bölerek, bunları dinamik olarak birbirinin yerine kullanabilmeyi sağlayan tasarım kalıbıdır. Projede son kullanma tarihi (SKT) durumlarının tespiti için upuzun `if-else` blokları yazmayı engelleyerek kullanılmıştır.
* **Detay:**
  * **`ExpiryStrategy` (Ortak Arayüz):** Tüm stratejilerin uygulaması gereken `evaluate()` kural şablonunu belirler.
  * **`ExpiredStrategy` (Süresi Dolan):** Kalan gün sayısı 0 veya daha az ise çalışır. Geriye `"EXPIRED"` döner.
  * **`CriticalStrategy` (Kritik):** Kalan gün sayısı 30 veya daha az ise çalışır. Geriye `"CRITICAL"` döner.
  * **`OkStrategy` (Güvenli):** Kalan gün sayısı 30 günden fazla ise çalışır. Geriye `"OK"` döner.
  * **Faydası (Open/Closed):** Gelecekte yeni bir SKT durumu (Örn: 90 günden az kalanlar için "Warning") eklemek istediğimizde, mevcut çalışan kodları hiç değiştirmeden (bozma riski almadan) sadece yeni bir strateji sınıfı ekleyerek sistemi kolayca büyütebilmemizi sağlar.





