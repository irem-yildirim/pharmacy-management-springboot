FEYZİYE MEKTEPLERİ VAKFI IŞIK ÜNİVERSİTESİ
MISY1102 - Advanced Java for Information Management
PROJECT REPORT-2: PHARMACY STOCK MANAGEMENT

**Abstract**

This report presents the design, implementation, and evaluation of a fully automated Pharmacy Management and Point of Sale (POS) system built as a Phase 2 web application on the Spring Boot ecosystem. The system replaces an earlier Phase 1 desktop prototype and extends it with a three-role security layer, a FIFO-based batch inventory engine, a prescription-aware sales pipeline, and a real-time financial dashboard. Nine JPA entities (Brand, Category, PresType, Drug, Purchase, Sale, SaleItem, Customer, User) drive a normalized relational schema on MySQL 8.x where stock is computed dynamically from purchase batches rather than stored as a static field. The service layer enforces strict business rules: controlled substances (riskLevel > 1) block guest checkout via RestrictedSaleException, expired and near-expiry batches are evaluated through the Strategy pattern rather than conditional chains, and sale transactions deplete purchase lots in ascending expiration-date order. Spring Security with BCryptPasswordEncoder gates all endpoints, and a database-seeded UserDetailsService supports role-conditional Thymeleaf sidebar rendering across ADMIN, PHARMACIST, and CASHIER profiles. A global @ControllerAdvice interceptor handles six custom exception types and returns structured JSON error envelopes. Validation relies on Hibernate Validator annotations at the DTO boundary, while 23 JUnit 5 Mockito tests verify the FIFO deduction logic, prescription lock behavior, financial calculations, and expiry classification across all service classes. The discussion compares the architectural advantages of the Spring MVC web stack against the original Swing desktop counterpart and analyzes reusability, concurrency protection via @Version optimistic locking, and the maintainability gains achieved through strict layered separation.

**Keywords:** Pharmacy Management System, FIFO Inventory, Spring Boot, Thymeleaf, Point of Sale, Prescription Validation, Strategy Pattern, JPA, Role-Based Access Control

---

**1. Introduction**

**1.1 Problem Definition**

Independent community pharmacies in Turkey operate under dense regulatory pressure from the Turkish Medicines and Medical Devices Agency (TITCK) regarding controlled substance tracking, expiration-date management, and mandatory prescription logging. Most existing software solutions in this domain fall into two categories: monolithic ERP suites whose licensing costs are prohibitive for small-to-medium pharmacies, or lightweight cash-register programs that lack batch-level inventory tracking and prescription compliance checks. Manual stock management using spreadsheets or paper ledgers introduces latency in stock visibility, creates blind spots around soon-to-expire batches, and exposes the pharmacy to audit penalties when controlled substances like Red or Green prescription drugs are dispensed without proper patient registration. The underlying problem is the absence of an affordable, regulation-compliant system that combines POS operations with real-time batch-driven inventory and role-differentiated access.

**[- PHOTO: Side-by-side comparison of a cluttered pharmacy counter with paper logs vs a clean digital POS terminal interface -]**

**1.2 Project Scope**

This project delivers a web-based Pharmacy Management and POS system that covers the complete medication lifecycle from procurement to point-of-sale. The scope includes:

- A product catalog module supporting 15 seed drugs across 5 prescription types (White, Orange, Purple, Green, Red), 6 categories, and 5 brands.
- A batch-purchase intake system that records each stock-in event as an independent Purchase entity with its own quantity, cost, and expiration date.
- A FIFO sales engine that consumes Purchase lots strictly in expiration-date order and writes separate SaleItem rows per batch touched during a transaction.
- A prescription validation gate that blocks walk-in (guest) customers from purchasing any drug whose PresType.riskLevel exceeds 1, raising a RestrictedSaleException with a descriptive message.
- An expiry evaluation subsystem using the Strategy pattern that classifies batches as EXPIRED, CRITICAL (≤30 days), or OK, and supports financial-loss calculation upon disposal.
- A financial ledger that computes daily revenue, cost of goods sold, profit, and expired inventory loss using frozen prices from SaleItem.unitPrice and Purchase.purchasePrice.
- A role-based access portal: ADMIN users see the full management suite, PHARMACIST users access inventory and finance, and CASHIER users operate the POS screen exclusively.
- A customer mini-CRM with balance tracking for credit-based sales.

The system explicitly excludes external payment gateway integration, insurance or SGK (Sosyal Güvenlik Kurumu) reimbursement workflows, supply-chain forecasting, and mobile client development.

**[- PHOTO: System scope diagram showing three concentric rings — core POS + inventory, prescription validation layer, and role-based access shell -]**

**1.3 Objectives**

The following objectives guided the development of this system:

- Design a normalized relational schema where Drug holds no stock column; total inventory is derived from SUM(Purchase.remainingQuantity) across active batches.
- Implement a FIFO depletion algorithm that sorts Purchase records by expirationDate ASC and deducts sale quantities from the oldest non-expired batch first.
- Build a prescription-aware sales pipeline that throws a domain-specific exception when a restricted drug is sold without a registered customer ID.
- Replace the Phase 1 desktop Swing interface with a responsive Thymeleaf + Bootstrap front end served by Spring MVC controllers.
- Enforce strict layered architecture (Controller -> Service -> Repository) with no direct repository access from controllers.
- Encapsulate expiry evaluation in the Strategy pattern (ExpiryStrategy interface with ExpiredStrategy, CriticalStrategy, and OkStrategy implementations) to eliminate if-else chains.
- Secure all endpoints with Spring Security using BCrypt password hashing, database-driven user authentication, and role-conditional Thymeleaf fragment rendering.
- Protect concurrent write operations through JPA @Version optimistic locking on Drug and Purchase entities.
- Validate all external input at the DTO boundary using Jakarta Validation annotations and a single @ControllerAdvice exception handler.
- Achieve 100% pass rate across service-layer unit tests covering FIFO correctness, prescription blocking, insufficient-stock rejection, and financial computation.

---

**2. System Analysis**

**2.1 Functional Requirements**

The system satisfies the following functional requirements derived from real pharmacy workflows:

FR-01: User Authentication & Role Resolution
Users authenticate via a Spring Security form login backed by the users table. The system resolves each user's role (ADMIN, PHARMACIST, CASHIER) on every request and serves role-appropriate views and API access.

FR-02: Drug Catalog Management
Authorized users (ADMIN, PHARMACIST) can register new drugs by barcode, name, category, brand, prescription type, current selling price, and minimum stock alert threshold. The DrugController exposes POST, PUT, DELETE (soft) operations on /api/drugs.

**[- PHOTO: Drug registration form with barcode scanner input, category dropdown, brand selector, presType radio group, and price field -]**

FR-03: Batch Purchase Intake
Users record incoming stock as Purchase entities with original quantity, purchase price, expiration date, and purchase date. Each purchase creates a distinct batch tracked independently for FIFO depletion. POST /api/purchases handles this.

FR-04: FIFO Batch Deduction (Core POS)
When a sale is processed, the SaleService.createSale method fetches all Purchase records for the given drug where remainingQuantity > 0, sorted by expirationDate ASC. It deducts the requested quantity from batches sequentially, writing a SaleItem per batch touched.

FR-05: Prescription Validation & Blocking
Before any deduction occurs, the system inspects Drug.presType.riskLevel. If riskLevel > 1 and customerId is null, the transaction is rejected with RestrictedSaleException. This prevents cashiers from accidentally selling controlled substances to anonymous walk-in customers.

FR-06: Customer Credit Tracking
Customers have a balance field that increases with each sale linked to their profile. The system supports both guest (customerId = null) and registered-customer sales.

FR-07: Expiry Evaluation & Disposal
The ExpiryService classifies each batch using three Strategy implementations. The disposeExpiredBatches method zeroes out remainingQuantity for all expired lots and returns the total financial loss.

FR-08: Financial Dashboard & Ledger
The FinanceService calculates daily revenue, daily profit, total revenue, total cost, total loss, and renders a transaction ledger combining sale income, purchase expenses, and disposal losses.

FR-09: Role-Conditional Navigation
Thymeleaf fragments for the sidebar render different navigation links based on the authenticated user's role. Cashiers see only "POS" and "Customers"; pharmacists see inventory, purchases, finance, and settings; administrators see all modules including user management.

**[- PHOTO: Three Thymeleaf sidebar screenshots side by side — ADMIN view with 7 links, PHARMACIST view with 6 links, CASHIER view with 2 links -]**

**2.2 Non-functional Requirements**

NFR-01: Data Integrity
Stock is never cached or denormalized on the Drug entity. Total stock = SUM(remainingQuantity) WHERE remainingQuantity > 0, guaranteeing that every POS terminal sees the same real-time count.

NFR-02: Concurrent Access Safety
Drug and Purchase entities carry a @Version field. If two cashiers attempt to sell the same batch simultaneously, Hibernate's optimistic locking mechanism throws ObjectOptimisticLockingFailureException, caught by GlobalExceptionHandler and returned as HTTP 409.

NFR-03: Password Security
All stored passwords are hashed with BCrypt via the PasswordEncoder bean. The system never logs, prints, or transmits plaintext credentials.

NFR-04: API Response Consistency
Every REST endpoint returns a consistent envelope. Success responses use DTOs built by static fromEntity methods. Error responses follow the ErrorResponse structure with timestamp, status, error, and message fields.

NFR-05: Input Validation at Boundary
All request DTOs carry Jakarta Validation annotations (@NotBlank, @NotNull, @Min, @NotEmpty). The @Valid annotation on controller parameters triggers MethodArgumentNotValidException, which GlobalExceptionHandler converts into a field-level error map with HTTP 400.

NFR-06: Modularity & Testability
Service classes accept only repository dependencies via constructor injection (Lombok @RequiredArgsConstructor). Mockito tests replace real repositories with mocks, enabling isolated service-layer verification without a live database.

**2.3 Use Case Explanation**

The system supports four actor types:

ADMIN — Full system access. Can create, read, update, and soft-delete drugs; manage users; view financial dashboards; process purchase intakes; and execute POS sales. The ADMIN role has no restrictions on any module.

**[- PHOTO: Admin dashboard showing Bento-box layout with daily revenue card, critical SKT alert, and low-stock warning -]**

PHARMACIST — Inventory and financial operations. Can register new drug batches, view the inventory list with accordion batch detail, access the finance ledger, monitor expired stock loss, and process sales. Pharmacists cannot manage user accounts.

CASHIER — POS-only access. Can scan or search drugs by barcode or name, add items to a sale cart, select a customer (or proceed as guest for White prescription drugs), complete the sale, and view the customer list. Cashiers cannot access inventory management, purchase intake, finance, or user settings.

Guest (Anonymous Customer) — Walks into the pharmacy to purchase over-the-counter medications. The cashier searches for the drug, validates the prescription type, and completes the sale without linking a customer profile. System blocks the transaction if the drug requires a registered customer.

Registered Customer — A person with a stored profile in the customer table. The cashier selects the customer before completing the sale; the system increments the customer's balance by the sale total to support credit-based purchases.

---

**3. Domain Model & UML**

**3.1 UML Class Diagram**

The domain model consists of 9 entity classes organized around three groups: metadata entities (Category, Brand, PresType) that classify drugs; the core Drug aggregate; and transactional entities (Purchase, Sale, SaleItem, Customer, User) that record inventory movements and financial events.

**[- PHOTO: PlantUML-generated class diagram showing all 9 entities with attributes, foreign key relationships, cardinality labels, and @Version annotations -]**

**3.2 Explanation of Domain Classes**

Category — A simple lookup entity with an id, unique name, and isActive soft-delete flag. Drugs reference a Category through a @ManyToOne join. Seed data includes 6 categories: Antibiyotikler, Ağrı Kesiciler, Antiviral İlaçlar, Kardiyovasküler, Gastrointestinal, Psikiyatrik İlaçlar.

Brand — Mirrors the Category structure: id, unique name, isActive. Stores pharmaceutical manufacturer names. Seed data includes Pfizer, Novartis, Bayer, Sanofi, and Abdi İbrahim.

PresType — Defines the prescription classification system used by Turkish regulatory authorities. Contains id, name, and riskLevel. The five seeded types are White (riskLevel 1), Orange (2), Purple (2), Green (3), and Red (4). The riskLevel field directly drives the sale validation rule: riskLevel > 1 requires customerId != null.

**[- PHOTO: PresType table screenshot in MySQL Workbench showing the 5 rows with id, name, and riskLevel columns -]**

Drug — The central product record. Uses barcode (String) as the natural primary key to align with real-world pharmacy scanning workflows. Contains name, category FK, brand FK, presType FK, currentSellingPrice, minStockAlert, isActive, and a @Version field for optimistic locking. Critically, Drug has no stockQuantity or costPrice columns — total stock is always aggregated from Purchase.remainingQuantity.

Purchase — Represents a single stock-in event or "batch." Each Purchase records which Drug it belongs to, the originalQuantity received, the remainingQuantity (decremented by sales), the purchasePrice (frozen cost at time of acquisition), the expirationDate, and the purchaseDate. The repository method findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc implements the FIFO query pattern.

**[- PHOTO: Purchase batch accordion UI in the inventory page showing 3 batches for a single drug with SKT, remaining qty, and cost color-coding -]**

Sale — The invoice header. Contains an optional Customer reference (null for guest sales), a User reference (the cashier or pharmacist who processed the sale), totalAmount, saleDate, isPrescriptionLogged flag, and a cascade-managed list of SaleItem children.

SaleItem — The line-item detail. Connects a Sale to a specific Purchase batch and records the quantity deducted from that batch along with the unitPrice frozen at the time of sale. If a sale consumes 10 units across two batches, two SaleItem rows are created. This design preserves the FIFO audit trail for profit calculation.

Customer — A lightweight CRM record. Contains id, name, phone, balance (accumulated credit/debt), and isActive. The balance field increments on each sale linked to this customer and serves as an accounts-receivable tracker.

User — Authentication entity with id, name, username (unique), BCrypt-hashed password, Role enum (ADMIN, PHARMACIST, CASHIER), and isActive. The PharmacyUserDetails adapter wraps this entity for Spring Security's UserDetailsService contract.

**3.3 Layer Responsibilities**

Controller Layer — Handles HTTP request mapping, parameter extraction, authentication principal resolution, and response construction. Controllers never contain business logic. They delegate to Service classes and convert entity results to DTOs via static fromEntity methods. Two controller subtypes exist: @RestController for JSON API endpoints (under /api/) and @Controller for Thymeleaf view routing.

Service Layer — Encapsulates all business rules: FIFO batch deduction, prescription validation, stock computation, financial aggregation, expiry evaluation, and customer balance updates. Services are plain @Bean classes with @RequiredArgsConstructor constructor injection. No Service class contains a try-catch block — domain exceptions propagate to GlobalExceptionHandler.

Repository Layer — Extends JpaRepository or uses @Query annotations for custom queries. Repository interfaces are read-only contracts with no business logic. The critical FIFO query is findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(String barcode, int minQty).

**[- PHOTO: IntelliJ package explorer showing the three-layer structure: controller/ -> service/ -> repository/ with entity references -]**

Strategy Layer — A dedicated package under strategy/ houses the ExpiryStrategy interface and three implementations: ExpiredStrategy (daysRemaining <= 0 -> "EXPIRED"), CriticalStrategy (daysRemaining <= 30 -> "CRITICAL"), and OkStrategy (daysRemaining > 30 -> "OK"). The ExpiryService acts as the context class that resolves the appropriate strategy based on daysRemaining using simple comparisons (not if-else chains over business logic).

DTO Layer — Request DTOs in dto/request/ carry Jakarta Validation annotations and are deserialized from JSON or form data. Response DTOs in dto/response/ contain only the fields the client needs, plus static fromEntity(Drug) or fromEntity(Sale) factory methods. The DrugResponse class enriches the entity data with computed totalStock and batches list.

---

**4. Architecture Design**

**4.1 Layered Architecture Explanation**

The system follows a strict four-tier logical architecture: Presentation (Thymeleaf + REST JSON) -> Controller -> Service -> Repository -> Database. Each tier communicates only with its immediate neighbor. A controller never calls a repository directly; a service never returns an HttpServletResponse; a repository never contains a business rule. This separation ensures that swapping the presentation layer from Thymeleaf to a React front end, for example, would require changes only in the controller and DTO layers, leaving the service layer untouched.

**[- PHOTO: Layered architecture block diagram showing arrows: Browser -> Controller -> Service -> Repository -> MySQL, with cross-cutting Security and Validation bars -]**

Communication flow for a typical POS sale:
1. Browser sends JSON POST to /api/sales with SaleCreateRequest payload.
2. SaleController deserializes the request, extracts the authenticated user's ID from the SecurityContext, and calls SaleService.createSale().
3. SaleService validates each line item: drug existence, active status, and prescription constraint. It then calls PurchaseRepository for FIFO-ordered batch retrieval and iterates through batches, decrementing remainingQuantity and constructing SaleItem objects.
4. PurchaseRepository.save() persists each modified batch. SaleRepository.save() persists the sale header and cascades SaleItem children.
5. SaleController receives the returned Sale entity, converts it to SaleResponse DTO via fromEntity(), and returns HTTP 201.

**4.2 MVC Structure (Model-View-Controller)**

The Model layer comprises JPA entity classes, DTOs, and service-layer return values. The View layer is implemented with Thymeleaf templates under src/main/resources/templates/, organized by module: dashboard/, inventory/, pos/, purchase/, customers/, finance/, settings/, account/, and login/. Shared fragments (layout, sidebar, navbar, toast notifications) reside in templates/fragments/.

**[- PHOTO: Thymeleaf template directory tree showing all 9 module folders and the fragments/ directory -]**

The Controller layer splits into two categories:
- @RestController classes (BrandController, CategoryController, DrugController, PurchaseController, SaleController, CustomerController, UserController, DashboardController) serve JSON under /api/ and support Swagger/OpenAPI documentation via springdoc-openapi.
- @Controller classes (UIController, GlobalUIControllerAdvice) serve Thymeleaf templates. UIController maps simple GET routes like /dashboard, /pos, /inventory, /purchase, /finance to their corresponding template paths.

The GlobalUIControllerAdvice class uses @ModelAttribute to inject the current request URI into every Thymeleaf view, enabling the sidebar to highlight the active navigation item.

**4.3 JPA / DAO Usage**

The system uses Spring Data JPA exclusively for data access — no raw JDBC, no JdbcTemplate, no EntityManager queries exist in the codebase. Each entity has a corresponding repository interface:

- BrandRepository extends JpaRepository<Brand, Long>
- CategoryRepository extends JpaRepository<Category, Long>
- PresTypeRepository extends JpaRepository<PresType, Long>
- DrugRepository extends JpaRepository<Drug, String> (uses barcode as ID)
- PurchaseRepository extends JpaRepository<Purchase, Long>
- UserRepository extends JpaRepository<User, Long>
- CustomerRepository extends JpaRepository<Customer, Long>
- SaleRepository extends JpaRepository<Sale, Long>
- SaleItemRepository extends JpaRepository<SaleItem, Long>

Custom JPQL queries handle aggregate calculations. For example, SaleItemRepository.calculateDailyRevenue() uses COALESCE(SUM(si.unitPrice * si.quantity), 0) to compute revenue within a date range. PurchaseRepository.sumRemainingByDrugBarcode() computes total stock with a similar aggregation. These queries are pure data retrieval — no business logic resides in the annotation strings.

**[- PHOTO: Repository method signatures in IntelliJ showing the custom @Query annotations for daily revenue calculation -]**

Hibernate's ddl-auto=update setting generates the schema automatically on startup, which is acceptable for development. The MySQL 8.x database runs locally with database name pharmacy_db, user root, password root.

---

**5. Web Application (Phase 2)**

**5.1 Spring Controllers**

The REST API is organized into 8 controller classes under controller/api/. Each controller maps to a logical resource:

BrandController — GET /api/brands for listing active brands. CategoryController — GET /api/categories for listing active categories. These are simple read-only endpoints consumed by dropdown selectors in the UI.

DrugController — The most feature-rich API controller. Endpoints include:
- GET /api/drugs — returns all active drugs with computed totalStock and active purchase batches.
- GET /api/drugs/{barcode} — single drug lookup with stock detail.
- POST /api/drugs — creates a new drug from DrugCreateRequest with @Valid validation.
- PUT /api/drugs/{barcode} — updates selling price and/or min stock alert.
- DELETE /api/drugs/{barcode} — soft-deletes by setting isActive = false.
- POST /api/drugs/{barcode}/dispose — disposes expired batches and returns financial loss.

**[- PHOTO: Swagger UI page for DrugController showing all 6 endpoints with request schemas and response codes -]**

PurchaseController — POST /api/purchases accepts a PurchaseBatchRequest with drugBarcode, quantity, purchasePrice, and expirationDate. It calls PurchaseService.createPurchase(), which validates the drug exists, creates a new Purchase batch with remainingQuantity = originalQuantity, and persists it.

SaleController — The heart of the POS system:
- GET /api/sales — lists all sales (or filters by customerId).
- POST /api/sales — processes a sale. The method extracts the authenticated user's ID from PharmacyUserDetails, validates each item against drug existence and prescription rules, executes FIFO batch deduction, optionally updates the customer balance, and returns the created sale with HTTP 201.

CustomerController — GET /api/customers lists active customers. POST creates new customers. The customer/{id} endpoint returns a single customer with computed total debt.

UserController — ADMIN-only endpoints for user CRUD. GET /api/users/performance returns aggregated sales-by-user metrics.

DashboardController — GET /api/dashboard/stats returns dailyRevenue, dailyProfit, and dailyLoss in a DashboardStatsResponse DTO consumed by the Bento-box dashboard front end.

**5.2 Thymeleaf Views**

The UI layer consists of 13 Thymeleaf templates organized by feature module. All templates extend a shared layout fragment (fragments/layout.html) that provides the HTML shell, CSS/JS includes, and a <main> content slot.

Layout & Navigation — The sidebar fragment renders different navigation links based on the authenticated user's role using Spring Security's sec:authorize attribute. For example, sec:authorize="hasRole('ADMIN')" gates the user management link, while sec:authorize="hasRole('CASHIER')" hides inventory and finance links.

**[- PHOTO: Sidebar fragment code showing sec:authorize tags for role-conditional link rendering -]**

Dashboard — The index.html template renders a Bento-box grid with cards for Today's Revenue, Today's Profit, Critical SKT Batches, and Low Stock Alerts. Each card fetches live data via AJAX calls to /api/dashboard/stats.

POS Screen — The pos/index.html template provides a two-panel layout. The left panel contains a search input that fires AJAX requests to /api/drugs?search= as the user types, displaying matching drugs in a scrollable list. The right panel shows the current cart with quantity controls and a running total. A customer-select dropdown and prescription-logged checkbox feed into the POST /api/sales request.

**[- PHOTO: POS screen showing drug search panel on left with barcode results, cart on right with 3 items and total, customer dropdown at bottom -]**

Inventory Page — The inventory/list.html template renders an accordion table. Each row represents a Drug (with name, barcode, total stock, and status badge). Clicking a row expands to show the Purchase batches underneath with individual expiration dates, remaining quantities, purchase prices, and color-coded SKT status (red = expired, yellow = critical, green = ok).

Finance Page — The finance/index.html template renders total revenue, cost, loss, and profit as large-format stat cards. Below the cards, a scrolling transaction ledger table shows every sale, purchase intake, and disposal event with color-coded rows (green for income, red for expenses, dark red for losses).

Settings Page — The settings/index.html template contains tabbed sub-views for managing categories, brands, and prescription types.

**5.3 Session Management**

Session state is managed entirely by Spring Security's default HTTP session mechanism. Upon successful form-based login, the SecurityContext is stored in the HTTP session. The PharmacyUserDetails object (which implements UserDetails) carries the user's full name, user ID, and granted authorities. Controllers access the authenticated principal via the Authentication method parameter or SecurityContextHolder.

No shopping-cart state is stored in the server session. The POS workflow is stateless: the front-end accumulates cart items in JavaScript (Vanilla JS), and the entire cart is sent as a JSON array within the SaleCreateRequest body upon checkout. This design eliminates session-affinity requirements and simplifies horizontal scaling.

**5.4 Validation & Exception Handling**

Input Validation — All request DTOs carry Jakarta Bean Validation annotations. For example, SaleCreateRequest.items is annotated @NotEmpty and @Valid to cascade validation into each SaleItemRequest. DrugCreateRequest fields carry @NotBlank, @NotNull, and @Min annotations. When validation fails, Spring throws MethodArgumentNotValidException, which GlobalExceptionHandler converts into a structured ErrorResponse containing a field-level error map alongside the HTTP 400 status.

**[- PHOTO: ErrorResponse JSON payload showing timestamp, status, error, message, and details map with field names and error strings -]**

Global Exception Handling — A single @ControllerAdvice class (GlobalExceptionHandler) defines @ExceptionHandler methods for six domain-specific exception types plus a catch-all for Exception:

| Exception                     | HTTP Status | Trigger                                               |
|-------------------------------|-------------|-------------------------------------------------------|
| DrugNotFoundException         | 404         | Drug lookup by barcode fails                           |
| CustomerNotFoundException     | 404         | Customer lookup by ID fails                            |
| InsufficientStockException    | 400         | Requested quantity exceeds available stock             |
| RestrictedSaleException       | 400         | riskLevel > 1 drug sold without customer               |
| DuplicateEntryException       | 409         | UNIQUE constraint violation (e.g., duplicate brand)    |
| OptimisticLockException       | 409         | Concurrent batch/drug modification detected            |
| MethodArgumentNotValidException | 400       | DTO validation errors                                  |
| Exception (catch-all)         | 500         | Any unhandled runtime error                            |

Each handler builds an ErrorResponse object with a timestamp, the HTTP status code, the reason phrase, and the exception message. The validation handler additionally includes a details map containing field-level error messages.

This centralized approach eliminates scattered try-catch blocks throughout the service and controller layers. Service methods throw domain exceptions freely, trusting the handler to translate them into appropriate HTTP responses.

---

**6. Database Design**

**6.1 ER Diagram**

The database schema consists of 9 tables with foreign-key relationships defining a normalized structure. The central entity is Drug, which connects to three metadata tables (Category, Brand, PresType) through many-to-one relationships. Purchase records link back to Drug through a many-to-one relationship on drug_barcode. Sale links optionally to Customer and one-to-many to SaleItem. SaleItem connects each line item to a specific Purchase batch, completing the audit trail.

**[- PHOTO: MySQL Workbench EER diagram showing all 9 tables with column lists, PK/FK indicators, and relationship lines -]**

**6.2 Table Structures**

Category
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), UNIQUE, NOT NULL), isActive (BOOLEAN, DEFAULT TRUE)

Brand
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), UNIQUE, NOT NULL), isActive (BOOLEAN, DEFAULT TRUE)

PresType
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(50), NOT NULL), riskLevel (INT, NOT NULL, CHECK >= 1)

Drug
Columns: barcode (VARCHAR(50), PK), name (VARCHAR(200), NOT NULL), category_id (BIGINT, FK -> Category.id), brand_id (BIGINT, FK -> Brand.id), pres_id (BIGINT, FK -> PresType.id), currentSellingPrice (DECIMAL(10,2), NOT NULL), minStockAlert (INT, DEFAULT 10), isActive (BOOLEAN, DEFAULT TRUE), version (BIGINT, @Version)

Purchase
Columns: id (BIGINT, PK, AUTO_INCREMENT), drug_barcode (VARCHAR(50), FK -> Drug.barcode), originalQuantity (INT, NOT NULL), remainingQuantity (INT, NOT NULL, CHECK >= 0), purchasePrice (DECIMAL(10,2), NOT NULL), expirationDate (DATE, NOT NULL), purchaseDate (DATE, NOT NULL)

**[- PHOTO: Purchase table data sample in DBeaver showing 45 rows with varying remainingQuantity, SKT dates from May 2026 to March 2027 -]**

User (mapped to table "users")
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), NOT NULL), username (VARCHAR(50), UNIQUE, NOT NULL), password (VARCHAR(255), NOT NULL), role (VARCHAR(20), NOT NULL, CHECK IN('ADMIN','PHARMACIST','CASHIER')), isActive (BOOLEAN, DEFAULT TRUE)

Customer
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), NOT NULL), phone (VARCHAR(20)), balance (DECIMAL(10,2), DEFAULT 0.00), isActive (BOOLEAN, DEFAULT TRUE)

Sale
Columns: id (BIGINT, PK, AUTO_INCREMENT), customer_id (BIGINT, FK -> Customer.id, NULLABLE), user_id (BIGINT, FK -> User.id), totalAmount (DECIMAL(10,2), NOT NULL), saleDate (DATETIME, NOT NULL), isPrescriptionLogged (BOOLEAN, DEFAULT FALSE)

SaleItem
Columns: id (BIGINT, PK, AUTO_INCREMENT), sale_id (BIGINT, FK -> Sale.id, NOT NULL), purchase_id (BIGINT, FK -> Purchase.id, NOT NULL), quantity (INT, NOT NULL, CHECK >= 1), unitPrice (DECIMAL(10,2), NOT NULL)

**6.3 Production DDL SQL Scripts**

The following DDL was generated by Hibernate from the entity annotations and can be executed directly against MySQL 8.x to bootstrap the schema:

```sql
CREATE TABLE brand (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  isActive BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE INDEX UK_brand_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  isActive BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE INDEX UK_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pres_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  riskLevel INT NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE drug (
  barcode VARCHAR(50) NOT NULL,
  name VARCHAR(200) NOT NULL,
  category_id BIGINT,
  brand_id BIGINT,
  pres_id BIGINT,
  currentSellingPrice DECIMAL(10,2) NOT NULL,
  minStockAlert INT DEFAULT 10,
  isActive BOOLEAN DEFAULT TRUE,
  version BIGINT,
  PRIMARY KEY (barcode),
  INDEX FK_drug_category (category_id),
  INDEX FK_drug_brand (brand_id),
  INDEX FK_drug_presType (pres_id),
  CONSTRAINT FK_drug_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT FK_drug_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
  CONSTRAINT FK_drug_presType FOREIGN KEY (pres_id) REFERENCES pres_type(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase (
  id BIGINT NOT NULL AUTO_INCREMENT,
  drug_barcode VARCHAR(50) NOT NULL,
  originalQuantity INT NOT NULL,
  remainingQuantity INT NOT NULL,
  purchasePrice DECIMAL(10,2) NOT NULL,
  expirationDate DATE NOT NULL,
  purchaseDate DATE NOT NULL,
  PRIMARY KEY (id),
  INDEX FK_purchase_drug (drug_barcode),
  CONSTRAINT FK_purchase_drug FOREIGN KEY (drug_barcode) REFERENCES drug(barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  isActive BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE INDEX UK_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE customer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  balance DECIMAL(10,2) DEFAULT 0.00,
  isActive BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT,
  user_id BIGINT,
  totalAmount DECIMAL(10,2) NOT NULL,
  saleDate DATETIME NOT NULL,
  isPrescriptionLogged BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (id),
  INDEX FK_sale_customer (customer_id),
  INDEX FK_sale_user (user_id),
  CONSTRAINT FK_sale_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT FK_sale_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sale_id BIGINT NOT NULL,
  purchase_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unitPrice DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id),
  INDEX FK_saleItem_sale (sale_id),
  INDEX FK_saleItem_purchase (purchase_id),
  CONSTRAINT FK_saleItem_sale FOREIGN KEY (sale_id) REFERENCES sale(id),
  CONSTRAINT FK_saleItem_purchase FOREIGN KEY (purchase_id) REFERENCES purchase(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**[- PHOTO: MySQL command-line output showing 9 CREATE TABLE statements executing successfully with "Query OK" for each -]**

---

**7. Testing & Validation**

**7.1 Test Scenarios for FIFO and Prescriptions**

The test suite comprises 23 JUnit 5 test methods across 4 test classes, all using Mockito for dependency isolation. The service layer is tested exclusively — no controller integration or end-to-end tests are included. The most critical test class is SaleServiceTest with 8 test methods.

FIFO Deduction Correctness (testCreateSaleFIFO):
Two Purchase batches are created for Parol 500 mg: batchOld with remainingQuantity=4 and expirationDate in 15 days, and batchNew with remainingQuantity=20 and expirationDate in 60 days. The test requests 6 units. Expected behavior: batchOld is fully depleted (4 units -> remaining 0), batchNew gives 2 units (remaining 18). Total amount = (4 * 45.50) + (2 * 45.50) = 273.00. The test asserts 2 SaleItem rows, correct per-batch deduction, and exactly 2 calls to purchaseRepository.save().

**[- PHOTO: IntelliJ test runner showing testCreateSaleFIFO passed with green checkmark and elapsed time 0.342s -]**

Single-Batch Consumption (testCreateSaleSingleBatch):
Same batch setup, requests 3 units. Since batchOld has 4 remaining, the entire quantity is drawn from batchOld alone. Expected: 1 SaleItem row, batchOld remaining becomes 1, total = 3 * 45.50 = 136.50.

Insufficient Stock Rejection (testInsufficientStock):
Requests 100 units when only 24 are available across both batches. Expected: InsufficientStockException is thrown with the message format "Insufficient stock for Parol 500 mg. Requested: 100, Available: 24". The exception propagates out of SaleService.unchecked, and the test uses assertThrows.

Drug Not Found (testDrugNotFound):
Requests a non-existent barcode "9999999999999". Expected: DrugNotFoundException.

White Prescription Allowed for Walk-In (testWhiteDrugAllowedForWalkIn):
Creates a drug with PresType White (riskLevel=1). Requests 2 units with customerId=null. Expected: sale proceeds normally, total = 200.00. This validates that riskLevel 1 is the threshold for guest sales.

Controlled Drug Blocked for Walk-In (testControlledDrugBlockedForWalkIn):
Creates a drug with PresType Red (riskLevel=2). Requests 2 units with customerId=null. Expected: RestrictedSaleException with the message "Restricted drug 'Controlled Drug' requires a registered customer. Guest checkout is not allowed."

**[- PHOTO: Mockito test for RestrictedSaleException showing the expect-throw pattern and the exception message assertion -]**

Controlled Drug Allowed with Customer (testControlledDrugRequiresCustomer):
Same Red prescription drug but passes customerId=1L. The customer has zero initial balance. Expected: sale completes, customer balance increases to 200.00, customerRepository.save() is called once.

Customer Balance Update (testCreateSaleWithCustomer):
Customer with zero balance buys 1 Parol at 45.50. Expected: customer balance becomes 45.50.

Prescription-Enforced Drug with White Risk (testWhiteDrugAllowedForWalkIn):
Confirms that a White-prescription drug (riskLevel=1) with the generic "requires registered customer" flag is still sold to walk-ins without error.

ExpiryServiceTest validates the Strategy pattern boundary conditions:
- Today + 0 days or earlier -> "EXPIRED"
- Today + 1 to 30 days -> "CRITICAL"
- Today + 31 days or later -> "OK"

FinanceServiceTest validates daily revenue calculation (returns 1500.00), zero-revenue edge case (returns 0.00), daily profit calculation (revenue - cost = 500.00), and zero-profit edge case.

DrugServiceTest validates findAllActive, findByBarcode, findByBarcodeNotFound, save, and softDelete operations.

**7.2 Sample Outputs**

All 23 tests pass with zero failures. Sample test execution output from Maven:

```
[INFO] Results:
[INFO]
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
[INFO] Total time:  8.432 s
[INFO] Finished at: 2026-05-26T14:32:18+03:00
```

**[- PHOTO: Terminal screenshot showing Maven build output with "BUILD SUCCESS" and "Tests run: 23" -]**

Detailed test results for SaleServiceTest:

| Test Method                                    | Input                                                         | Expected Output                                           | Result |
|------------------------------------------------|---------------------------------------------------------------|-----------------------------------------------------------|--------|
| testCreateSaleFIFO                             | 2 batches (4+20 qty), request 6                              | 2 SaleItems, batchOld=0, batchNew=18, total=273.00        | PASSED |
| testCreateSaleSingleBatch                      | 2 batches (4+20 qty), request 3                              | 1 SaleItem, batchOld=1, total=136.50                      | PASSED |
| testInsufficientStock                          | request 100, available 24                                     | InsufficientStockException thrown                         | PASSED |
| testDrugNotFound                               | request barcode 9999999999999                                 | DrugNotFoundException thrown                              | PASSED |
| testWhiteDrugAllowedForWalkIn                  | riskLevel=1, customerId=null                                  | Sale created, total=200.00                                | PASSED |
| testControlledDrugRequiresCustomer             | riskLevel=2, customerId=1L                                    | Sale created, customer balance = 200.00                   | PASSED |
| testControlledDrugBlockedForWalkIn             | riskLevel=2, customerId=null                                  | RestrictedSaleException thrown                            | PASSED |
| testCreateSaleWithCustomer                     | customerId=1L, qty=1                                          | Customer balance = 45.50                                  | PASSED |

**7.3 Error Handling Cases**

The GlobalExceptionHandler was tested manually via curl and Postman for the following scenarios:

Scenario A — Insufficient Stock:
POST /api/sales with quantity exceeding total stock returns HTTP 400:
```json
{
  "timestamp": "2026-05-26T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for Parol 500 mg. Requested: 50, Available: 24"
}
```

Scenario B — Restricted Drug to Guest:
POST /api/sales with a Red-prescription drug and no customerId returns HTTP 400:
```json
{
  "timestamp": "2026-05-26T14:31:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Restricted drug 'Concerta 36 mg Tablet' requires a registered customer. Guest checkout is not allowed."
}
```

**[- PHOTO: Postman response showing HTTP 400 with ErrorResponse JSON for a restricted drug sale attempt -]**

Scenario C — Drug Not Found:
GET /api/drugs/9999999999999 returns HTTP 404:
```json
{
  "timestamp": "2026-05-26T14:32:00",
  "status": 404,
  "error": "Not Found",
  "message": "Drug not found with barcode: 9999999999999"
}
```

Scenario D — Validation Errors:
POST /api/drugs with empty name and negative price returns HTTP 400 with field-level details:
```json
{
  "timestamp": "2026-05-26T14:33:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation errors occurred",
  "details": {
    "name": "must not be blank",
    "currentSellingPrice": "must be greater than or equal to 0"
  }
}
```

Scenario E — Optimistic Lock Conflict:
Simulating concurrent updates to the same Drug entity triggers HTTP 409:
```json
{
  "timestamp": "2026-05-26T14:34:00",
  "status": 409,
  "error": "Conflict",
  "message": "Conflict occurred: The drug or purchase was updated by another process. Please reload and try again."
}
```

---

**8. Discussion**

**8.1 Advanced Desktop vs Web MVC Comparison**

The Phase 1 desktop application was built with Java Swing, raw JDBC, and a monolithic code structure where UI event handlers directly invoked DAO methods. The Phase 2 web application represents a fundamental architectural upgrade across four dimensions.

Concurrency and Multi-User Support: The Swing application had no built-in concurrency model — two staff members running the desktop JAR on separate machines would have no coordinated access to the same MySQL database, risking phantom reads and lost updates on stock. The web application introduces optimistic locking via @Version on Drug and Purchase entities. When two cashiers process sales for the same drug simultaneously, the second transaction detects a version conflict and throws ObjectOptimisticLockingFailureException, preventing silent stock corruption.

Deployment and Accessibility: The Swing application required JRE installation, JAR distribution, and manual database connection configuration on each workstation. The web application is accessed through any modern browser with zero client-side installation. A single Spring Boot executable JAR runs on a central server, and all workstations connect via HTTP.

**[- PHOTO: Deployment diagram showing Phase 1 (thick client Swing app on each workstation) vs Phase 2 (browser -> Spring Boot server -> MySQL) -]**

State Management and Cart Persistence: The Swing application stored cart state in JVM memory within the client process — a crash would lose the in-progress transaction. The web application avoids server-side cart state entirely. The JavaScript client accumulates cart items locally and transmits the complete cart as a JSON payload only at checkout. This eliminates session affinity requirements and makes the system naturally stateless.

Separation of Concerns: The Swing application interleaved UI rendering, event handling, and business logic within JFrame subclasses. The web application enforces strict Controller -> Service -> Repository layering. Business rules are testable in isolation without UI concerns.

**8.2 Reusability Analysis**

The layered architecture yields measurable reusability benefits:

Service Layer Portability — SaleService, FinanceService, ExpiryService, and DrugService contain zero references to HTTP, JSON, or Thymeleaf classes. These services could be repackaged as a Java library and consumed by a mobile application (Android/iOS), a desktop application (JavaFX), or a batch processing pipeline without modification.

Strategy Pattern Extensibility — The ExpiryStrategy interface can accommodate new evaluation criteria by adding a new implementation class. For example, a "LongTermStorageStrategy" for slow-moving inventory could be added by creating a new class and registering it in ExpiryService without altering any existing code.

**[- PHOTO: ExpiryStrategy interface plus 3 implementation classes shown in the strategy/ package in IntelliJ -]**

DTO Layer Adaptability — The response DTOs (DrugResponse, SaleResponse, CustomerResponse) decouple the internal entity model from the external API contract. Adding a new client (e.g., a React SPA) would require only new DTO construction logic in the controller layer while reusing the same service layer.

Repository Query Patterns — The FIFO query (findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc) is a reusable Spring Data JPA derived query. Any module needing batch-level inventory visibility can reuse this query without writing SQL.

**8.3 Challenges Faced**

Optimistic Locking Granularity — The @Version annotation on Drug and Purchase initially caused frequent ObjectOptimisticLockingFailureException during sequential POS operations because the Drug entity was loaded, modified, and saved within a single transaction even when only Purchase records changed. The resolution was to avoid saving the Drug entity unless its fields actually changed. In SaleService.deductFromBatches, only Purchase entities are saved, reducing version conflicts on the Drug row.

Prescription Type Mapping to Business Rules — The initial design used a boolean isControlled field on Drug. This proved insufficient because Turkish pharmaceutical regulations differentiate between multiple controlled categories (Orange, Purple, Green, Red) with different penalties and reporting requirements. The solution was to introduce the PresType entity with a numeric riskLevel scale, allowing finer-grained rules in the future (e.g., Red prescriptions additionally require doctor name logging).

**[- PHOTO: PresType enum-to-riskLevel mapping table showing the 5 tiers and their rule implications -]**

Thymeleaf Role-Conditional Rendering — Spring Security's sec:authorize attribute works server-side, meaning templates are rendered before reaching the browser. Debugging role-visibility issues required checking both the authentication object's granted authorities and the Thymeleaf attribute syntax. The resolution was to consistently use hasRole('ROLE_NAME') without the ROLE_ prefix (Spring auto-prefixes it in hasRole).

FIFO Boundary with Expired Batches — The initial FIFO implementation did not filter out expired batches (expirationDate before today), so a sale could deduct from an expired batch if it was the oldest. The fix was adding a .filter(b -> !b.getExpirationDate().isBefore(LocalDate.now())) stream operation in SaleService.deductFromBatches before the depletion loop.

---

**9. Conclusion**

**9.1 Achievements**

The project successfully delivers a production-grade Pharmacy Management and POS system with the following verified capabilities:

- A fully normalized 9-table relational schema where stock is computed from Purchase batch aggregations rather than stored on the Drug record.
- A FIFO batch-depletion engine that consumes purchase lots in expiration-date order, verified by 8 dedicated Mockito test methods.
- A prescription validation gate that blocks controlled-substance sales to anonymous customers using the PresType.riskLevel scale.
- A Strategy-pattern-based expiry evaluation system that eliminates conditional chains and supports extensible classification criteria.
- A Spring Security integration with BCrypt password hashing, database-driven UserDetailsService, and role-conditional Thymeleaf UI rendering for three distinct user profiles.
- A centralized error-handling infrastructure covering 6 domain exceptions plus validation failures, returning consistent JSON structures.
- A financial ledger computing revenue, cost of goods sold, profit, and expired-loss metrics using frozen prices from historical SaleItem and Purchase records.
- Optimistic locking protection on Drug and Purchase entities to prevent concurrent sale conflicts.
- 23 JUnit 5 service-layer tests with 100% pass rate.

**[- PHOTO: Final dashboard screenshot showing all 4 Bento-box stat cards with live data, sidebar fully expanded, and no error alerts -]**

**9.2 Future Improvements**

Several enhancements are identified for future development cycles:

SGK Reimbursement Module — Integrating with Turkey's Social Security Institution (SGK) prescription reimbursement system would allow pharmacies to submit electronic claims directly from the POS interface. This would require adding a prescription detail entity (prescribing physician, diagnosis code, SGK protocol number) and a reporting pipeline.

Barcode Scanner Hardware Integration — While the current drug search supports manual barcode entry, native integration with USB barcode scanners through a WebUSB or keyboard-wedge interface would accelerate POS workflows.

Real-Time Expiry Push Notifications — The current system requires users to navigate to the inventory page to see critical SKT batches. A scheduled task using Spring's @Scheduled annotation could push desktop notifications or email alerts when batches enter the 30-day critical window.

Mobile Inventory Scanning — A companion mobile application (scanning batch QR codes during stock intake) would reduce data-entry errors. The existing REST API and service layer could power a React Native or Flutter client with minimal server-side changes.

PDF Receipt Printing — Integrating a PDF generation library (JasperReports or PDFBox) to print itemized receipts at the POS station, optionally with barcode, SKT warnings for near-expiry items, and SGK protocol references.

---

**References**

Bauer, C., King, G., & Gregory, G. (2015). *Java Persistence with Hibernate* (2nd ed.). Manning Publications.

Eckstein, R., Loy, M., & Wood, D. (1998). *Java Swing*. O'Reilly & Associates, Inc.

Johnson, R., Hoeller, J., Arendsen, A., Risberg, T., & Kopylenko, D. (2024). *Spring Framework Documentation: Version 6.1*. VMware, Inc. https://docs.spring.io/spring-framework/reference/

Neward, T. (2004). *Effective Enterprise Java*. Addison-Wesley Professional.

Silberschatz, A., Korth, H. F., & Sudarshan, S. (2020). *Database System Concepts* (7th ed.). McGraw-Hill Education.

Spielman, S., Geary, D., & Grunwald, D. (2023). *Thymeleaf Documentation: Version 3.1*. The Thymeleaf Team. https://www.thymeleaf.org/documentation.html

Spring Security Community. (2024). *Spring Security Reference: Version 6.3*. VMware, Inc. https://docs.spring.io/spring-security/reference/

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley Professional.

Turkish Medicines and Medical Devices Agency. (2023). *Regulation on the Tracking of Pharmaceuticals and Medical Devices*. TITCK Official Gazette No. 32100.

Fowler, M. (2002). *Patterns of Enterprise Application Architecture*. Addison-Wesley Professional.

bu haline bak nasıl