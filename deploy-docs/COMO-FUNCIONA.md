# Como o deploy funciona

## Onde a aplicação está hospedada

- **Backend + Frontend (app completa)**: [Render](https://render.com), serviço `SistemaMoedaEstudantil`, rodando via Docker (plano free).
  URL: https://sistemamoedaestudantil-7khl.onrender.com
- **Banco de dados**: Postgres gerenciado pelo Render, serviço `moedaestudantil-db` (plano free).

Por que não GitHub Pages? GitHub Pages só serve arquivos estáticos (HTML/CSS/JS) — não roda uma JVM nem conecta a um banco de dados. Como este projeto é uma aplicação Spring Boot (Java) com API REST e Postgres, precisa de um serviço que rode um servidor de verdade, por isso usamos o Render.

## O que foi configurado

1. **[Dockerfile](../Dockerfile)** na raiz do projeto: builda o `.jar` com Maven e empacota numa imagem Java enxuta. É o que o Render usa para buildar e rodar a aplicação.
2. **[.github/workflows/ci.yml](../.github/workflows/ci.yml)**: workflow do GitHub Actions que roda `mvn package` (build + testes) a cada push ou pull request na branch `main`. Serve só como checagem — não faz deploy.
3. **Variáveis de ambiente no Render** (aba "Environment" do serviço `SistemaMoedaEstudantil`): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (apontando para o Postgres do Render), `JWT_SECRET` e `JWT_EXPIRACAO_MINUTOS`.

## Deploy automático a cada push

O Render está conectado diretamente ao repositório GitHub (`EnricoBessa/SistemaMoedaEstudantil`) e tem **auto-deploy ativado por padrão**. Isso significa:

> Todo `git push` na branch `main` dispara automaticamente um novo build + deploy no Render.

Você não precisa fazer nada manualmente — nem rodar comando, nem clicar em botão. Em paralelo, o GitHub Actions roda o build/testes e avisa (ícone verde/vermelho no commit) se algo quebrou.

## Como subir uma alteração nova

```bash
git add .
git commit -m "sua mensagem aqui"
git push
```

Depois do push:
- Acompanhe o build/testes em: `https://github.com/EnricoBessa/SistemaMoedaEstudantil/actions`
- Acompanhe o deploy em: Render → serviço `SistemaMoedaEstudantil` → aba **Deploys**
- O deploy novo costuma levar de 3 a 5 minutos (build Docker + start da aplicação).

## Limitações do plano gratuito (importante saber)

- **O Web Service "dorme" após inatividade**: se ninguém acessar por um tempo, a próxima requisição pode demorar até ~50 segundos enquanto o serviço "acorda". Isso é normal no plano free.
- **O banco Postgres gratuito expira**: o banco criado tem validade (apareceu aviso de expiração em dezembro/2026 no dashboard). Perto da data, é preciso fazer upgrade para um plano pago ou recriar o banco, senão ele é apagado.
- Veja mais detalhes de diagnóstico em [TROUBLESHOOTING.md](./TROUBLESHOOTING.md).
