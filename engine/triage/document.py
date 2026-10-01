from __future__ import annotations
import base64, io, re
from pypdf import PdfReader
from .contracts import EvidenceQuote, ExtractedFields
from .failures import DocumentUnreadable
from .evidence import classify_benefit

REFERENCE=re.compile(r"(?im)^\s*Reference\s*:\s*([A-Z]+-\d+)\s*$")
MONEY=re.compile(r"\b([A-Z]{3})\s*([0-9][0-9,]*)\b")

def decode_payload(payload_b64: str) -> bytes:
    try: return base64.b64decode(payload_b64, validate=True)
    except Exception as exc: raise DocumentUnreadable("INVALID_DOCUMENT_ENCODING") from exc

def read_text(filename: str, raw: bytes) -> str:
    if not raw: raise DocumentUnreadable("EMPTY_OR_UNREADABLE")
    suffix=filename.lower().rsplit('.',1)[-1] if '.' in filename else ''
    try:
        if suffix == 'txt':
            text=raw.decode('utf-8', errors='strict')
        elif suffix == 'pdf':
            reader=PdfReader(io.BytesIO(raw))
            text='\n'.join((page.extract_text() or '') for page in reader.pages)
        else:
            raise DocumentUnreadable("UNSUPPORTED_DOCUMENT_TYPE")
    except DocumentUnreadable: raise
    except Exception as exc: raise DocumentUnreadable("EMPTY_OR_UNREADABLE") from exc
    if not text.strip(): raise DocumentUnreadable("EMPTY_OR_UNREADABLE")
    return text

def _quote_for(text: str, token: str) -> str:
    for line in text.splitlines():
        if token.lower() in line.lower(): return line.strip()
    return text.strip().splitlines()[0]

def extract(text: str):
    benefit=classify_benefit(text)
    ref_match=REFERENCE.search(text)
    ref=ref_match.group(1) if ref_match else None
    money=[(m.group(1), int(m.group(2).replace(',','')), m.group(0)) for m in MONEY.finditer(text)]
    distinct=[]
    for c,a,tok in money:
        if (c,a) not in [(x[0],x[1]) for x in distinct]: distinct.append((c,a,tok))
    amount=None; currency=None; ambiguous=False
    if len(distinct)==1:
        currency,amount,_=distinct[0]
    elif len(distinct)>1:
        currencies={c for c,_,_ in distinct}; currency=next(iter(currencies)) if len(currencies)==1 else None; ambiguous=True
    ev={}
    if benefit: ev['benefit']=EvidenceQuote(quote=_quote_for(text, benefit.replace('-',' ')))
    if ref: ev['reference']=EvidenceQuote(quote=_quote_for(text, ref))
    if amount is not None:
        tok=next(tok for c,a,tok in distinct if c==currency and a==amount)
        ev['amount']=EvidenceQuote(quote=_quote_for(text,tok)); ev['currency']=EvidenceQuote(quote=_quote_for(text,tok))
    elif currency:
        ev['currency']=EvidenceQuote(quote=_quote_for(text,currency))
    return ExtractedFields(benefit=benefit, amount=amount, currency=currency, reference=ref), ev, ambiguous
