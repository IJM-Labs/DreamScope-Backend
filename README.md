# DreamScope-Backend

Spring Boot backend til DreamScope, hvor brugere logger ind via engangskode og får AI-fortolket nattedrømme.

## Sikkerhed og GDPR

- Email, nickname, drømmeindhold og AI-fortolkninger gemmes krypteret med AES-256-GCM.
- Email slås op via SHA-256 hash, så databasen kan håndhæve unik email uden at gemme email i klartekst.
- Login-koder gemmes kun som SHA-256 hash og udløber efter 10 minutter.
- `DELETE /api/users/me` sletter bruger, drømme, fortolkninger, magic links og accepterede vilkår via cascade.
- Sæt altid en unik produktionsnøgle i `DREAMSCOPE_ENCRYPTION_KEY`. Den skal være 32 bytes Base64, fx:

```bash
openssl rand -base64 32
```

## Miljøvariabler

Backenden importerer automatisk `.env` fra backend-mappen via `spring.config.import`.

```bash
export DREAMSCOPE_ENCRYPTION_KEY="<base64-32-byte-key>"
export OPENAI_API_KEY="<openai-key>"
export RESEND_API_KEY="<resend-key>"
export FRONTEND_URL="http://localhost"
```

## Database

H2 er standardprofilen og virker uden Docker:

```bash
./mvnw spring-boot:run
```

H2 console er aktiv på `/h2-console` med JDBC URL `jdbc:h2:mem:dreamscope`.

Start kun MySQL-databasen til lokal udvikling:

```bash
cp .env.example .env
docker compose -f compose.dev.yaml up -d
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
```

Hvis du bruger værdierne fra `.env` direkte i terminalen, så indlæs dem først:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Hvis port `3306` allerede er optaget, ret `DB_PORT` og `DB_URL` i `.env`, fx til `3307`.

Start frontend, backend og MySQL i Docker:

```bash
cp .env.example .env
docker compose up --build
```

Åbn derefter frontend via Nginx:

```text
http://localhost
```

Nginx server de statiske frontend-filer og proxyer alle `/api/*` requests videre til Spring Boot-containeren på `app:8080`. Derfor kalder frontenden samme origin i browseren, mens Docker-netværket forbinder videre til backenden.

## Production Docker på DigitalOcean

Til MVP kan DreamScope køres med MySQL i Docker på DigitalOcean. Brug production-filen, så databasen kun er tilgængelig internt i Docker-netværket og gemmer data i et persistent Docker volume.

1. Klon backend og frontend på serveren, så mapperne ligger som søskende:

```text
DreamScope-Backend/
DreamScope-Frontend/
```

2. Opret production environment-filen:

```bash
cp .env.production.example .env.production
```

Udfyld stærke værdier til `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `DREAMSCOPE_ENCRYPTION_KEY`, `OPENAI_API_KEY`, `RESEND_API_KEY` og `FRONTEND_URL`.

3. Start appen:

```bash
docker compose --env-file .env.production -f compose.prod.yaml up -d --build
```

4. Tjek at containerne kører:

```bash
docker compose --env-file .env.production -f compose.prod.yaml ps
```

MySQL publicerer ikke port `3306` i production. Backenden forbinder til databasen via Docker service-navnet `db`.

Lav en database-backup med:

```bash
scripts/backup-mysql.sh
```

Backup-filer gemmes lokalt i `backups/mysql/` og er ignoreret af Git. Kopier dem regelmæssigt væk fra serveren, fx til DigitalOcean Spaces.

## Postman Login

Opret eller login bruger:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "nickname": "Night Owl"
}
```

Hvis mailservice ikke er sat op, printes koden i terminalen:

```text
DreamScope one-time code for test@example.com: 123456
```

Verificer koden:

```http
POST /api/auth/verify
Content-Type: application/json
```

```json
{
  "code": "123456"
}
```

## TDD

Al videre backend-udvikling skal følge test-first:

1. Skriv eller opdater en test for kravet.
2. Kør testen og se den fejle.
3. Implementer mindst mulig kode for at gøre testen grøn.
4. Refaktorer med hele testpakken grøn.

Kør hele pakken med:

```bash
./mvnw clean test
```
