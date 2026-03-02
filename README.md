# pos-mono-api

Spring Boot 3 REST API for CicdPOS: products, categories, inventory, orders, labels, auth (JWT). Uses PostgreSQL and optional Azure Blob Storage for images.

## Prerequisites

- **Java 21**
- **PostgreSQL 14+** (local or Docker)
- Gradle Wrapper included (`./gradlew`)

## Quick start

1. **Create a database** (e.g. `cicdpos`) and set env vars (recommended; do not hardcode passwords):

   ```bash
   export DATABASE_URL="jdbc:postgresql://localhost:5432/cicdpos"
   export DATABASE_USERNAME="postgres"
   export DATABASE_PASSWORD="<your-password>"
   ```

   Optional: set `JWT_SECRET` to a strong random value. Defaults exist for local dev only.

2. **Run the API:**

   ```bash
   ./gradlew bootRun
   ```

   API: **http://localhost:8080**. Swagger UI: **http://localhost:8080/swagger-ui.html**

## Commands

| Command           | Description                |
|-------------------|----------------------------|
| `./gradlew bootRun` | Start API (dev profile)   |
| `./gradlew build`   | Build + run tests         |
| `./gradlew test`    | Run unit tests            |

## Configuration

- **Profile:** `dev` (default) or `prod` via `SPRING_PROFILES_ACTIVE`.
- **Dev:** DB and Swagger use defaults above; override with env vars.
- **Prod:** Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`; optionally `AZURE_STORAGE_CONNECTION_STRING`, `CORS_ORIGINS`.

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) builds, tests, and deploys to Azure App Service on push to `main`. Configure repo secrets for Azure (e.g. publish profile, app name).
