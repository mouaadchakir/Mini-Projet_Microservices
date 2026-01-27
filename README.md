# Hospital Management Microservices System

This project is a simplified Hospital Management System built using a microservices architecture with Spring Boot and Spring Cloud.

## Microservices

- **patient-service**: Manage patients (create, list, get by id)
- **appointment-service**: Manage appointments linked to patients
- **medical-record-service**: Manage medical records for patients
- **eureka-server**: Service discovery
- **config-server**: Centralized configuration (native file system backend)
- **api-gateway**: Single entry point, routes requests to microservices
- **zipkin-server**: Stub module (can be wired to an external Zipkin instance)

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Cloud 2023.x
- Spring Cloud Config Server
- Spring Cloud Gateway
- Eureka Server / Eureka Client
- OpenFeign
- Resilience4j (via Spring Cloud Circuit Breaker)
- MySQL (separate database per business microservice)

## Ports

- **Config Server**: `8888`
- **Eureka Server**: `8761` (dashboard at http://localhost:8761)
- **API Gateway**: `8080`
- **patient-service**: `8081`
- **appointment-service**: `8082`
- **medical-record-service**: `8083`

## Databases

Create or ensure MySQL is running locally and accessible with user `root` / password `root`.
Each service uses its own schema (auto-created if not present):

- `patient_db`
- `appointment_db`
- `medical_record_db`

Connection URLs are configured centrally in the Config Server configuration files.

## Startup Order

1. **Config Server**
   - Run module `config-server` (class `ConfigServerApplication`).
   - Verify `http://localhost:8888/patient-service/default` returns configuration.

2. **Eureka Server**
   - Run module `eureka-server` (class `EurekaServerApplication`).
   - Open http://localhost:8761 to see registry.

3. **API Gateway and Business Services**
   - Run `patient-service` (`PatientServiceApplication`).
   - Run `appointment-service` (`AppointmentServiceApplication`).
   - Run `medical-record-service` (`MedicalRecordServiceApplication`).
   - Run `api-gateway` (`ApiGatewayApplication`).

All services will fetch configuration from the Config Server (via `bootstrap.yml`) and register with Eureka.

## REST Endpoints (via API Gateway)

Use the gateway as the single entry point (base URL: `http://localhost:8080`).

### Patient Service

- **Create patient**  
  `POST /patients`  
  Body example:
  ```json
  {
    "nom": "Dupont",
    "prenom": "Jean",
    "dateNaissance": "1990-05-10",
    "contact": "0600000000"
  }
  ```

- **Get patient by id**  
  `GET /patients/{id}`

- **List all patients**  
  `GET /patients`

### Appointment Service

- **Create appointment**  
  `POST /appointments`  
  Body example:
  ```json
  {
    "date": "2026-01-27T10:00:00",
    "reason": "Consultation",
    "patientId": 1
  }
  ```

- **Get appointments for a patient**  
  `GET /appointments/patient/{patientId}`

The Appointment Service validates the patient by calling the Patient Service through the API Gateway using OpenFeign.  
If the Patient Service is unavailable, Resilience4j (circuit breaker + retry + timeout) triggers a fallback with the message:

> `"Patient service unavailable, please try later"`

### Medical Record Service

- **Create medical record**  
  `POST /records`  
  Body example:
  ```json
  {
    "patientId": 1,
    "diagnosis": "Flu",
    "description": "High fever and cough",
    "date": "2026-01-27"
  }
  ```

- **Get medical records for a patient**  
  `GET /records/patient/{patientId}`

The Medical Record Service also validates the patient via the Patient Service using Feign and applies the same Resilience4j configuration and fallback message.

## Resilience

- Circuit breaker, retry and timeout configured per-service (see `config-server/src/main/resources/config/*-service.yml`).
- Both Appointment and Medical Record services use Spring Cloud Circuit Breaker with Resilience4j annotations and a common instance name `patientService`.
- Fallback methods return an error indicating: `"Patient service unavailable, please try later"`.

## Service Discovery and Routing

- All business services and the API Gateway register with Eureka (`eureka-server`).
- The API Gateway routes:
  - `/patients/**` → `patient-service`
  - `/appointments/**` → `appointment-service`
  - `/records/**` → `medical-record-service`

## Notes

- Zipkin is provided as a stub module; for full tracing, point your services to a running Zipkin instance and add the appropriate tracing dependencies.
- This project is structured like a production microservices system but simplified for academic purposes.
