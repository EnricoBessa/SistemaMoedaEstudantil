# Problemas comuns e como resolver

## "Not Found" ao abrir a URL do site

Causas mais comuns, nessa ordem:

1. **URL digitada errada.** Confira letra por letra no Render (Web Service → topo da página, link abaixo do nome do commit). É fácil confundir `l` (L minúsculo) com `1` (número um) no subdomínio gerado automaticamente.
2. **Deploy ainda em andamento ou falhou.** Vá em Render → `SistemaMoedaEstudantil` → aba **Deploys** e veja o status do item mais recente (topo da lista). Se tiver um X vermelho, clique nele para ver o log do erro.
3. **Serviço dormindo (plano free).** Se a última requisição foi há muito tempo, a primeira tentativa pode demorar ~50s — tente recarregar a página depois de esperar um pouco.

Se quiser confirmar via terminal o que o servidor está respondendo:
```bash
curl -i https://sistemamoedaestudantil-7khl.onrender.com/
```
Se aparecer o cabeçalho `x-render-routing: no-server`, o erro é do Render (nenhuma instância respondendo) — não é um bug da aplicação.

## Deploy falhou com erro de conexão com o banco (`Connection to localhost:5432 refused`)

Isso significa que as variáveis de ambiente `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` não estão definidas (ou foram apagadas) no Render, então a aplicação caiu no valor padrão `localhost`, que não existe no ambiente do Render.

Como corrigir:
1. Render → serviço `moedaestudantil-db` → aba **Info** → seção **Connections**: copie `Hostname`, `Port`, `Database`, `Username` e `Password` (clique no ícone de olho para revelar a senha).
2. Render → serviço `SistemaMoedaEstudantil` → aba **Environment**: adicione/confira as 5 variáveis acima, mais `JWT_SECRET` e `JWT_EXPIRACAO_MINUTOS=120`.
3. Clique em **Save Changes** — isso já dispara um novo deploy automaticamente.

## Como ver o log de um deploy

Render → `SistemaMoedaEstudantil` → aba **Deploys** → clique no deploy que quer investigar → o log aparece embaixo, com scroll. O final do log costuma mostrar a causa real do erro (stack trace do Java) ou, se deu certo, uma linha como:
```
==> Your service is live 🎉
```

## O build quebrou no GitHub Actions (ícone vermelho no commit)

Isso significa que `mvn package` falhou — geralmente erro de compilação ou teste quebrado. Veja o log completo em:
`https://github.com/EnricoBessa/SistemaMoedaEstudantil/actions`

Isso **não impede** o Render de tentar fazer deploy mesmo assim (são processos independentes) — por isso vale sempre conferir os dois: Actions (qualidade do código) e Render (se o site está no ar).

## O banco gratuito do Render expirou / sumiu

Plano free do Postgres no Render tem data de expiração (visível no dashboard do banco, seção de avisos no topo). Quando expira, os dados são apagados. Para manter o banco permanentemente é preciso fazer upgrade para um plano pago, ou recriar o banco e repetir a configuração de variáveis de ambiente descrita acima.

## Quero testar uma alteração localmente antes de subir

Veja o [SETUP.md](../SETUP.md) na raiz do projeto — ele explica como rodar a aplicação e o banco Postgres localmente via Docker, antes de dar `git push`.
