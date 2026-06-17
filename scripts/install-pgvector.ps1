$ErrorActionPreference = "Stop"

$pgRoot = "D:\tool\PostgreSQL\17"
$pgvectorRoot = "D:\tool\pgvector-src\pgvector-0.8.1"
$projectRoot = "D:\JavaMaterial\ai-agent"
$envFile = Join-Path $projectRoot "local-api-keys.txt"

Copy-Item (Join-Path $pgvectorRoot "vector.dll") (Join-Path $pgRoot "lib\vector.dll") -Force
Copy-Item (Join-Path $pgvectorRoot "vector.control") (Join-Path $pgRoot "share\extension\vector.control") -Force
Copy-Item (Join-Path $pgvectorRoot "sql\vector--*.sql") (Join-Path $pgRoot "share\extension") -Force

$headerDir = Join-Path $pgRoot "include\server\extension\vector"
New-Item -ItemType Directory -Force -Path $headerDir | Out-Null
Copy-Item @(
    (Join-Path $pgvectorRoot "src\halfvec.h"),
    (Join-Path $pgvectorRoot "src\sparsevec.h"),
    (Join-Path $pgvectorRoot "src\vector.h")
) $headerDir -Force

$vars = @{}
Get-Content $envFile | ForEach-Object {
    if ($_ -match "^([^=]+)=(.*)$") {
        $vars[$matches[1]] = $matches[2]
    }
}

$env:PGPASSWORD = $vars["SPRING_DATASOURCE_PASSWORD"]
$psql = Join-Path $pgRoot "bin\psql.exe"
& $psql -h localhost -p 5432 -U $vars["SPRING_DATASOURCE_USERNAME"] -d "yun_ai_agent" -v ON_ERROR_STOP=1 -c "CREATE EXTENSION IF NOT EXISTS vector;" | Out-Null

Set-Content -Path (Join-Path $projectRoot "target\pgvector-install.ok") -Value "ok" -Encoding UTF8
