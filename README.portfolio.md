<div align="center">

# 🪙 Student Coin System

**An academic merit recognition platform built around a virtual currency**, where professors reward students for performance and students redeem their balance for perks offered by partner companies.

[![Live demo](https://img.shields.io/badge/demo-online-brightgreen)](https://sistemamoedaestudantil-7khl.onrender.com)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![JWT](https://img.shields.io/badge/Auth-JWT%20%2B%20BCrypt-lightgrey)

**[🔗 View live demo](https://sistemamoedaestudantil-7khl.onrender.com)**

</div>

---

## About the project

A full-stack system modeling a real three-role workflow — **Professor**, **Student**, and **Partner Company** — with non-trivial business rules: a per-semester cumulative coin balance, traceable coin transfers with a mandatory reason, perk redemption with unique coupon generation, and asynchronous notifications.

Built from scratch with a layered architecture (Controller → Service → Repository), migration-versioned persistence, and stateless JWT authentication.

## ✨ Key features

* Sign-up and authentication for three distinct roles (Student, Professor, Partner Company), with JWT and BCrypt-hashed passwords — or one-click login/sign-up via **Google (OAuth)**, with the sign-up form pre-filled from the Google account.
* User profile picture (avatar) with image upload, shown across the whole application.
* Coin distribution by professors, with a cumulative per-semester balance (1,000 coins/semester) and a mandatory reason for every transfer.
* Transaction history for both students and professors.
* Perk catalog managed by partner companies and redeemed by students, with automatic balance deduction and unique coupon code generation.
* Asynchronous in-app notifications and real e-mails (SMTP) on receiving coins and redeeming a perk (event + `@Async` listener).

## 🏗️ Architecture

* **MVC pattern** with a clear separation between `controllers`, `models` (JPA entities), `repositories` (Spring Data JPA), and `services` (business rules).
* **Authentication & security:** Spring Security + a custom JWT filter (`JwtAuthenticationFilter`) and password hashing with `BCryptPasswordEncoder`.
* **Persistence:** PostgreSQL with a schema versioned by **Flyway**; Hibernate only validates the mapping (`ddl-auto=validate`) — it never alters the schema in production.
* **Asynchronous events:** notifications and e-mails (SMTP) decoupled from transactions via `ApplicationEventListener` + `@Async`.
* **File uploads:** profile picture (avatar) uploaded via `multipart/form-data`, validated by type/size, and served as a static resource at `/uploads/**`.
* **Social login:** server-side verification of Google ID tokens (Google Identity Services), with automatic linking to existing accounts and an assisted sign-up flow for new ones.
* **Deployment:** containerized with Docker, deployed on Render with a managed Postgres database and automatic redeploy on every push (CI via GitHub Actions running `mvn package`).

### Architecture documentation
* [Class Diagram](./Documentos/DiagramaDeClasse.png)
* [Component Diagram](./Documentos/DiagramaDeComponente.png)
* [Entity-Relationship Diagram](./Documentos/Diagrama%20Entidade-Relacionamento.png)
* [Sequence Diagram — Sending Coins](./Documentos/Diagrama%20de%20Sequência%20—%20Envio%20de%20Moedas.png)
* [Sequence Diagram — Redeeming a Perk](./Documentos/Diagrama%20de%20Sequência%20—%20Resgate%20de%20Vantagem.png)
* [Deployment Diagram](./Documentos/Diagrama%20de%20Implantação.png)

## 🛠️ Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot (Web, Data JPA, Security) |
| Authentication | JWT + BCrypt |
| Database | PostgreSQL + Flyway |
| Front-end | Static HTML/CSS/JS served by Spring |
| Infrastructure | Docker, Docker Compose, Render, GitHub Actions |

## 🚀 Running locally

Prerequisites: JDK 17+, Maven 3.8+, and Docker Desktop.

```bash
# 1. Start the PostgreSQL database
docker compose up -d

# 2. Run the application (schema loaded automatically via Flyway)
mvn spring-boot:run
```

The app comes up at `http://localhost:8080`, already seeded with demo data by a `DataSeeder` (one student, one professor, one partner company, and sample perks).

Full setup guide, environment variables, and troubleshooting: [`SETUP.md`](./SETUP.md) and [`deploy-docs/`](./deploy-docs/).

## 🗺️ Roadmap

* [ ] Automated test coverage (unit and integration tests).

---

<div align="center">

Built by **Enrico Bessa** · Software Engineering — PUC Minas

</div>
