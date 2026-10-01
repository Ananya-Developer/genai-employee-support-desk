from __future__ import annotations
import os
from .contracts import Citation, PolicyFinding
from .failures import ProviderTimeout, ProviderUnavailable, ProviderProtocolError
from .corpus import PolicyPassage

class DeterministicComposer:
    """Offline model double. It can only compose from already-filtered evidence."""
    def __init__(self, behavior: str | None=None):
        self.behavior=behavior or os.getenv("COMPOSER_BEHAVIOR","normal")

    def compose(self, evidence: tuple[PolicyPassage,...]) -> PolicyFinding:
        if self.behavior == "timeout": raise ProviderTimeout()
        if self.behavior == "unavailable": raise ProviderUnavailable()
        if self.behavior == "malformed": raise ProviderProtocolError()
        if not evidence:
            return PolicyFinding(status="INSUFFICIENT_EVIDENCE", answer=None, citations=[])
        citations=[Citation(chunk_id=p.id, quote=p.text) for p in evidence]
        assertions={p.text for p in evidence}
        if len(assertions) > 1:
            return PolicyFinding(status="CONFLICT", answer=None, citations=citations)
        return PolicyFinding(status="ANSWERED", answer=evidence[0].text, citations=citations)
