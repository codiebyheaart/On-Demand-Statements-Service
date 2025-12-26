# API Documentation

Complete REST API reference for On-Demand Statements Service.

**Base URL:** `http://localhost:8080`

---

## 📝 Statement API Endpoints

### 1. Create Statement

Create a new statement and trigger AFP ingestion.

**Endpoint:** `POST /api/statements`

**Request Body:**
```json
{
  "customerId": "CUST-12345",
  "statementDate": "2024-12-24",
  "documentType": "MONTHLY_STATEMENT"
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/statements \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "statementDate": "2024-12-24",
    "documentType": "MONTHLY_STATEMENT"
  }'
```

**Windows PowerShell:**
```powershell
curl.exe -X POST http://localhost:8080/api/statements `
  -H "Content-Type: application/json" `
  -d "{\"customerId\": \"CUST-12345\", \"statementDate\": \"2024-12-24\", \"documentType\": \"MONTHLY_STATEMENT\"}"
```

**Response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "statementDate": "2024-12-24",
  "documentPath": "CUST-12345_a1b2c3d4.pdf",
  "documentType": "MONTHLY_STATEMENT",
  "status": "AVAILABLE",
  "fileSizeBytes": 12345,
  "createdAt": "2024-12-25 10:30:00",
  "updatedAt": "2024-12-25 10:30:00",
  "downloadUrl": "/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890/download"
}
```

---

### 2. Get Statement by ID

Retrieve a specific statement by its ID.

**Endpoint:** `GET /api/statements/{id}`

**curl Example:**
```bash
curl http://localhost:8080/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "statementDate": "2024-12-24",
  "documentPath": "CUST-12345_a1b2c3d4.pdf",
  "documentType": "MONTHLY_STATEMENT",
  "status": "AVAILABLE",
  "fileSizeBytes": 12345,
  "createdAt": "2024-12-25 10:30:00",
  "updatedAt": "2024-12-25 10:30:00",
  "downloadUrl": "/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890/download"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2024-12-25T10:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Statement not found with ID: invalid-id",
  "path": "/api/statements/invalid-id"
}
```

---

### 3. Get Statements by Customer ID

Retrieve all statements for a specific customer.

**Endpoint:** `GET /api/statements?customerId={customerId}`

**curl Example:**
```bash
curl "http://localhost:8080/api/statements?customerId=CUST-12345"
```

**Response (200 OK):**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "customerId": "CUST-12345",
    "statementDate": "2024-12-24",
    "status": "AVAILABLE",
    ...
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "customerId": "CUST-12345",
    "statementDate": "2024-11-24",
    "status": "AVAILABLE",
    ...
  }
]
```

---

### 4. Get All Statements

Retrieve all statements in the system.

**Endpoint:** `GET /api/statements`

**curl Example:**
```bash
curl http://localhost:8080/api/statements
```

**Response (200 OK):** Array of all statements

---

### 5. Update Statement

Update statement metadata.

**Endpoint:** `PUT /api/statements/{id}`

**Request Body:**
```json
{
  "customerId": "CUST-54321",
  "statementDate": "2024-12-25",
  "documentType": "QUARTERLY_STATEMENT"
}
```

**curl Example:**
```bash
curl -X PUT http://localhost:8080/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-54321",
    "statementDate": "2024-12-25",
    "documentType": "QUARTERLY_STATEMENT"
  }'
```

**Response (200 OK):** Updated statement object

---

### 6. Delete Statement

Soft-delete a statement (sets status to DELETED).

**Endpoint:** `DELETE /api/statements/{id}`

**curl Example:**
```bash
curl -X DELETE http://localhost:8080/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response (204 No Content)**

---

### 7. Download Statement File

Download the AFP/PDF file for a statement.

**Endpoint:** `GET /api/statements/{id}/download`

**curl Example (save to file):**
```bash
curl http://localhost:8080/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890/download \
  --output statement.pdf
```

**Browser Access:**
```
http://localhost:8080/api/statements/a1b2c3d4-e5f6-7890-abcd-ef1234567890/download
```

**Response:** Binary PDF file (Content-Type: application/pdf)

---

### 8. Trigger Manual Ingestion

Trigger batch ingestion of statements for a specific date.

**Endpoint:** `POST /api/statements/ingest?date={yyyy-MM-dd}`

**curl Example:**
```bash
curl -X POST "http://localhost:8080/api/statements/ingest?date=2024-12-24"
```

**Response (200 OK):**
```json
{
  "totalProcessed": 7,
  "successCount": 7,
  "failureCount": 0,
  "startTime": "2024-12-25 10:00:00",
  "endTime": "2024-12-25 10:00:03",
  "processingTimeMs": 2850,
  "successfulStatements": [
    "uuid-1",
    "uuid-2",
    "uuid-3",
    ...
  ],
  "failures": []
}
```

---

### 9. Health Check

Check if the service is running.

**Endpoint:** `GET /api/statements/health`

**curl Example:**
```bash
curl http://localhost:8080/api/statements/health
```

**Response (200 OK):**
```
On-Demand Statements Service is running
```

---

## ⚠️ Error Responses

### Validation Error (400 Bad Request)
```json
{
  "customerId": "Customer ID is required",
  "statementDate": "Statement date must be in the past"
}
```

### Statement Not Found (404 Not Found)
```json
{
  "timestamp": "2024-12-25T10:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Statement not found with ID: invalid-id",
  "path": "/api/statements/invalid-id"
}
```

### Internal Server Error (500)
```json
{
  "timestamp": "2024-12-25T10:35:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred: ...",
  "path": "/api/statements"
}
```

---

## 📊 Request/Response Examples

### Complete Workflow Example

```bash
# 1. Create a statement
RESPONSE=$(curl -s -X POST http://localhost:8080/api/statements \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-12345", "statementDate": "2024-12-24", "documentType": "MONTHLY_STATEMENT"}')

# Extract ID from response (requires jq)
ID=$(echo $RESPONSE | jq -r '.id')

# 2. Get the statement
curl http://localhost:8080/api/statements/$ID

# 3. Download the PDF
curl http://localhost:8080/api/statements/$ID/download --output statement.pdf

# 4. Update the statement
curl -X PUT http://localhost:8080/api/statements/$ID \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-54321", "statementDate": "2024-12-25", "documentType": "MONTHLY_STATEMENT"}'

# 5. Delete the statement
curl -X DELETE http://localhost:8080/api/statements/$ID
```

---

## 🔐 Future Authentication (Not implemented in demo)

In production, all endpoints will require authentication:

```bash
curl http://localhost:8080/api/statements \
  -H "Authorization: Bearer {access_token}"
```

---

## 📝 Notes

- All dates must be in **ISO 8601 format** (`yyyy-MM-dd`)
- Statement dates must be **in the past**
- Customer IDs are **case-sensitive**
- File downloads return **application/pdf** content type
- Batch ingestion processes **5-10 random statements** for demo purposes

---

## 🛠️ Tools for Testing

### Recommended Tools:
1. **Postman** - Import collection from `postman/` directory
2. **curl** - Command-line testing (examples above)
3. **Browser** - For download and health endpoints
4. **H2 Console** - Database verification

### Postman Collection:
Import `postman/OnDemandStatements.postman_collection.json` for pre-configured requests.
