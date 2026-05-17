<#
.SYNOPSIS
  End-to-end smoke: create workflow -> publish -> trigger execution -> trigger webhook.
  Requires: docker compose stack up; uses curl in curlimages/curl on the compose network.
.PARAMETER Network
  Docker network name (default: v1_default for project directory v1).
#>
param(
    [string]$Network = "v1_default"
)

$ErrorActionPreference = "Stop"
$CurlImage = "curlimages/curl:8.5.0"
$wf = "http://workflow-service:8081"
$ex = "http://execution-service:8082"
$tr = "http://trigger-service:8084"

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$createPath = Join-Path $env:TEMP "e2e-create-$ts.json"
$triggerPath = Join-Path $env:TEMP "e2e-trigger-$ts.json"
$webhookPath = Join-Path $env:TEMP "e2e-webhook-$ts.json"

$createBody = (@{ name = "e2e-smoke-$ts"; definitionJson = "{}" } | ConvertTo-Json -Compress)
[System.IO.File]::WriteAllText($createPath, $createBody, [System.Text.UTF8Encoding]::new($false))

Write-Host "== 1) POST $wf/api/workflows"
$createOut = docker run --rm --network $Network -v "${createPath}:/tmp/b.json:ro" $CurlImage -s `
    -X POST "$wf/api/workflows" -H "Content-Type: application/json" --data-binary "@/tmp/b.json"
Write-Host $createOut
$wfObj = $createOut | ConvertFrom-Json
$id = [long]$wfObj.id
if (-not $id) { throw "Create workflow failed: $createOut" }

Write-Host "== 2) POST $wf/api/workflows/$id/publish"
docker run --rm --network $Network $CurlImage -s -f -X POST "$wf/api/workflows/$id/publish" | Write-Host

$idem1 = "e2e-exec-$ts"
$triggerBody = (@{ workflowId = $id; tenantId = "e2e-tenant"; idempotencyKey = $idem1 } | ConvertTo-Json -Compress)
[System.IO.File]::WriteAllText($triggerPath, $triggerBody, [System.Text.UTF8Encoding]::new($false))

Write-Host "== 3) POST $ex/api/executions/trigger"
$execOut = docker run --rm --network $Network -v "${triggerPath}:/tmp/t.json:ro" $CurlImage -s `
    -X POST "$ex/api/executions/trigger" -H "Content-Type: application/json" --data-binary "@/tmp/t.json"
Write-Host $execOut
$null = $execOut | ConvertFrom-Json

$idem2 = "e2e-hook-$ts"
$webhookBody = (@{ workflowId = $id; tenantId = "webhook-tenant"; idempotencyKey = $idem2 } | ConvertTo-Json -Compress)
[System.IO.File]::WriteAllText($webhookPath, $webhookBody, [System.Text.UTF8Encoding]::new($false))

Write-Host "== 4) POST $tr/api/triggers/webhook"
docker run --rm --network $Network -v "${webhookPath}:/tmp/w.json:ro" $CurlImage -s `
    -X POST "$tr/api/triggers/webhook" -H "Content-Type: application/json" --data-binary "@/tmp/w.json" | Write-Host

Write-Host "== 5) GET $ex/api/executions/runs"
docker run --rm --network $Network $CurlImage -s "$ex/api/executions/runs" | Write-Host

Write-Host ""
Write-Host "OK. Published workflow id: $id"
Write-Host "For Docker cron, set in .env next to docker-compose.yml:"
Write-Host "  TRIGGER_CRON_WORKFLOW_ID=$id"
Write-Host "then: docker compose up -d trigger-service"

Remove-Item -Force $createPath, $triggerPath, $webhookPath -ErrorAction SilentlyContinue
