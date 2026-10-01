from pathlib import Path
import base64
from triage.contracts import TrustedContext, EngineDocument
from triage.failures import DocumentUnreadable
import pytest

ROOT=Path(__file__).resolve().parents[2]
CTX=TrustedContext(tenant="Atlas",role="employee",as_of="2026-09-21")

def cmd(name):
    raw=(ROOT/'fixtures/inbox'/name).read_bytes()
    return EngineDocument(context=CTX,document_id=name.split('.')[0],filename=name,payload_b64=base64.b64encode(raw).decode())

def test_txt_extracts_supported_fields_and_quotes(docs):
    r=docs.execute(cmd('request-01.txt'))
    assert r.extracted.model_dump()=={"benefit":"certification","amount":18000,"currency":"INR","reference":"CERT-101"}
    assert "CERT-101" in r.field_evidence['reference'].quote
    assert r.policy.answer.endswith("INR 25000.")

def test_text_pdf_is_supported(docs):
    r=docs.execute(cmd('request-02.pdf'))
    assert r.extracted.benefit=="home-office" and r.extracted.amount==14000 and r.extracted.reference=="HOME-202"
    assert r.policy.status=="CONFLICT"

def test_ambiguous_amount_stays_null(docs):
    r=docs.execute(cmd('request-03.txt'))
    assert r.extracted.amount is None and r.extracted.currency=="INR" and "AMOUNT_AMBIGUOUS" in r.issues

def test_request_text_cannot_switch_tenant(docs):
    r=docs.execute(cmd('request-05.txt'))
    assert r.policy.answer.endswith("INR 25000.")

def test_training_missing_amount_and_manager_approval(docs):
    r=docs.execute(cmd('request-07.txt'))
    assert r.extracted.amount is None
    assert "AMOUNT_MISSING" in r.issues and "MANAGER_APPROVAL_NOT_OBTAINED" in r.issues

def test_zero_byte_is_a_document_failure(docs):
    with pytest.raises(DocumentUnreadable): docs.execute(cmd('request-08.txt'))
