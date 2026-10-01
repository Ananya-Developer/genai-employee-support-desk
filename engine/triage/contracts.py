from __future__ import annotations
from typing import Literal, Optional
from pydantic import BaseModel, Field

class TrustedContext(BaseModel):
    tenant: str
    role: str
    as_of: str

class EngineQuestion(BaseModel):
    context: TrustedContext
    question: str = Field(min_length=1)

class EngineDocument(BaseModel):
    context: TrustedContext
    document_id: str
    filename: str
    payload_b64: str

class Citation(BaseModel):
    chunk_id: str
    quote: str

class PolicyFinding(BaseModel):
    status: Literal["ANSWERED","INSUFFICIENT_EVIDENCE","CONFLICT"]
    answer: Optional[str] = None
    citations: list[Citation] = []

class EvidenceQuote(BaseModel):
    quote: str

class ExtractedFields(BaseModel):
    benefit: Optional[str] = None
    amount: Optional[int] = None
    currency: Optional[str] = None
    reference: Optional[str] = None

class DocumentFinding(BaseModel):
    extracted: ExtractedFields
    field_evidence: dict[str,EvidenceQuote]
    policy: Optional[PolicyFinding]
    issues: list[str]
