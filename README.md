# Rabbit B2C Mall

Java 8 / Spring Boot 2.7 multi-service learning project. The current backend covers shop registration, employee authentication, product publishing and inventory, and order lifecycle operations.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| gateway | 8080 | Routes `/api/shop/**`, `/api/employee/**`, `/api/product/**`, and `/api/order/**` |
| shop | 8081 | Shop registration and asynchronous welcome/admin initialization |
| employee | 8082 | Employee initialization, BCrypt login, signed bearer tokens, Redis-backed single-session logout |
| product | 8083 | Shop-scoped products, listing/status changes, atomic stock reserve/release |
| order | 8084 | Order snapshots, stock reservation, payment simulation, shipping, confirmation, cancellation |

Each service uses its own SQLite database. Product creation starts as a draft. Publish it with `POST /api/product/{id}/status?status=1` before creating an order. Order creation reads product prices from the product service and reserves stock; cancelling a waiting order releases it. Payment is a local classroom simulation, not a real payment integration.

## Local setup

1. Copy `.env.example` to `.env` and replace `JWT_TOKEN_KEY` and `INTERNAL_SERVICE_TOKEN` with separate random secrets. `.env` is ignored by Git.
2. Start Redis and Nacos using their local defaults, or set the corresponding environment variables.
3. Run `mvn -DskipTests install` from the repository root to compile and install the shared `common` module.
4. Start each module with `mvn -pl <module> spring-boot:run`; start `common` only as a dependency.

Shop registration asynchronously creates a matching employee and a welcome message. Log in through `/api/employee/login` with the same shop name's admin account as the employee username and the registration password. Use the returned token as `Authorization: Bearer <token>` for product and order operations.

## Core API flow

1. `POST /api/shop/register` with `shopName`, `adminAccount`, and `adminPassword`.
2. `POST /api/employee/login` with `shopId`, `username`, and `password`.
3. `POST /api/product/create` with `name`, `type`, `price`, `stock`, and optional `description`; publish using the status endpoint.
4. `POST /api/order/create` with `items: [{"productId": 1, "quantity": 2}]`.
5. Advance with `POST /api/order/{id}/pay`, then `/ship`, then `/confirm`. A waiting order can instead be cancelled with `/cancel`.

Product and order data are initialized from `schema.sql` and kept in separate SQLite files. Stock reservation spans two services, so the order service compensates earlier reservations if a later reservation or local order write fails; a process crash between the services still requires operational reconciliation.

## Current scope

There is no customer account/catalog UI, real payment provider, refund/timeout worker, distributed transaction coordinator, or automated test suite yet. The order API currently models employees in a shop as the actor; a separate customer identity model is a future step.
