# Financia

Sistema de gestão financeira pessoal: Angular 17 + Spring Boot 3 + MySQL 8.

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8 em `localhost:3306` (database `financia`, user/senha padrão `root`/`root`)

## Backend

```bash
cd backend
netstat -ano | findstr :8080
taskkill /PID 21544 /F
cd /d/repos/Financia/app-financia/backend
export DB_USER=root
export DB_PASSWORD='<password>>'
mvn spring-boot:run
```

API em `http://localhost:8080/api`.

## Frontend

```bash
cd frontend
npm install
npm start
```

App em `http://localhost:4200`. A rota inicial é o login.

## Validação

Os 8 cenários end-to-end estão em `specs/001-finance-management/quickstart.md`.
