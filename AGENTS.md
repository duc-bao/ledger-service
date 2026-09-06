# Repository Guidelines

## Project Structure & Module Organization

This repository is a single Spring Boot service (`ledger-service`) built with Gradle and Java 21.

- Application code: `src/main/java/com/ledger/ledgerservice`
- Config and runtime resources: `src/main/resources`
- DB migrations (Flyway): `src/main/resources/db/migration` (`V1__...sql`, `V2__...sql`)
- i18n bundles: `src/main/resources/i18n`
- Seed data: `src/main/resources/seed`
- Tests: `src/test/java/com/ledger/ledgerservice`
- Local infra: `docker-compose.yml` (Postgres + Redis)

## Package Structure

All application packages reside under `com.ledger.ledgerservice`. Below is the full package tree derived from the project source:

```
com.ledger.ledgerservice
├── aspect/                         # AOP aspects (e.g. logging, auditing cross-cuts)
├── config/                         # Spring configuration classes
│   ├── feign/                      # Feign client configuration (interceptors, decoders)
│   ├── filter/                     # Servlet/security filter configuration
│   ├── i18n/                       # MessageSource and locale resolver setup
│   ├── properties/                 # @ConfigurationProperties binding classes
│   ├── redis/                      # Redis connection factory and cache config
│   ├── security/                   # Spring Security configuration (filter chains, CORS)
│   └── thread/                     # ThreadPoolTaskExecutor and async config
├── controller/                     # REST controllers (@RestController); one per domain
├── exception/                      # Global exception handlers (@ControllerAdvice, custom exceptions)
├── external.feign/                 # Feign client interfaces for external services
├── model/                          # All domain model types
│   ├── constant/                   # Application-wide string/numeric constants
│   ├── context/                    # Request-scoped context holders (e.g. UserContext)
│   ├── dto/                        # Data Transfer Objects (request/response bodies)
│   ├── entity/                     # JPA entities (@Entity, @Table)
│   ├── enums/                      # Enum types shared across layers
│   └── security/                   # Security-related models (e.g. UserPrincipal, JwtClaims)
├── repository/                     # Spring Data JPA repositories (@Repository)
├── service/                        # Business logic (@Service); one interface + impl per domain
├── util/                           # Stateless utility/helper classes
└── LedgerServiceApplication.java   # Spring Boot entry point (@SpringBootApplication)
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `aspect` | Cross-cutting concerns via AOP: logging, metrics, audit trails. |
| `config` | All Spring bean configuration. Sub-packages mirror the technology being configured. |
| `config.feign` | Feign client decoders, error decoders, request interceptors (e.g. auth header propagation). |
| `config.filter` | Servlet filter registration and ordering (e.g. MDC, request-id injection). |
| `config.i18n` | `MessageSource` bean and locale resolution strategy for API error messages. |
| `config.properties` | Typed config classes bound via `@ConfigurationProperties`; validated with JSR-303. |
| `config.redis` | `RedisTemplate`, `CacheManager`, and TTL configuration. |
| `config.security` | `SecurityFilterChain`, JWT filter wiring, role-based access rules. |
| `config.thread` | `@Async` executor pools and `TaskScheduler` configuration. |
| `controller` | Thin HTTP layer: request mapping, input validation, delegating to services. No business logic. |
| `exception` | `@ControllerAdvice` translating domain exceptions to structured error responses. |
| `external.feign` | Declarative Feign interfaces; group by upstream system, not by endpoint. |
| `model.constant` | `public static final` constants; organise by domain in inner classes or separate files. |
| `model.context` | `ThreadLocal`-backed holders for request-scoped data (user id, tenant, trace id). |
| `model.dto` | Plain POJOs for API boundaries. Suffix: `*Request`, `*Response`, `*Dto`. No JPA annotations. |
| `model.entity` | JPA-mapped domain objects. One entity per database table. Suffix: `*Entity` (or bare domain noun). |
| `model.enums` | Enumerations used across multiple layers. Keep persistence mapping (e.g. `@Enumerated`) in the entity, not here. |
| `model.security` | Auth principal models, JWT payload classes, permission enumerations. |
| `repository` | `JpaRepository` / `CrudRepository` extensions. Custom queries via JPQL or `@NativeQuery`. |
| `service` | Core business logic. Define an interface; place implementation in the same package. |
| `util` | Pure functions (date formatting, string helpers, etc.). Must have no Spring bean dependencies. |

### Naming Conventions by Package

| Layer | Class suffix example |
|---|---|
| Controller | `AccountController` |
| Service interface | `AccountService` |
| Service implementation | `AccountServiceImpl` |
| Repository | `AccountRepository` |
| Entity | `AccountEntity` |
| Request DTO | `CreateAccountRequest` |
| Response DTO | `AccountResponse` |
| Config | `RedisConfig`, `SecurityConfig` |
| Filter | `JwtAuthFilter` |
| Exception | `AccountNotFoundException` |
| Feign client | `PaymentServiceClient` |
| Utility | `DateUtil`, `JwtUtil` |

## Build, Test, and Development Commands

Use the Gradle wrapper from repo root:

- `./gradlew.bat clean build` (Windows) or `./gradlew clean build` (Unix): compile, run tests, and package.
- `./gradlew.bat test`: run JUnit 5 test suite only.
- `./gradlew.bat bootRun`: run service locally on port `8091` using `application.yaml` defaults.
- `docker compose up -d postgres redis`: start local dependencies only.
- `docker compose up --build`: run full stack including `ledger-service` container.

## Coding Style & Naming Conventions

- Follow standard Java conventions: 4-space indentation, UTF-8, one public class per file.
- Package names are lowercase (`...service`, `...repository`, `...config`); classes use `PascalCase`; methods/fields use `camelCase`; constants use `UPPER_SNAKE_CASE`.
- Keep Spring stereotypes consistent by layer (`*Service`, `*Repository`, `*Config`, `*Filter`).
- Flyway scripts must be append-only and versioned as `V<version>__<snake_case_description>.sql`.


## Commit & Pull Request Guidelines

No shared commit history is available yet on `master`, so adopt this convention now:

- Commit format: `type(scope): summary` (example: `feat(security): add JWT permission filter`).
- Keep commits focused; include migration/config updates in the same commit when tightly coupled.
- PRs should include: purpose, key changes, test evidence (command + result), migration impact, and related issue/ticket.
- For API/security/config changes, include sample request/response or relevant logs.

## Security & Configuration Tips

- Do not commit real secrets. Values in `application.yaml` should be treated as local defaults only.
- Prefer environment variables for credentials (`SPRING_DATASOURCE_*`, Redis/JWT settings).
- Validate new endpoints against existing auth filters and permission checks before merge.

## Agent Permission & Safety Policy

### 1. Quyền được phép tự động thực thi (No approval needed)
- Đọc, viết và sửa đổi code, tài liệu trong workspace.
- Các lệnh xem trạng thái và so sánh Git an toàn: `git status`, `git diff`, `git log`, `git branch`.
- Chạy lệnh build và test tự động: `./gradlew test`, `./gradlew build`, `mvn test`, v.v.
- Chạy các công cụ MCP Database: thực thi câu lệnh `SELECT`, câu lệnh `INSERT`/`UPDATE` dữ liệu kiểm thử (test/dev data) mà không cần hỏi duyệt từng bước.

### 2. Hành vi BẮT BUỘC phải hỏi ý kiến và được User chấp thuận (Explicit User Approval Required)
- **Xóa file (Delete files)**: Tuyệt đối KHÔNG tự ý xóa file mã nguồn, file cấu hình hoặc bất kỳ file nào trong workspace (qua lệnh shell như `rm`, `del`, `Remove-Item` hay git tools). Phải hỏi và nêu rõ lý do trước khi thực hiện.
- **Merge / Rebase code**: Tuyệt đối KHÔNG tự ý chạy `git merge`, `git rebase` giữa các branch.
- **Push code**: Tuyệt đối KHÔNG chạy `git push` lên bất kỳ remote nào.

### 3. Quy trình kết thúc tác vụ (Completion & Final Review)
- Sau khi hoàn thành code và chạy test thành công: Agent **phải dừng lại**.
- Tổng hợp kết quả và xuất báo cáo/Walkthrough chi tiết:
  + Danh sách các file đã thay đổi / tạo mới.
  + Kết quả chạy test tự động (pass/fail logs).
  + Trạng thái `git status` và tóm tắt diff.
- Chờ User trực tiếp review, nghiệm thu và quyết định commit/merge/push.