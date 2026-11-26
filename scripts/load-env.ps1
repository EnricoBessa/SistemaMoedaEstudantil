# Carrega o arquivo .env (na raiz do projeto) como variáveis de ambiente da sessão
# atual do PowerShell. Rode isso ANTES de "mvn spring-boot:run":
#
#   .\scripts\load-env.ps1
#   mvn spring-boot:run
#
# As variáveis ficam valendo só nesta janela do PowerShell (processo atual).
# Feche e reabra o terminal, ou rode o script de novo, se precisar recarregar.

$envFile = Join-Path $PSScriptRoot "..\.env"

if (-not (Test-Path $envFile)) {
    Write-Host "Arquivo .env não encontrado em '$envFile'." -ForegroundColor Yellow
    Write-Host "Copie .env.example para .env e preencha com os valores reais." -ForegroundColor Yellow
    return
}

Get-Content $envFile | ForEach-Object {
    $linha = $_.Trim()
    if ($linha -eq "" -or $linha.StartsWith("#")) { return }

    $partes = $linha.Split("=", 2)
    if ($partes.Length -ne 2) { return }

    $chave = $partes[0].Trim()
    $valor = $partes[1].Trim()
    Set-Item -Path "env:$chave" -Value $valor
}

Write-Host "Variáveis de ambiente carregadas de .env" -ForegroundColor Green
