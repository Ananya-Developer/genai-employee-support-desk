$ErrorActionPreference="Stop"
Set-Location "$PSScriptRoot\..\engine"
if (!(Test-Path .venv)) { py -3.12 -m venv .venv }
& .\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
$env:POLICY_CORPUS=(Resolve-Path "..\fixtures\policy-corpus.json")
python -m uvicorn app:app --port 8000
