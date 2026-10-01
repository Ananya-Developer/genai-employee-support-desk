from __future__ import annotations
from collections import OrderedDict
from .corpus import PolicyPassage

DOMAINS = {
  "certification": ("certification reimbursement",),
  "home-office": ("home-office allowance",),
  "travel": ("rail travel",),
  "training": ("external training", "manager approval"),
  "wellness": ("wellness benefit",),
}

def classify_benefit(text: str) -> str | None:
    s=text.lower()
    if "certification" in s: return "certification"
    if any(x in s for x in ("home-office", "home office", "desk", "chair")): return "home-office"
    if "training" in s: return "training"
    if "rail travel" in s or "business trip" in s: return "travel"
    if "wellness" in s or "gym" in s: return "wellness"
    return None

def select_evidence(visible: tuple[PolicyPassage,...], benefit: str | None) -> tuple[PolicyPassage,...]:
    if benefit is None: return ()
    needles=DOMAINS.get(benefit,())
    chosen=[]
    seen=set()
    for p in visible:
        low=p.text.lower()
        # Prompt-like prose is data, not an instruction, and is not domain evidence.
        if "prompt-injection example" in low: continue
        if any(n in low for n in needles):
            # Exact duplicate records should not create artificial conflict/citation inflation.
            signature=(p.tenant,p.role,p.effective_from,p.effective_to,p.text)
            if signature not in seen:
                chosen.append(p); seen.add(signature)
    return tuple(chosen)
