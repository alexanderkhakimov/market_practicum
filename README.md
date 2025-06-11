
# Приложение Market 🛍️

Современная платформа электронной коммерции, построенная на Spring Boot и MySQL.

## 🚀 Быстрый запуск

### Вариант 1: Запуск через Docker (рекомендуется)

```bash
# 1. Склонируйте репозиторий
git clone https://github.com/alexanderkhakimov/market_practicum.git
cd market

# 2. Запустите приложение и базу данных
docker-compose up -d
```

Приложение будет доступно по адресу: [http://localhost:8080/main/](http://localhost:8080/main/)

### Вариант 2: Локальный запуск (без Docker)

```bash
# 1. Установите Java 21 и MySQL 8+
# 2. Создайте базу данных
mysql -u root -p -e "CREATE DATABASE market_db;"

# 3. Настройте доступы в файле:
nano src/main/resources/application.properties

# 4. Запустите приложение
./mvnw spring-boot:run
```

## 📋 Требования

- **Docker** (для Варианта 1)
- **Java 21** (для Варианта 2)
- **MySQL 8+** (для Варианта 2)
- **Maven** (для Варианта 2)

