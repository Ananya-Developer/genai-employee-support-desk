# Requirement traceability

| Assessment requirement | Implementation |
|---|---|
| Spring public API / Python extraction-retrieval-generation | `gateway/http/PublicApi`, `gateway/integration/TriageEngineGateway`, `engine/triage/*` |
| Trusted caller header | `CallerDirectory` |
| Strict `as_of` | `DateGate` |
| Approved tenant/role/date policy filtering | `JsonPolicyCorpus.visible_to` |
| ANSWERED / INSUFFICIENT_EVIDENCE / CONFLICT + verbatim citations | `DeterministicComposer` |
| TXT/text-PDF extraction + quotes | `document.py` |
| Missing/ambiguous values not guessed | `document.extract`, `ReimbursementDocumentUseCase` |
| Human review + annual-limit limitation | `ReimbursementDocumentUseCase` and gateway result shape |
| Exact duplicate retained | `FingerprintBook`, `RunReimbursementBatch` |
| Item failure isolation | per-entry try/catch in `RunReimbursementBatch` |
| Prompt injection / wrong tenant cannot change context | `CallerDirectory` + `TrustedContext` + evidence firewall |
| Records outside application logic | `fixtures/policy-corpus.json` |
| Reorder/duplicate stability | `test_reordering_and_exact_duplication_do_not_change_outcome` |
| Provider failure modes | `DeterministicComposer` fault behavior + FastAPI error mapping |
| Bounded timeouts / no retries | `TriageEngineGateway`; no retry loop configured |
| Demonstration and reproducibility | Swagger, README, PowerShell script, fixtures, CI |
