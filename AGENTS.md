# AGENTS.md

## Commands

- `./backend/mvnw spring-boot:run` — start dev server (auto-launches Postgres via `compose.yaml`)
- `./backend/mvnw test` — run all tests
- `./backend/mvnw test -Dtest=ClassName` — run a single test class
- `./backend/mvnw clean compile` — compile (runs Lombok annotation processing)
- `docker compose up -d` — start Postgres + pgAdmin manually if needed
- `npm start` (w `frontend/`) — start Angular dev server

## Architecture

- **Backend**: Spring Boot 4.0.3, Java 21, Maven single-module project (`backend/`)
- **Frontend**: Angular 21, SSR (`frontend/`)
- **Main class**: `com.chordsandtabs.ProjectApplication`
- **No service layer yet** — controllers talk directly to repositories
- Package layout: `controller/`, `model/`, `dto/`, `repository/`, `security/`, `config/`
- **PostgreSQL** database with **Flyway** migrations (`backend/src/main/resources/db/migration/`)
- **JWT auth** (jjwt 0.12.6), stateless sessions, CSRF disabled
- Public endpoints: `/api/auth/**`, `/error`; everything else requires authentication

## Dev Environment

- DB credentials: `myuser` / `secret`, database `mydatabase` on `localhost:5432`
- `spring-boot-docker-compose` dependency auto-starts `compose.yaml` on `./backend/mvnw spring-boot:run`
- pgAdmin available at `localhost:5050` (admin@email.com / admin)
- `application.yaml` has hardcoded dev credentials — no profile switching yet

## Toolchain Quirks

- **Lombok** is required as annotation processor — `./backend/mvnw clean compile` must succeed before IDE import
- Schema uses double-quoted table names — case-sensitive in Postgres
- Seed data in `V1__init_schema.sql`: roles (`ROLE_USER`, `ROLE_ADMIN`) and 5 instrument types

## Testing

- Backend tests: `./backend/mvnw test`
- Frontend tests: `npm test` (w `frontend/`)
