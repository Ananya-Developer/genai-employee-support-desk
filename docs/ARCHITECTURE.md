# Architecture

## Trust boundary

```text
Caller
  -> Spring public gateway
       -> resolve X-Caller-Id from trusted directory
       -> validate date / manifest
       -> compute exact-byte duplicate linkage
       -> internal command containing trusted tenant + role
            -> Python evidence engine
                 -> decode/read document
                 -> extract fields + quotations
                 -> eligibility firewall
                 -> benefit relevance
                 -> deterministic composer
       <- bounded result
  <- public response
```

The Python engine cannot derive tenant or role from document text. The document command already contains the trusted context created by Spring.

## Why this shape

The public service is an application boundary, not a thin proxy. Batch semantics, identity, validation, ordering, duplicate bookkeeping and public error semantics stay in Spring. Python is a replaceable evidence-analysis engine with a functional core.

## Evidence firewall

`JsonPolicyCorpus.visible_to()` is the only path from stored corpus to downstream selection. It filters by tenant, role, approval state and effective interval. `select_evidence()` runs only on the filtered tuple. The composer therefore never receives an ineligible passage.
