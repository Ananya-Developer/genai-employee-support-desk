from triage.contracts import TrustedContext, EngineQuestion
from triage.composer import DeterministicComposer
from triage.corpus import JsonPolicyCorpus
from triage.usecases import PolicyQuestionUseCase
from pathlib import Path
import json

def ask(service, tenant, role, as_of, q):
    return service.execute(EngineQuestion(context=TrustedContext(tenant=tenant,role=role,as_of=as_of),question=q))

def test_current_atlas_certification(questions):
    r=ask(questions,"Atlas","employee","2026-09-21","certification reimbursement")
    assert r.status=="ANSWERED" and r.answer.endswith("INR 25000.")
    assert [c.chunk_id for c in r.citations]==["atlas-cert-current"]

def test_effective_interval_start_inclusive_end_exclusive(questions):
    assert ask(questions,"Atlas","employee","2026-05-31","certification").answer.endswith("INR 40000.")
    assert ask(questions,"Atlas","employee","2026-06-01","certification").answer.endswith("INR 25000.")
    assert ask(questions,"Atlas","employee","2027-01-01","certification").answer.endswith("INR 35000.")

def test_role_and_tenant_are_isolated(questions):
    assert ask(questions,"Atlas","contractor","2026-09-21","certification").answer.endswith("INR 10000.")
    assert ask(questions,"Boreal","employee","2026-09-21","certification").answer.endswith("INR 80000.")

def test_conflict_is_not_arbitrated(questions):
    r=ask(questions,"Atlas","employee","2026-09-21","home-office")
    assert r.status=="CONFLICT" and r.answer is None
    assert {c.chunk_id for c in r.citations}=={"atlas-home-office-a","atlas-home-office-b"}

def test_no_wellness_policy_means_insufficient(questions):
    r=ask(questions,"Atlas","employee","2026-09-21","gym wellness")
    assert r.status=="INSUFFICIENT_EVIDENCE" and r.citations==[]

def test_prompt_injection_record_never_becomes_evidence(questions):
    r=ask(questions,"Atlas","employee","2026-09-21","home-office allowance")
    assert all(c.chunk_id!="atlas-injection-example" for c in r.citations)

def test_reordering_and_exact_duplication_do_not_change_outcome(tmp_path):
    root=Path(__file__).resolve().parents[2]
    original=json.loads((root/'fixtures/policy-corpus.json').read_text())
    mutated=list(reversed(original))+[dict(next(x for x in original if x['id']=='atlas-cert-current'), id='atlas-cert-copy')]
    path=tmp_path/'corpus.json'; path.write_text(json.dumps(mutated))
    service=PolicyQuestionUseCase(JsonPolicyCorpus(str(path)), DeterministicComposer())
    r=ask(service,"Atlas","employee","2026-09-21","certification")
    assert r.status=="ANSWERED" and r.answer.endswith("INR 25000.") and len(r.citations)==1
