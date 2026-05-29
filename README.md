# ChordsAndTabs

A full-stack application for sharing and discovering guitar chords, tabs, tunings, and songs. Built with Spring Boot 4.0 + Java 21 (backend) and Angular 21 (frontend).

## Features

- **Authentication & Authorization** — JWT-based login/registration with two roles: `ROLE_USER` and `ROLE_ADMIN`. Email verification flow included. Admins can moderate all content; regular users can only modify their own.
- **Songs & Artists** — Browse a curated library of songs with artist associations. Filter by name, artist, and release year with pagination.
- **Chords** — Define chord fingerings for any instrument/tuning combination. Fingerings stored as hyphen-separated fret numbers (e.g. `0-3-2-0-1-0` for C major). Custom validation ensures format correctness.
- **Tunings** — Manage instrument tunings (standard, drop D, open G, etc.) scoped to instrument type.
- **Song Chords (Tabs)** — The core entity: a tab/chord sheet for a song. Includes key, tuning, instrument, strumming pattern, time signature, tempo, capo position, and the full song body text. Supports `CHORDS` and `TABS` notation types with `PUBLIC`, `PRIVATE`, or `ARCHIVED` status.
- **Soft Delete** — All resources use `deleted_at` with `@SQLRestriction`, so nothing is ever permanently lost.
- **Caching** — Spring Cache with Caffeine, per-user cache keys to isolate data between users. Caches are automatically evicted on write operations.
- **OpenAPI / Swagger** — Interactive API documentation at `/swagger-ui.html` with JWT bearer token support.
- **Rich Seed Data** — 90+ artists, 130+ songs, basic open chords (C, D, E, Em, F, G, A, Am, Bm, Dm, A7, D7, E7, Fmaj7, Cadd9), and sample tab entries for popular songs.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 4.0.3 |
| Language | Java 21 |
| Frontend | Angular 21, SSR |
| Database | PostgreSQL 16 |
| ORM | Hibernate / JPA |
| Migrations | Flyway |
| Auth | JWT (jjwt 0.12.6), BCrypt |
| Cache | Caffeine |
| API Docs | Springdoc OpenAPI 2.8.5 |
| Validation | Jakarta Validation |
| Build (backend) | Maven |
| Build (frontend) | npm |
| Dev Tools | Lombok, Docker Compose |

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+ and npm
- Docker (for PostgreSQL) or a local PostgreSQL 16 instance

### 1. Configure environment

```bash
cp .env.example .env    # optional — defaults work out of the box
```

The `.env` file is only needed if you want to customize database credentials or SMTP settings. All values have sensible defaults defined in `backend/src/main/resources/application.yaml`.

### 2. Start the database

```bash
cd backend && docker compose up -d
```

Starts PostgreSQL 16 on `localhost:5432` and pgAdmin on `localhost:5050`.

### 3. Start the backend

```bash
cd backend && ./mvnw spring-boot:run
```

The `spring-boot-docker-compose` dependency will auto-start PostgreSQL from `backend/compose.yaml`. The API will be available at `http://localhost:8080`.

### 4. Start the frontend

```bash
cd frontend
npm install    # first time only
npm start      # starts on http://localhost:4200
```

The Angular dev server proxies API requests to `localhost:8080`.

### 5. Access the application

- **Frontend**: [http://localhost:4200](http://localhost:4200)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API base**: `http://localhost:8080/api`
- **pgAdmin**: [http://localhost:5050](http://localhost:5050) (admin@email.com / admin)

### Test accounts

| Email | Password | Role |
|---|---|---|
| `user@test.pl` | `password123` | USER |
| `admin@test.pl` | `password123` | ADMIN |

Seed data (artists, songs, chords, sample tabs) is loaded automatically via Flyway migrations.

## API Overview

All endpoints except `/api/auth/**` and `/swagger-ui/**` require authentication. Include the JWT token as a `Bearer` header:

```
Authorization: Bearer <token>
```

### Auth

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new account |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/auth/verify?token=` | Verify email |

### Artists

| Method | Path | Description |
|---|---|---|
| GET | `/api/artists` | List all artists (visible to user) |
| GET | `/api/artists/{id}` | Get artist by ID |
| POST | `/api/artists` | Create artist |
| PUT | `/api/artists/{id}` | Update artist (owner/admin only) |
| DELETE | `/api/artists/{id}` | Soft-delete artist (owner/admin only) |

### Songs

| Method | Path | Description |
|---|---|---|
| GET | `/api/songs` | List songs (pagination, filter by artist/year/name) |
| GET | `/api/songs/{id}` | Get song by ID |
| POST | `/api/songs` | Create song |
| PUT | `/api/songs/{id}` | Update song (owner/admin only) |
| DELETE | `/api/songs/{id}` | Soft-delete song (owner/admin only) |

### Chords

| Method | Path | Description |
|---|---|---|
| GET | `/api/chords` | List all chords (visible to user) |
| GET | `/api/chords/select?tuningId=&instrumentTypeId=` | Filtered chord list for select widgets |
| POST | `/api/chords` | Create chord |
| DELETE | `/api/chords/{id}` | Soft-delete chord (owner/admin only) |

### Tunings

| Method | Path | Description |
|---|---|---|
| GET | `/api/tunings?instrumentTypeId=` | List tunings, optionally filtered by instrument |
| POST | `/api/tunings` | Create tuning |
| DELETE | `/api/tunings/{id}` | Soft-delete tuning (owner/admin only) |

### Song Chords (Tabs)

| Method | Path | Description |
|---|---|---|
| GET | `/api/songs/{songId}/chords` | List tabs for a song (filter by notation/tuning/instrument) |
| GET | `/api/songs/{songId}/chords/{id}` | Get a specific tab |
| POST | `/api/songs/{songId}/chords` | Create a tab for a song |
| PUT | `/api/songs/{songId}/chords/{id}` | Update tab (owner/admin only) |
| DELETE | `/api/songs/{songId}/chords/{id}` | Soft-delete tab (owner/admin only) |

### Other

| Method | Path | Description |
|---|---|---|
| GET | `/api/instruments` | List instrument types |
| GET | `/api/keys` | List musical keys |
| GET | `/api/notation-types` | List notation types |
| GET/PUT | `/api/profile` | Get/update account profile |

## Project Structure

```
ChordsAndTabs/
├── backend/
│   ├── src/main/java/com/chordsandtabs/
│   │   ├── ProjectApplication.java
│   │   ├── config/           # Security, Cache, OpenAPI, TestDataSeeder
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Request/response records
│   │   ├── exception/        # Global exception handler
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Spring Data repositories
│   │   ├── security/         # JWT filter, util, UserDetailsService
│   │   ├── service/          # CurrentUserService, EmailService
│   │   ├── specification/    # JPA Specifications for dynamic queries
│   │   └── validation/       # Custom validators (ChordFingering)
│   ├── src/main/resources/
│   │   └── db/migration/     # Flyway migrations (V1, V2)
│   ├── compose.yaml           # PostgreSQL + pgAdmin for local dev
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   │   ├── components/       # Angular components
│   │   ├── services/         # API client services
│   │   ├── interceptors/     # HTTP interceptors (auth)
│   │   └── layouts/          # Layout components
│   └── package.json
├── .env                       # Environment variables (dev defaults)
└── AGENTS.md                  # Dev notes for AI assistants
```

## Database

Migrations in `backend/src/main/resources/db/migration/`:
- **V1** — Initial schema (tables, enums, seed roles and instrument types)
- **V2** — Seed data (artists, songs, chords, sample tabs)

## Development

```bash
# Backend

# Run all tests
./backend/mvnw test

# Run a single test class
./backend/mvnw test -Dtest=ClassName

# Compile (triggers Lombok annotation processing)
./backend/mvnw clean compile

# Start backend with dev database
cd backend && ./mvnw spring-boot:run

# Reset database (deletes all data)
cd backend && docker compose down -v && docker compose up -d
```

```bash
# Frontend

# Install dependencies (first time)
cd frontend && npm install

# Start dev server
cd frontend && npm start

# Run tests
cd frontend && npm test
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `mydatabase` | Database name |
| `DB_USER` | `myuser` | Database user |
| `DB_PASSWORD` | `secret` | Database password |
| `JWT_SECRET` | `secret` | JWT signing key |
| `MAIL_HOST` | `sandbox.smtp.mailtrap.io` | SMTP host |
| `MAIL_PORT` | `2525` | SMTP port |
| `MAIL_USER` | `api` | SMTP username |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
