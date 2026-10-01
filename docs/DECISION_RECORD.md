# Decision record

**Decision:** Make policy eligibility a hard data-flow boundary before retrieval/generation, and keep caller identity solely in Spring.

**Why:** The most consequential failure would be cross-tenant/wrong-role evidence leakage or a document changing caller context. Treating eligibility as a first-class firewall makes that property auditable and testable independently of retrieval quality.

**Alternative rejected:** Send the full policy corpus plus caller metadata to a model and ask it to follow access rules. That makes authorization dependent on prompt obedience and allows ineligible passages to reach generation.

**Limitation:** Relevance/classification is intentionally deterministic and corpus-specific. A broader production corpus would need indexed retrieval and policy taxonomy, but the eligibility firewall would remain ahead of that retrieval step.

**Time spent:** approximately one focused working session for implementation, tests and documentation.

**AI assistance:** AI was used to accelerate scaffolding, test-case brainstorming and documentation review. The implementation decisions, expected behaviors and final verification remain the candidate's responsibility.
