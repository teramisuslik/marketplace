# Marketplace

маркетплейс на Spring Boot 3.5 и Java 17. Есть JWT, пользователи, товары, корзина, шлюз на Feign и оплата через Kafka.

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
- PostgreSQL на `localhost:5432`, в `application.yml` сейчас одна БД `marketplace` (юзеры, товары и корзина в одной базе). Логин/пароль — как у тебя в Postgres; в конфиге по умолчанию `postgres` / `1234`. Другой порт или имя БД — через переменные `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` или правка `application.yml`.
- Kafka — если будешь гонять покупку (шлюз + ServiceForPay)
- Maven (IntelliJ обычно сама качает зависимости по pom)

## Как запускать (IntelliJ)

У каждого модуля своё Spring Boot приложение: открываешь класс с `main` и жмёшь зелёную стрелку у метода `main` (или правый клик по классу → Run).

Сначала подними PostgreSQL (и Kafka, если тестируешь покупку). Потом приложения, примерно в таком порядке:

1. **marketplace** — `MarketplaceApplication` (корень репо, `src/main/java/...`)
2. **ServiceForProduct** — `ServiceForProductApplication`
3. **ServiceForCart** — `ServiceForCartApplication`
4. **ServiceForPay** — `ServiceForPayApplication` (только если нужна оплата / Kafka)
5. **Controller** — `ControllerApplication` (шлюз в конце, когда остальные уже живые)

Чтобы не запускать по одному каждый раз, можно в Run → **Edit Configurations** сделать **Compound** и добавить туда несколько Spring Boot конфигов подряд.

Если IDEA не видит `main` в каком-то сервисе — смотри раздел **IntelliJ** в конце (про подключение pom-ов).

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

## IntelliJ

В репо несколько отдельных Maven-проектов (у каждого свой `pom.xml`). Корневой pom — только модуль marketplace. Если после **Reload Maven** пропали нормальные модули и в дереве одни «чашечки», добавь pom-ы заново: ПКМ по `Controller/pom.xml` и т.д. → **Add as Maven Project**, или в панели Maven кнопка **+**.

## API

[docs/API.md](docs/API.md) — таблицы с путями и полями сущностей.
