$root=Resolve-Path "$PSScriptRoot\.."
$headers=@{"X-Caller-Id"="atlas-employee-01"}
$body=@{question="What is my annual certification reimbursement limit?";as_of="2026-09-21"}|ConvertTo-Json
Invoke-RestMethod http://127.0.0.1:8080/answer -Method Post -Headers $headers -ContentType "application/json" -Body $body | ConvertTo-Json -Depth 10
