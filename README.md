# Employee Support Desk — Evidence-First Reference Implementation

This repository implements the supplied Marlabs assessment as two deliberately separate trust zones:

- **Gateway (Spring Boot)** — the only public API. It owns caller identity, date/manifest validation, duplicate accounting, public response shape, and item-level failure isolation.
- **Evidence engine (Python/FastAPI)** — an internal deterministic analysis service. It reads TXT/text-PDF documents, extracts supported fields with quotes, filters policy evidence, and composes evidence-bound answers.

The design is intentionally evidence-first: **eligibility is decided before relevance, and relevance is decided before generation.** Untrusted document text and prompt-like policy text never control identity or execution.

## Public contract

- `POST /answer`
- `POST /batches`
- Header: `X-Caller-Id`

Spring Swagger: `http://127.0.0.1:8080/docs`
Spring health: `http://127.0.0.1:8080/actuator/health`
Internal Python docs: `http://127.0.0.1:8000/docs`

## Local run (Windows)

### 1. Evidence engine

```powershell
cd engine
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
$env:POLICY_CORPUS=(Resolve-Path "..\fixtures\policy-corpus.json")
python -m uvicorn app:app --port 8000
```

### 2. Gateway

```powershell
cd gateway
mvn spring-boot:run
```

## Tests

```powershell
cd engine
.\.venv\Scripts\activate
pytest

cd ..\gateway
mvn test
```

## `/answer` example

```bash
curl -X POST http://127.0.0.1:8080/answer \
  -H "X-Caller-Id: atlas-employee-01" \
  -H "Content-Type: application/json" \
  -d '{"question":"What is my annual certification reimbursement limit?","as_of":"2026-09-21"}'
```

Expected business outcome: `ANSWERED`, INR 25000, citation `atlas-cert-current`.

## `/batches`

Use multipart/form-data with:
- one JSON part named `metadata` using `fixtures/demo-batch.json`
- repeated `files` parts from `fixtures/inbox/`

The gateway validates exact manifest/file correspondence before processing. Results are emitted in manifest order. Exact byte duplicates remain separate results and use `duplicate_of`. A failed item never aborts later items.

## Important behaviors

- Approved + same tenant + same role + effective interval only.
- Start date inclusive, end date exclusive.
- Draft, future, historical, wrong-role, wrong-tenant passages are filtered before relevance/generation.
- Simultaneously applicable contradictory passages return `CONFLICT`; no precedence is invented.
- Request 03 keeps amount `null` because two different stated amounts exist.
- Request 05 cannot switch the trusted Atlas caller to Boreal.
- Request 06 is retained and marked duplicate of request 01.
- Request 08 fails as an item while the rest of the batch remains present.
- Every item requires human review.
- Annual limits never imply remaining balance, eligibility, or payable amount.

## Deterministic provider double

No paid model or API key is required. `DeterministicComposer` only receives already-eligible evidence. For failure demonstrations set:

- `COMPOSER_BEHAVIOR=timeout`
- `COMPOSER_BEHAVIOR=unavailable`
- `COMPOSER_BEHAVIOR=malformed`

There are no retry loops. Spring uses bounded dependency timeouts.

## Repository map

- `gateway/` — public boundary and batch orchestration
- `engine/triage/` — functional evidence/extraction core
- `fixtures/` — supplied policy corpus and request documents
- `docs/` — architecture, decision record, traceability, production design
- `.github/workflows/ci.yml` — independent Java/Python verification

## Known limitation

The extraction layer intentionally supports only UTF-8 TXT and text-based PDF. It does not OCR scans. Domain classification is deliberately small and deterministic for this supplied corpus; a production version would use versioned retrieval/indexing while preserving the same eligibility firewall.
