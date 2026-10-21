# Habsida Delivery — Backend

REST API маркетплейса доставки еды: магазины, меню, клиенты и заказы.

**Стек:** Java 25, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL, Flyway, Swagger (springdoc).

## Что нужно установить

- JDK 25
- Docker (для базы данных и интеграционных тестов)

Maven устанавливать не нужно, используется встроенный `./mvnw`.

____

## Быстрый старт

### 1. Поднять базу данных

PostgreSQL запускается в Docker из файла `src/main/resources/docker-compose.yml`:

```bash
docker compose -f src/main/resources/docker-compose.yml up -d
```

| Параметр | Значение |
|---|---|
| Хост и порт | `localhost:5433` |
| База | `habsidadb` |
| Пользователь | `postgres` |
| Пароль | `root` |

**Остановить базу:** `docker compose -f src/main/resources/docker-compose.yml down`.

**Остановить и удалить все данные:** `docker compose -f src/main/resources/docker-compose.yml down -v`.

### 2. Задать переменные окружения

Профиль приложения выбирается явно, а настройки подключения и секрет для JWT читаются из переменных окружения:

| Переменная | Значение для локальной разработки | Описание |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | профиль приложения (без него приложение не найдёт настройки БД) |
| `DB_USERNAME` | `postgres` | пользователь БД |
| `DB_PASSWORD` | `root` | пароль БД |
| `JWT_SECRET` | любая строка не короче 32 символов | ключ подписи токенов |

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_USERNAME=postgres
export DB_PASSWORD=root
export JWT_SECRET=habsida-local-secret-key-at-least-32-chars
```

В IntelliJ IDEA эти же переменные добавляются в Run Configuration: поле Environment variables, кнопка с иконкой списка, затем `+` для каждой переменной (в однострочном поле их можно перечислить через точку с запятой).

### 3. Запустить приложение

```bash
./mvnw spring-boot:run
```

Приложение стартует на `http://localhost:8080`.

____

## Миграции базы данных

Схемой БД управляет **Flyway**. Отдельных команд запускать не нужно: все миграции применяются автоматически при старте приложения.

- Файлы лежат в `src/main/resources/db/migration`, имена вида `V<номер>__<описание>.sql`.
- Flyway выполняет их по порядку номеров и записывает применённые версии в таблицу `flyway_schema_history`.
- Уже применённые миграции нельзя редактировать. Для любого изменения схемы создаётся новый файл со следующим номером.
- Чтобы начать с чистой базы, удалите данные (см. раздел «1. Поднять базу данных») и запустите приложение заново: все миграции применятся с нуля.

____

## Swagger

После запуска документация API доступна без авторизации:

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

**Как вызывать защищённые методы:**

1. Выполните `POST /auth/login` и скопируйте значение поля `token`(без кавычек) из ответа.
2. Нажмите кнопку **Authorize** в правом верхнем углу Swagger UI.
3. Вставьте токен и подтвердите.

Примеры запросов смотрите в [документации API](docs/API_examples.md).
____

## Роли и доступ

В системе две роли: `ADMIN` и `MERCHANT`.
Администратор видит все магазины и управляет ими, мерчант работает только с теми магазинами, к которым ему выдан доступ.

____

## Тестовые данные

Для локальной разработки базу можно автоматически заполнить тестовыми данными. Seeder (`DevDataSeeder`) **выключен по умолчанию** и включается только при двух условиях: активен профиль `dev` и задан флаг `APP_SEED_ENABLED=true`.

Дополнительные переменные окружения:

| Переменная | Описание |
|---|---|
| `APP_SEED_ENABLED` | `true` включает seeder, по умолчанию `false` |
| `SEED_ADMIN_PASSWORD` | пароль администратора (значения по умолчанию нет, задайте свой) |
| `SEED_MERCHANT_PASSWORD` | пароль сотрудника магазина (значения по умолчанию нет, задайте свой) |

Seeder создаёт:

| Что | Данные |
|---|---|
| Администратор | `admin@habsida.com` |
| Магазин | Burger House (доставка и самовывоз) |
| Сотрудник магазина | `merchant@habsida.com` (роль `MERCHANT`, привязан к Burger House) |
| Категории | Burgers, Drinks, Sides |
| Товары | Double Cheese Burger, Sprite, Potato |
| Клиенты | 2 клиента с адресами |
| Заказы | 2 заказа: один на доставку, один на самовывоз |

Пароли для входа берутся из переменных `SEED_ADMIN_PASSWORD` и `SEED_MERCHANT_PASSWORD`.

Seeder запускается один раз: если администратор уже есть в базе, он ничего не делает. Чтобы заполнить данные заново, пересоздайте базу (см. раздел «1. Поднять базу данных»).

____

## Тесты

```bash
./mvnw test
```

- Unit-тесты сервисов и безопасности работают на Mockito, без Spring и без БД.
- Тесты контроллеров (`@WebMvcTest`) проверяют веб-слой с замоканными сервисами, база не нужна.
- Интеграционные тесты (классы с названием `...IntegrationTest`, например `ProductIntegrationTest`) идут через весь стек на реальном PostgreSQL, который поднимается автоматически через Testcontainers. **Для них нужен запущенный Docker.**

Тесты используют отдельный профиль `test`, seeder в них не запускается.

____

## Профили и деплой

Профиль всегда задаётся явно через `SPRING_PROFILES_ACTIVE`. Без него Spring использует профиль `default`, для которого нужных настроек нет.

| Профиль | Назначение |
|---|---|
| `dev` | локальная разработка, БД на `localhost:5433` |
| `prod` | боевой режим, подключение берётся из переменных `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` |

____

## Структура проекта

```
src/main/java/com/delivery/habsida
├── config       — настройки (OpenAPI, JPA), seeder для dev
├── controller   — REST-контроллеры
├── dto          — объекты запросов и ответов
├── entity       — JPA-сущности
├── exception    — исключения и глобальный обработчик ошибок
├── mapper       — преобразование сущностей в DTO
├── repository   — Spring Data репозитории
├── security     — JWT, фильтр, проверка доступа к магазину
└── service      — бизнес-логика
src/main/resources
├── db/migration — миграции Flyway
└── docker-compose.yml
src/test/java/com/delivery/habsida
├── controller   — тесты контроллеров (@WebMvcTest)
├── dto          — тесты DTO
├── security     — тесты безопасности
├── service      — unit-тесты сервисов
└── *IntegrationTest.java — интеграционные тесты на реальном PostgreSQL
src/test/resources
└── application.yml — настройки профиля test
```