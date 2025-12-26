# Client Setup Guide - Phase-Wise Instructions

This guide provides step-by-step instructions for setting up and deploying the On-Demand Statements Service on the client machine.

## 📋 Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Windows 11** (or Windows 10)
- [ ] **Java Development Kit (JDK) 17** or higher
- [ ] **Maven 3.8+** installed and added to PATH
- [ ] **Git** (optional, for version control)
- [ ] **Postman** or **curl** for API testing
- [ ] **IntelliJ IDEA** or **VS Code** (optional, for code review)
- [ ] **Minimum 4GB RAM** available
- [ ] **Administrator privileges** on the machine

---

## Phase 1: Environment Setup (30 minutes)

### Step 1.1: Verify Java Installation

Open PowerShell or Command Prompt and run:

```bash
java -version
```

**Expected Output:**
```
openjdk version "17.0.x" or higher
```

If Java is not installed:
1. Download JDK 17 from [Adoptium](https://adoptium.net/)
2. Install with default settings
3. Add `JAVA_HOME` to environment variables
4. Verify installation again

### Step 1.2: Verify Maven Installation

```bash
mvn -version
```

**Expected Output:**
```
Apache Maven 3.8.x or higher
```

If Maven is not installed:
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract to `C:\Program Files\Apache\maven`
3. Add `MAVEN_HOME` and update PATH
4. Verify installation again

### Step 1.3: Extract Project Files

1. Copy the project folder to: `C:\Projects\ondemand-statements-service`
2. Ensure all files are extracted completely

---

## Phase 2: Project Build (15 minutes)

### Step 2.1: Navigate to Project Directory

```bash
cd C:\Projects\ondemand-statements-service
```

### Step 2.2: Clean and Build Project

```bash
mvn clean install
```

**What this does:**
- Downloads all required dependencies
- Compiles the source code
- Runs Flyway database migrations
- Packages the application as a JAR file

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2-3 minutes
```

**Troubleshooting:**
- If build fails, check internet connectivity (Maven needs to download dependencies)
- Ensure no firewall is blocking Maven Central repository
- Delete `~/.m2/repository` folder and rebuild if dependency issues persist

### Step 2.3: Verify JAR File Creation

Check that the JAR file exists:

```bash
dir target\ondemand-statements-service.jar
```

---

## Phase 3: Application Startup (10 minutes)

### Step 3.1: Start the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using JAR File**
```bash
java -jar target\ondemand-statements-service.jar
```

### Step 3.2: Verify Application Started Successfully

Watch the console logs for:

```
Started OnDemandStatementsApplication in X.XXX seconds
```

The application should be running on: **http://localhost:8080**

### Step 3.3: Test Health Endpoint

Open a new terminal and run:

```bash
curl http://localhost:8080/api/statements/health
```

**Expected Response:**
```
On-Demand Statements Service is running
```

---

## Phase 4: Database Verification (10 minutes)

### Step 4.1: Access H2 Console

1. Open browser and navigate to: **http://localhost:8080/h2-console**
2. Use these credentials:
   - **JDBC URL:** `jdbc:h2:mem:statementsdb`
   - **Username:** `sa`
   - **Password:** *(leave blank)*
3. Click **Connect**

### Step 4.2: Verify Database Schema

Run this SQL query:

```sql
SELECT * FROM statements;
```

**Expected Result:** Empty table with columns:
- id
- customer_id
- statement_date
- document_path
- document_type
- status
- file_size_bytes
- created_at
- updated_at
- version

---

## Phase 5: API Testing (30 minutes)

### Step 5.1: Test CREATE Endpoint

```bash
curl -X POST http://localhost:8080/api/statements ^
  -H "Content-Type: application/json" ^
  -d "{\"customerId\": \"CUST-12345\", \"statementDate\": \"2024-12-24\", \"documentType\": \"MONTHLY_STATEMENT\"}"
```

**Expected Response (200 Created):**
```json
{
  "id": "uuid-here",
  "customerId": "CUST-12345",
  "statementDate": "2024-12-24",
  "status": "AVAILABLE",
  "downloadUrl": "/api/statements/{id}/download"
}
```

**Copy the `id` value for next steps.**

### Step 5.2: Test READ Endpoint

```bash
curl http://localhost:8080/api/statements/{paste-id-here}
```

### Step 5.3: Test DOWNLOAD Endpoint

Open browser and navigate to:
```
http://localhost:8080/api/statements/{paste-id-here}/download
```

A PDF file should download automatically.

### Step 5.4: Test BATCH INGESTION Endpoint

```bash
curl -X POST "http://localhost:8080/api/statements/ingest?date=2024-12-24"
```

**Expected Response:**
```json
{
  "totalProcessed": 7,
  "successCount": 7,
  "failureCount": 0,
  "processingTimeMs": 2500
}
```

### Step 5.5: Verify Statements Created

```bash
curl http://localhost:8080/api/statements
```

You should see multiple statements created by the batch ingestion.

---

## Phase 6: Postman Collection Import (15 minutes)

### Step 6.1: Install Postman

1. Download from [Postman](https://www.postman.com/downloads/)
2. Install and launch

### Step 6.2: Import Collection

1. Click **Import** button
2. Select file: `C:\Projects\ondemand-statements-service\postman\OnDemandStatements.postman_collection.json`
3. Click **Import**

### Step 6.3: Run Collection Tests

1. Select **On-Demand Statements** collection
2. Click **Run** button
3. Click **Run On-Demand Statements**
4. Verify all tests pass ✅

---

## Phase 7: Configuration Customization (Optional)

### Modify Application Port

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 9090  # Change from 8080
```

### Modify File Storage Location

```yaml
app:
  storage:
    location: C:\StatementFiles\afp-files
```

### Modify Batch Job Schedule

```yaml
app:
  batch:
    ingestion:
      cron: "0 0 3 * * ?"  # Run at 3 AM instead of 2 AM
```

After changes, rebuild and restart:
```bash
mvn clean install
mvn spring-boot:run
```

---

## Phase 8: Production Deployment Considerations

### For Production Environment:

1. **Switch to PostgreSQL Database**
   - Add PostgreSQL driver to `pom.xml`
   - Update `application.yml` with PostgreSQL connection details

2. **Disable H2 Console**
   ```yaml
   spring:
     h2:
       console:
         enabled: false
   ```

3. **Configure External ODWEK**
   - Replace `MockODWEKClientImpl` with actual ODWEK client
   - Add ODWEK connection properties

4. **Add Security**
   - Implement Spring Security
   - Add authentication/authorization

5. **External Logging**
   - Configure log aggregation (ELK, Splunk)

6. **Monitoring**
   - Add actuator endpoints
   - Configure Prometheus/Grafana

---

## 🔧 Troubleshooting

### Issue: Port 8080 already in use

**Solution:**
```bash
# Kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <process-id> /F
```

### Issue: Maven build fails with "Cannot resolve dependencies"

**Solution:**
```bash
# Clear Maven cache
rmdir /s /q %USERPROFILE%\.m2\repository
mvn clean install
```

### Issue: Application fails to start

**Check:**
1. Java version is 17+
2. No other application using port 8080
3. Sufficient disk space for file storage
4. Check application logs in `logs/application.log`

---

## 📞 Support Contacts

For technical issues or questions:
- **Email:** support@example.com
- **Phone:** +1-XXX-XXX-XXXX

---

## ✅ Success Criteria

At the end of setup, you should be able to:

- ✅ Start application successfully
- ✅ Access H2 console
- ✅ Create statements via API
- ✅ Download statement PDFs
- ✅ Run batch ingestion
- ✅ View statements in database
- ✅ Run Postman collection successfully

---

**Estimated Total Setup Time:** 2 hours

**Client Machine Ready for Demo! 🎉**
