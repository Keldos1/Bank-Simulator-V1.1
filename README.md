# Bank Simulator

A full-stack banking simulator built with **Java 21, Spring Boot, React, PostgreSQL, and Docker**.

This educational project demonstrates backend development, REST API design, database transactions, authentication, and integration with a web frontend.

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Gradle

**Frontend**
- React
- TypeScript
- Vite
- Nginx

**Infrastructure**
- Docker
- Docker Compose
- Git & GitHub

## Features

- User registration and authentication
- Bank account management
- Account balance tracking
- Money deposits and withdrawals
- Money transfers using recipient account numbers
- Transaction history with pagination
- Account and user status validation
- Transactional operations using Spring `@Transactional`
- REST API integration with a React frontend

## Project Structure

```text
bank-simulator/
├── backend/          # Spring Boot REST API
├── frontend/         # React + TypeScript application
├── compose.yaml      # Docker Compose configuration
├── .env.example      # Environment configuration example
└── README.md
```

## Getting Started

### Requirements

- Docker
- Docker Compose

### Run with Docker

1. Clone the repository:

```bash
git clone https://github.com/Keldos1/bank-simulator.git
cd bank-simulator
```

2. Create a `.env` file based on `.env.example` and set a PostgreSQL password.

3. Start the application:

```bash
docker compose up -d --build
```

4. Open the application:

**Frontend:** http://localhost:3000

**Backend:** http://localhost:8081

## Banking Operations

The application supports three main banking operations:

**Deposit** — add money to an account.

**Withdrawal** — withdraw funds after checking the account balance and account status.

**Transfer** — transfer money between accounts using the recipient's account number.

Spring transactions help ensure that database changes are committed together or rolled back when an operation fails.

## Security

The project uses Spring Security for authentication and access control.

Account ownership checks are implemented for withdrawals, transfers, and transaction history. Authorization checks for all operations are still being reviewed.

## Future Improvements
- **Multi-currency Accounts** — support KZT, USD, and EUR accounts.
- **Currency Exchange** — implement transfers with automatic currency conversion.
- **Advanced Transaction History** — add filtering by date, transaction type, and amount.
- **Account Statements** — generate downloadable PDF statements.
- **Notifications** — notify users about successful transactions and account activity.
- **Database Migrations** — add database version control using Flyway or Liquibase.

## Author

**Keldos**

GitHub: https://github.com/Keldos1

---

*This project is intended for educational and portfolio purposes, not real banking operations.*
