from __future__ import annotations
import os
from pathlib import Path
from fastapi import FastAPI, Header, HTTPException
from triage.contracts import EngineQuestion, EngineDocument, PolicyFinding, DocumentFinding
from triage.corpus import JsonPolicyCorpus
from triage.composer import DeterministicComposer
from triage.usecases import PolicyQuestionUseCase, ReimbursementDocumentUseCase
from triage.failures import ProviderTimeout, ProviderUnavailable, ProviderProtocolError, DocumentUnreadable

BASE=Path(__file__).resolve().parents[1]
CORPUS=os.getenv("POLICY_CORPUS", str(BASE/"fixtures"/"policy-corpus.json"))
ENGINE_KEY=os.getenv("ENGINE_KEY","local-engine-key")
corpus=JsonPolicyCorpus(CORPUS)
questions=PolicyQuestionUseCase(corpus, DeterministicComposer())
documents=ReimbursementDocumentUseCase(questions)
app=FastAPI(title="Evidence Triage Engine", version="1.0.0")

def require_internal_key(value: str | None):
    if value != ENGINE_KEY: raise HTTPException(status_code=401, detail={"code":"INVALID_ENGINE_KEY"})

def call(fn):
    try: return fn()
    except ProviderTimeout: raise HTTPException(504, detail={"code":"PROVIDER_TIMEOUT"})
    except ProviderUnavailable: raise HTTPException(503, detail={"code":"PROVIDER_UNAVAILABLE"})
    except ProviderProtocolError: raise HTTPException(502, detail={"code":"MALFORMED_PROVIDER_OUTPUT"})
    except DocumentUnreadable as e: raise HTTPException(422, detail={"code":str(e)})
    except ValueError: raise HTTPException(400, detail={"code":"INVALID_DATE"})

@app.get("/status")
def status(): return {"status":"ok","composer":os.getenv("COMPOSER_BEHAVIOR","normal")}

@app.post("/engine/question", response_model=PolicyFinding)
def question(cmd: EngineQuestion, x_engine_key: str | None=Header(default=None)):
    require_internal_key(x_engine_key); return call(lambda: questions.execute(cmd))

@app.post("/engine/document", response_model=DocumentFinding)
def document(cmd: EngineDocument, x_engine_key: str | None=Header(default=None)):
    require_internal_key(x_engine_key); return call(lambda: documents.execute(cmd))
