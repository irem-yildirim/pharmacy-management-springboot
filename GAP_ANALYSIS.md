# Gap Analysis: Pharmacy Management System vs. Instructor's Student Management System

> **Date:** 2026-05-24 (Updated)
> **Scope:** Comparison against `student-management-final` (Instructor's reference project)
> **Goal:** Identify every missing feature, pattern, and convention required for parity with the taught curriculum.

---

## 1. What the Instructor's Project Has That Mine Had — Current Status

### 1.1 Testing Infrastructure

| Feature | Instructor Has | My Status | Priority |
|---|---|---|---|
| **Unit tests (Mockito + JUnit 5)** | `StudentServiceTest` with `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` | ✅ **DONE** — 4 service tests exist | 🔴 Completed |
| **Context-load smoke test** | `@SpringBootTest` verifies app starts | ✅ **DONE** | 🔴 Completed |
| **Test dependencies in pom.xml** | `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`, etc. | 🟡 **PARTIAL** — `spring-security-test` present, missing `data-jpa-test`, `validation-test`, `webmvc-test`, `thymeleaf-test` | 🟡 Medium |

### 1.2 Spring Security — Full Integration

| Feature | Instructor's Approach | My Approach | Gap |
|---|---|---|---|
| **Spring Security `SecurityFilterChain`** | Declarative `HttpSecurity` config via `SecurityConfig.java` | ✅ **DONE** — `SecurityConfig` with `SecurityFilterChain` bean | ✅ Completed |
| **User details** | `UserDetailsService` / `InMemoryUserDetailsManager` with encoded passwords | ✅ **DONE** — `PharmacyUserDetails` + DB-backed `UserDetailsService` | ✅ Completed |
| **Form login** | Spring Security built-in form login with `defaultSuccessUrl` | ✅ **DONE** — Spring Security form login with custom success handler | ✅ Completed |
| **Logout** | Spring Security `/logout` (CSRF-protected POST) | ✅ **DONE** — Spring Security logout configured | ✅ Completed |
| **Role annotations in templates** | `sec:authorize="hasRole('ADMIN')"` + `sec:authentication` | ✅ **DONE** — templates use `sec:authorize` | ✅ Completed |
| **Password encoder bean** | `BCryptPasswordEncoder` bean | ✅ **DONE** — `PasswordEncoder` bean in `SecurityConfig` | ✅ Completed |
| **Thymeleaf extras** | `thymeleaf-extras-springsecurity6` in pom.xml | ✅ **DONE** — present in pom.xml | ✅ Completed |

### 1.3 API Documentation (Swagger/OpenAPI)

| Feature | Instructor | My Status |
|---|---|---|
| **springdoc-openapi** | `springdoc-openapi-starter-webmvc-ui:2.3.0` in pom.xml | ✅ **DONE** — present in pom.xml |
| **`@Tag(name = "Student API")`** | Documented REST controller | ✅ **DONE** — all 8 REST controllers have `@Tag` |
| **`@Operation(summary = "...")`** | Documented every endpoint | 🟡 **NEARLY DONE** — all endpoints except `CustomerController.createCustomer()` |
| **Swagger UI** | Available at `/swagger-ui.html` | ✅ **DONE** — available |

### 1.4 Build & Project Infrastructure

| Feature | Instructor | My Status | Priority |
|---|---|---|---|
| **Maven Wrapper (`mvnw`)** | Present (v3.3.4) | ✅ **DONE** | 🟢 Completed |
| **`.gitignore`** | Present (comprehensive) | ✅ **DONE** | 🟢 Completed |
| **`.gitattributes`** | Present | ✅ **DONE** | 🟢 Completed |
| **`HELP.md`** | Present | ✅ **DONE** | 🟢 Completed |
| **Spring Boot version** | `4.0.3` (latest) | ❌ `3.4.5` (one major behind) | 🟡 Medium |
| **Java version** | `25` | ❌ `21` | 🟢 Low |
| **Separate test dependencies** | `*-test` starters explicitly listed | ❌ **PARTIAL** — see 1.1 | 🟡 Medium |

### 1.5 Code Organization & Naming Conventions

| Aspect | Instructor | My Project | Gap |
|---|---|---|---|
| **REST controller package** | `rest/` | ✅ **DONE** — all REST controllers in `rest/` | ✅ Completed |
| **Entity package name** | `model/` | ✅ **DONE** — `model/` package used | ✅ Completed |
| **Exception handler package** | `advice/` | ✅ **DONE** — `advice/` package used | ✅ Completed |
| **Validation in MVC controllers** | `@Valid` + `BindingResult` with form re-render on error | ❌ `@Valid` on `@RequestBody` only (REST-style) | ❌ Missing MVC form validation with graceful re-render |
| **Error page view** | Returns `"error-page"` Thymeleaf view for web errors | ❌ Returns JSON `ErrorResponse` for all errors | ❌ Missing a dedicated HTML error page |

### 1.6 Spring MVC Form Handling

| Feature | Instructor | My Status |
|---|---|---|
| **`@ModelAttribute` binding** | `@ModelAttribute("newStudent") Student student` | ❌ Not used — all data flows through REST endpoints |
| **`BindingResult` re-render** | If validation fails, re-renders form with inline errors | ❌ REST controllers return 400 with field error map — no Thymeleaf re-render |
| **Thymeleaf inline validation errors** | `th:errors`, `th:classappend="is-invalid"` | ❌ Not used — validation is handled client-side |
| **Post-Redirect-Get pattern** | `redirect:/students` after save | ❌ JS `showToast` + `fetchDrugs()` after save |

### 1.7 Testing — Deep Dive

The instructor's `StudentServiceTest` verifies:
1. `getAllStudentsForUI()` — DTO conversion (name uppercased) and strategy integration
2. `saveStudent()` — repository.save() is called
3. `deleteStudent()` — repository.deleteById() is called

My project has **4 service tests** covering:
- ✅ `DrugServiceTest` — findAllActive, findByBarcode, save, softDelete, notFound
- ✅ `SaleServiceTest` — FIFO batch deduction, single batch, insufficient stock, prescription required, prescription logged, customer balance
- ✅ `FinanceServiceTest` — daily revenue, zero revenue, daily profit, zero profit
- ✅ `ExpiryServiceTest` — expired, expired today, critical, critical threshold, OK

**Still missing:** Repository `@Query` method tests, integration tests.

### 1.8 Missing Dependencies in pom.xml

| Dependency | In Instructor's pom | In My pom |
|---|---|---|
| `spring-boot-starter-security` | ✅ | ✅ **DONE** |
| `spring-boot-starter-data-jpa-test` | ✅ | ❌ **MISSING** |
| `spring-boot-starter-security-test` | ✅ | ✅ **DONE** |
| `spring-boot-starter-thymeleaf-test` | ✅ | ❌ **MISSING** |
| `spring-boot-starter-validation-test` | ✅ | ❌ **MISSING** |
| `spring-boot-starter-webmvc-test` | ✅ | ❌ **MISSING** |
| `springdoc-openapi-starter-webmvc-ui` | ✅ | ✅ **DONE** |
| `thymeleaf-extras-springsecurity6` | ✅ | ✅ **DONE** |

### 1.9 Architecture Violations (Discovered During Audit)

| # | Violation | Files Affected | Severity |
|---|---|---|---|
| **R1** | **Controller injects Repository directly** (breaks Rule 1: Controller → Service → Repository) | `BrandController`, `CategoryController`, `CustomerController`, `DrugController`, `PurchaseController`, `SaleController`, `UserController` — 7 controllers call `Repository` directly | 🔴 Critical |
| **R2** | **Controller returns Entity instead of DTO** (breaks Rule 2: DTOs only in controllers) | `BrandController` (`List<Brand>`), `CategoryController` (`List<Category>`), `CustomerController` (`List<Customer>`), `UserController` (`List<User>`) — 4 controllers expose entities | 🔴 Critical |
| **R3** | **Missing exception classes** defined in ARCHITECTURE.md | `CustomerNotFoundException`, `DuplicateEntryException` — do not exist | 🟡 Medium |

---

## 2. Detailed TODO List (Updated — Remaining Work Only)

> ✅ = Completed items are marked and moved to "Completed Tasks" summary below.

---

### ✅ 2.1 Completed Tasks Summary

| # | Task | Status |
|---|---|---|
| T1 | Unit tests for all services (DrugServiceTest, SaleServiceTest, FinanceServiceTest, ExpiryServiceTest) | ✅ DONE |
| T2 | Context-load smoke test (EczaneApplicationTests) | ✅ DONE |
| T4 | `spring-boot-starter-security` replacing `spring-security-crypto` | ✅ DONE |
| T5 | Implement Spring Security `SecurityFilterChain` | ✅ DONE |
| T6 | Add `thymeleaf-extras-springsecurity6` | ✅ DONE |
| T7 | Convert templates to `sec:authorize` | ✅ DONE |
| T8 | Add `springdoc-openapi-starter-webmvc-ui` | ✅ DONE |
| T9 | Add `@Tag` and `@Operation` to REST controllers | ✅ NEARLY DONE (1 missing) |
| T10 | Add Maven Wrapper (`mvnw`) | ✅ DONE |
| T11 | Add `.gitattributes` | ✅ DONE |
| T12 | Add `HELP.md` | ✅ DONE |
| T13 | Extract REST controllers to `rest/` package | ✅ DONE |
| T16 | Rename `entity/` → `model/` | ✅ DONE |
| T17 | Rename `exception/` → `advice/` | ✅ DONE |
| T18 | Refactor `AuthController` to use Spring Security auth manager | ✅ DONE (AuthController removed) |
| T19 | Update logout to Spring Security POST-based logout | ✅ DONE |

---

### 🔴 CRITICAL — Phase 1: Fix Architecture Violations

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **R1** | **Remove direct Repository injection from 7 controllers** — route through Service layer | `BrandController`, `CategoryController`, `CustomerController`, `DrugController`, `PurchaseController`, `SaleController`, `UserController` | Create/use existing Service methods for all DB operations. `DrugController` already has `DrugService` but also injects `PurchaseRepository` directly. |
| **R2** | **Replace Entity responses with DTOs in 4 controllers** | `BrandController`, `CategoryController`, `CustomerController`, `UserController` | Create `BrandResponse`, `CategoryResponse`, `CustomerResponse`, `UserResponse` DTOs. Map entities to DTOs in Service layer. |
| **R3-a** | Create `CustomerNotFoundException` exception class | `src/main/java/com/pharmacy/advice/` | Extend `RuntimeException`. Match pattern of `DrugNotFoundException`. |
| **R3-b** | Create `DuplicateEntryException` exception class | `src/main/java/com/pharmacy/advice/` | Extend `RuntimeException`. Match pattern of existing exceptions. |
| **R3-c** | Add handler for `DuplicateEntryException` in `GlobalExceptionHandler` | `GlobalExceptionHandler.java` | Return HTTP 409 Conflict. |
| **R3-d** | Add handler for `CustomerNotFoundException` in `GlobalExceptionHandler` | `GlobalExceptionHandler.java` | Return HTTP 404 Not Found. |
| **T9-remainder** | Add missing `@Operation` to `CustomerController.createCustomer()` | `CustomerController.java` | Single missing annotation. |

---

### 🟡 MEDIUM — Phase 2: MVC Polish & Error Handling

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T14** | Add `BindingResult` validation to MVC controllers | Controllers + templates | Implement form re-render pattern: `@Valid` + `BindingResult` + return to form view on error. Add `th:errors`, `th:classappend="is-invalid"` to relevant templates. |
| **T15** | Add dedicated HTML error page | `src/main/resources/templates/error.html` | Match instructor's `error-page` view. Modify `GlobalExceptionHandler` to detect web requests (via `Accept` header) and return HTML view instead of JSON. |
| **T20** | Add Post-Redirect-Get pattern for form-based operations | New MVC endpoints or refactor existing | Add parallel MVC endpoints with `redirect:` after save for non-JS fallback. |

---

### 🟡 MEDIUM — Phase 3: Infrastructure Completion

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T3-remainder** | Add missing test dependencies | `pom.xml` | Add `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-thymeleaf-test` with `<scope>test</scope>`. |
| **T24** | Add Swagger `@Schema` annotations to DTOs | All DTO classes under `dto/request/` and `dto/response/` | Add `@Schema(description = "...")` on each class and `@Schema(example = "...")` on fields for richer API docs. |
| **T23** | Add `@SpringBootTest` integration tests for each service | `src/test/java/com/pharmacy/service/` | Optional — integration tests that load full context. |

---

### 🟢 LOW — Phase 4: Version Upgrades & Optional

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T21** | Upgrade Spring Boot from 3.4.5 to 4.0.3 | `pom.xml` | Check for breaking changes in API (Jakarta namespace already in use). |
| **T22** | Upgrade Java from 21 to 25 | `pom.xml` `<java.version>` | Ensure JDK 25 is available on build machine. |
| **T25** | Add `spring-boot-starter-validation-test` | `pom.xml` | For testing validation constraints (already in T3). |
| **T26** | Add `spring-boot-starter-data-jpa-test` | `pom.xml` | For testing repository queries with `@DataJpaTest` (already in T3). |
| **T27** | Add `spring-boot-starter-webmvc-test` | `pom.xml` | For testing web layer with `@WebMvcTest` (already in T3). |
| **T28** | Add `spring-boot-starter-thymeleaf-test` | `pom.xml` | For testing Thymeleaf templates (already in T3). |

---

## 3. Quick Reference — Files to Create / Modify (Remaining)

| File | Action |
|---|---|
| `pom.xml` | Add 4 missing test dependencies. Optionally bump Spring Boot & Java versions. |
| `.../rest/BrandController.java` | **Refactor** — Remove `BrandRepository`/`DrugRepository` injection. Use Service layer. Return DTOs. |
| `.../rest/CategoryController.java` | **Refactor** — Remove `CategoryRepository`/`DrugRepository` injection. Use Service layer. Return DTOs. |
| `.../rest/CustomerController.java` | **Refactor** — Remove `CustomerRepository` injection. Use Service layer. Return DTO. Add `@Operation`. |
| `.../rest/DrugController.java` | **Refactor** — Remove `PurchaseRepository` injection (already has `DrugService`). Route via Service. |
| `.../rest/PurchaseController.java` | **Refactor** — Remove `PurchaseRepository`/`DrugRepository` injection. Use Service layer. |
| `.../rest/SaleController.java` | **Refactor** — Remove `UserRepository` injection. Get user from `Authentication` principal. |
| `.../rest/UserController.java` | **Refactor** — Remove `UserRepository`/`SaleRepository` injection. Use Service layer. Return DTOs. |
| `.../advice/CustomerNotFoundException.java` | **Create.** Extend `RuntimeException`. |
| `.../advice/DuplicateEntryException.java` | **Create.** Extend `RuntimeException`. |
| `.../advice/GlobalExceptionHandler.java` | Add handlers for new exceptions. Add view-resolution for web requests. |
| `src/main/resources/templates/error.html` | **Create.** Simple error page for web exceptions. |
| `.../dto/response/BrandResponse.java` | **Create.** DTO for brand data. |
| `.../dto/response/CategoryResponse.java` | **Create.** DTO for category data. |
| `.../dto/response/CustomerResponse.java` | **Create.** DTO for customer data. |
| `.../dto/response/UserResponse.java` | **Create.** DTO for user data (without password). |
| `templates/**/*.html` | Add `th:errors`, `th:classappend="is-invalid"` for `BindingResult` integration. |
| `.../service/BrandService.java` | **Create or verify.** Service methods for brand CRUD. |
| `.../service/CategoryService.java` | **Create or verify.** Service methods for category CRUD. |
| `.../service/CustomerService.java` | **Create or verify.** Service methods for customer CRUD. |
| `.../service/PurchaseService.java` | **Create or verify.** Service methods for purchase CRUD. |
| `.../service/UserService.java` | **Create or verify.** Service methods for user CRUD. |

---

## 4. Recommended Implementation Order

### Phase 1 — Fix Architecture Violations (R1, R2, R3, T9-remainder)
Create missing Service methods, DTOs, exception classes. Refactor all 7 controllers. Most critical — without this, the app violates its own architecture rules. Blocks all other phases.

### Phase 2 — MVC Polish & Error Handling (T14, T15, T20)
Add `BindingResult` form validation, HTML error page, PRG pattern for form submissions. This brings parity with the instructor's MVC conventions.

### Phase 3 — Infrastructure Completion (T3-remainder, T24, T23)
Add missing test starter dependencies to pom.xml. Add `@Schema` annotations to DTOs. Add integration tests.

### Phase 4 — Version Upgrades (T21, T22, T25–T28)
Spring Boot 4.0.3, Java 25, and remaining optional test dependencies. Lowest risk, done last.

---

*Generated from comparative analysis against `student-management-final` (Instructor's reference project). Updated 2026-05-24 to reflect current project state after Phase 1-2 completion.*
