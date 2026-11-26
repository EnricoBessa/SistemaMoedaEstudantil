<div align="center">

# 🪙 Sistema de Moeda Estudantil

**Plataforma de reconhecimento de mérito acadêmico via moeda virtual**, onde professores recompensam alunos por desempenho e os alunos trocam o saldo por vantagens oferecidas por empresas parceiras.

[![App no ar](https://img.shields.io/badge/demo-online-brightgreen)](https://sistemamoedaestudantil-7khl.onrender.com)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![JWT](https://img.shields.io/badge/Auth-JWT%20%2B%20BCrypt-lightgrey)

**[🔗 Ver demo ao vivo](https://sistemamoedaestudantil-7khl.onrender.com)**

</div>

---

## Sobre o projeto

Sistema full-stack que modela um fluxo real de três perfis de usuário — **Professor**, **Aluno** e **Empresa Parceira** — com regras de negócio não triviais: saldo de moedas acumulável por semestre, envio rastreável com motivo obrigatório, resgate de vantagens com geração de cupom único e notificações assíncronas.

Construído do zero com arquitetura em camadas (Controller → Service → Repository), persistência versionada por migrations e autenticação stateless via JWT.

## ✨ Principais funcionalidades

* Cadastro e autenticação de três perfis distintos (Aluno, Professor, Empresa Parceira), com JWT e senhas protegidas por BCrypt — ou login/cadastro em um clique via **Google (OAuth)**, com o cadastro pré-preenchido a partir da conta Google.
* Foto de perfil (avatar) do usuário, com upload de imagem exibido em toda a aplicação.
* Distribuição de moedas por professores, com saldo semestral acumulável (1.000 moedas/semestre) e motivo obrigatório por envio.
* Extrato de transações para aluno e professor.
* Catálogo de vantagens cadastrado por empresas parceiras e resgate pelo aluno, com desconto automático de saldo e geração de código de cupom.
* Notificações internas assíncronas e e-mails reais (SMTP) ao receber moeda e ao resgatar vantagem (evento + listener `@Async`).

## 🏗️ Arquitetura

* **Padrão MVC** com separação clara entre `controllers`, `modelos` (entidades JPA), `repositorios` (Spring Data JPA) e `servicos` (regras de negócio).
* **Autenticação e segurança:** Spring Security + filtro JWT customizado (`JwtAuthenticationFilter`) e hashing de senha com `BCryptPasswordEncoder`.
* **Persistência:** PostgreSQL com schema versionado por **Flyway**, Hibernate apenas validando o mapeamento (`ddl-auto=validate`) — nunca alterando o schema em produção.
* **Eventos assíncronos:** notificações e e-mails (SMTP) desacoplados das transações via `ApplicationEventListener` + `@Async`.
* **Upload de arquivos:** foto de perfil (avatar) enviada via `multipart/form-data`, validada por tipo/tamanho e servida como recurso estático em `/uploads/**`.
* **Login social:** verificação de ID token do Google (Google Identity Services) no backend, com vínculo automático a contas existentes e cadastro assistido para contas novas.
* **Deploy:** containerizado com Docker, publicado no Render com banco Postgres gerenciado e redeploy automático a cada push (CI via GitHub Actions rodando `mvn package`).

### Documentação de arquitetura
* [Diagrama de Classes](./Documentos/DiagramaDeClasse.png)
* [Diagrama de Componentes](./Documentos/DiagramaDeComponente.png)
* [Diagrama Entidade-Relacionamento](./Documentos/Diagrama%20Entidade-Relacionamento.png)
* [Diagrama de Sequência — Envio de Moedas](./Documentos/Diagrama%20de%20Sequência%20—%20Envio%20de%20Moedas.png)
* [Diagrama de Sequência — Resgate de Vantagem](./Documentos/Diagrama%20de%20Sequência%20—%20Resgate%20de%20Vantagem.png)
* [Diagrama de Implantação](./Documentos/Diagrama%20de%20Implantação.png)

## 🛠️ Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot (Web, Data JPA, Security) |
| Autenticação | JWT + BCrypt |
| Banco de dados | PostgreSQL + Flyway |
| Front-end | HTML/CSS/JS estático servido pelo Spring |
| Infraestrutura | Docker, Docker Compose, Render, GitHub Actions |

## 🚀 Rodando localmente

Pré-requisitos: JDK 17+, Maven 3.8+ e Docker Desktop.

```bash
# 1. Subir o banco PostgreSQL
docker compose up -d

# 2. Rodar a aplicação (lê o schema via Flyway automaticamente)
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`, já populada com dados de demonstração por um `DataSeeder` (um aluno, um professor, uma empresa parceira e vantagens de exemplo).

Guia completo de setup, variáveis de ambiente e troubleshooting: [`SETUP.md`](./SETUP.md) e [`deploy-docs/`](./deploy-docs/).

## 🗺️ Roadmap

* [ ] Cobertura de testes automatizados (unitários e de integração).

---

<div align="center">

Desenvolvido por **[seu nome]** · Engenharia de Software — PUC Minas

</div>
