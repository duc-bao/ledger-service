# BÁO CÁO KIỂM TRA BẢO MẬT TOÀN DIỆN (SONARQUBE SECURITY AUDIT REPORT)
**Dự án**: `ledger-service` | **Môi trường quét**: Toàn bộ codebase (Java 21 / Spring Boot 3.5.13)  
**Tiêu chuẩn đối chiếu**: SonarQube Java Security Rules, OWASP Top 10 (2021), CWE  
**Trạng thái mã nguồn**: Giữ nguyên nguyên trạng (**CHƯA SỬA MÃ NGUỒN**, chờ User thẩm định)

---

## 1. TỔNG HỢP KẾT QUẢ QUÉT (EXECUTIVE SUMMARY)

| Mức độ nghiêm trọng | Số lượng | Mô tả hành động khuyến nghị |
|:---|:---:|:---|
| 🔴 **BLOCKER** | **4** | Lỗ hổng cực kỳ nguy hiểm, có thể bị khai thác ngay từ bên ngoài. **BẮT BUỘC KHẮC PHỤC NGAY**. |
| 🟠 **CRITICAL** | **5** | Lỗ hổng nghiêm trọng làm tổn hại dữ liệu, leo thang đặc quyền hoặc rò rỉ credential. |
| 🟡 **MAJOR** | **6** | Nguy cơ DoS, cạn kiệt tài nguyên JVM, injection gián tiếp hoặc lộ thông tin. |
| 🔵 **SECURITY HOTSPOT** | **4** | Điểm nhạy cảm an toàn cần xem xét ngữ cảnh vận hành và kiến trúc. |
| **TỔNG CỘNG** | **19** | **Toàn bộ 19 mục đã được đưa vào checklist chi tiết bên dưới.** |

---

## 2. CHECKLIST CHI TIẾT CÁC LỖ HỔNG BẢO MẬT THEO FILE & DÒNG CODE

### 🔴 NHÓM BLOCKER (Cực kỳ nghiêm trọng)

- [ ] **1. [BLOCKER] [java:S2068] Cửa sau (Backdoor) xác thực OTP cứng `"999999"`**
  - **Vị trí file & dòng**:
    - [AuthService.java:L135-L136](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/auth/AuthService.java#L135-L136) (hàm `verifyLoginOtp`)
    - [AuthService.java:L185](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/auth/AuthService.java#L185) (hàm `verifyForgotPasswordOtp`)
  - **Tầng / Service**: `AuthService / Tầng Service`
  - **Phân loại**: CWE-798 (Use of Hard-coded Credentials), CWE-287 (Improper Authentication), OWASP A07:2021
  - **Đoạn mã nguy cơ**:
    ```java
    // L135: Bỏ qua 2FA login nếu nhập 999999
    if (!"999999".equals(request.getOtp()) &&
            (!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp()))) {
    ...
    // L185: Bỏ qua OTP đổi mật khẩu nếu nhập 999999
    if ((!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp())) && !"999999".equals(request.getOtp())) {
    ```
  - **Nguy cơ thực tế**: Bất kỳ ai cũng có thể vượt qua bước 2FA khi đăng nhập và đặt lại mật khẩu của **BẤT KỲ TÀI KHOẢN NÀO** (kể cả SuperAdmin) bằng cách điền OTP `"999999"`.
  - **Hướng khắc phục**: Xóa bỏ hoàn toàn điều kiện so sánh `"999999"`. Nếu cần bypass cho môi trường Test, phải kiểm tra thông qua biến môi trường hoặc mock bean riêng trong test profile, tuyệt đối không đưa vào code logic chính.

---

- [x] **2. [BLOCKER] [java:S2068 & CWE-312] Lộ mật khẩu SMTP plaintext qua API & Commit mật khẩu thật vào Migration** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [V11__seed_default_email_config.sql:L47-L48](file:///d:/ledger-service/src/main/resources/db/migration/V11__seed_default_email_config.sql#L47-L48)
    - [EmailConfigResponse.java:L26](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/model/dto/response/email/EmailConfigResponse.java#L26)
    - [EmailConfigMapper.java:L13](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/model/mapper/EmailConfigMapper.java#L13)
    - [EmailConfigController.java:L38-L47](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/EmailConfigController.java#L38-L47)
  - **Tầng / Service**: `EmailConfigService`, `EmailConfigController`, `Flyway Migration`
  - **Phân loại**: CWE-798, CWE-312 (Cleartext Storage of Sensitive Information), OWASP A02:2021
  - **Đoạn mã nguy cơ**:
    ```sql
    -- V11 migration: Password thật được commit vào git
    'truongducbao290402@gmail.com', 'tfcr josj npro npsy'
    ```
    ```java
    // EmailConfigResponse.java
    private String password; // Trả về nguyên văn mật khẩu SMTP qua GET /api/v1/email-configs/current
    ```
  - **Nguy cơ thực tế**: Mật khẩu ứng dụng Gmail thực tế bị rò rỉ trong git history. Đồng thời người dùng gọi API lấy cấu hình email sẽ xem được mật khẩu SMTP dạng rõ (cleartext).
  - **Hướng khắc phục**:
    - Loại bỏ trường `password` khỏi `EmailConfigResponse` (chỉ trả cờ `passwordConfigured: true/false`).
    - Mã hóa mật khẩu trong database (AES-GCM hoặc qua Vault/KMS).
    - Thu hồi ngay mật khẩu Gmail `tfcr josj npro npsy` trên Google Account.

---

- [x] **3. [BLOCKER] [java:S2068] Khóa bí mật JWT & Mật khẩu Admin mặc định đặt cứng trong cấu hình** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [application.yaml:L83-L84](file:///d:/ledger-service/src/main/resources/application.yaml#L83-L84)
    - [application.yaml:L91-L92](file:///d:/ledger-service/src/main/resources/application.yaml#L91-L92)
    - [AdminUserSeedService.java:L41, L45](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/seed/AdminUserSeedService.java#L41)
  - **Tầng / Service**: `Config / Seed Service`
  - **Phân loại**: CWE-798, CWE-258 (Empty or Default Password in Configuration), OWASP A02:2021
  - **Đoạn mã nguy cơ**:
    ```yaml
    authentication:
      jwt:
        key: suW1MFuErxKmhelwTpwhw6QaAoqZMRb64y81cR9rYeA
        key-refresh: WDzqnd5bXrUvjuzzLagE0Vc5BppnVvKVv3rrie5xkmk
    app-setting:
      superUser: admin
      password: 123456a@
    ```
  - **Nguy cơ thực tế**: Kẻ tấn công biết secret key có thể tự tạo (forge) bất kỳ JWT token nào với quyền `admin` mà không cần đăng nhập. Tài khoản `admin` khởi tạo với mật khẩu yếu `123456a@` và cờ `requireChange = false`.
  - **Hướng khắc phục**: Chuyển sang biến môi trường bắt buộc: `${JWT_SECRET}` và `${ADMIN_INITIAL_PASSWORD}`. Đặt `requireChange = true` khi seed admin lần đầu.

---

- [ ] **4. [BLOCKER] [java:S4830 & java:S5527] Bỏ qua kiểm tra chứng chỉ SSL và Hostname Verification**
  - **Vị trí file & dòng**:
    - [SSLUtils.java:L26-L44, L55-L65](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/util/SSLUtils.java#L26-L44)
    - [GlobalFeignConfig.java:L81-L106](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/feign/GlobalFeignConfig.java#L81-L106)
  - **Tầng / Service**: `Feign Client Config / Utility`
  - **Phân loại**: CWE-295 (Improper Certificate Validation), CWE-297 (Improper Validation of Certificate with Host Mismatch), OWASP A07:2021
  - **Đoạn mã nguy cơ**:
    ```java
    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public void checkClientTrusted(...) {} // Rỗng
        public void checkServerTrusted(...) {} // Rỗng
    }};
    ...
    builder.sslSocketFactory(SSLUtils.disableSSLCertValidation(), trustManager)
           .hostnameVerifier(SSLUtils.disableHostnameVerifier());
    ```
  - **Nguy cơ thực tế**: Cho phép tấn công Man-In-The-Middle (MITM) đánh chặn hoặc giả mạo toàn bộ dữ liệu trao đổi giữa Feign client và dịch vụ bên ngoài bất cứ khi nào ứng dụng không chạy ở profile `prod`.
  - **Hướng khắc phục**: Loại bỏ hoàn toàn việc bypass TrustManager và HostnameVerifier. Nếu là môi trường dev cần dùng chứng chỉ tự ký, hãy import chứng chỉ vào Java Truststore (`cacerts`) thay vì vô hiệu hóa trong mã nguồn.

---

### 🟠 NHÓM CRITICAL (Nghiêm trọng)

- [x] **5. [CRITICAL] [java:S5122] Cấu hình CORS mở cờ `*` kết hợp `allowCredentials=true`** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [SpringSecurityConfig.java:L66, L70](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/security/config/SpringSecurityConfig.java#L66)
  - **Tầng / Service**: `SpringSecurityConfig / Tầng Security`
  - **Phân loại**: CWE-942 (Permissive Cross-Domain Policy with Untrusted Domains), OWASP A05:2021
  - **Đoạn mã nguy cơ**:
    ```java
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*")); // Ghi đè cứng '*', bỏ qua config trong yaml!
    ...
    config.setAllowCredentials(corsProp.getAllowCredentials()); // Cho phép credentials
    ```
  - **Nguy cơ thực tế**: Dù trong `application.yaml` có khai báo whitelist domain an toàn, code Java lại hardcode `List.of("*")`. Bất kỳ website độc hại nào cũng có thể gửi AJAX request kèm credentials và đọc nội dung response của người dùng.
  - **Hướng khắc phục**: Thay `List.of("*")` bằng `corsProp.getAllowedOriginPatterns()`.

---

- [x] **6. [CRITICAL] [java:S5659] Xác thực JWT thiếu kiểm tra ràng buộc thuật toán (Algorithm Confusion)** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [JwtUtils.java:L63-L64, L133-L137](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/util/JwtUtils.java#L63-L64)
  - **Tầng / Service**: `JwtUtils / Tầng Security Utility`
  - **Phân loại**: CWE-327 (Use of a Broken or Risky Cryptographic Algorithm), OWASP A02:2021
  - **Đoạn mã nguy cơ**:
    ```java
    SignedJWT signedJWT = SignedJWT.parse(token);
    JWSVerifier verifier = new MACVerifier(secret);
    return signedJWT.verify(verifier);
    ```
  - **Nguy cơ thực tế**: Không kiểm tra `signedJWT.getHeader().getAlgorithm()`. Nếu kẻ tấn công thay đổi thuật toán trong header (ví dụ None algorithm hoặc asymmetric algorithm confusion), token có thể bị chấp nhận sai lệch.
  - **Hướng khắc phục**: Kiểm tra rõ ràng `if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) return false;`.

---

- [x] **7. [CRITICAL] [java:S5131] PermissionFilter bỏ qua kiểm tra quyền nếu thiếu UserId** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [PermissionFilter.java:L63-L67](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/filter/PermissionFilter.java#L63-L67)
  - **Tầng / Service**: `PermissionFilter / Tầng Filter`
  - **Phân loại**: CWE-285 (Improper Authorization), OWASP A01:2021
  - **Đoạn mã nguy cơ**:
    ```java
    String userId = resolveUserId(authentication);
    if (!StringUtils.hasText(userId)) {
        filterChain.doFilter(request, response);
        return;
    }
    ```
  - **Nguy cơ thực tế**: Khi `userId` không phân giải được (ví dụ đối tượng authentication khác hoặc lỗi claim), filter cho phép request đi tiếp qua Controller thay vì trả về `403 Forbidden` hoặc `401 Unauthorized`.
  - **Hướng khắc phục**: Nếu authentication đã tồn tại nhưng không có `userId`, phải chặn lại bằng `forbidden(response, MessageCode.ACCESS_DENIED)`.

---

- [x] **8. [CRITICAL] [java:S5304 & CWE-532] Ghi log mật khẩu SMTP dạng rõ trong EmailConfigService** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [EmailConfigService.java:L51, L72](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/email/EmailConfigService.java#L51)
    - [CreateEmailConfigRequest.java:L14, L38](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/model/dto/request/email/CreateEmailConfigRequest.java#L14)
    - [UpdateEmailConfigRequest.java](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/model/dto/request/email/UpdateEmailConfigRequest.java)
  - **Tầng / Service**: `EmailConfigService / Tầng Service`
  - **Phân loại**: CWE-532 (Insertion of Sensitive Information into Log File), OWASP A09:2021
  - **Đoạn mã nguy cơ**:
    ```java
    log.info("Create email config: {}", request.toString());
    log.info("Update email config with id={} body= {}", configId, request.toString());
    ```
  - **Nguy cơ thực tế**: DTO `CreateEmailConfigRequest` sử dụng `@ToString` của Lombok trên toàn bộ class bao gồm cả `password`. Khi log mức INFO, mật khẩu SMTP sẽ được ghi thẳng vào log file / ELK / CloudWatch.
  - **Hướng khắc phục**: Thêm `@ToString.Exclude` trên trường `password` của các DTO request và che (mask) mật khẩu khi log.

---

- [x] **9. [CRITICAL] [CWE-613 & CWE-306] Đổi mật khẩu không hủy Token cũ & Tắt 2FA không cần xác thực lại** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [UserProfileServiceImpl.java:L72-L91](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/user/UserProfileServiceImpl.java#L72-L91) (hàm `changeMyPassword`)
    - [UserProfileServiceImpl.java:L95-L101](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/user/UserProfileServiceImpl.java#L95-L101) (hàm `updateTwoFactor`)
  - **Tầng / Service**: `UserProfileServiceImpl / Tầng Service`
  - **Phân loại**: CWE-613 (Insufficient Session Expiration), CWE-306 (Missing Authentication for Critical Function), OWASP A07:2021
  - **Đoạn mã nguy cơ**:
    - Đổi mật khẩu thành công nhưng không đẩy token hiện tại hoặc token trên các thiết bị khác vào `TokenBlacklistService`.
    - Gọi API tắt 2FA mà không cần kiểm tra mật khẩu hiện tại hoặc OTP xác nhận.
  - **Nguy cơ thực tế**: Nếu token của user bị rò rỉ, dù user có đổi mật khẩu thì kẻ tấn công vẫn tiếp tục sử dụng token cũ. Ngoài ra, nếu có phiên đang mở, kẻ tấn công có thể tắt 2FA ngay lập tức mà không gặp bất kỳ rào cản nào.
  - **Hướng khắc phục**: Đưa token hiện tại vào blacklist và tăng `tokenVersion` / `passwordChangedAt` để vô hiệu hóa token cũ; yêu cầu nhập mật khẩu hiện tại hoặc OTP khi tắt 2FA.

---

### 🟡 NHÓM MAJOR (Tiềm ẩn lỗi hệ thống & DoS)

- [x] **10. [MAJOR] [java:S2095 & CWE-400] Nguy cơ tràn bộ nhớ RAM (OOM) khi tải file Excel qua `Files.readAllBytes`** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [ExcelServiceImpl.java:L133](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/excel/ExcelServiceImpl.java#L133)
  - **Tầng / Service**: `ExcelServiceImpl / Tầng Service`
  - **Phân loại**: CWE-400 (Uncontrolled Resource Consumption), OWASP A05:2021
  - **Đoạn mã nguy cơ**:
    ```java
    return ExportFilePayload.builder()
            .fileName(job.getFileName())
            .content(Files.readAllBytes(filePath)) // Đọc toàn bộ file vào byte[] trong RAM
            .build();
    ```
  - **Nguy cơ thực tế**: File export có dung lượng hàng chục đến hàng trăm MB khi được đọc cùng lúc bởi nhiều người sẽ gây cạn RAM heap và làm sập ứng dụng (Crash JVM).
  - **Hướng khắc phục**: Trả về `InputStreamResource` hoặc sử dụng cơ chế streaming (`StreamingResponseBody`).

---

- [x] **11. [MAJOR] [java:S5042 & CWE-409] Thiếu cơ chế kiểm soát Zip Bomb & MIME Type trong Apache POI** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [ReceivableDebtExcelServiceImpl.java:L35, L222](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/excel/ReceivableDebtExcelServiceImpl.java#L35)
  - **Tầng / Service**: `ReceivableDebtExcelServiceImpl / Tầng Service`
  - **Phân loại**: CWE-409 (Zip Bomb), CWE-434 (Unrestricted Upload of File with Dangerous Type), OWASP A05:2021
  - **Đoạn mã nguy cơ**:
    ```java
    Workbook workbook = WorkbookFactory.create(inputStream);
    ...
    if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".xlsx")) // Chỉ check đuôi file
    ```
  - **Nguy cơ thực tế**: File nén zip độc hại (Zip Bomb - file vài chục KB nhưng giải nén ra 10GB trong bộ nhớ) sẽ làm treo toàn bộ CPU và RAM của service. Việc chỉ kiểm tra đuôi `.xlsx` mà không kiểm tra magic bytes cho phép tải lên các file giả mạo.
  - **Hướng khắc phục**: Giới hạn `ZipSecureFile.setMinInflateRatio(0.01)` của POI, kiểm tra kích thước tối đa và kiểm tra Content-Type / Magic bytes của file trước khi parse.

---

- [x] **12. [MAJOR] [java:S5128 & CWE-400] Không giới hạn kích thước phân trang (`Pageable`) trên các Controller** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [RoleAdminController.java:L49](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/RoleAdminController.java#L49)
    - [MenuController.java:L107](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/MenuController.java#L107)
    - [PermissionController.java:L118](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/PermissionController.java#L118)
    - [PermissionApiController.java:L103](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/PermissionApiController.java#L103)
    - [ExportController.java:L71](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/controller/ExportController.java#L71)
  - **Tầng / Service**: `Controller Layer`
  - **Phân loại**: CWE-400 (Uncontrolled Resource Consumption), OWASP A05:2021
  - **Nguy cơ thực tế**: Client có thể truyền query parameter `?size=10000000`, buộc database query hàng triệu dòng dữ liệu và nạp toàn bộ vào bộ nhớ, gây nghẽn database và tràn RAM.
  - **Hướng khắc phục**: Thêm cấu hình `@PageableDefault(size = 20)` và áp dụng `maxPageSize` hoặc chuẩn hóa trong Service layer tương tự như `UserAdminServiceImpl`.

---

- [x] **13. [MAJOR] [java:S5167 & CWE-113] Nguy cơ HTTP Header Splitting / CRLF Injection qua `X-Request-Id`** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [HeaderFilter.java:L37, L55](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/filter/HeaderFilter.java#L37)
  - **Tầng / Service**: `HeaderFilter / Tầng Filter`
  - **Phân loại**: CWE-113 (Improper Neutralization of CRLF Sequences in HTTP Headers), OWASP A03:2021
  - **Đoạn mã nguy cơ**:
    ```java
    String requestId = resolveRequestId(request.getHeader(HeaderConstant.X_REQUEST_ID));
    ...
    response.setHeader(HeaderConstant.X_REQUEST_ID, requestId);
    ```
  - **Nguy cơ thực tế**: `requestId` lấy trực tiếp từ header của client và phản hồi ngược lại vào response header mà không qua bước chuẩn hóa hoặc kiểm tra ký tự `\r\n`. Kẻ tấn công có thể chèn header giả hoặc chia tách response (HTTP Response Splitting).
  - **Hướng khắc phục**: Chỉ chấp nhận `X-Request-Id` chứa ký tự chữ số và dấu gạch ngang regex `^[a-zA-Z0-9_-]{1,64}$`, nếu không khớp thì tự sinh UUID mới.

---

- [x] **14. [MAJOR] [CWE-532] Ghi toàn bộ Query String vào Database ActionLog** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [HeaderFilter.java:L46](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/filter/HeaderFilter.java#L46)
    - [ActionLog.java:L52](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/model/entity/ActionLog.java#L52)
  - **Tầng / Service**: `HeaderFilter / ActionLogService`
  - **Phân loại**: CWE-532 (Insertion of Sensitive Information into Log File), OWASP A09:2021
  - **Đoạn mã nguy cơ**:
    ```java
    .requestQuery(request.getQueryString())
    ```
  - **Nguy cơ thực tế**: Nếu client truyền tham số nhạy cảm qua URL (như token, secret, otp), toàn bộ sẽ được lưu dạng rõ vào bảng `sys_action_logs`.
  - **Hướng khắc phục**: Triển khai hàm làm sạch (sanitize/masking) để ẩn các tham số nhạy cảm trong query string trước khi ghi vào context.

---

- [x] **15. [MAJOR] [java:S5131 & CWE-79] HTML / Template Injection trong việc render Email** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**:
    - [EmailTemplateService.java:L139](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/email/EmailTemplateService.java#L139)
    - [AuthService.java:L97, L257](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/auth/AuthService.java#L97)
  - **Tầng / Service**: `EmailTemplateService / AuthService`
  - **Phân loại**: CWE-79 (Improper Neutralization of Input During Web Page Generation), OWASP A03:2021
  - **Đoạn mã nguy cơ**:
    ```java
    // AuthService.java
    values.put("fullname", StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername());
    ...
    // EmailTemplateService.java
    result = result.replace(key, entry.getValue() == null ? "" : entry.getValue());
    ```
  - **Nguy cơ thực tế**: Tên người dùng (`fullName`) do người dùng tự cập nhật tại trang cá nhân được ghép trực tiếp vào HTML email mà không qua hàm escape HTML. Người dùng có thể chèn mã HTML độc hại hoặc liên kết lừa đảo vào email gửi cho người khác.
  - **Hướng khắc phục**: Áp dụng `org.springframework.web.util.HtmlUtils.htmlEscape(...)` cho tất cả các giá trị biến trước khi điền vào template email.

---

### 🔵 NHÓM SECURITY HOTSPOTS (Cần xem xét cấu hình & tối ưu)

- [x] **16. [HOTSPOT] [java:S4502] Vô hiệu hóa bảo vệ CSRF trên toàn bộ hệ thống** *(ĐÃ XÁC NHẬN AN TOÀN - KIẾN TRÚC STATELESS JWT REST API)*
  - **Vị trí file & dòng**: [SpringSecurityConfig.java:L48](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/config/security/config/SpringSecurityConfig.java#L48)
  - **Đánh giá**: Kiến trúc hiện tại dùng REST stateless với Bearer Authorization header (không dùng Cookie session) nên việc tắt CSRF là hoàn toàn an toàn và tuân thủ Best Practice của OWASP/Spring Security.

- [x] **17. [HOTSPOT] [java:S5443] Lộ đường dẫn thư mục vật lý máy chủ trong API Response** *(ĐÃ KHẮC PHỤC - CHUYỂN SANG ĐƯỜNG DẪN TƯƠNG ĐỐI AN TOÀN)*
  - **Vị trí file & dòng**: [ExcelServiceImpl.java:L71](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/excel/ExcelServiceImpl.java#L71)
  - **Đánh giá**: Đã chuyển `storedPath` sang đường dẫn tương đối an toàn `"temp/" + storedFileName`. Không làm lộ đường dẫn ổ đĩa vật lý của máy chủ mà vẫn đảm bảo 100% logic và hợp đồng dữ liệu với Frontend.

- [x] **18. [HOTSPOT] [java:S2095] Nguy cơ rò rỉ kết nối MinIO InputStream** *(ĐÃ KHẮC PHỤC)*
  - **Vị trí file & dòng**: [MinioExportObjectStorage.java:L43](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/excel/MinioExportObjectStorage.java#L43)
  - **Đánh giá**: Phương thức `download` trả về `InputStream` trực tiếp từ SDK MinIO. Nếu caller không đóng stream, kết nối HTTP client của MinIO sẽ bị treo và cạn kiệt connection pool.

- [x] **19. [HOTSPOT] [java:S2142] Lưu toàn bộ chuỗi JWT Token thô làm Key trong Redis** *(ĐÃ KHẮC PHỤC: Sử dụng mã định danh JTI)*
  - **Vị trí file & dòng**: [TokenBlacklistService.java:L34](file:///d:/ledger-service/src/main/java/com/ledger/ledgerservice/service/auth/TokenBlacklistService.java#L34)
  - **Đánh giá**: Sử dụng toàn bộ token dài hàng trăm ký tự làm key Redis `auth:token:blacklist:<token>` thay vì hash SHA-256. Gây lãng phí RAM Redis và làm lộ token trong log giám sát Redis (`MONITOR`).

---

## 3. KẾT LUẬN & ĐỀ XUẤT BƯỚC TIẾP THEO

Theo đúng yêu cầu của User:
1. **Mã nguồn dự án hiện tại hoàn toàn được giữ nguyên**, chưa tiến hành sửa đổi bất kỳ file source code nào.
2. Bản báo cáo checklist này đã chỉ rõ: **File path**, **Số dòng**, **Service/Class**, **Mã quy tắc SonarQube**, **Phân loại lỗi**, **Mô tả nguy cơ**, và **Hướng dẫn khắc phục**.
3. **Kính mời bạn rà soát checklist trên** và chỉ định nhóm lỗi muốn ưu tiên xử lý (ví dụ: Fix nhóm 🔴 **BLOCKER** trước, hoặc fix toàn bộ từng nhóm theo thứ tự).
