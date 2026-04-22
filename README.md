# Bank Cards API

REST API для управления банковскими картами и пользователями с аутентификацией через JWT и ролевой моделью `ADMIN` / `USER`.

## Возможности

### ADMIN
- создание карт
- просмотр всех карт
- просмотр карты по id
- изменение статуса карты
- просмотр всех пользователей
- просмотр пользователя по id
- изменение статуса пользователя

### USER
- просмотр своих карт
- просмотр своей карты по id
- фильтрация своих карт по статусу
- просмотр баланса карты
- запрос блокировки карты
- переводы между своими картами

## Безопасность
- аутентификация через JWT
- роли: `ROLE_ADMIN`, `ROLE_USER`
- ролевой доступ к endpoint'ам
- номер карты хранится в зашифрованном виде
- в API номер карты возвращается только в маскированном виде
- для проверки уникальности номера карты используется `card_hash`

## Стек
- Java 21
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker Compose
- Swagger / OpenAPI
- Maven

## Структура ролей

### ADMIN
Администратор управляет пользователями и картами:
- создаёт карты
- активирует и блокирует карты
- просматривает все карты
- просматривает пользователей
- изменяет статус пользователей

### USER
Пользователь работает только со своими картами:
- просматривает свои карты
- смотрит баланс
- отправляет запрос на блокировку
- переводит деньги между своими картами

## Основные сущности

### User
- `id`
- `login`
- `email`
- `password`
- `role`
- `enabled`

### Card
- `id`
- `owner`
- `encryptedCardNumber`
- `lastFourDigits`
- `cardHash`
- `holderName`
- `expirationDate`
- `balance`
- `status`
- `createdAt`

## Основные endpoint'ы

### Auth
- `POST /auth/register` — регистрация пользователя
- `POST /auth/login` — вход и получение JWT

### Admin cards
- `POST /api/admin/cards` — создать карту
- `GET /api/admin/cards` — получить все карты
- `GET /api/admin/cards/{id}` — получить карту по id
- `PATCH /api/admin/cards/{id}/status` — изменить статус карты

### User cards
- `GET /api/cards/my` — получить свои карты
- `GET /api/cards/my/{id}` — получить свою карту по id
- `GET /api/cards/my/{id}/balance` — получить баланс карты
- `PATCH /api/cards/my/{id}/block-request` — запросить блокировку карты
- `POST /api/cards/my/transfer` — перевод между своими картами

### Admin users
- `GET /api/admin/users` — получить всех пользователей
- `GET /api/admin/users/{id}` — получить пользователя по id
- `PATCH /api/admin/users/{id}/status` — изменить статус пользователя

## Валидация и ошибки
Приложение обрабатывает:
- ошибки валидации входных DTO
- отсутствие пользователя
- отсутствие карты
- конфликт при создании сущности
- некорректные бизнес-операции
- ошибки ограничения БД

Все ошибки возвращаются в едином формате:

```json
{
  "timestamp": "2026-04-22T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/api/admin/cards"
}
```
## Как запустить локально
 - Указать переменные окружения
    Пример:
```
DB_BANK_APP_URL
DB_BANK_APP_USERNAME
DB_BANK_APP_PASSWORD
JWT_SECRET
APP_ENCRYPTION_KEY
```

Пример значений:
```
DB_BANK_APP_URL=jdbc:postgresql://localhost:5432/bankcards
DB_BANK_APP_USERNAME=postgres
DB_BANK_APP_PASSWORD=postgres
JWT_SECRET=your-super-secret-jwt-key
APP_ENCRYPTION_KEY=1234567890abcdef
```
- Собрать проект
```
   mvn clean package
   ```
- Запустить приложение
```
   mvn spring-boot:run
   ```

 ##  Запуск через Docker Compose
1. Собрать jar
```
   mvn clean package
```
2. Запустить контейнеры
```
   docker compose up --build
 ```
3. Проверить доступность приложения
4. 
   приложение: http://localhost:8080
   Swagger UI: http://localhost:8080/swagger-ui/index.html

##   Liquibase

Для управления схемой БД используются миграции Liquibase.

Миграции создают и обновляют:

 - таблицу пользователей
 - таблицу карт
 - дополнительные поля для безопасности и бизнес-логики
 - Swagger / OpenAPI

Swagger UI доступен по адресу:
```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI schema:
```
http://localhost:8080/v3/api-docs
```
Пример запроса на создание карты

Request
```
POST /api/admin/cards
Authorization: Bearer <admin_token>
Content-Type: application/json
{
"ownerId": 2,
"holderName": "ANNA PETROVA",
"balance": 1000.00
}
```
Response
```
{
"id": 1,
"maskedCardNumber": "**** **** **** 8873",
"balance": 1000.00,
"expirationDate": "2031-04-22",
"status": "ACTIVE"
}
```
Пример перевода между своими картами

Request
```
POST /api/cards/my/transfer
Authorization: Bearer <user_token>
Content-Type: application/json
{
"fromCardId": 1,
"toCardId": 2,
"amount": 100.00
}
```
