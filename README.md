# MuleTrap

**Spring Boot** service for real-time mule account detection using **vector embeddings** (pgvector + Ollama).

## A. Overview

MuleTrap processes incoming financial transactions, generates a semantic embedding via a local Ollama model, and stores it in PostgreSQL with the pgvector extension. It then applies both vector similarity and rule-based patterns to flag potential money mule accounts.

## B. Pre-requisites

| #   | Tool                   | Purpose                         | Notes                                                     |
| --- | ---------------------- | ------------------------------- | --------------------------------------------------------- |
| 1 | **Java 21**            | Run Spring Boot 3.5.4+          | Ensure `JAVA_HOME` is set correctly.                      |
| 2 | **IntelliJ IDEA**      | Development IDE                 | Community Edition is sufficient.                          |
| 3 | **PostgreSQL (v14+)**  | Database with pgvector support  | Install via Homebrew (see below).                         |
| 4 | **pgvector extension** | Store & query vector embeddings | Install and configure schema (see below).                 |
| 5 | **DBeaver**            | DB GUI                          | Optional but highly useful.                               |
| 6 | **Ollama**             | Local embedding inference       | Pull model and test (see below).                          |
| 7 | **Maven 3.9+**         | Build the project               | IntelliJ bundles Maven; CLI optional.                     |
| 8 | **curl or Postman**    | API testing                     | Test `/api/transactions` and `/api/transactions/similar`. |

---

## C. Setup Instructions

### 1. Install and Start PostgreSQL

```bash
brew install postgresql@17

brew services start postgresql@17

export PATH="/opt/homebrew/opt/postgresql@17/bin:$PATH"
```

**Windows (using Chocolatey):**
```powershell

choco install postgresql
# Ensure psql is on your PATH (restart shell if needed)
```
Alternatively, download and run the Windows installer from https://www.postgresql.org/download/windows/.

### 2. Enable pgvector Extension & Create Schema

Connect as the `postgres` superuser:

```bash
psql postgres
```

Then run:

```sql
-- Enable vector extension globally
CREATE EXTENSION IF NOT EXISTS vector;

-- Create application user and database
CREATE USER muletrapadmin WITH ENCRYPTED PASSWORD 'PROVIDE_YOUR_DB_PASSWORD_HERE';

ALTER ROLE muletrapadmin CREATEDB;

CREATE DATABASE muletrapdb;

GRANT ALL PRIVILEGES ON DATABASE muletrapdb TO muletrapadmin;

-- Connect and set up schema
\c muletrapdb

CREATE SCHEMA muletrapschema;

ALTER SCHEMA muletrapschema OWNER TO muletrapadmin;

GRANT USAGE, CREATE ON SCHEMA muletrapschema TO muletrapadmin;

-- Ensure vector extension in this database
CREATE EXTENSION IF NOT EXISTS vector;

-- Create transactions table
CREATE TABLE muletrapschema.transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  amount NUMERIC,
  channel TEXT,
  time TEXT,
  country TEXT,
  account_age_days INTEGER,
  activity_summary TEXT,
  embedding VECTOR(768),
  is_mule BOOLEAN,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Grant minimal privileges
GRANT SELECT, INSERT, UPDATE ON muletrapschema.transactions TO muletrapadmin;
```

### 3. (Optional) Connect with DBeaver

- Launch DBeaver and create a new PostgreSQL connection using:
  - **Host:** localhost
  - **Database:** muletrapdb
  - **Username:** muletrapadmin
  - **Password:** [Use the password you have given while creating DB Application User]

---

### 4. Install and Test Ollama Embeddings

To install the Ollama command-line tool (macOS):

```bash
brew install ollama
```

*Windows (via Winget):*
```powershell
winget install Ollama.Ollama
```

Verify installation:

```bash
ollama version
```

Pull the local embedding model
```bash
ollama pull nomic-embed-text
```

Test via curl
```bash
curl http://localhost:11434/api/embeddings \
  -d '{"model":"nomic-embed-text","prompt":"Transaction of $900 via mobile at 1:38 AM from Singapore"}'
```

*Windows PowerShell alternative:*
```powershell
Invoke-RestMethod -Uri http://localhost:11434/api/embeddings -Method POST -Body (
  '{"model":"nomic-embed-text","prompt":"Transaction of $900 via mobile at 1:38 AM from Singapore"}'
) -ContentType 'application/json'
```

### 5. Build & Run MuleTrap

```bash
# Build fat jar
mvn clean package

# Run the application
java -jar target/muletrap-0.0.1-SNAPSHOT.jar
```

- Service base path: `http://localhost:8688/api`
- Swagger UI: `http://localhost:8688/api/swagger-ui.html`

---

# Happy coding!

