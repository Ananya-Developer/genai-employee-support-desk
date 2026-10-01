from __future__ import annotations
import json
from dataclasses import dataclass
from datetime import date
from pathlib import Path

@dataclass(frozen=True)
class PolicyPassage:
    id: str; tenant: str; role: str; approval_state: str
    effective_from: date; effective_to: date; text: str

class JsonPolicyCorpus:
    def __init__(self, path: str):
        raw=json.loads(Path(path).read_text(encoding="utf-8"))
        self._passages=tuple(PolicyPassage(
            id=x["id"], tenant=x["tenant"], role=x["role"], approval_state=x["approval_state"],
            effective_from=date.fromisoformat(x["effective_from"]), effective_to=date.fromisoformat(x["effective_to"]), text=x["text"]
        ) for x in raw)

    def visible_to(self, tenant: str, role: str, on: date) -> tuple[PolicyPassage,...]:
        # Security gate: nothing ineligible leaves this method.
        return tuple(p for p in self._passages
            if p.tenant == tenant and p.role == role and p.approval_state == "Approved"
            and p.effective_from <= on < p.effective_to)
