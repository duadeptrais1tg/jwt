# JWT với Spring Boot 3 – Security 6

| Thư mục | Bài | Thư viện JWT | Port | Database |
|---|---|---|---|---|
| `jwt-jjwt/` | 1. Ví dụ trong bài giảng | `io.jsonwebtoken:jjwt` 0.12.6 | 8005 | `jwt_springboot3` |
| `jwt-nimbus/` | 2. Thay JJWT bằng Nimbus | `com.nimbusds:nimbus-jose-jwt` 10.10 | 8006 | `jwt_nimbus` |

Hai project giống nhau, chỉ khác `pom.xml`, `services/JwtService.java` và các exception JWT
(`exceptions/`). Database MySQL tự tạo (`createDatabaseIfNotExist=true`, user `root/123456`).

## Chạy

```
cd jwt-jjwt      # hoặc jwt-nimbus
mvn spring-boot:run
```

## Test (Postman / curl)

1. `POST /auth/signup`  body `{"email":"pacman16012004@gmail.com","password":"123456","fullName":"Phạm Viết Thư"}`
2. `POST /auth/login`   body `{"email":"pacman16012004@gmail.com","password":"123456"}` → `{"token": "...", "expiresIn": 3600000}`
3. `GET /users/me`, `GET /users` với header `Authorization: Bearer <token>` (thiếu token → 401)
4. Giao diện AJAX: mở `http://localhost:8005/login` (hoặc 8006) → đăng nhập → `/user/profile`

## Lỗi trả về (ProblemDetail)

| Lỗi | Bài 1 (jjwt) | Bài 2 (Nimbus) | HTTP |
|---|---|---|---|
| Sai email/mật khẩu | `BadCredentialsException` | `BadCredentialsException` | 401 |
| Tài khoản bị khóa | `AccountStatusException` | `AccountStatusException` | 403 |
| Không có quyền | `AccessDeniedException` | `AccessDeniedException` | 403 |
| Sai chữ ký | `SignatureException` | `InvalidJwtSignatureException` | 401 |
| Hết hạn | `ExpiredJwtException` | `ExpiredJwtTokenException` | 401 |
| Sai định dạng / alg | `JwtException` | `InvalidJwtException` | 401 |
