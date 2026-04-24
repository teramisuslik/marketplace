# Marketplace

Маркетплейс на Spring Boot 3.5 и Java 17. Есть JWT, пользователи, товары, корзина, шлюз на Feign и оплата через Kafka.

## Модули (порты)

| Папка / модуль | Порт | Что делает |
|----------------|------|------------|
| Controller | 8085 | Шлюз: сюда обращается клиент, внутри зовёт user/product/cart, в Kafka шлёт покупку |
| marketplace (корневой pom) | 8082 | Юзеры: регистрация, логин, JWT |
| ServiceForProduct | 8081 | Товары |
| ServiceForCart | 8083 | Корзина |
| ServiceForPay | 8086 | Слушает Kafka, своего REST нет |

Эндпоинты подробнее — в [docs/API.md](docs/API.md).

## Что нужно поставить

- JDK 17
- PostgreSQL на `localhost:5432`, в `application.yml` Каждый микросервис использует свою базу данных:
  - marketplace -> Users
  - ServiceForCart -> Cart
  - ServiceForProduct -> Products.
  - Сервисы без JPA не требуют БД.

Логин/пароль — как в вашем Postgres; в конфиге по умолчанию `postgres` / `1234`. Другой порт или имя БД — через переменные `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` или правка `application.yml`.
- Kafka — если будете проверять покупку (шлюз + ServiceForPay)
- **Ollama** (чат-ассистент на шлюзе): `docker compose up -d ollama`, затем подтянуть модель из `assistant.model` (по умолчанию **llama3.2:latest**): `docker compose exec ollama ollama pull llama3.2`. Для **llama3.1:8b** нужен запас RAM под Ollama (~5+ GiB); иначе Ollama вернёт ошибку нехватки памяти. API: `http://localhost:11434`; в [Controller/src/main/resources/application.yml](Controller/src/main/resources/application.yml) — `assistant.ollama-base-url`, `assistant.model`, опционально `assistant.response-format-json-object`.
- Maven (IntelliJ обычно сама качает зависимости по pom)

## Docker: весь стек и фронт (dev, hot reload)

Поднимается Kafka/Zookeeper/Ollama из [docker-compose.yml](docker-compose.yml) и Postgres со всеми Spring-сервисами + Vite из [docker-compose.dev.yml](docker-compose.dev.yml) (профиль `dev`).

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev up
```

Порты на хосте: **8085** шлюз, **8082** пользователи, **8081** товары, **8083** корзина, **8086** оплата, **5173** фронт, **5432** Postgres, **29092** Kafka, **11434** Ollama.

- **Фронт**: каталог [frontend_for_pius](frontend_for_pius) смонтирован в контейнер — правки в коде сразу видны через Vite HMR (`HUSKY=0`, чтобы не падал `npm install` без `.git` в образе).
- **Java**: в контейнере `spring-boot:run` + **Spring DevTools**. После изменения `.java` пересобери модуль **на хосте** (сборка IDEA или `./mvnw compile` в папке сервиса), чтобы обновился `target/classes` в смонтированной папке — DevTools перезапустит приложение в контейнере.
- Профиль **`docker`** подставляет URL сервисов и Postgres внутри сети compose (см. `application-docker.yml` в модулях).

Без профиля `dev` по-прежнему можно поднять только инфраструктуру: `docker compose up -d`.

## Как запускать (IntelliJ)

У каждого модуля своё Spring Boot приложение: открываете микросервис из запускаете класс [название сервиса]Application
Сначала поднимите PostgreSQL (и Kafka, если тестируете покупку). Потом приложения, примерно в таком порядке:

1. **marketplace** — `MarketplaceApplication` (корень репо, `src/main/java/...`)
2. **ServiceForProduct** — `ServiceForProductApplication`
3. **ServiceForCart** — `ServiceForCartApplication`
4. **ServiceForPay** — `ServiceForPayApplication` (только если нужна оплата / Kafka)
5. **Controller** — `ControllerApplication` (шлюз в конце, когда остальные уже живые)

Чтобы не запускать по одному каждый раз, можно в Run → **Edit Configurations** сделать **Compound** и добавить туда несколько Spring Boot конфигов подряд.

Если IDEA не видит Application класс в каком-то сервисе — смотри раздел **IntelliJ** в конце (про подключение pom-ов).

## Swagger

Подключён springdoc, UI открывается так:

| Сервис | URL |
|--------|-----|
| Шлюз | http://localhost:8085/swagger-ui/index.html |
| User | http://localhost:8082/swagger-ui/index.html |
| Product | http://localhost:8081/swagger-ui/index.html |
| Cart | http://localhost:8083/swagger-ui/index.html |

Сырой OpenAPI: `http://localhost:<порт>/v3/api-docs`.

На шлюзе для методов с замком в Swagger жми Authorize и вставь JWT после логина (Bearer подставится сам).

## ИИ-ассистент (шлюз + Ollama)

1. Подними Ollama: `docker compose up -d ollama` и один раз `docker compose exec ollama ollama pull llama3.2` (или модель из `assistant.model` в [Controller/src/main/resources/application.yml](Controller/src/main/resources/application.yml)).
2. Запрос (подставь JWT после логина на шлюзе):

```bash
curl -s http://localhost:8085/assistant/chat \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"message":"Что посоветуешь купить?","history":[]}'
```

В ответе поле `reply` — текст для пользователя; `mentionedProductIds` — id товаров для UI; при команде добавления в корзину бэк выполнит вызов корзины и перечислит шаги в `executedActions`.

## IntelliJ

В репо несколько отдельных Maven-проектов (у каждого свой `pom.xml`). Корневой pom — только модуль marketplace. Если после **Reload Maven** пропали нормальные модули и в дереве одни «чашечки», добавь pom-ы заново: ПКМ по `Controller/pom.xml` и т.д. → **Add as Maven Project**, или в панели Maven кнопка **+**.

## API

[docs/API.md](docs/API.md) — таблицы с путями и полями сущностей.
