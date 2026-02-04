# Swagger API Documentation

## Giới thiệu

Hệ thống đã được tích hợp SpringDoc OpenAPI (Swagger) để tự động tạo API documentation.

## Truy cập Swagger UI

Sau khi khởi động ứng dụng, bạn có thể truy cập Swagger UI tại:

- **Swagger UI**: http://localhost:8080/swagger-ui.html hoặc http://localhost:8080/swagger-ui/index.html
- **OpenAPI Docs (JSON)**: http://localhost:8080/v3/api-docs
- **OpenAPI Docs (YAML)**: http://localhost:8080/v3/api-docs.yaml

## Cách sử dụng Swagger Annotations

### 1. Annotate Controller

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs cho quản lý người dùng")
public class UserController {
    
    @Operation(
        summary = "Lấy danh sách người dùng",
        description = "Trả về danh sách tất cả người dùng trong hệ thống"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        // Implementation
    }
    
    @Operation(summary = "Tạo người dùng mới")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
        @Parameter(description = "Thông tin người dùng cần tạo") 
        @RequestBody @Valid UserCreateRequest request
    ) {
        // Implementation
    }
}
```

### 2. Annotate DTO/Model

```java
@Schema(description = "Thông tin người dùng")
public class UserDTO {
    
    @Schema(description = "ID người dùng", example = "1")
    private Long id;
    
    @Schema(description = "Tên đăng nhập", example = "john.doe", required = true)
    private String username;
    
    @Schema(description = "Email", example = "john@example.com")
    private String email;
    
    @Schema(description = "Vai trò", example = "USER", allowableValues = {"USER", "ADMIN", "DOCTOR"})
    private String role;
}
```

### 3. Ví dụ annotation đầy đủ

```java
@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient Management", description = "APIs quản lý bệnh nhân")
public class PatientController {

    @Operation(
        summary = "Lấy thông tin bệnh nhân theo ID",
        description = "Trả về thông tin chi tiết của một bệnh nhân"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tìm thấy bệnh nhân",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy bệnh nhân")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(
        @Parameter(description = "ID của bệnh nhân", required = true)
        @PathVariable Long id
    ) {
        // Implementation
    }
}
```

## Các annotation thường dùng

- `@Tag`: Nhóm các API endpoint
- `@Operation`: Mô tả API endpoint
- `@ApiResponses` / `@ApiResponse`: Mô tả các response codes
- `@Parameter`: Mô tả request parameters
- `@Schema`: Mô tả model/DTO
- `@Hidden`: Ẩn endpoint khỏi documentation

## Authentication trong Swagger

Swagger đã được cấu hình hỗ trợ JWT Bearer Token và OAuth2. Để test API cần authentication:

1. Click nút **Authorize** trên Swagger UI
2. Nhập JWT token vào ô "Value" (format: `Bearer your-token-here` hoặc chỉ `your-token-here`)
3. Click **Authorize** để lưu
4. Bây giờ bạn có thể test các API cần authentication

## Tùy chỉnh

Để tùy chỉnh cấu hình Swagger, sửa file:
- `src/main/java/com/g4/capstoneproject/config/SwaggerConfig.java`
- `src/main/resources/application.properties` (phần springdoc)

## Lưu ý

- Swagger UI chỉ nên enable ở môi trường development
- Trong production, nên disable hoặc bảo vệ Swagger UI bằng authentication
- Để disable Swagger trong production, thêm vào application-prod.properties:
  ```properties
  springdoc.swagger-ui.enabled=false
  springdoc.api-docs.enabled=false
  ```
