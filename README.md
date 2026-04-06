# ZorvynFinance

Spring Boot finance API with JWT auth, role-based access, transactions, dashboard, and admin user management.

## Docs

- API reference: `docs/api.md`
- Architecture/decisions notes: `Decisions.md`

## Boot The Project

### Prerequisites

- Java 21
- Docker (optional, for container run)

### Run locally (Gradle)

From project root:

```powershell
.\gradlew.bat bootRun
```

App starts on port `8080` by default.

### Build and run with Docker

From project root:

```powershell
docker build -t zorvynfinance:local .
docker run --rm -p 8080:8080 zorvynfinance:local
```

## Configuration

- Main config file: `src/main/resources/application.properties`