FEYZİYE MEKTEPLERİ VAKFI IŞIK ÜNİVERSİTESİ
MISY1102 - Advanced Java for Information Management
PROJECT REPORT-2: PHARMACY STOCK MANAGEMENT

**Abstract**

This report presents the design and implementation of a Pharmacy Management and Point of Sale (POS) system built as a web application using the Spring Boot framework. The application manages the complete medication lifecycle, from stock procurement to customer sales, and includes prescription-based access restrictions, batch-level inventory tracking, and financial reporting.

The system uses nine JPA entities — Brand, Category, PresType, Drug, Purchase, Sale, SaleItem, Customer, and User — mapped to a normalized MySQL database. Instead of storing a single stock count on the Drug record, total stock is calculated dynamically from Purchase batches, which improves data accuracy and supports FIFO (First In, First Out) depletion during sales.

The service layer handles the core business rules. Controlled substances with a risk level above 1 cannot be sold to anonymous customers; the system raises a RestrictedSaleException in such cases. Expired and near-expiry batches are classified using the Strategy pattern with three implementations (ExpiredStrategy, CriticalStrategy, OkStrategy). During a sale, purchase lots are consumed in ascending expiration-date order to ensure older stock is sold first.

Spring Security provides authentication and authorization with BCrypt password hashing. Three user roles — ADMIN, PHARMACIST, and CASHIER — see different views and have different permissions. A global @ControllerAdvice class handles five custom domain exceptions along with standard validation and locking exceptions, returning consistent JSON error responses.

Input validation is handled through Jakarta Validation annotations on DTO classes. The application also includes 23 JUnit 5 unit tests using Mockito to verify the FIFO deduction logic, prescription blocking, financial calculations, and expiry classification across the service layer. Optimistic locking via @Version on both Drug and Purchase entities protects against concurrent modification issues.

**Keywords:** Pharmacy Management System, FIFO Inventory, Spring Boot, Thymeleaf, Point of Sale, Prescription Validation, Strategy Pattern, JPA, Role-Based Access Control

---

**1. Introduction**

**1.1 Problem Definition**

Independent pharmacies face a range of operational challenges related to stock management, prescription tracking, and regulatory compliance. Many existing software solutions in this area are either expensive enterprise systems that small pharmacies cannot afford, or simple cash-register programs that lack batch-level inventory tracking and prescription enforcement. When pharmacies rely on manual methods like spreadsheets or paper records, it becomes difficult to track expiration dates accurately, identify low-stock items in time, and ensure that controlled substances are only sold to registered patients.

In Turkey, pharmaceuticals are classified under different prescription categories (White, Orange, Purple, Green, Red), each with different dispensing rules. Selling a controlled drug without verifying the customer's identity can lead to regulatory penalties. The core problem this project addresses is the lack of an affordable system that combines point-of-sale operations with batch-driven inventory management, automatic expiry tracking, and prescription compliance — all within a role-based access environment.

**[- PHOTO: Side-by-side comparison of a cluttered pharmacy counter with paper logs vs a clean digital POS terminal interface -]**

**1.2 Project Scope**

This project delivers a web-based Pharmacy Management and POS system that covers the medication lifecycle from procurement to point-of-sale. The scope includes:

- A product catalog module supporting 15 drugs across 5 prescription types (White, Orange, Purple, Green, Red), 6 categories, and 5 brands.
- A batch-purchase intake system where each stock-in event is recorded as an independent Purchase entity with its own quantity, cost, and expiration date.
- A FIFO sales engine that consumes purchase lots in expiration-date order and creates separate SaleItem rows for each batch involved in a transaction.
- A prescription validation mechanism that prevents anonymous customers from purchasing any drug whose prescription type has a risk level above 1.
- An expiry evaluation module using the Strategy pattern that classifies batches as EXPIRED, CRITICAL (≤30 days remaining), or OK, and supports financial-loss calculation when batches are disposed.
- A financial reporting module that calculates daily revenue, cost of goods sold, profit, and expired-inventory loss based on prices recorded at the time of sale and purchase.
- A role-based access system: ADMIN users have full access, PHARMACIST users can manage inventory and finances, and CASHIER users can only operate the POS screen.
- A customer management module with balance tracking for credit-based sales.

The system does not include external payment gateway integration, insurance (SGK) reimbursement workflows, supply-chain forecasting, or mobile client development.

**[- PHOTO: System scope diagram showing three concentric rings — core POS + inventory, prescription validation layer, and role-based access shell -]**

**1.3 Objectives**

The following objectives guided the development:

- Design a normalized relational schema where the Drug entity does not hold a stock column. Total inventory is derived from SUM(Purchase.remainingQuantity) across active batches.
- Implement a FIFO depletion algorithm that sorts Purchase records by expirationDate in ascending order and deducts sale quantities from the oldest non-expired batch first.
- Build a prescription-aware sales flow that throws a domain-specific exception when a restricted drug is sold without a registered customer.
- Develop a responsive web interface using Thymeleaf templates served by Spring MVC controllers.
- Follow a strict layered architecture (Controller → Service → Repository) where controllers do not access repositories directly.
- Use the Strategy pattern for expiry evaluation, with three separate implementations (ExpiredStrategy, CriticalStrategy, OkStrategy) to avoid conditional if-else chains.
- Secure all endpoints with Spring Security, using BCrypt password hashing, database-driven user authentication, and role-based view rendering in Thymeleaf templates.
- Protect concurrent write operations using JPA @Version optimistic locking on both Drug and Purchase entities.
- Validate all external input at the DTO level using Jakarta Validation annotations and handle errors through a centralized @ControllerAdvice exception handler.
- Achieve a 100% pass rate across all service-layer unit tests covering FIFO correctness, prescription blocking, insufficient-stock rejection, and financial computation.

---

**2. System Analysis**

**2.1 Functional Requirements**

The system satisfies the following functional requirements based on real pharmacy workflows:

FR-01: User Authentication & Role Resolution
Users log in through a Spring Security form-based login page. The system reads each user's role (ADMIN, PHARMACIST, CASHIER) from the database and displays the appropriate views and API access based on that role.

FR-02: Drug Catalog Management
Authorized users (ADMIN, PHARMACIST) can register new drugs by entering a barcode, name, category, brand, prescription type, selling price, and minimum stock alert threshold. The DrugController provides POST, PUT, and soft-DELETE operations through the /api/drugs endpoint.

**[- PHOTO: Drug registration form with barcode scanner input, category dropdown, brand selector, presType radio group, and price field -]**

FR-03: Batch Purchase Intake
Users record incoming stock as Purchase entities with original quantity, purchase price, expiration date, and purchase date. Each purchase creates a separate batch that is tracked independently for FIFO depletion. The PurchaseController handles this through POST /api/purchases.

FR-04: FIFO Batch Deduction (Core POS)
When a sale is processed, the SaleService fetches all Purchase records for the selected drug where remainingQuantity is greater than 0, sorted by expirationDate in ascending order. It deducts the requested quantity from batches sequentially, creating a SaleItem record for each batch that is touched during the transaction.

FR-05: Prescription Validation
Before any stock deduction occurs, the system checks the drug's prescription type and risk level. If the risk level is greater than 1 and no customer is selected, the transaction is rejected with a RestrictedSaleException. This prevents controlled substances from being sold to anonymous walk-in customers.

FR-06: Customer Credit Tracking
Customers have a balance field that increases with each sale linked to their profile. The system supports both guest sales (no customer selected) and registered-customer sales.

FR-07: Expiry Evaluation & Disposal
The ExpiryService classifies each batch using three Strategy implementations. The disposeExpiredBatches method sets the remaining quantity to zero for all expired lots of a given drug and returns the total financial loss.

FR-08: Financial Dashboard & Ledger
The FinanceService calculates daily revenue, daily profit, total revenue, total cost, total expired-stock loss, and generates a transaction ledger that combines sale income, purchase expenses, and disposal losses in a single chronological view.

FR-09: Role-Based Navigation
The sidebar navigation menu displays different links depending on the logged-in user's role. Cashiers see only "POS" and "Customers." Pharmacists see inventory, purchases, finance, and settings. Administrators see all modules including user management.

**[- PHOTO: Three Thymeleaf sidebar screenshots side by side — ADMIN view with 7 links, PHARMACIST view with 6 links, CASHIER view with 2 links -]**

**2.2 Non-functional Requirements**

NFR-01: Data Integrity
Stock is never stored directly on the Drug entity. Total stock equals the sum of remainingQuantity across all active Purchase batches. This approach ensures that every terminal sees the same real-time inventory count.

NFR-02: Concurrent Access Safety
Drug and Purchase entities use a @Version field. If two cashiers try to sell from the same batch at the same time, Hibernate's optimistic locking mechanism detects the conflict and throws an ObjectOptimisticLockingFailureException. The GlobalExceptionHandler catches this and returns an HTTP 409 response.

NFR-03: Password Security
All passwords are hashed with BCrypt through the PasswordEncoder bean. The system never stores, logs, or transmits plaintext passwords.

NFR-04: API Response Consistency
Every REST endpoint returns a consistent response structure. Successful responses use DTO objects built by static fromEntity methods. Error responses follow the ErrorResponse format with timestamp, status, error type, and message fields.

NFR-05: Input Validation at Boundary
All request DTOs carry Jakarta Validation annotations (@NotBlank, @NotNull, @Min, @NotEmpty). When validation fails, the GlobalExceptionHandler converts the resulting MethodArgumentNotValidException into a field-level error map returned with an HTTP 400 status.

NFR-06: Modularity & Testability
Service classes receive their dependencies through constructor injection using Lombok's @RequiredArgsConstructor. This makes it straightforward to replace real repositories with mocks in unit tests, enabling service-layer testing without a live database.

**2.3 Use Case Explanation**

The system supports four actor types:

ADMIN — Has full access to the system. Can create, read, update, and soft-delete drugs; manage user accounts; view financial dashboards; record purchase intakes; and process POS sales. The ADMIN role has no restrictions on any module.

**[- PHOTO: Admin dashboard showing Bento-box layout with daily revenue card, critical SKT alert, and low-stock warning -]**

PHARMACIST — Handles inventory and financial operations. Can register new drug batches, view the inventory list with batch details, access the finance ledger, monitor expired stock loss, and process sales. Pharmacists cannot manage user accounts.

CASHIER — Limited to POS operations. Can search for drugs by barcode or name, add items to a sale cart, select a customer (or proceed as guest for non-restricted drugs), complete the sale, and view the customer list. Cashiers cannot access inventory management, purchase intake, finance, or user settings.

Guest (Anonymous Customer) — A walk-in customer who purchases over-the-counter medications. The cashier processes the sale without selecting a customer profile. The system blocks the transaction if the drug requires a registered customer due to its prescription type.

Registered Customer — A patient with a stored profile in the customer table. The cashier selects the customer before completing the sale. The system adds the sale total to the customer's balance for credit tracking.

---

**3. Domain Model & UML**

**3.1 UML Class Diagram**

The domain model consists of 9 entity classes organized into three groups: metadata entities (Category, Brand, PresType) that classify drugs; the core Drug entity; and transactional entities (Purchase, Sale, SaleItem, Customer, User) that record inventory movements and financial events.

**[- PHOTO: PlantUML-generated class diagram showing all 9 entities with attributes, foreign key relationships, cardinality labels, and @Version annotations -]**

**3.2 Explanation of Domain Classes**

Category — A simple lookup entity with an id, a unique name, and an isActive flag for soft deletion. Drugs reference a Category through a @ManyToOne relationship. The system includes 6 categories: Antibiyotikler, Ağrı Kesiciler, Antiviral İlaçlar, Kardiyovasküler, Gastrointestinal, and Psikiyatrik İlaçlar.

Brand — Has the same structure as Category: id, unique name, and isActive. It stores pharmaceutical manufacturer names. The system includes 5 brands: Pfizer, Novartis, Bayer, Sanofi, and Abdi İbrahim.

PresType — Represents the prescription classification system used in Turkish pharmaceutical regulations. It contains id, name, and riskLevel. The five types are White (riskLevel 1), Orange (2), Purple (2), Green (3), and Red (4). The riskLevel field determines the sale restriction: if riskLevel is greater than 1, the system requires a registered customer for the transaction.

**[- PHOTO: PresType table screenshot in MySQL Workbench showing the 5 rows with id, name, and riskLevel columns -]**

Drug — The central product entity. It uses the barcode (String) as its primary key, which aligns with how pharmacies identify drugs in real-world workflows. The entity includes name, category, brand, and prescription type references, along with currentSellingPrice, minStockAlert, isActive, and a @Version field for optimistic locking. Notably, the Drug entity does not contain a stock quantity column. Total stock is always calculated from the sum of Purchase.remainingQuantity.

Purchase — Represents a single stock-in event, or "batch." Each Purchase records the drug it belongs to, the original quantity received, the remaining quantity (which decreases as items are sold), the purchase price at the time of acquisition, the expiration date, the purchase date, and a @Version field for concurrent write protection. The repository provides a query method that retrieves batches sorted by expiration date in ascending order, which is the foundation of the FIFO logic.

**[- PHOTO: Purchase batch accordion UI in the inventory page showing 3 batches for a single drug with SKT, remaining qty, and cost color-coding -]**

Sale — Serves as the invoice header. It contains an optional reference to a Customer (null for guest sales), a reference to the User who processed the sale, the total amount, the sale date, a prescription-logged flag, and a list of SaleItem children managed via cascade.

SaleItem — The line-item detail that connects a Sale to a specific Purchase batch. It records the quantity taken from that batch and the unit price at the time of sale. If a single sale consumes units from two different batches, two SaleItem rows are created. This design preserves the FIFO audit trail and enables accurate profit calculation.

Customer — A lightweight customer record with id, name, phone, balance (accumulated total from linked sales), and isActive. The balance field increases with each sale linked to the customer and serves as a simple accounts-receivable tracker.

User — The authentication entity with id, name, unique username, BCrypt-hashed password, role (ADMIN, PHARMACIST, or CASHIER), and isActive. The PharmacyUserDetails adapter wraps this entity for Spring Security's UserDetailsService. A DataInitializer class exists in the config package for seeding initial data in development environments; it is currently disabled in the codebase for database safety, but can be re-enabled on fresh setups.

**3.3 Layer Responsibilities**

Controller Layer — Handles HTTP request mapping, parameter extraction, authentication principal resolution, and response construction. Controllers act as a thin entry and exit layer. They delegate all business operations to service classes and convert returned entities to DTO responses using static fromEntity methods. Two controller types exist: @RestController classes for JSON API endpoints (under /api/) and @Controller classes for Thymeleaf view routing.

Service Layer — Contains all business rules: FIFO batch deduction, prescription validation, stock computation, financial aggregation, expiry evaluation, customer balance updates, and DTO-to-entity mapping. Services are standard Spring @Service beans with constructor dependency injection. Domain exceptions are thrown freely in the service layer and handled by the global exception handler — there are no try-catch blocks in service code.

Repository Layer — Extends JpaRepository and uses @Query annotations for custom queries. Repositories serve as pure data access contracts with no business logic. The most important query is the FIFO query: findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(String barcode, int minQty).

**[- PHOTO: IntelliJ package explorer showing the three-layer structure: controller/ -> service/ -> repository/ with entity references -]**

Strategy Layer — A dedicated strategy package contains the ExpiryStrategy interface and its three implementations: ExpiredStrategy, CriticalStrategy, and OkStrategy.
- ExpiryStrategy defines two methods: `boolean isApplicable(long daysRemaining)` and `String evaluate(long daysRemaining)`.
- ExpiredStrategy returns true when `daysRemaining ≤ 0` and evaluates to `"EXPIRED"`.
- CriticalStrategy returns true when `daysRemaining > 0 && daysRemaining ≤ 30` and evaluates to `"CRITICAL"`.
- OkStrategy returns true when `daysRemaining > 30` and evaluates to `"OK"`.

The ExpiryService acts as the context class. It autowires all strategy implementations as a `List<ExpiryStrategy>`, then uses a stream filter to find and apply the matching strategy for a given expiration date. This approach avoids if-else chains and makes it easy to add new expiry classifications in the future.

DTO Layer — Request DTOs in the dto/request package carry Jakarta Validation annotations and are deserialized from JSON or form data. Response DTOs in the dto/response package contain only the fields the client needs, along with static fromEntity factory methods. This separation prevents entity objects and their database connections from being exposed to the presentation layer.

---

**4. Architecture Design**

**4.1 Layered Architecture Explanation**

The system follows a four-tier logical architecture: Presentation (Thymeleaf + REST JSON) → Controller → Service → Repository → Database. Each tier communicates only with its immediate neighbor. A controller never calls a repository directly, a service never returns an HTTP response object, and a repository never contains business rules. This separation means that replacing the presentation layer — for example, switching from Thymeleaf to a React front end — would require changes only in the controller and DTO layers, while the service layer would remain unchanged.

**[- PHOTO: Layered architecture block diagram showing arrows: Browser -> Controller -> Service -> Repository -> MySQL, with cross-cutting Security and Validation bars -]**

A typical POS sale follows this communication flow:
1. The browser sends a JSON POST request to /api/sales with a SaleCreateRequest payload.
2. SaleController deserializes the request, extracts the authenticated user's ID from the security context, and calls SaleService.createSale().
3. SaleService validates each line item: checking that the drug exists, is active, and that prescription rules are satisfied. It then retrieves FIFO-ordered batches from PurchaseRepository and iterates through them, decrementing remainingQuantity and constructing SaleItem objects.
4. PurchaseRepository.save() persists each modified batch. SaleRepository.save() persists the sale header and cascades the SaleItem children.
5. SaleController receives the returned Sale entity, converts it to a SaleResponse DTO using fromEntity(), and returns HTTP 201.

**4.2 MVC Structure (Model-View-Controller)**

The Model layer includes JPA entity classes, DTOs, and service-layer return values. The View layer uses Thymeleaf templates stored under src/main/resources/templates/, organized by module: dashboard/, inventory/, pos/, purchase/, customers/, finance/, settings/, account/, and login/. Shared fragments for the layout, sidebar, navbar, and toast notifications are placed in templates/fragments/.

**[- PHOTO: Thymeleaf template directory tree showing all 9 module folders and the fragments/ directory -]**

The Controller layer is split into two categories:
- @RestController classes (BrandController, CategoryController, DrugController, PurchaseController, SaleController, CustomerController, UserController, DashboardController) serve JSON responses under /api/ and support Swagger/OpenAPI documentation through the springdoc-openapi library.
- @Controller classes (UIController, GlobalUIControllerAdvice) serve Thymeleaf templates. UIController maps simple GET routes like /dashboard, /pos, /inventory, /purchase, and /finance to their corresponding template paths.

The GlobalUIControllerAdvice class uses @ModelAttribute to inject the current request URI into every Thymeleaf view. This allows the sidebar to highlight the active navigation item.

**4.3 JPA / DAO Usage**

The system uses Spring Data JPA exclusively for data access. There is no raw JDBC, JdbcTemplate, or direct EntityManager usage in the codebase. Each entity has a corresponding repository interface:

- BrandRepository extends JpaRepository<Brand, Long>
- CategoryRepository extends JpaRepository<Category, Long>
- PresTypeRepository extends JpaRepository<PresType, Long>
- DrugRepository extends JpaRepository<Drug, String> (uses barcode as ID)
- PurchaseRepository extends JpaRepository<Purchase, Long>
- UserRepository extends JpaRepository<User, Long>
- CustomerRepository extends JpaRepository<Customer, Long>
- SaleRepository extends JpaRepository<Sale, Long>
- SaleItemRepository extends JpaRepository<SaleItem, Long>

Custom JPQL queries handle aggregate calculations. For example, SaleItemRepository.calculateDailyRevenue() uses COALESCE(SUM(si.unitPrice * si.quantity), 0) to compute revenue within a date range. PurchaseRepository.sumRemainingByDrugBarcode() calculates total stock with a similar aggregation. These queries perform data retrieval only — they do not contain business logic.

**[- PHOTO: Repository method signatures in IntelliJ showing the custom @Query annotations for daily revenue calculation -]**

Hibernate's ddl-auto=update setting generates the schema automatically on startup, which is suitable for development. The MySQL 8.x database runs locally with the database name pharmacy_db, using root credentials.

---

**5. Web Application (Phase 2)**

**5.1 Spring Controllers**

The REST API consists of 8 controller classes under the controller/api/ package. Each controller corresponds to a logical resource:

BrandController — Provides the following endpoints:
- GET /api/brands — Lists all active, non-deleted brands.
- POST /api/brands — Creates a new brand using the BrandCreateRequest DTO with @Valid input validation.
- DELETE /api/brands/{id} — Soft-deletes a brand. The BrandService checks for linked drugs before allowing the deletion.

CategoryController — Provides similar endpoints:
- GET /api/categories — Lists all active categories.
- POST /api/categories — Creates a new category from a CategoryCreateRequest DTO.
- DELETE /api/categories/{id} — Soft-deletes a category after checking for linked drugs.

DrugController — The most feature-rich controller. It includes:
- GET /api/drugs — Returns all active drugs with their computed total stock and active purchase batches.
- GET /api/drugs/{barcode} — Returns a single drug with stock details.
- POST /api/drugs — Creates a new drug using a DrugCreateRequest DTO with @Valid validation.
- PUT /api/drugs/{barcode} — Updates the selling price and/or minimum stock alert threshold.
- DELETE /api/drugs/{barcode} — Soft-deletes the drug by setting isActive to false.
- POST /api/drugs/{barcode}/dispose — Disposes expired batches and returns the financial loss.

**[- PHOTO: Swagger UI page for DrugController showing all 6 endpoints with request schemas and response codes -]**

PurchaseController — Provides POST /api/purchases, which accepts a PurchaseBatchRequest containing the drug barcode, quantity, purchase price, and expiration date. The PurchaseService validates that the drug exists, creates a new Purchase batch with remainingQuantity equal to the original quantity, and persists it.

SaleController — Handles POS transactions:
- GET /api/sales — Lists all sales, with an optional customerId filter.
- POST /api/sales — Processes a sale. The controller extracts the authenticated user's ID from the security context, validates each item against drug existence and prescription rules, runs the FIFO batch deduction logic, optionally updates the customer balance, and returns the created sale with HTTP 201.

CustomerController — GET /api/customers lists active customers. GET /api/customers/search allows searching by name or phone. POST /api/customers creates a new customer.

UserController — Provides ADMIN-only endpoints for user management. GET /api/users returns all staff accounts. POST /api/users creates a new user. GET /api/users/performance returns aggregated sales metrics for the logged-in user.

DashboardController — GET /api/dashboard/stats returns daily revenue, daily profit, and expired-stock loss in a DashboardStatsResponse DTO used by the dashboard front end.

**5.2 Thymeleaf Views**

The UI layer consists of Thymeleaf templates organized by feature module. All templates extend a shared layout fragment (fragments/layout.html) that provides the HTML structure, CSS/JS includes, and a main content area.

Layout & Navigation — The sidebar fragment shows different navigation links based on the authenticated user's role using Spring Security's sec:authorize attribute. For example, the user management link is visible only to users with the ADMIN role, while inventory and finance links are hidden from CASHIER users.

**[- PHOTO: Sidebar fragment code showing sec:authorize tags for role-conditional link rendering -]**

Dashboard — The dashboard template displays a grid of cards showing Today's Revenue, Today's Profit, Critical Expiry Batches, and Low Stock Alerts. Each card fetches live data via AJAX calls to /api/dashboard/stats.

POS Screen — The POS template has a two-panel layout. The left panel contains a search input that sends AJAX requests to the drug API as the user types, displaying matching results in a scrollable list. The right panel shows the current cart with quantity controls and a running total. A customer dropdown and prescription-logged checkbox are included in the checkout form.

**[- PHOTO: POS screen showing drug search panel on left with barcode results, cart on right with 3 items and total, customer dropdown at bottom -]**

Inventory Page — The inventory template renders an accordion table. Each row represents a drug with its name, barcode, total stock, and status badge. Clicking a row expands to show the purchase batches underneath with individual expiration dates, remaining quantities, purchase prices, and color-coded expiry status (red = expired, yellow = critical, green = ok).

Finance Page — The finance template displays total revenue, cost, loss, and profit as stat cards. Below the cards, a scrolling transaction ledger shows every sale, purchase intake, and disposal event with color-coded rows (green for income, red for expenses, dark red for losses).

Settings Page — The settings template contains tabbed sub-views for managing categories, brands, and prescription types.

**5.3 Session Management**

Session state is managed by Spring Security's default HTTP session mechanism. After a successful form-based login, the security context is stored in the HTTP session. The PharmacyUserDetails object carries the user's full name, user ID, and granted authorities. Controllers access the authenticated user through the Authentication method parameter or the SecurityContextHolder.

No shopping-cart state is stored on the server. The POS workflow is stateless: the front-end accumulates cart items in JavaScript, and the entire cart is sent as a JSON array within the SaleCreateRequest body when the user clicks checkout. This design avoids session-affinity issues and simplifies the application architecture.

**5.4 Validation & Exception Handling**

Input Validation — All request DTOs use Jakarta Bean Validation annotations. For example, the items field in SaleCreateRequest is annotated with @NotEmpty and @Valid to cascade validation into each SaleItemRequest. DrugCreateRequest fields use @NotBlank, @NotNull, and @Min annotations. When validation fails, Spring raises a MethodArgumentNotValidException, which the GlobalExceptionHandler converts into a structured ErrorResponse containing a field-level error map with HTTP 400 status.

**[- PHOTO: ErrorResponse JSON payload showing timestamp, status, error, message, and details map with field names and error strings -]**

Global Exception Handling — The application includes a single @ControllerAdvice class (GlobalExceptionHandler) that defines handler methods for five custom domain exceptions as well as standard JPA and validation exceptions:

| Exception                     | HTTP Status | Trigger                                               |
|-------------------------------|-------------|-------------------------------------------------------|
| DrugNotFoundException         | 404         | Drug lookup by barcode fails                           |
| CustomerNotFoundException     | 404         | Customer lookup by ID fails                            |
| InsufficientStockException    | 400         | Requested quantity exceeds available stock             |
| RestrictedSaleException       | 400         | Controlled drug sold without a registered customer     |
| DuplicateEntryException       | 409         | Linked entity prevents soft-delete (e.g., brand with drugs) |
| OptimisticLockException       | 409         | Concurrent modification detected on the same record   |
| MethodArgumentNotValidException | 400       | DTO validation errors                                 |
| Exception (catch-all)         | 500         | Any unhandled runtime error                            |

Each handler builds an ErrorResponse object with a timestamp, the HTTP status code, a reason phrase, and the exception message. The validation handler also includes a details map with field-level error messages.

This centralized approach means that service methods can throw domain exceptions without needing try-catch blocks. The exception handler takes care of translating each exception into the appropriate HTTP response. For both REST API calls and Thymeleaf template endpoints, errors are returned in a consistent JSON format, which simplifies debugging and allows the front-end JavaScript (especially in the POS interface) to parse and display error messages without full-page reloads.

---

**6. Database Design**

**6.1 ER Diagram**

The database schema consists of 9 tables with foreign-key relationships. The central entity is Drug, which connects to three metadata tables (Category, Brand, PresType) through many-to-one relationships. Purchase records link back to Drug through a many-to-one relationship on drug_barcode. Sale links optionally to Customer and has a one-to-many relationship with SaleItem. SaleItem connects each line item to a specific Purchase batch, completing the audit trail from sale to original stock batch.

**[- PHOTO: MySQL Workbench EER diagram showing all 9 tables with column lists, PK/FK indicators, and relationship lines -]**

**6.2 Table Structures**

Category
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), UNIQUE, NOT NULL), isActive (BOOLEAN, DEFAULT TRUE)

Brand
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), UNIQUE, NOT NULL), isActive (BOOLEAN, DEFAULT TRUE)

PresType
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(50), NOT NULL), riskLevel (INT, NOT NULL)

Drug
Columns: barcode (VARCHAR(50), PK), name (VARCHAR(200), NOT NULL), category_id (BIGINT, FK → Category.id), brand_id (BIGINT, FK → Brand.id), pres_id (BIGINT, FK → PresType.id), currentSellingPrice (DECIMAL(10,2), NOT NULL), minStockAlert (INT, DEFAULT 10), isActive (BOOLEAN, DEFAULT TRUE), version (BIGINT)

Purchase
Columns: id (BIGINT, PK, AUTO_INCREMENT), drug_barcode (VARCHAR(50), FK → Drug.barcode), originalQuantity (INT, NOT NULL), remainingQuantity (INT, NOT NULL), purchasePrice (DECIMAL(10,2), NOT NULL), expirationDate (DATE, NOT NULL), purchaseDate (DATE, NOT NULL), version (BIGINT)

**[- PHOTO: Purchase table data sample in DBeaver showing 45 rows with varying remainingQuantity, SKT dates from May 2026 to March 2027 -]**

User (mapped to table "users")
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), NOT NULL), username (VARCHAR(50), UNIQUE, NOT NULL), password (VARCHAR(255), NOT NULL), role (VARCHAR(20), NOT NULL), isActive (BOOLEAN, DEFAULT TRUE)

Customer
Columns: id (BIGINT, PK, AUTO_INCREMENT), name (VARCHAR(100), NOT NULL), phone (VARCHAR(20)), balance (DECIMAL(10,2), DEFAULT 0.00), isActive (BOOLEAN, DEFAULT TRUE)

Sale
Columns: id (BIGINT, PK, AUTO_INCREMENT), customer_id (BIGINT, FK → Customer.id, NULLABLE), user_id (BIGINT, FK → User.id), totalAmount (DECIMAL(10,2), NOT NULL), saleDate (DATETIME, NOT NULL), isPrescriptionLogged (BOOLEAN, DEFAULT FALSE)

SaleItem
Columns: id (BIGINT, PK, AUTO_INCREMENT), sale_id (BIGINT, FK → Sale.id, NOT NULL), purchase_id (BIGINT, FK → Purchase.id, NOT NULL), quantity (INT, NOT NULL), unitPrice (DECIMAL(10,2), NOT NULL)

**6.3 Production DDL SQL Scripts**

The following DDL was generated by Hibernate from the entity annotations and can be executed directly against MySQL 8.x:

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
  version BIGINT,
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

The test suite includes 23 JUnit 5 test methods across 4 test classes, all using Mockito for dependency isolation. Only the service layer is tested — no controller integration or end-to-end tests are included. The most critical test class is SaleServiceTest with 8 test methods.

FIFO Deduction Correctness (testCreateSaleFIFO):
Two Purchase batches are created for Parol 500 mg: batchOld with remainingQuantity = 4 and an expiration date in 15 days, and batchNew with remainingQuantity = 20 and an expiration date in 60 days. The test requests 6 units. Expected behavior: batchOld is fully depleted (4 units → remaining 0), batchNew gives 2 units (remaining 18). Total amount = (4 × 45.50) + (2 × 45.50) = 273.00. The test asserts 2 SaleItem rows, correct per-batch deduction, and exactly 2 calls to purchaseRepository.save().

**[- PHOTO: IntelliJ test runner showing testCreateSaleFIFO passed with green checkmark and elapsed time 0.342s -]**

Single-Batch Consumption (testCreateSaleSingleBatch):
Same batch setup, but only 3 units are requested. Since batchOld has 4 remaining, the entire quantity is drawn from batchOld alone. Expected: 1 SaleItem row, batchOld remaining becomes 1, total = 3 × 45.50 = 136.50.

Insufficient Stock Rejection (testInsufficientStock):
Requests 100 units when only 24 are available across both batches. Expected: InsufficientStockException is thrown with the message "Insufficient stock for Parol 500 mg. Requested: 100, Available: 24."

Drug Not Found (testDrugNotFound):
Requests a non-existent barcode "9999999999999." Expected: DrugNotFoundException is thrown.

White Prescription Allowed for Walk-In (testWhiteDrugAllowedForWalkIn):
A drug with PresType White (riskLevel = 1) is created. A sale of 2 units is attempted with customerId = null. Expected: the sale proceeds normally, total = 200.00. This confirms that riskLevel 1 is the threshold for guest sales.

Controlled Drug Blocked for Walk-In (testControlledDrugBlockedForWalkIn):
A drug with PresType Red (riskLevel = 2) is created. A sale of 2 units is attempted with customerId = null. Expected: RestrictedSaleException is thrown with the message "Restricted drug 'Controlled Drug' requires a registered customer. Guest checkout is not allowed."

**[- PHOTO: Mockito test for RestrictedSaleException showing the expect-throw pattern and the exception message assertion -]**

Controlled Drug Allowed with Customer (testControlledDrugRequiresCustomer):
Same Red prescription drug but with customerId = 1L. The customer has zero initial balance. Expected: sale completes, customer balance increases to 200.00, customerRepository.save() is called once.

Customer Balance Update (testCreateSaleWithCustomer):
A customer with zero balance purchases 1 Parol at 45.50. Expected: customer balance becomes 45.50.

ExpiryServiceTest validates the Strategy pattern behavior with boundary conditions:
- Today + 0 days or earlier → "EXPIRED" (handled by ExpiredStrategy)
- Today + 1 to 30 days → "CRITICAL" (handled by CriticalStrategy)
- Today + 31 days or later → "OK" (handled by OkStrategy)

FinanceServiceTest validates daily revenue calculation (returns 1500.00), zero-revenue edge case (returns 0.00), daily profit calculation (revenue − cost = 500.00), and the zero-profit edge case.

DrugServiceTest validates core service operations: creating a drug from a DrugCreateRequest DTO, soft-deleting a drug (flipping the isActive flag to false), retrieving all active drugs (findAllActive), and barcode-based lookups — both successful (findByBarcode) and failure cases (findByBarcodeNotFound, which verifies that DrugNotFoundException is thrown).

**7.2 Sample Outputs**

All 23 tests pass with zero failures. Sample test execution output from Maven:

```
[INFO] Results:
[INFO]
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
[INFO] Total time:  8.432 s
[INFO] Finished at: 2026-05-26T14:32:18+03:00
```

**[- PHOTO: Terminal screenshot showing Maven build output with "BUILD SUCCESS" and "Tests run: 23" -]**

Detailed test results for SaleServiceTest:

| Test Method                                    | Input                                                         | Expected Output                                           | Result |
|------------------------------------------------|---------------------------------------------------------------|-----------------------------------------------------------|--------|
| testCreateSaleFIFO                             | 2 batches (4+20 qty), request 6                               | 2 SaleItems, batchOld=0, batchNew=18, total=273.00        | PASSED |
| testCreateSaleSingleBatch                      | 2 batches (4+20 qty), request 3                               | 1 SaleItem, batchOld=1, total=136.50                      | PASSED |
| testInsufficientStock                          | request 100, available 24                                     | InsufficientStockException thrown                         | PASSED |
| testDrugNotFound                               | request barcode 9999999999999                                 | DrugNotFoundException thrown                              | PASSED |
| testWhiteDrugAllowedForWalkIn                  | riskLevel=1, customerId=null                                  | Sale created, total=200.00                                | PASSED |
| testControlledDrugRequiresCustomer             | riskLevel=2, customerId=1L                                    | Sale created, customer balance = 200.00                   | PASSED |
| testControlledDrugBlockedForWalkIn             | riskLevel=2, customerId=null                                  | RestrictedSaleException thrown                            | PASSED |
| testCreateSaleWithCustomer                     | customerId=1L, qty=1                                          | Customer balance = 45.50                                  | PASSED |

**7.3 Error Handling Cases**

The GlobalExceptionHandler was tested manually using curl and Postman for the following scenarios:

Scenario A — Insufficient Stock:
POST /api/sales with a quantity exceeding total stock returns HTTP 400:
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
POST /api/drugs with an empty name and a negative price returns HTTP 400 with field-level details:
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
Simulating concurrent updates to the same Drug or Purchase entity returns HTTP 409:
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

**8.1 Desktop vs Web Application Comparison**

The Phase 1 desktop application was built with Java Swing, raw JDBC, and a monolithic code structure where UI event handlers called DAO methods directly. The Phase 2 web application represents a significant improvement across several dimensions.

Concurrency and Multi-User Support: The Swing application had no built-in concurrency model. If two staff members ran the desktop application on separate machines, they would have no coordinated access to the database, which could lead to stock inconsistencies. The web application addresses this through optimistic locking via @Version on both Drug and Purchase entities. When two cashiers process sales for the same drug at the same time, the second transaction detects a version conflict and the system returns a clear error message, preventing silent stock corruption.

Deployment and Accessibility: The Swing application required JRE installation, JAR distribution, and manual database configuration on each workstation. The web application runs as a single Spring Boot executable JAR on a central server. All workstations access it through a web browser with no client-side installation needed.

**[- PHOTO: Deployment diagram showing Phase 1 (thick client Swing app on each workstation) vs Phase 2 (browser -> Spring Boot server -> MySQL) -]**

State Management: The Swing application stored cart state in JVM memory — a crash would lose the in-progress transaction. The web application avoids server-side cart state entirely. The front-end JavaScript accumulates cart items locally and sends the complete cart as a JSON payload only at checkout. This keeps the system stateless and eliminates session management concerns.

Separation of Concerns: The Swing application mixed UI rendering, event handling, and business logic within JFrame subclasses. The web application uses a strict Controller → Service → Repository layering, which makes business rules testable in isolation without any UI dependency.

**8.2 Reusability Analysis**

The layered architecture provides clear reusability benefits:

Service Layer Portability — SaleService, FinanceService, ExpiryService, and DrugService do not reference any HTTP, JSON, or Thymeleaf classes. These services could be reused by a mobile application (Android/iOS), a desktop application (JavaFX), or a batch processing pipeline without any modifications.

Strategy Pattern Extensibility — The ExpiryStrategy interface makes it easy to add new expiry classifications. For example, a "LongTermStorageStrategy" for slow-moving inventory could be added by creating a single new class without changing any existing code.

**[- PHOTO: ExpiryStrategy interface plus 3 implementation classes shown in the strategy/ package in IntelliJ -]**

DTO Layer Adaptability — The response DTOs (DrugResponse, SaleResponse, CustomerResponse) separate the internal entity model from the external API contract. Adding a new client, such as a React single-page application, would require only new controller-level logic while reusing the same service layer.

Repository Query Patterns — The FIFO query (findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc) is a reusable Spring Data JPA derived query. Any module that needs batch-level inventory information can reuse this query without writing custom SQL.

**8.3 Challenges Faced**

Optimistic Locking Granularity: The @Version annotation on Drug and Purchase initially caused frequent conflicts during POS operations because the Drug entity was being loaded and saved even when only Purchase records were changing. The solution was to save the Drug entity only when its fields actually change. In SaleService.deductFromBatches, only Purchase entities are saved, which reduces unnecessary version conflicts on the Drug row.

Prescription Type Modeling: The initial design used a simple boolean isControlled field on Drug. This turned out to be insufficient because Turkish regulations differentiate between multiple controlled categories (Orange, Purple, Green, Red), each with different requirements. The solution was to introduce the PresType entity with a numeric riskLevel, which allows more detailed rules in the future (for example, Red prescriptions could additionally require a doctor name).

**[- PHOTO: PresType enum-to-riskLevel mapping table showing the 5 tiers and their rule implications -]**

Thymeleaf Role-Based Rendering: Spring Security's sec:authorize attribute works server-side, meaning templates are rendered before reaching the browser. Debugging role-visibility issues required verifying both the authentication object's granted authorities and the Thymeleaf attribute syntax. The key was to use hasRole('ROLE_NAME') consistently, keeping in mind that Spring automatically prefixes role names with "ROLE_".

FIFO Boundary with Expired Batches: The initial FIFO implementation did not filter out expired batches, so a sale could accidentally deduct from an expired batch if it was the oldest. This was fixed by adding a filter in SaleService.deductFromBatches that excludes batches where the expiration date is before today, before the depletion loop begins.

DTO Extraction for Clean Architecture: To maintain clean package separation, the PurchaseBatchRequest class (originally a static nested class inside PurchaseController) was extracted into its own file under the dto/request package. This ensures that all request binding models are kept separate from controller logic.

Entity Mapping in the Service Layer: To keep controllers as thin routing layers, all DTO-to-entity conversion and builder logic was moved into service classes. For example, DrugService.create() accepts a DrugCreateRequest and handles the entity construction internally, and UserService.createUser() does the same for user accounts. This prevents JPA model manipulation from appearing in controller code.

Eliminating Duplicate Methods: An audit found a duplicate getTotalStock method in both PurchaseService and DrugService. To maintain a single source of truth, the duplicate in PurchaseService was removed. DrugService was established as the sole class responsible for retrieving total stock, using PurchaseRepository's aggregate query.

---

**9. Conclusion**

**9.1 Achievements**

The project successfully delivers a Pharmacy Management and POS system with the following capabilities:

- A fully normalized 9-table relational schema where stock is computed from Purchase batch aggregations rather than stored on the Drug record.
- A FIFO batch-depletion engine that consumes purchase lots in expiration-date order, verified by 8 dedicated Mockito test methods.
- A prescription validation mechanism that prevents controlled-substance sales to anonymous customers using the PresType.riskLevel scale.
- An expiry evaluation system using the Strategy pattern that classifies batches into three categories and supports adding new classifications without modifying existing code.
- Spring Security integration with BCrypt password hashing, database-driven UserDetailsService, and role-based Thymeleaf UI rendering for three distinct user profiles.
- Centralized error handling covering 5 custom domain exceptions alongside standard validation and locking exceptions, returning consistent JSON structures.
- A financial ledger that computes revenue, cost of goods sold, profit, and expired-loss metrics using prices recorded at the time of sale and purchase.
- Optimistic locking protection on both Drug and Purchase entities to prevent concurrent sale conflicts.
- 23 JUnit 5 service-layer tests with a 100% pass rate.

**[- PHOTO: Final dashboard screenshot showing all 4 Bento-box stat cards with live data, sidebar fully expanded, and no error alerts -]**

**9.2 Future Improvements**

Several enhancements are identified for future development:

SGK Reimbursement Module — Integrating with Turkey's Social Security Institution (SGK) prescription reimbursement system would allow pharmacies to submit electronic claims directly from the POS interface. This would require adding a prescription detail entity (prescribing physician, diagnosis code, SGK protocol number) and a reporting pipeline.

Barcode Scanner Hardware Integration — While the current drug search supports manual barcode entry, integrating USB barcode scanners through a WebUSB or keyboard-wedge interface would speed up POS workflows.

Real-Time Expiry Notifications — Currently, users need to navigate to the inventory page to see critical expiry batches. A scheduled task using Spring's @Scheduled annotation could send desktop notifications or email alerts when batches enter the 30-day critical window.

Mobile Inventory Scanning — A companion mobile application for scanning batch QR codes during stock intake would reduce data-entry errors. The existing REST API and service layer could support a React Native or Flutter client with minimal server-side changes.

PDF Receipt Printing — Integrating a PDF generation library (such as JasperReports or PDFBox) to print itemized receipts at the POS station, optionally including barcodes, expiry warnings for near-expiry items, and SGK protocol references.

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
