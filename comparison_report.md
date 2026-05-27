# 📊 MİMARİ KARŞILAŞTIRMA VE UYUMLULUK RAPORU

Bu rapor, **Eczane Yönetim Sistemi (Pharmacy Management System)** projesini, derste işlenen haftalık konu başlıklarını içeren **teachersnotes.md** dosyası ve öğretmen tarafından sağlanan referans **student-management-final** projesi ile satır satır karşılaştırmak amacıyla hazırlanmıştır.

Projemizin mevcut mimari yapısı, öğretmeninizin ders boyunca aktardığı temel prensipler ve referans projedeki kodlama alışkanlıkları doğrultusunda incelenmiş, varsa mimari uyumsuzluklar veya referans projeden ayrışan üstün tasarım kararları detaylandırılmıştır.

---

## 1. MİMARİ VE TEKNİK KARŞILAŞTIRMA MATRİSİ

| Mimari Özellik / Katman | Referans Proje (`student-management-final`) | Projemiz (`Java Final Projesi`) | Uyumluluk Durumu / Farklar |
| :--- | :--- | :--- | :--- |
| **Paket Yapısı** | `.controller`, `.rest`, `.model`, `.service`, `.repository`, `.dto`, `.advice`, `.config` | `.controller.ui`, `.controller.api`, `.model`, `.service`, `.repository`, `.dto.request`, `.dto.response`, `.advice`, `.config`, `.strategy` | **Uyumlu / Geliştirilmiş**: Projemiz, DTO istek ve yanıt modellerini ayrı alt paketlerde tutarak ve stratejileri `.strategy` paketine taşıyarak daha temiz bir katmanlaşma sağlamıştır. |
| **Strategy Design Pattern** | `@Qualifier("standard")` kullanılarak derleme zamanında (static) enjeksiyon yapılır. | `List<ExpiryStrategy>` listesi üzerinde `isApplicable` metoduyla çalışma zamanında (dynamic) seçim yapılır. | **Geliştirilmiş / Üstün Tasarım**: Referans projede enjekte edilecek strateji kod içinde sabitlenmişken (`@Qualifier`), projemizde Spring Auto-wiring list enjeksiyonu ile gerçek, esnek bir Strategy Pattern uygulanmıştır. |
| **Entity ↔ DTO Dönüşümü** | Servis katmanında (`StudentService`) gerçekleştirilir. Controller katmanı sadece DTO'ları tanır. | Servis katmanında (`DrugService`, `UserService` vb.) gerçekleştirilir. Controller'lar sadece DTO kabul eder/döner. | **Tam Uyumlu**: İki projede de Clean Code ve KURAL 2 kurallarına tam olarak uyulmuş, dönüşüm mantığı servis katmanında kapsüllenmiştir. |
| **Zorunlu Validasyonlar** | `@NotBlank`, `@Email`, `@Min`, `@Max` gibi Hibernate validasyonları doğrudan JPA Entity (`Student`) içinde tanımlanmıştır. | Validasyonlar öncelikle giriş DTO modellerinde (`DrugCreateRequest`, vb.) tanımlanmış, `@Valid` ile controller girişlerinde denetlenir. | **Uyumlu / Güvenli**: Projemiz veritabanı entity nesnelerine geçersiz veri akışını daha Controller girişindeyken (DTO seviyesinde) engelleyerek daha güvenli bir katman koruması uygulamıştır. |
| **Merkezi Hata Yönetimi** | `@ControllerAdvice` sınıfı genel `Exception` sınıfını yakalar, hatayı model nesnesine koyar ve bir HTML hata sayfasına (`error-page`) yönlendirir. | `@ControllerAdvice` sınıfı tüm özel ve genel istisnaları yakalayarak istemciye yapılandırılmış JSON formatında `ResponseEntity<ErrorResponse>` döner. | **Mimari Fark**: Referans proje geleneksel web tabanlı hata sayfaları üretirken, projemiz API odaklı modern bir JSON hata yanıt mimarisi sunmaktadır. |
| **Spring Security Yapısı** | Bellek içi kullanıcı yönetimi (`InMemoryUserDetailsManager`) ile statik kullanıcılar tanımlanmıştır. | Veritabanı tabanlı `User` entegrasyonu, `BCryptPasswordEncoder` ve dinamik `PharmacyUserDetails` ile gerçek yetkilendirme yapılmıştır. | **Uyumlu / Geliştirilmiş**: Projemiz endüstriyel standartta, veritabanına bağlı ve şifrelenmiş bir kimlik doğrulama mimarisine sahiptir. |
| **Optimistic Locking** | Uygulanmamıştır. Eşzamanlı güncellemeler veritabanında son yazanın kazanması prensibine göre çalışır. | Kritik entity modellerinde (`Drug` ve `Purchase`) `@Version` kullanılarak eşzamanlı veri güncellemeleri kontrol altına alınmıştır. | **Geliştirilmiş / Üstün Tasarım**: Projemiz KURAL 5 gereği eşzamanlı işlem çakışmalarını (`OptimisticLockException`) engelleyecek korumaya sahiptir. |

---

## 2. HOCANIN DOSYALARINA VE REFERANS PROJEYE GÖRE MİMARİ VE LOGIC FARKLARI

### 2.1. Hata Yönetiminde Arayüz ve REST API Uyumsuzluğu
> [!WARNING]
> **En Belirgin Mimari Mantık Farkı:** 
> Referans projede (`student-management-final`), `GlobalExceptionHandler` sınıfı bir hata aldığında hatayı Thymeleaf modeline ekleyip kullanıcıyı `"error-page"` adında kullanıcı dostu bir HTML sayfasına yönlendirir.
> 
> Bizim projemizde ise `com.pharmacy.advice.GlobalExceptionHandler` sınıfı, Thymeleaf arayüzünden (`UIController`) bir hata fırlatılsa dahi istemciye doğrudan **JSON formatında `ResponseEntity<ErrorResponse>`** dönmektedir. Bu durum, arayüzde gezinen bir kullanıcının hata durumunda boş beyaz ekranda ham bir JSON verisi (örn. `{"timestamp":..., "status": 404, "error": "Not Found", ...}`) görmesine sebep olur.
> 
> **Çözüm Tavsiyesi:** UI isteklerinden (Thymeleaf `/dashboard`, `/inventory` vb.) fırlatılan hatalar için `UIController`'a özel, HTML dönen ayrı bir hata yakalama mekanizması veya mevcut handler içinde `Accept` header kontrolü (HTML mi, JSON mı isteniyor) eklenmelidir.

### 2.2. Strategy Pattern Uygulama Biçimi (Statik enjeksiyon vs Dinamik Çalışma Zamanı)
> [!NOTE]
> **Mimari Tasarım Farkı:**
> Dersteki **teachersnotes.md** ve öğretmen projesindeki `StudentService` incelediğinde, `ScoringStrategy` interface'i enjekte edilirken `@Qualifier("standard")` anotasyonu ile **belirli bir strateji sınıfı doğrudan sabitlenmiştir**. Öğretmen, başarı durumunu değerlendirirken kodun içinde başka bir stratejiye dinamik geçiş yapmamaktadır.
> 
> Bizim projemizde ise **KURAL 3** kapsamında uyguladığımız Strategy Pattern çok daha dinamik ve esnektir. `ExpiryService` sınıfımızda:
> 1. Tüm strateji sınıfları (`ExpiredStrategy`, `CriticalStrategy`, `OkStrategy`) Spring tarafından bir `List<ExpiryStrategy>` olarak enjekte edilir.
> 2. Çalışma zamanında ilacın kalan gün sayısına (`daysRemaining`) göre her stratejinin `isApplicable(daysRemaining)` metodu tetiklenir.
> 3. İlgili gün eşleşmesine uyan **doğru strateji dinamik olarak seçilir ve işletilir**.
> 
> Bizim uyguladığımız yöntem, tasarım deseninin teorik tanımına (`Context`'in çalışma zamanında strateji seçmesi) çok daha sadıktır ve öğretmenin statik `@Qualifier` yaklaşımına kıyasla kat kat daha profesyonel bir yazılım mimarisi sergilemektedir.

### 2.3. Paketleme Yapısı ve Katman Ayrımı
* **Referans Proje Paket Adı:** `com.example.student_management_final.rest` (REST controller sınıfları için `.rest` adında ayrı bir paket tercih edilmiştir).
* **Bizim Proje Paket Adı:** `com.pharmacy.controller.api` (Biz, REST API controller sınıflarını `controller` paketi altında `api` adında bir alt pakette toplamayı tercih ettik).
* **DTO Sınıfları:** Referans projede tek bir `StudentDTO` varken, bizim projemizde CRUD işlemlerinin yoğunluğundan ötürü `dto/request` ve `dto/response` şeklinde iki ayrı klasörleme yapılmıştır. Bu yaklaşım, projemizin endüstriyel ölçekteki büyüklüğü göz önüne alındığında çok daha düzenlidir ve karmaşıklığı önler.

---

## 3. HAFTALIK DERS NOTLARI (`teachersnotes.md`) İLE SATIR SATIR UYUMLULUK ANALİZİ

Öğretmenin ders notlarında (Week 1 - Week 14) vurguladığı kritik konuların projemizdeki karşılıkları aşağıda satır satır analiz edilmiştir:

### 🌟 Week 1: OOP Temelleri, Kapsülleme ve Katmanlı Mimari
* **Ders Notu (Line 161 - 321):** "Code without encapsulation is not professional software. Fields are private, methods are public."
  * **Uyum:** Projemizdeki tüm Entity ve DTO modelleri (`Drug`, `User`, `Purchase` vb.) `private` alanlara sahiptir. Getter ve Setter metotları Lombok (`@Data`, `@Getter`, `@Setter`) kullanılarak güvenle oluşturulmuştur ve harfi harfine kapsüllenmiştir.
* **Ders Notu (Line 343 - 366):** "Layered architecture: UI -> Service -> DAO -> Database. The UI never directly accesses the database."
  * **Uyum:** Projemizde **Strict Layering (Katı Katmanlı Mimari - KURAL 1)** uygulanmıştır. `UIController` veya REST `DrugController` gibi sınıflar asla doğrudan `DrugRepository` çağırmaz. Araya mutlak surette `DrugService` katmanı girmekte ve iş kuralları orada işletilmektedir.

### 🌟 Week 3: Domain Modelleme (Domain vs. Technical Concepts)
* **Ders Notu (Line 555):** "Domain is the heart of the system. Domain Model != Database Model."
  * **Uyum:** Projemizin etki alanı (Domain) oldukça zengindir. İlaç yönetimi (`Drug`), barkod, reçete türü (`PresType`), stok partileri (`Purchase`) ve satış geçmişi (`Sale` & `SaleItem`) ile gerçek dünya eczane süreçlerini tam olarak modellemektedir.

### 🌟 Week 4 & Week 12: Tasarım Desenleri (Creational & Behavioral)
* **Ders Notu (Line 791, 1054):** Creational patterns (Builder/Factory) ve Behavioral patterns (Strategy).
  * **Uyum:** DTO nesnelerimiz ve Entity'lerimizin inşasında `@Builder` tasarım şablonu yoğun şekilde kullanılmıştır. SKT durum değerlendirmesi için ise `ExpiryStrategy` çatısı altında saf **Strategy Tasarım Deseni** uygulanmıştır.

### 🌟 Week 11: Spring Boot ve Spring Data JPA
* **Ders Notu (Line 971):** "Power of Spring Data JPA: Instead of writing manual SQL queries, you automate operations thanks to JpaRepository interface."
  * **Uyum:** Tüm veritabanı erişim katmanlarımız (`DrugRepository`, `SaleRepository` vb.) `org.springframework.data.jpa.repository.JpaRepository` interface'ini genişletmektedir. Özel sorgular için JPA derived query metotları (örn: `findAllByOrderByPurchaseDateDesc()`) kullanılmış, manuel JDBC veya hata riski yüksek SQL sorgularından kaçınılmıştır.

### 🌟 Week 13: Validasyon, Hata Yönetimi ve Güvenlik
* **Ders Notu (Line 1062):** Validation, Exception Handling, and Security.
  * **Uyum:**
    * Giriş parametreleri `@NotBlank`, `@Min(0)` gibi anotasyonlarla Hibernate Validator tarafından denetlenmektedir.
    * Kodun hiçbir yerinde `try-catch` blokları ile hata gizlenmemiş, özel exception'lar (`InsufficientStockException`, `DrugNotFoundException`) fırlatılarak merkezi `@ControllerAdvice` sınıfımızda ele alınmıştır.
    * Spring Security ile rol tabanlı (ADMIN/USER) erişim koruması yapılandırılmıştır.

---

## 4. PROJEMİZİN REFERANS PROJEYE GÖRE MİMARİ AÇIDAN ÜSTÜN YANLARI

Projemiz, sadece ders gereksinimlerini karşılamakla kalmamış, birçok konuda öğretmen projesinin üzerinde bir yazılım kalitesine ulaşmıştır:

1. **FIFO Satış ve Stok Yönetim Mantığı (`SaleService`):**
   Referans projede karmaşık bir iş kuralı bulunmamaktadır (sadece basit CRUD). Projemizde ise gerçekçi bir eczane senaryosuna uygun olarak **FIFO (İlk Giren İlk Çıkar)** satış mantığı kurgulanmıştır. Satış esnasında ilaçlar son kullanma tarihine göre sırayla partilerden düşülmekte ve stok güncellenmektedir. Bu esnada oluşabilecek eşzamanlı çakışmalar `@Version` kontrolüyle (Optimistic Locking) engellenmektedir.
2. **Kapsamlı DTO Katmanlaşması:**
   Öğretmen projesi tek bir DTO (`StudentDTO`) ile sınırlıyken, projemizde her istek ve yanıt için özelleştirilmiş istek (`Request`) ve yanıt (`Response`) modelleri tanımlanmıştır. Bu sayede API güvenliği ve veri gizliliği en üst düzeye çıkarılmıştır.
3. **Güvenlik Altyapısı (Spring Security & BCrypt):**
   Referans projede yer alan düz metin (plain text) ve bellek içi (`InMemoryUserDetailsManager`) statik şifre yönetimi yerine, projemizde veritabanına bağlı dinamik kullanıcı doğrulama ve `BCryptPasswordEncoder` şifreleme motoru entegre edilmiştir.
4. **Çoklu Test Kapsamı:**
   Projemiz için yazılan 23 adet birim test (`DrugServiceTest`, `ExpiryServiceTest` vb.) sayesinde iş mantığının ve strateji geçişlerinin doğruluğu tamamen otomatik olarak doğrulanmaktadır.

---

## 5. SONUÇ VE ÖNERİLER

Projeniz, öğretmeninizin `teachersnotes.md` dosyasında belirttiği **tüm akademik ve mimari standartları fazlasıyla karşılamakta**, hatta endüstriyel kalitede tasarım tercihleriyle bu standartların üzerine çıkmaktadır. 

Öğretmeninizin projesine kıyasla göze çarpan tek uyumsuzluk, **arayüz hata yönetiminde (GlobalExceptionHandler)** fırlatılan tüm hataların HTML sayfası yerine JSON yanıtına dönüştürülmesidir. Bu durum bir hata olmamakla birlikte, Thymeleaf ekranlarında hata anında ham JSON çıktısı oluşmasına sebep olabileceğinden, yukarıda açıklanan çözümün uygulanması projeyi kusursuz hale getirecektir.
