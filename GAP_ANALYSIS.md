# Gap Analysis: Pharmacy Management System vs. Instructor's Student Management System

> **Date:** 2026-05-24
> **Scope:** Comparison against `student-management-final` (Instructor's reference project)
> **Goal:** Identify every missing feature, pattern, and convention required for parity with the taught curriculum.

---

## 1. What the Instructor's Project Has That Mine is Missing

### 1.1 Testing Infrastructure

| Feature | Instructor Has | My Status | Priority |
|---|---|---|---|
| **Unit tests (Mockito + JUnit 5)** | `StudentServiceTest` with `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` | **NONE** — zero test files exist | 🔴 Critical |
| **Context-load smoke test** | `@SpringBootTest` verifies app starts | **NONE** | 🔴 Critical |
| **Test dependencies in pom.xml** | `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`, etc. | Missing entirely | 🟡 Medium |

### 1.2 Spring Security — Full Integration

| Feature | Instructor's Approach | My Approach | Gap |
|---|---|---|---|
| **Spring Security `SecurityFilterChain`** | Declarative `HttpSecurity` config via `SecurityConfig.java` | Custom `AuthInterceptor` + `WebConfig` | Instructor uses framework-native security; I roll my own |
| **In-memory user details** | `InMemoryUserDetailsManager` with encoded passwords | Seed via `CommandLineRunner` + raw `BCrypt.hashpw()` | Functionally equivalent, but instructor uses standard Spring Security APIs |
| **Form login** | Spring Security built-in form login with `defaultSuccessUrl` | Custom `/login` page + JS `fetch` to `/api/auth/login` | My approach is more modern (SPA-style) but diverges from the taught pattern |
| **Logout** | Spring Security `/logout` (CSRF-protected POST) | Manual session invalidation on `GET /logout` | My approach is less secure (GET, no CSRF) |
| **Role annotations in templates** | `sec:authorize="hasRole('ADMIN')"` + `sec:authentication` | `th:if="${user.role.name() == 'ADMIN'}"` | My approach requires the user object on every model; instructor's is framework-managed |
| **Password encoder bean** | `User.withDefaultPasswordEncoder()` + `BCryptPasswordEncoder` | `BCrypt.hashpw()` in CommandLineRunner | Instructor follows Spring Security conventions |
| **Thymeleaf extras** | `thymeleaf-extras-springsecurity6` in pom.xml | **Missing** — no `sec:` namespace available | 🟡 Medium |

### 1.3 API Documentation (Swagger/OpenAPI)

| Feature | Instructor | My Status |
|---|---|---|
| **springdoc-openapi** | `springdoc-openapi-starter-webmvc-ui:2.3.0` in pom.xml | **Missing entirely** |
| **`@Tag(name = "Student API")`** | Documented REST controller | **No documentation annotations** |
| **`@Operation(summary = "...")`** | Documented every endpoint | **No documentation annotations** |
| **Swagger UI** | Available at `/swagger-ui.html` | Not available |

### 1.4 Build & Project Infrastructure

| Feature | Instructor | My Status | Priority |
|---|---|---|---|
| **Maven Wrapper (`mvnw`)** | Present (v3.3.4) | **Missing** | 🟡 Medium |
| **`.gitignore`** | Present (comprehensive) | Present | 🟢 Low |
| **`.gitattributes`** | Present | **Missing** | 🟢 Low |
| **`HELP.md`** | Present | **Missing** | 🟢 Low |
| **Spring Boot version** | `4.0.3` (latest) | `3.4.5` (one major behind) | 🟡 Medium |
| **Java version** | `25` | `21` | 🟢 Low |
| **Separate test dependencies** | `*-test` starters explicitly listed | Not listed | 🟡 Medium |

### 1.5 Code Organization & Naming Conventions

| Aspect | Instructor | My Project | Gap |
|---|---|---|---|
| **REST controller package** | `rest/StudentRestController.java` | All controllers in `controller/` | Instructor separates REST from MVC concerns |
| **Entity package name** | `model/` | `entity/` | Naming difference — `model/` is more conventional |
| **Exception handler package** | `advice/` | `exception/` | `advice/` is more conventional for `@ControllerAdvice` |
| **Validation in MVC controllers** | `@Valid` + `BindingResult` with form re-render on error | `@Valid` on `@RequestBody` only (REST-style) | Missing MVC form validation with graceful re-render |
| **Error page view** | Returns `"error-page"` Thymeleaf view for web errors | Returns JSON `ErrorResponse` for all errors | Missing a dedicated HTML error page |

### 1.6 Spring MVC Form Handling

| Feature | Instructor | My Status |
|---|---|---|
| **`@ModelAttribute` binding** | `@ModelAttribute("newStudent") Student student` | Not used — all data flows through REST endpoints |
| **`BindingResult` re-render** | If validation fails, re-renders form with inline errors | REST controllers return 400 with field error map — no Thymeleaf re-render |
| **Thymeleaf inline validation errors** | `th:errors`, `th:classappend="is-invalid"` | Not used — validation is handled client-side |
| **Post-Redirect-Get pattern** | `redirect:/students` after save | JS `showToast` + `fetchDrugs()` after save |

### 1.7 Testing — Deep Dive

The instructor's `StudentServiceTest` verifies:
1. `getAllStudentsForUI()` — DTO conversion (name uppercased) and strategy integration
2. `saveStudent()` — repository.save() is called
3. `deleteStudent()` — repository.deleteById() is called

My project has **zero** equivalent tests. Key methods needing coverage:
- `SaleService.createSale()` — FIFO batch deduction logic
- `DrugService.softDelete()` — isActive flag toggle
- `FinanceService.calculateDailyRevenue()` — aggregation query
- `ExpiryService.evaluateExpiry()` — strategy selection logic
- All repository `@Query` methods

### 1.8 Missing Dependencies in pom.xml

| Dependency | In Instructor's pom | In My pom |
|---|---|---|
| `spring-boot-starter-security` | ✅ | ❌ (uses `spring-security-crypto` only) |
| `spring-boot-starter-data-jpa-test` | ✅ | ❌ |
| `spring-boot-starter-security-test` | ✅ | ❌ |
| `spring-boot-starter-thymeleaf-test` | ✅ | ❌ |
| `spring-boot-starter-validation-test` | ✅ | ❌ |
| `spring-boot-starter-webmvc-test` | ✅ (`*-webmvc`) | ❌ |
| `springdoc-openapi-starter-webmvc-ui` | ✅ | ❌ |
| `thymeleaf-extras-springsecurity6` | ✅ | ❌ |

---

## 2. Detailed TODO List

### 🔴 CRITICAL — Must Add

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T1** | Add unit tests for ALL services | `src/test/java/com/pharmacy/service/DrugServiceTest.java` `SaleServiceTest.java` `FinanceServiceTest.java` `ExpiryServiceTest.java` | Use `@ExtendWith(MockitoExtension.class)`, `@Mock` repositories, `@InjectMocks` services. Test CRUD, edge cases, exception paths. Follow `StudentServiceTest.java` as template. |
| **T2** | Add context-load smoke test | `src/test/java/com/pharmacy/EczaneApplicationTests.java` | `@SpringBootTest` → `contextLoads()`. Same as instructor's. |
| **T3** | Add test dependencies to pom.xml | `pom.xml` | Add `spring-boot-starter-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test` with `<scope>test</scope>`. |
| **T4** | Add `spring-boot-starter-security` replacing `spring-security-crypto` | `pom.xml` | Replace minimal `spring-security-crypto` with full `spring-boot-starter-security`. |
| **T5** | Implement Spring Security `SecurityFilterChain` | `src/main/java/com/pharmacy/config/SecurityConfig.java` | Replace `AuthInterceptor` with `SecurityFilterChain` bean. Configure form login, logout, role-based URL security, and password encoder. |
| **T6** | Add `thymeleaf-extras-springsecurity6` | `pom.xml` | Required for `sec:authorize` and `sec:authentication` in Thymeleaf. |
| **T7** | Convert templates from manual `th:if` role checks to `sec:authorize` | All `.html` files in `templates/` | Replace `th:if="${user != null and user.role.name() == 'ADMIN'}"` with `sec:authorize="hasRole('ADMIN')"`. Remove `GlobalUIControllerAdvice` that adds user to every model. |
| **T8** | Add `springdoc-openapi-starter-webmvc-ui` | `pom.xml` | For Swagger UI auto-documentation. |
| **T9** | Add `@Tag` and `@Operation` to all REST controllers | `DashboardController`, `DrugController`, `SaleController`, `PurchaseController`, `UserController`, `AuthController`, `CategoryController`, `BrandController`, `CustomerController` | Match instructor's `StudentRestController` pattern. |

### 🟡 MEDIUM — Should Add

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T10** | Add Maven Wrapper | Project root | Run `mvn wrapper:wrapper` or download `mvnw` + `mvnw.cmd`. |
| **T11** | Add `.gitattributes` | Project root | Standard Git attributes for line endings, binary files. |
| **T12** | Add `HELP.md` | Project root | Basic info about running the project. |
| **T13** | Extract REST controllers to `rest/` package | Create `src/main/java/com/pharmacy/rest/` | Move `DashboardController`, `DrugController`, `SaleController`, `PurchaseController`, `UserController`, `CategoryController`, `BrandController`, `CustomerController` to `rest/` package. Keep `AuthController` and `UIController` in `controller/`. |
| **T14** | Add `BindingResult` validation to MVC controllers | `PurchaseController` (if converted to MVC) or add MVC endpoints | Implement form re-render pattern: `@Valid` + `BindingResult` + return to form view on error. |
| **T15** | Add dedicated HTML error page | `src/main/resources/templates/error.html` | Match instructor's `error-page` view returned by `GlobalExceptionHandler` for web requests. |
| **T16** | Rename `entity/` → `model/` | All entity files + all imports | Align with instructor's naming convention. Requires updating all imports across 50+ files. |
| **T17** | Rename `exception/GlobalExceptionHandler` → `advice/GlobalExceptionHandler` | Move file + update imports | Align with instructor's `advice/` package for `@ControllerAdvice`. |
| **T18** | Refactor `AuthController` to use Spring Security authentication manager | `AuthController.java` | Replace manual `userRepository.findByUsername()` + `BCrypt.checkpw()` with `AuthenticationManager.authenticate()`. |
| **T19** | Update logout to Spring Security POST-based logout | `AuthInterceptor.java`, `UIController.java` | Remove manual `/logout` GET endpoint. Configure Spring Security logout. |
| **T20** | Add Post-Redirect-Get pattern for form-based operations | New MVC endpoints or refactor existing | Instructor uses `redirect:/students` after save. My project uses JS fetch. Add MVC parallel endpoints with PRG. |
| **T21** | Upgrade Spring Boot from 3.4.5 to 4.0.3 | `pom.xml` | Check for breaking changes in API. |
| **T22** | Upgrade Java from 21 to 25 | `pom.xml` `<java.version>` | Ensure JDK 25 is available. |

### 🟢 LOW — Nice to Have

| # | Task | Files Affected | Implementation Notes |
|---|---|---|---|
| **T23** | Add `@SpringBootTest` for each service integration | `src/test/java/com/pharmacy/service/` | Integration tests (optional if unit tests exist). |
| **T24** | Add Swagger `@Schema` annotations to DTOs | All DTO classes | Enhance API docs with field descriptions. |
| **T25** | Add `spring-boot-starter-validation-test` | `pom.xml` | For testing validation constraints. |
| **T26** | Add `spring-boot-starter-data-jpa-test` | `pom.xml` | For testing repository queries with `@DataJpaTest`. |
| **T27** | Add `spring-boot-starter-webmvc-test` | `pom.xml` | For testing web layer with `@WebMvcTest`. |
| **T28** | Add `spring-boot-starter-thymeleaf-test` | `pom.xml` | For testing Thymeleaf templates. |

---

## 3. Quick Reference — Files to Create / Modify

| File | Action |
|---|---|
| `pom.xml` | Add 7+ dependencies (test starters, security, swagger, thymeleaf extras). Optionally bump versions. |
| `EczaneApplication.java` | No changes needed (already has `@SpringBootApplication`). |
| `.../config/SecurityConfig.java` | **Create new.** `SecurityFilterChain` bean, `InMemoryUserDetailsManager`, form login, logout, password encoder. |
| `.../config/WebConfig.java` | **Delete or gut.** Remove `AuthInterceptor` registration (handled by Spring Security now). |
| `.../interceptor/AuthInterceptor.java` | **Delete or gut.** Role checks move to `SecurityConfig` + `sec:authorize` in templates. |
| `.../controller/AuthController.java` | Refactor to use Spring Security `AuthenticationManager`. |
| `.../controller/GlobalUIControllerAdvice.java` | **Delete.** No longer needed (Spring Security provides authentication info to templates). |
| `.../controller/UIController.java` | Remove `/logout` mapping. Remove session references. |
| `.../controller/*.java` → `.../rest/*.java` | Move REST controllers to `rest/` package. Add `@Tag` + `@Operation`. |
| `.../entity/*.java` → `.../model/*.java` | Move + rename package. Update 50+ imports. |
| `.../exception/GlobalExceptionHandler.java` → `.../advice/GlobalExceptionHandler.java` | Move + package rename. Add error-page view for web requests. |
| `templates/*.html` (all) | Replace `th:if="${user.role...}"` with `sec:authorize="hasRole(...)"` + `sec:authentication`. |
| `templates/fragments/layout.html` | Remove `GlobalUIControllerAdvice`-provided `user` attribute references. Use Spring Security's `#authentication`. |
| `templates/error.html` | **Create.** Simple error page for web exceptions. |
| `src/test/java/com/pharmacy/EczaneApplicationTests.java` | **Create.** `@SpringBootTest` + `contextLoads()`. |
| `src/test/java/com/pharmacy/service/DrugServiceTest.java` | **Create.** Mockito unit test. |
| `src/test/java/com/pharmacy/service/SaleServiceTest.java` | **Create.** Mockito unit test (focus on FIFO logic). |
| `src/test/java/com/pharmacy/service/FinanceServiceTest.java` | **Create.** Mockito unit test. |
| `src/test/java/com/pharmacy/service/ExpiryServiceTest.java` | **Create.** Mockito unit test (strategy selection). |
| `HELP.md` | **Create.** Basic project info. |
| `.gitattributes` | **Create.** Standard Git attributes. |
| Project root | Run `mvn wrapper:wrapper` to generate `mvnw`. |

---

## 4. Recommended Implementation Order

### Phase 1 — Foundation (T4, T5, T6, T7)
Replace custom auth with Spring Security. This is the most invasive change and must come first since it affects every template and the interceptor.

### Phase 2 — Infrastructure (T8, T9, T10, T11, T12, T1, T2, T3)
Add tests, Swagger, Maven wrapper, `.gitattributes`, `HELP.md`. These are additive and don't break existing functionality.

### Phase 3 — Code Organization (T13, T16, T17)
Restructure packages for alignment. Risky only due to import changes — verify with compile.

### Phase 4 — MVC Polish (T14, T15, T18, T19, T20)
Add `BindingResult` validation to remaining MVC flows, error page, proper logout.

### Phase 5 — Upgrades (T21, T22, T23–T28)
Version bumps and optional enhancements.

---

*Generated from comparative analysis against `student-management-final` (Instructor's reference project).*
