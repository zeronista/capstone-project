# 🚀 Quick Start

```bash
# 1. Clone & config
git clone <repository-url>
cd capstone-project
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# Edit application-local.properties với database credentials của bạn

# 2. Setup database (MySQL 8.0+)
mysql -u root -p -e "CREATE DATABASE medical_system; CREATE USER 'medicaluser'@'localhost' IDENTIFIED BY 'password'; GRANT ALL ON medical_system.* TO 'medicaluser'@'localhost';"
mysql -u medicaluser -p medical_system < docs/database_full_schema.sql

# 3. Build Docker ASR service + Run app (Java 21 required)
./mvnw clean package && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Hoặc skip Docker build nếu không cần ASR:
# ./mvnw clean package -Ddocker.build.skip=true && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Truy cập: http://localhost:8080  
ASR Service: http://localhost:8001
