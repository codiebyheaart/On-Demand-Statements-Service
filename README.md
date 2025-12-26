# On-Demand Statements Service 📄

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen) ![License](https://img.shields.io/badge/license-MIT-blue)

A production-ready Spring Boot microservice for managing On-Demand Statements with **ODWEK** and **IBM CMOD** integration. This service provides complete CRUD operations, AFP file handling, daily batch ingestion, and REST APIs for statement management.

## 🎯 Features

- ✅ **Complete CRUD Operations** for statement management
- ✅ **Mock ODWEK Integration** simulating IBM CMOD connectivity
- ✅ **AFP File Handling** (using PDF for demo purposes)
- ✅ **Daily Batch Ingestion** with configurable scheduler
- ✅ **RESTful APIs** with comprehensive error handling
- ✅ **H2 In-Memory Database** (easily switchable to PostgreSQL)
- ✅ **Flyway Database Migrations**
- ✅ **File Storage** for statement documents
- ✅ **Comprehensive Logging** with structured output
- ✅ **Health Check Endpoints**

## 🏗️ Architecture

```
ICN/REST Client
      ↓
Statement Controller (REST APIs)
      ↓
Statement Service (Business Logic)
      ↓
┌─────────────┬──────────────┬─────────────┐
│  Statement  │    ODWEK     │    File     │
│  Repository │    Client    │   Storage   │
└─────────────┴──────────────┴─────────────┘
      ↓              ↓               ↓
   H2 Database    IBM CMOD    File System
```

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Git** (optional)
- **Postman** or **curl** for API testing

## 🚀 Quick Start

### 1. Clone or Extract Project

```bash
cd e:\fiverr-2025\ondemand-statements-service
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/ondemand-statements-service.jar
```

### 4. Verify Application is Running

```bash
curl http://localhost:8080/api/statements/health
```

**Expected Response:** `On-Demand Statements Service is running`

## 📚 API Endpoints

### Create Statement
```bash
POST http://localhost:8080/api/statements
Content-Type: application/json

{
  "customerId": "CUST-12345",
  "statementDate": "2024-12-24",
  "documentType": "MONTHLY_STATEMENT"
}
```

### Get Statement by ID
```bash
GET http://localhost:8080/api/statements/{id}
```

### Get Statements by Customer ID
```bash
GET http://localhost:8080/api/statements?customerId=CUST-12345
```

### Update Statement
```bash
PUT http://localhost:8080/api/statements/{id}
Content-Type: application/json

{
  "customerId": "CUST-12345",
  "statementDate": "2024-12-25",
  "documentType": "MONTHLY_STATEMENT"
}
```

### Delete Statement
```bash
DELETE http://localhost:8080/api/statements/{id}
```

### Download Statement PDF
```bash
GET http://localhost:8080/api/statements/{id}/download
```

### Trigger Manual Ingestion
```bash
POST http://localhost:8080/api/statements/ingest?date=2024-12-24
```

## 📖 Complete Documentation

- **[CLIENT_SETUP_GUIDE.md](docs/CLIENT_SETUP_GUIDE.md)** - Phase-wise setup for client machine
- **[API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)** - Detailed API reference with curl examples
- **[TESTING_GUIDE.md](docs/TESTING_GUIDE.md)** - Complete testing scenarios
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System architecture and design

## 🗂️ Project Structure

```
ondemand-statements-service/
├── src/main/java/com/ibm/cmod/ondemand/
│   ├── controller/         # REST Controllers
│   ├── dto/                # Data Transfer Objects
│   ├── entity/             # JPA Entities
│   ├── exception/          # Exception Handling
│   ├── repository/         # Data Access Layer
│   ├── scheduler/          # Batch Job Schedulers
│   ├── service/            # Business Logic
│   │   └── odwek/          # ODWEK Integration
│   └── util/               # Utilities
├── src/main/resources/
│   ├── application.yml     # Configuration
│   └── db/migration/       # Flyway Scripts
├── docs/                   # Documentation
├── postman/                # Postman Collection
└── pom.xml                 # Maven Configuration
```

## ⚙️ Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8080

app:
  storage:
    location: ./storage/afp-files
  
  odwek:
    mock: true
    simulation:
      min-delay-ms: 100
      max-delay-ms: 500
  
  batch:
    ingestion:
      cron: "0 0 2 * * ?"  # Daily at 2 AM
      enabled: true
```

## 🔍 Database Access

H2 Console is available at: **http://localhost:8080/h2-console**

- **JDBC URL:** `jdbc:h2:mem:statementsdb`
- **Username:** `sa`
- **Password:** *(leave blank)*

## 📊 Status Workflow

Statements follow this lifecycle:

`PENDING` → `AVAILABLE` → `ARCHIVED` → `DELETED`

- **PENDING**: Statement created, awaiting AFP ingestion
- **AVAILABLE**: AFP file ready for download
- **ARCHIVED**: Statement archived (not implemented in demo)
- **DELETED**: Soft-deleted statement

## 🧪 Testing

### Using curl (see TESTING_GUIDE.md for complete examples)

```bash
# Create a statement
curl -X POST http://localhost:8080/api/statements \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "statementDate": "2024-12-24",
    "documentType": "MONTHLY_STATEMENT"
  }'
```

### Using Postman

Import the collection from `postman/OnDemandStatements.postman_collection.json`

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.2.1
- **Language:** Java 17
- **Database:** H2 (in-memory)
- **Build Tool:** Maven
- **PDF Generation:** Apache PDFBox
- **Database Migration:** Flyway
- **Logging:** Logback

## 📝 Notes for Production

1. **Replace H2 with PostgreSQL/Oracle** for persistent storage
2. **Integrate actual ODWEK client** instead of mock implementation
3. **Add authentication/authorization** (Spring Security + OAuth2)
4. **Configure external logging** (ELK stack, Splunk)
5. **Add monitoring** (Prometheus, Grafana)
6. **Implement retry logic** with exponential backoff
7. **Add circuit breaker** for ODWEK calls (Resilience4j)

## 🤝 Support

For questions or issues, please contact the development team.

## 📄 License

This project is created for demonstration purposes.

---

**Built with ❤️ using Spring Boot**
