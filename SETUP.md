# Como rodar o Sistema de Moeda Estudantil

Este guia cobre o que mudou na issue #1 (notificações assíncronas, segurança
JWT/BCrypt e persistência real em Postgres) e como colocar o projeto pra
rodar e testar do zero.

## 1. Pré-requisitos

- **Java 17** (JDK)
- **Maven** (ou use o wrapper `mvnw`/`mvnw.cmd` se o projeto tiver um)
- **Docker Desktop** (só pra rodar o banco Postgres — não precisa instalar
  Postgres "de verdade" no Windows)

## 2. Subir o banco de dados (Postgres via Docker)

Na raiz do projeto:

```bash
docker compose up -d
```

Isso sobe um container Postgres na porta `5432`, já com banco, usuário e
senha configurados (`moedaestudantil` / `moedaestudantil` / `moedaestudantil`).
Os dados ficam salvos num volume Docker, então você pode parar e subir de
novo sem perder nada:

```bash
docker compose down
```

(`down` só para e remove o container; o volume com os dados continua
existindo. Se quiser apagar os dados também, use `docker compose down -v`.)

Pra conferir se o banco subiu certo:

```bash
docker ps
```

Deve aparecer um container chamado `moedaestudantil-db`.

## 3. Rodar a aplicação

Com o banco no ar, na raiz do projeto:

```bash
mvn spring-boot:run
```

Na primeira subida:
1. O **Flyway** roda a migration em `src/back/db/migration/V1__schema_inicial.sql`
   e cria todas as tabelas no Postgres (antes isso era feito "no automático"
   pelo Hibernate com `ddl-auto=update`; agora o schema é versionado em SQL).
2. O **DataSeeder** popula o banco com dados de demonstração (instituições,
   1 professor, 1 aluna, 1 empresa parceira, 2 vantagens e 1 transação),
   mas só na primeira vez — se o banco já tiver dados, ele não roda de novo.

A aplicação sobe em `http://localhost:8080` e o front-end (HTML/JS estático)
é servido na raiz desse mesmo endereço.

### Variáveis de ambiente (opcional)

Por padrão a aplicação já aponta para o Postgres do `docker-compose.yml`
(`localhost:5432`, banco/usuário/senha `moedaestudantil`) e usa uma chave JWT
de desenvolvimento. Pra mudar algo, defina antes de rodar:

| Variável | Para que serve | Padrão |
|---|---|---|
| `DB_HOST` | host do Postgres | `localhost` |
| `DB_PORT` | porta do Postgres | `5432` |
| `DB_NAME` | nome do banco | `moedaestudantil` |
| `DB_USER` | usuário do banco | `moedaestudantil` |
| `DB_PASSWORD` | senha do banco | `moedaestudantil` |
| `JWT_SECRET` | chave usada pra assinar o token JWT | valor de desenvolvimento, **trocar em produção** |
| `JWT_EXPIRACAO_MINUTOS` | validade do token, em minutos | `120` |

## 4. Como testar

### 4.1. Testar pela interface (mais simples)

1. Com o banco e a aplicação no ar, abra `http://localhost:8080` no navegador.
2. Faça login com um dos usuários de demonstração (senha `123456` para todos):
   - **Professor**: e-mail `ricardo.alves@puc.br`
   - **Aluno**: e-mail `camila.duarte@puc.br`
   - **Empresa**: e-mail `contato@ru.com.br`
3. Fluxo pra validar a issue #1 de ponta a ponta:
   - Logue como **professor** e envie moedas para a aluna (CPF `22222222222`).
   - Logue como **aluna** (`camila.duarte@puc.br`) e confira o sininho de
     notificações no topo — deve aparecer a notificação do envio de moedas
     (pode levar um instante, já que agora é processada de forma assíncrona).
   - Resgate uma vantagem como aluna e confira que surge uma segunda
     notificação de resgate.
   - Dê F5 na página — a sessão deve continuar logada (o token JWT fica salvo
     no `localStorage` do navegador).
   - Abra uma aba anônima e tente chamar a API diretamente sem logar (veja
     seção 4.2) — deve ser bloqueado com erro 401/403.

### 4.2. Testar a API diretamente (curl / Postman / Insomnia)

**Login** (pega o token JWT):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"camila.duarte@puc.br\",\"senha\":\"123456\",\"tipo\":\"ALUNO\"}"
```
A resposta traz um campo `token`. Use esse valor nas chamadas seguintes.

**Chamada autenticada** (sem o token deve dar 401/403; com o token deve funcionar):
```bash
curl http://localhost:8080/api/notificacoes/aluno/22222222222 \
  -H "Authorization: Bearer COLE_O_TOKEN_AQUI"
```

**Enviar moedas** (logado como professor) e, em seguida, conferir que a
notificação foi criada (pode levar um instante por ser assíncrono):
```bash
curl -X POST "http://localhost:8080/api/transacoes/enviar?professorCpf=11111111111&alunoCpf=22222222222&quantidade=50&motivo=Teste" \
  -H "Authorization: Bearer TOKEN_DO_PROFESSOR"
```

### 4.3. Conferir o banco diretamente

Com o container no ar:
```bash
docker exec -it moedaestudantil-db psql -U moedaestudantil -d moedaestudantil
```
E dentro do `psql`, por exemplo:
```sql
SELECT * FROM tb_notificacao ORDER BY data_criacao DESC;
SELECT * FROM flyway_schema_history; -- confirma que a migration V1 rodou
```

### 4.4. O que não dá pra testar nesta máquina (sem Java/Maven instalados)

Este ambiente usado para gerar o código não tem Java/Maven instalados, então
o build e os testes acima **não foram executados automaticamente** — rode os
passos deste guia na sua máquina (que tem JDK e Docker) para validar antes de
fechar a issue.

## 5. O que mudou (resumo técnico)

- `TransacaoController` não cria mais a `Notificacao` direto: ele chama
  `TransacaoService`, que publica `MoedaRecebidaEvent` / `VantagemResgatadaEvent`;
  `NotificacaoEventListener` (`@Async @EventListener`) escuta esses eventos e
  cria a notificação numa thread separada.
- Login deixou de comparar senha em texto puro: senhas são salvas com
  `BCryptPasswordEncoder` e o login devolve um JWT (`/api/auth/login`).
  Todas as rotas da API exigem o header `Authorization: Bearer <token>`,
  exceto login e os dois cadastros públicos (aluno/empresa).
- Banco deixou de ser H2 em memória: agora é Postgres (via Docker), com o
  schema versionado por Flyway em `src/back/db/migration/`.
