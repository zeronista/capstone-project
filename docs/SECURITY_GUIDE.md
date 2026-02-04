# 🔒 Hướng dẫn Bảo mật API Keys và Secrets

## ⚠️ TẠI SAO PHẢI BẢO MẬT API KEYS?

Nếu API keys bị lộ lên GitHub/Git:
- ❌ Hacker có thể sử dụng API của bạn → Mất tiền
- ❌ Dữ liệu khách hàng bị đánh cắp
- ❌ Vi phạm quy định bảo mật (GDPR, PCI-DSS)
- ❌ Mất uy tín và tài khoản có thể bị khóa

## 📋 CÁC PHƯƠNG PHÁP BẢO MẬT

### Phương pháp 1: Environment Variables (Khuyên dùng cho Production)

#### Bước 1: Tạo file `.env` (Local Development)

```bash
# Copy file example
cp .env.example .env
```

Nội dung file `.env`:
```properties
STRINGEE_KEY_SID=SKxxxxxxxxxxxxxxxxxxxxx
STRINGEE_KEY_SECRET=your_actual_secret_key
STRINGEE_WEBHOOK_DOMAIN=https://your-ngrok.ngrok.io
```

#### Bước 2: Load Environment Variables

**Trên Windows PowerShell:**
```powershell
# Set biến môi trường tạm thời (chỉ trong session hiện tại)
$env:STRINGEE_KEY_SID="SKxxxxxxxxxxxxxxxxxxxxx"
$env:STRINGEE_KEY_SECRET="your_secret_key"

# Hoặc set vĩnh viễn (System Environment Variables)
[System.Environment]::SetEnvironmentVariable("STRINGEE_KEY_SID", "SKxxxxx", "User")
```

**Trên Linux/Mac:**
```bash
export STRINGEE_KEY_SID="SKxxxxxxxxxxxxxxxxxxxxx"
export STRINGEE_KEY_SECRET="your_secret_key"

# Hoặc thêm vào ~/.bashrc hoặc ~/.zshrc để tự động load
echo 'export STRINGEE_KEY_SID="SKxxxxx"' >> ~/.bashrc
```

#### Bước 3: Chạy Spring Boot với Environment Variables

```bash
# Cách 1: Trong terminal đã set env vars
mvn spring-boot:run

# Cách 2: Set ngay khi chạy (Linux/Mac)
STRINGEE_KEY_SID=SKxxxxx STRINGEE_KEY_SECRET=your_secret mvn spring-boot:run

# Cách 3: Trong IntelliJ IDEA
# Run → Edit Configurations → Environment Variables
# Thêm: STRINGEE_KEY_SID=SKxxxxx;STRINGEE_KEY_SECRET=your_secret

# Cách 4: Trong VS Code
# Tạo file .vscode/launch.json
```

**VS Code launch.json:**
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.g4.capstoneproject.CapstoneProjectApplication",
      "env": {
        "STRINGEE_KEY_SID": "SKxxxxxxxxxxxxxxxxxxxxx",
        "STRINGEE_KEY_SECRET": "your_secret_key",
        "SPRING_PROFILES_ACTIVE": "local"
      }
    }
  ]
}
```

---

### Phương pháp 2: application-local.properties (Khuyên dùng cho Development)

#### Bước 1: Tạo file `application-local.properties`

```bash
# Copy file example
cd src/main/resources
cp application-local.properties.example application-local.properties
```

#### Bước 2: Điền API keys thực vào file

File `application-local.properties`:
```properties
stringee.key.sid=SKxxxxxxxxxxxxxxxxxxxxx
stringee.key.secret=your_actual_secret_key
stringee.webhook.domain=https://abc123.ngrok.io
```

#### Bước 3: Kích hoạt profile "local"

**Cách 1: Trong application.properties**
```properties
spring.profiles.active=local
```

**Cách 2: Environment Variable**
```bash
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

**Cách 3: VM Options (IntelliJ/Eclipse)**
```
-Dspring.profiles.active=local
```

✅ **Lợi ích:** File `application-local.properties` đã được thêm vào `.gitignore` nên sẽ KHÔNG bao giờ bị commit lên Git.

---

### Phương pháp 3: Spring Profiles cho nhiều môi trường

Tạo các file riêng biệt cho từng môi trường:

```
src/main/resources/
├── application.properties           # Cấu hình chung
├── application-local.properties     # Local development (KHÔNG commit)
├── application-dev.properties       # Development server (KHÔNG commit)
├── application-staging.properties   # Staging server (KHÔNG commit)
├── application-prod.properties      # Production server (KHÔNG commit)
└── application.properties.example   # Template (Commit được)
```

Chạy với profile cụ thể:
```bash
# Development
java -jar app.jar --spring.profiles.active=dev

# Staging
java -jar app.jar --spring.profiles.active=staging

# Production
java -jar app.jar --spring.profiles.active=prod
```

---

## 🚀 TRIỂN KHAI LÊN SERVER (PRODUCTION)

### 1. Heroku

```bash
# Set config vars trong Heroku Dashboard hoặc CLI
heroku config:set STRINGEE_KEY_SID=SKxxxxx
heroku config:set STRINGEE_KEY_SECRET=your_secret
heroku config:set STRINGEE_WEBHOOK_DOMAIN=https://your-app.herokuapp.com
```

### 2. AWS Elastic Beanstalk

Trong AWS Console:
- Configuration → Software → Environment Properties
- Thêm: `STRINGEE_KEY_SID`, `STRINGEE_KEY_SECRET`, etc.

### 3. Docker

**Dockerfile:**
```dockerfile
FROM openjdk:21-slim
COPY target/*.jar app.jar

# KHÔNG hard-code secrets trong Dockerfile!
# Sử dụng -e khi run container
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Run với secrets:**
```bash
docker run -d \
  -e STRINGEE_KEY_SID=SKxxxxx \
  -e STRINGEE_KEY_SECRET=your_secret \
  -p 8080:8080 \
  your-app-image
```

**Docker Compose (sử dụng .env file):**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    env_file:
      - .env  # File này KHÔNG commit vào Git
```

### 4. Kubernetes

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: stringee-secrets
type: Opaque
data:
  # Base64 encoded values
  STRINGEE_KEY_SID: U0t4eHh4eHh4eHh4eHh4eA==
  STRINGEE_KEY_SECRET: eW91cl9zZWNyZXRfa2V5

---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: app
        image: your-app:latest
        envFrom:
        - secretRef:
            name: stringee-secrets
```

---

## 🔍 KIỂM TRA BẢO MẬT

### 1. Kiểm tra xem đã commit API keys chưa

```bash
# Tìm kiếm trong Git history
git log -p | grep -i "stringee.key.sid"
git log -p | grep -i "SK[a-zA-Z0-9]"

# Kiểm tra trong working directory
grep -r "SKxxxxxx" . --exclude-dir=node_modules --exclude-dir=target
```

### 2. Xóa API keys khỏi Git history (Nếu đã commit nhầm)

⚠️ **NGUY HIỂM - Sẽ thay đổi Git history!**

```bash
# Sử dụng BFG Repo-Cleaner
java -jar bfg.jar --replace-text passwords.txt your-repo.git

# Hoặc git filter-branch
git filter-branch --tree-filter 'git ls-files -z "*.properties" | xargs -0 sed -i "s/SKxxxxx/REMOVED/g"' HEAD

# Force push (CẨN THẬN!)
git push origin --force --all
```

**SAU KHI XÓA:** Phải REVOKE (hủy) API keys cũ và tạo keys mới!

### 3. Sử dụng GitHub Secrets Scanning

- GitHub tự động quét và cảnh báo nếu phát hiện API keys
- Vào: Settings → Code security and analysis → Enable Secret scanning

---

## ✅ CHECKLIST BẢO MẬT

Trước khi commit code:

- [ ] File `.gitignore` đã bao gồm `application-local.properties`
- [ ] File `.gitignore` đã bao gồm `.env`
- [ ] Tất cả API keys trong `application.properties` đều dùng `${ENV_VAR:default}`
- [ ] Đã tạo file `application.properties.example` làm template
- [ ] KHÔNG có API keys thật trong bất kỳ file nào sẽ được commit
- [ ] Đã test với environment variables ở local
- [ ] Đã review `git diff` trước khi commit

```bash
# Trước mỗi lần commit, chạy:
git diff
git status

# Đảm bảo KHÔNG thấy các file sau:
# - application-local.properties
# - .env
# - Bất kỳ file nào chứa API keys thật
```

---

## 🛠️ TOOLS HỖ TRỢ

### 1. git-secrets (Ngăn chặn commit secrets)

```bash
# Install
brew install git-secrets  # Mac
apt-get install git-secrets  # Ubuntu

# Setup
git secrets --install
git secrets --register-aws  # AWS keys
git secrets --add 'SK[a-zA-Z0-9]{32}'  # Stringee keys pattern

# Scan repository
git secrets --scan
git secrets --scan-history
```

### 2. truffleHog (Quét secrets trong Git history)

```bash
# Install
pip install truffleHog

# Scan
truffleHog --regex --entropy=True https://github.com/your-repo
```

### 3. dotenv-vault (Quản lý .env files)

```bash
npm install -g dotenv-vault

# Encrypt .env file
dotenv-vault encrypt

# Decrypt
dotenv-vault decrypt
```

---

## 📚 TÀI LIỆU THAM KHẢO

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)
- [OWASP Secret Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_CheatSheet.html)

---

## 🆘 NẾU ĐÃ LỘ API KEYS

1. **NGAY LẬP TỨC:** Revoke/Delete API keys bị lộ trong Stringee Dashboard
2. **TẠO KEYS MỚI:** Generate keys mới thay thế
3. **XÓA KHỎI GIT:** Sử dụng BFG hoặc git filter-branch để xóa khỏi history
4. **THAY ĐỔI PASSWORDS:** Nếu có passwords liên quan
5. **MONITOR:** Theo dõi usage để phát hiện lạm dụng
6. **HỌC BÀI HỌC:** Setup git-secrets để không lặp lại

---

## 💡 BEST PRACTICES

1. ✅ **KHÔNG BAO GIỜ** hard-code API keys trong code
2. ✅ **LUÔN LUÔN** sử dụng environment variables hoặc secret management
3. ✅ **REVIEW CODE** kỹ trước khi commit
4. ✅ **ROTATE KEYS** định kỳ (3-6 tháng/lần)
5. ✅ **PRINCIPLE OF LEAST PRIVILEGE:** Chỉ cấp quyền tối thiểu cần thiết
6. ✅ **SEPARATE SECRETS** cho từng môi trường (dev/staging/prod)
7. ✅ **AUDIT LOGS:** Theo dõi ai truy cập secrets khi nào
8. ✅ **BACKUP SECRETS:** Lưu trữ an toàn (password manager, vault)

**Nhớ:** An toàn hơn cả là NGĂN CHẶN secrets bị lộ ngay từ đầu! 🔐
