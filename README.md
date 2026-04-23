# circuit-break

A demo workspace showcasing the **Circuit Breaker pattern** in Spring Boot
microservices using **Spring Cloud Circuit Breaker** and **Resilience4j**.
It contains four independent Spring Boot (Java 17) applications that
illustrate two different styles of applying a circuit breaker — the
programmatic `CircuitBreakerFactory` API and the declarative
`@CircuitBreaker` annotation — together with `RestTemplate` and
`OpenFeign` based inter-service calls.

---

## Modules

| Module          | Port | Group                | Role                                                                                  |
| --------------- | ---- | -------------------- | ------------------------------------------------------------------------------------- |
| `album-service` | 8080 | `com.dynamic`        | Consumer that calls `test-album` through a programmatic Resilience4j circuit breaker. |
| `test-album`    | 8585 | `com.example`        | Simple downstream service used by `album-service` to demo fallbacks.                  |
| `loan-service`  | 8000 | `com.circuit-breaker`| Consumer that calls `rate-service` with both `RestTemplate` and OpenFeign, guarded by a `@CircuitBreaker` annotation. |
| `rate-service`  | 9000 | `com.circuit-breaker`| Provider that returns interest-rate data backed by an in-memory H2 database.          |

Each module is a standalone Gradle project with its own `build.gradle`
and `gradlew` wrapper.

---

## Tech Stack

- Java 17
- Spring Boot 3.0.6 / 3.1.0
- Spring Cloud 2022.0.x
- Spring Cloud Circuit Breaker (Resilience4j)
- Spring Cloud OpenFeign
- Spring Data JPA + H2 (in-memory) for `loan-service` and `rate-service`
- Spring Boot Actuator (health + circuit-breaker indicators)
- Lombok
- Gradle (wrapper included)

---

## Prerequisites

- JDK 17+
- Git
- No external database required — the loan and rate services use
  in-memory H2.

---

## Getting Started

Clone the repository:

```bash
git clone <repo-url>
cd circuit-break
```

Each module is built and run from its own folder using its Gradle
wrapper.

### 1. Album-service demo (programmatic circuit breaker)

Start the downstream service first, then the consumer:

```bash
# terminal 1
cd test-album
./gradlew bootRun         # serves http://localhost:8585/test/{name}

# terminal 2
cd album-service
./gradlew bootRun         # serves http://localhost:8080/albums
```

Try it:

```bash
curl http://localhost:8080/albums
# => "Hello himmat"        (when test-album is up)
# => "service not working" (fallback when test-album is down)
```

Stop `test-album` and call `/albums` again to watch the circuit breaker
trip and return the fallback response.

### 2. Loan-service demo (annotation-based circuit breaker + Feign)

```bash
# terminal 1
cd rate-service
./gradlew bootRun         # http://localhost:9000

# terminal 2
cd loan-service
./gradlew bootRun         # http://localhost:8000
```

Endpoints:

- `GET http://localhost:9000/api/rates/{type}` — rate-service, returns
  the interest rate for a given loan type.
- `GET http://localhost:8000/api/rest/loans?type=home` — loan-service
  using `RestTemplate`, guarded by `@CircuitBreaker`.
- `GET http://localhost:8000/api/feign/loans?type=home` — loan-service
  using an OpenFeign client.

Shut down `rate-service` and keep hitting the REST endpoint to see the
circuit open and the fallback (`"Rate service is down. Please try after
sometime"`) kick in.

---

## Circuit-Breaker Configuration

The `loan-service` is configured with an explicit Resilience4j instance
in `loan-service/src/main/resources/application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      loan-service:
        registerHealthIndicator: true
        failureRateThreshold: 50
        minimumNumberOfCalls: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowSize: 10
        slidingWindowType: COUNT_BASED
```

The `album-service` uses the default Resilience4j configuration via
Spring Cloud's `CircuitBreakerFactory`.

---

## Health & Monitoring

Actuator is enabled on both consumer services with the circuit-breaker
health indicator:

- `http://localhost:8080/actuator/health` (album-service)
- `http://localhost:8000/actuator/health` (loan-service)

The H2 console is enabled for the JPA-backed services:

- `http://localhost:8000/h2-console` (loan-service, jdbc url
  `jdbc:h2:mem:cb-loan-db`)
- `http://localhost:9000/h2-console` (rate-service, jdbc url
  `jdbc:h2:mem:cb-rate-db`)

Default credentials: `root` / `123`.

---

## Running Tests

From any module:

```bash
./gradlew test
```

---

## Project Layout

```
circuit-break/
├── album-service/     # programmatic CircuitBreakerFactory demo
├── test-album/        # downstream service for album-service
├── loan-service/      # @CircuitBreaker annotation + Feign demo
├── rate-service/      # JPA-backed rate provider
└── README.md
```

---

## Notes

- Ports are hard-coded in each module's configuration; adjust them in
  `application.yml` / `application.properties` if they clash with
  something already running locally.
- The Feign client in `loan-service` is declared as
  `@FeignClient(name = "iam-service", url = "http://localhost:9000")` —
  update the URL if you move `rate-service`.
