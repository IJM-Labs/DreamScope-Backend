# DreamScope Backend

Backend til DreamScope – en Spring Boot applikation der håndterer autentificering, AI-fortolkning af drømme, datalagring og sikkerhed.

---

# Funktioner

- Login via magic link / engangskode
- AI-fortolkning af drømme
- Mailudsendelse
- Brugerhåndtering
- Krypteret datalagring
- GDPR-compliant løsning
- Docker deployment

---

# Teknologier

## Backend
- Java
- Spring Boot
- REST API

## Database
- H2
- MySQL

## Infrastruktur
- Docker
- Docker Compose
- Nginx

## Integrationer
- OpenAI
- Resend

---

# Sikkerhed og GDPR

Projektet indeholder:

- AES-256-GCM kryptering af data
- SHA-256 hashing af email
- Magic links med udløb
- Sessionbaseret autentificering
- Permanent datasletning

Følgende data krypteres:

- Email
- Nickname
- Drømme
- AI-fortolkninger

Brugersletning:

```text
DELETE /api/users/me
```

sletter:

- Bruger
- Drømme
- Fortolkninger
- Magic links
- Accepterede vilkår

Generér produktionsnøgle:

```bash
openssl rand -base64 32
```

---

# Miljøvariabler

Projektet understøtter `.env`.

Eksempel:

```bash
export DREAMSCOPE_ENCRYPTION_KEY=""
export OPENAI_API_KEY=""
export RESEND_API_KEY=""
export FRONTEND_URL=""
```

---

# Lokal udvikling

## H2 (standard)

Start backend:

```bash
./mvnw spring-boot:run
```

Database:

```text
jdbc:h2:mem:dreamscope
```

Console:

```text
/h2-console
```

---

## MySQL

Start database:

```bash
cp .env.example .env

docker compose -f compose.dev.yaml up -d
```

Start backend:

```bash
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
```

Indlæs `.env` manuelt hvis nødvendigt:

```bash
set -a
source .env
set +a
```

---

# Docker

Start hele stacken:

```bash
cp .env.example .env

docker compose up --build
```

Åbn:

```text
http://localhost
```

Docker indeholder:

- Frontend
- Backend
- Database
- Nginx

---

# Production

Deploy til DigitalOcean.

Struktur:

```text
DreamScope-Backend/
DreamScope-Frontend/
```

Opsæt miljø:

```bash
cp .env.production.example .env.production
```

Start:

```bash
docker compose \
--env-file .env.production \
-f compose.prod.yaml \
up -d --build
```

Status:

```bash
docker compose ps
```

Backup:

```bash
scripts/backup-mysql.sh
```

---

# API Test

Login:

```http
POST /api/auth/login
```

```json
{
  "email":"test@example.com",
  "nickname":"Night Owl"
}
```

Verificering:

```http
POST /api/auth/verify
```

```json
{
  "code":"123456"
}
```

---

# Test

Projektet følger TDD.

Proces:

1. Skriv test
2. Kør test
3. Implementér
4. Refaktorér

Kør tests:

```bash
./mvnw clean test
```

---

# Team

DreamScope projektgruppe
