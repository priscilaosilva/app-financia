# Como executar o Financia

Ambiente de desenvolvimento local. **Não** coloque senha real de banco neste arquivo — use variáveis de ambiente na sua máquina.

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8 em `localhost:3306`, database `financia`

## API (Spring Boot)

A partir da pasta `backend/` (o plugin `spring-boot` não resolve na raiz do repositório):

```bash
cd backend
mvn spring-boot:run
```

A API fica em `http://localhost:8080/api`. Credenciais de banco via `DB_USER` e `DB_PASSWORD` no ambiente, ou o padrão do `application` local.

## Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

O app fica em `http://localhost:4200`. A rota inicial é o login; o proxy `/api` aponta para a API na porta 8080.

## Portas

| Serviço   | Porta |
|-----------|-------|
| API       | 8080  |
| Angular   | 4200  |
| MySQL     | 3306  |
