from __future__ import annotations
from datetime import date
from .contracts import EngineQuestion, EngineDocument, DocumentFinding
from .corpus import JsonPolicyCorpus
from .evidence import classify_benefit, select_evidence
from .composer import DeterministicComposer
from .document import decode_payload, read_text, extract

ANNUAL_LIMIT_NOTE="ANNUAL_LIMIT_DOES_NOT_ESTABLISH_REMAINING_BALANCE_ELIGIBILITY_OR_PAYABLE_AMOUNT"

class PolicyQuestionUseCase:
    def __init__(self, corpus: JsonPolicyCorpus, composer: DeterministicComposer):
        self.corpus=corpus; self.composer=composer
    def execute(self, cmd: EngineQuestion):
        on=date.fromisoformat(cmd.context.as_of)
        visible=self.corpus.visible_to(cmd.context.tenant, cmd.context.role, on)
        evidence=select_evidence(visible, classify_benefit(cmd.question))
        return self.composer.compose(evidence)

class ReimbursementDocumentUseCase:
    def __init__(self, questions: PolicyQuestionUseCase): self.questions=questions
    def execute(self, cmd: EngineDocument) -> DocumentFinding:
        text=read_text(cmd.filename, decode_payload(cmd.payload_b64))
        fields, evidence, ambiguous=extract(text)
        issues=["HUMAN_REVIEW_REQUIRED"]
        if ambiguous: issues.append("AMOUNT_AMBIGUOUS")
        elif fields.amount is None: issues.append("AMOUNT_MISSING")
        if fields.reference is None: issues.append("REFERENCE_MISSING")
        policy=None
        if fields.benefit:
            q=EngineQuestion(context=cmd.context, question=f"Policy for {fields.benefit}")
            policy=self.questions.execute(q)
            if policy.status == "INSUFFICIENT_EVIDENCE": issues.append("POLICY_INSUFFICIENT_EVIDENCE")
            elif policy.status == "CONFLICT": issues.append("POLICY_CONFLICT")
            if fields.benefit in {"certification","home-office"}: issues.append(ANNUAL_LIMIT_NOTE)
            if fields.benefit == "training" and "not obtained manager approval" in text.lower():
                issues.append("MANAGER_APPROVAL_NOT_OBTAINED")
        else:
            issues.append("BENEFIT_UNRESOLVED")
        return DocumentFinding(extracted=fields, field_evidence=evidence, policy=policy, issues=issues)
