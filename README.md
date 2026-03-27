# Trouble Ticket API

REST API zgodne z kontraktem OpenAPI. Spring Boot 3, Java 21, PostgreSQL.

---

## Uruchomienie

Wymagania: **Docker Desktop 4.x+**

```bash
git clone https://github.com/[login]/trouble-ticket-api.git
cd trouble-ticket-api
docker-compose up --build
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

Zatrzymanie (dane zostają)
```bash
   docker-compose down
```

Zatrzymanie z usunięciem danych
```bash
   docker-compose down -v
```

---

## Autoryzacja

API wymaga tokenu JWT w nagłówku `Authorization: Bearer <token>`.

### Generowanie tokenu testowego

```bash
   ./mvnw exec:java '-Dexec.mainClass=pl.netia.troubleticket.security.JwtTestTokenGenerator'
```

Alternatywnie: uruchomienie `JwtTestTokenGenerator.main()` z poziomu IDE.

### Użycie w Swagger UI

1. Otwórz Swagger UI
2. Kliknij **Authorize** 🔒
3. Wklej token (bez prefixu `Bearer`)
4. **Authorize → Close**

---

## Testy

Wszystkie (wymaga Dockera — Testcontainers)
```bash
   ./mvnw test
```

Tylko unit testy (bez Dockera)
```bash
   ./mvnw test -Dtest=TroubleTicketServiceTest
```

Tylko integracyjne
```bash
   ./mvnw test -Dtest=TroubleTicketControllerTest
```

---

## Architektura

### Podejście API-first

Kontrakt `trouble-ticket-api.yaml` stanowi główne źrodło informacji.
Interfejsy kontrolerów i modele DTO są generowane z niego przy każdej kompilacji — niezgodność implementacji z kontraktem skutkuje błędem kompilacji.

```
trouble-ticket-api.yaml
        ↓  OpenAPI Generator
TroubleTicketApi.java (interfejs)
        ↓  implementuje
TroubleTicketController
        ↓  wywołuje
TroubleTicketService
        ↓  używa
TroubleTicketRepository → PostgreSQL
        ↓  mapuje
TroubleTicketMapper (MapStruct)
        ↓  zwraca
TroubleTicket DTO
```

### Struktura pakietów

```
src/main/java/pl/netia/troubleticket/
├── config/        # konfiguracja Spring (Security, OpenAPI)
├── controller/    # warstwa HTTP — bez logiki biznesowej
├── entity/        # encje JPA
├── exception/     # GlobalExceptionHandler
├── mapper/        # Entity ↔ DTO (MapStruct)
├── repository/    # Spring Data
├── security/      # JWT, TenantContext
└── service/       # logika biznesowa
```

---

## Decyzje projektowe

### 1. Separacja warstwy API od warstwy danych

Encja posiada własny enum `TicketStatus`, niezależny od wygenerowanego `TroubleTicketStatus`. Zmiana kontraktu OpenAPI nie wymusza tym samym migracji bazy danych.

Alternatywą było użycie `TroubleTicketStatus` bezpośrednio w encji — mniej kodu, ale kontrakt publiczny zacząłby sterować schematem bazy.

### 2. Tenant isolation

`tenantId` jest wyciągany z JWT i dołączany do każdego zapytania. Zasób należący do innego tenanta zwraca `404` zamiast `403` — celowo nie ujawniamy istnienia zasobu poza scopem tenanta.

Dodatkowe zabezpieczenie stanowi constraint `UNIQUE(tenant_id, external_id)` na poziomie bazy danych.

### 3. Idempotentny POST

Para `(tenantId, externalId)` pełni rolę klucza biznesowego. Powtórne wywołanie `POST /troubleTicket` z tym samym `externalId` zwraca `200` z istniejącym rekordem zamiast tworzyć duplikat. Nowy rekord otrzymuje `201`.

### 4. Flyway zamiast Hibernate auto-DDL

Schemat bazy jest wersjonowany przez Flyway. Hibernate działa w trybie `validate` — weryfikuje zgodność encji ze schematem, ale go nie modyfikuje.

`hibernate.ddl-auto=update` w środowisku produkcyjnym niesie ryzyko nieodwracalnych zmian schematu. Flyway zapewnia pełną kontrolę i audytowalność migracji.

### 5. Testcontainers zamiast H2

Testy integracyjne uruchamiają PostgreSQL przez Testcontainers, eliminując rozbieżności między H2 a PostgreSQL w zakresie constraintów, typów danych i sekwencji.


### 6. Multi-stage Docker build

```
Stage 1 (builder):  maven:3.9-eclipse-temurin-21  →  kompilacja + JAR
Stage 2 (runtime):  eclipse-temurin:21-jre         →  tylko JRE + JAR
```

Obraz produkcyjny nie zawiera JDK ani Mavena — mniejszy rozmiar, lepsze security.

---

## Założenia i znane ograniczenia

### Walidacja `serviceId`

Kontrakt przewiduje `404 SERVICE_NOT_FOUND`, ale nie dostarcza rejestru usług. Walidacja ogranicza się do sprawdzenia formatu (`serviceId > 0`).

### Status po utworzeniu

Nowe zgłoszenie automatycznie otrzymuje status `acknowledged` — symulacja natychmiastowego przyjęcia przez system obsługi zgłoszeń. Klient wysyła `new`, otrzymuje `acknowledged`, zgodnie z przykładem w kontrakcie.

### Location header

Definicja headera `Location` w kontrakcie YAML powoduje NPE w OpenAPI Generator 7.12.0 (błąd w `OpenAPINormalizer` przy typie `uri-reference`). Definicja została usunięta z YAML — header jest zwracany poprawnie przez `ResponseEntity.created(URI)` w kontrolerze.

### Brak paginacji

`GET /troubleTicket` nie wspiera paginacji — zgodnie z kontraktem. Przy dużej liczbie zgłoszeń może to być problematyczne. Do uwzględnienia w v2.

### JWT — klucz symetryczny

Weryfikacja tokenu JWT opiera się na kluczu HMAC SHA-256. W środowisku produkcyjnym należałoby zastosować klucz asymetryczny (RSA) i dedykowany serwer autoryzacji (np. Keycloak). Na potrzeby tego zadania klucz symetryczny jest wystarczający.

### Sekret JWT w konfiguracji

Sekret JWT znajduje się w `application.yml` i `docker-compose.yml`. Produkcyjnie powinien być dostarczany przez vault lub zewnętrzny secret manager.