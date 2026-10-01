import pytest
from pathlib import Path
from triage.corpus import JsonPolicyCorpus
from triage.composer import DeterministicComposer
from triage.usecases import PolicyQuestionUseCase
from triage.contracts import EngineQuestion, TrustedContext
from triage.failures import ProviderTimeout, ProviderUnavailable, ProviderProtocolError

ROOT=Path(__file__).resolve().parents[2]
CMD=EngineQuestion(context=TrustedContext(tenant="Atlas",role="employee",as_of="2026-09-21"),question="certification")
@pytest.mark.parametrize("mode,exc",[("timeout",ProviderTimeout),("unavailable",ProviderUnavailable),("malformed",ProviderProtocolError)])
def test_fault_double(mode,exc):
    svc=PolicyQuestionUseCase(JsonPolicyCorpus(str(ROOT/'fixtures/policy-corpus.json')),DeterministicComposer(mode))
    with pytest.raises(exc): svc.execute(CMD)
