from pathlib import Path
import pytest
from triage.corpus import JsonPolicyCorpus
from triage.composer import DeterministicComposer
from triage.usecases import PolicyQuestionUseCase, ReimbursementDocumentUseCase

ROOT=Path(__file__).resolve().parents[2]
@pytest.fixture
def questions(): return PolicyQuestionUseCase(JsonPolicyCorpus(str(ROOT/'fixtures/policy-corpus.json')), DeterministicComposer())
@pytest.fixture
def docs(questions): return ReimbursementDocumentUseCase(questions)
