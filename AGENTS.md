# AGENTS.md — Pharmacy Management System

## Quick start

```bash
mvn spring-boot:run          # run dev server
mvn clean install             # full build
```

Requires MySQL 8.x running locally with database `pharmacy_db` (credentials: `root`/`root`). Hibernate DDL is `ddl-auto=update` — tables auto-create on startup. No tests, no linter, no formatter, no CI.

## Login seeds (reset on every startup via `CommandLineRunner` in `EczaneApplication`)

| Username      | Password      | Role      |
|---------------|---------------|-----------|
| `admin`       | `password123` | ADMIN     |
| `eczaci_ayse` | `password123` | PHARMACIST|
| `kasiyer_veli`| `password123` | CASHIER   |

## Architecture rules (verified against codebase)

- **Strict layering:** Controller → Service → Repository. Controller MUST NOT inject or call Repository directly.
- **DTO rule:** Controllers accept/return DTOs only (under `dto/request/` and `dto/response/`). Entities are NEVER exposed to Thymeleaf views or JSON responses.
- **Strategy Pattern:** Expiry evaluation uses `ExpiryStrategy` interface with `ExpiredStrategy`, `CriticalStrategy`, `OkStrategy` in `strategy/` package. No if-else chains.
- **Global error handling:** Single `@ControllerAdvice` class (`GlobalExceptionHandler`). No try-catch in Service or Controller — throw custom exceptions instead (e.g., `InsufficientStockException`).
- **No stock field on Drug:** Total stock = `SUM(Purchase.remainingQuantity)`. Cost = `Purchase.purchasePrice`.
- **FIFO sales:** Purchase batches are consumed in `expirationDate ASC` order via `SaleService`.
- **Soft delete:** All deletion sets `isActive = false` — no SQL `DELETE`.
- **Optimistic Locking:** `@Version` on `Drug` and `Purchase` entities. Collision → `OptimisticLockException` → `GlobalExceptionHandler`.

## Reference files

- `ARCHITECTURE.md` — full architectural rules (includes strategy pattern contract, naming conventions, module build order)
- `DATABASE_STATE.md` — authoritative DB schema (all 9 tables, FK relationships, critical query signatures)
- `data.sql` — seed data for development (brands, categories, pres_type, drugs, users, customers, purchases)
