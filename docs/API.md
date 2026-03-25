# API (черновик для себя)

Набросок по сущностям и эндпоинтам. Живые примеры запросов удобнее смотреть в Swagger (ссылки в README).

## Как устроено

- Снаружи удобно бить в **Controller** на порту **8085**. Он через Feign ходит в user (8082), product (8081), cart (8083).
- JWT: логин на шлюзе, потом заголовок `Authorization: Bearer <токен>` там, где нужна авторизация.

## Сущности

### User (БД Users, модуль marketplace)

| Поле | Заметка |
|------|---------|
| id | |
| username | уникальный |
| password | |
| role | USER / ADMIN / SELLER (в коде так) |

### Product (БД Products, ServiceForProduct)

| Поле | Заметка |
|------|---------|
| id | |
| name | уникальный |
| description | |
| countOfProduct | кол-во |
| rating | |
| sellerId | id продавца |

### Cart (БД Cart, ServiceForCart)

Строка корзины: кто какой товар положил.

| Поле | Заметка |
|------|---------|
| id | |
| userId | |
| productId | |

### Role

Enum роли пользователя.

### ServiceForPay

REST нет, слушает Kafka, топик `buy-product`.

---

## Controller (шлюз), :8085

`http://localhost:8085`

| Метод | Путь | Что делает | JWT |
|-------|------|------------|-----|
| POST | /register | регистрация покупателя | нет |
| POST | /register_seller | регистрация продавца | нет |
| POST | /login | логин, отдаёт токен | нет |
| POST | /addproduct | добавить товар | да |
| GET | /main | все товары | нет |
| GET | /main/{word} | поиск по слову | нет |
| POST | /add_product_to_cart/{name} | в корзину по имени товара | да |
| GET | /display/cast | корзина | да |
| POST | /buy_product/{productId} | покупка (уходит в Kafka и т.д.) | да |

Тела запросов — см. DTO в `com.example.controller.DTO` или в Swagger.

---

## User service, :8082

Префикс `/api/user`.

| Метод | Путь | Заметка |
|-------|------|---------|
| POST | /api/user/register | создать пользователя |
| POST | /api/user/register/seller | продавец |
| POST | /api/user/login | возвращает jwt строкой |
| POST | /api/user/userid | userid по Authorization |
| GET | /api/user/get_role | роль по Authorization |
| GET | /api/user/load_user_by_username | ?username=… , для feign и т.п. |

---

## Product service, :8081

Префикс `/api/product`.

| Метод | Путь | Заметка |
|-------|------|---------|
| POST | /api/product/add | только SELLER, роль проверяется через user |
| GET | /api/product/main | список |
| GET | /api/product/main/{word} | поиск |
| GET | /api/product/find_product_by_name/{name} | id по имени |
| GET | /api/product/find_all_by_id | ?id= |

---

## Cart service, :8083

Префикс `/api/cart`.

| Метод | Путь | Заметка |
|-------|------|---------|
| POST | /api/cart/add_product_to_cart/{name} | добавить |
| GET | /api/cart/display | что в корзине |

---

## OpenAPI

У сервисов выше есть `/v3/api-docs` и `/swagger-ui/index.html`.
