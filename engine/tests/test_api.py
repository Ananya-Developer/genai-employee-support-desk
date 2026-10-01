from fastapi.testclient import TestClient
from app import app

client=TestClient(app)

def test_internal_api_requires_engine_key():
    r=client.post('/engine/question',json={
        'context':{'tenant':'Atlas','role':'employee','as_of':'2026-09-21'},
        'question':'certification'
    })
    assert r.status_code==401
    assert r.json()['detail']['code']=='INVALID_ENGINE_KEY'

def test_internal_question_contract():
    r=client.post('/engine/question',headers={'X-Engine-Key':'local-engine-key'},json={
        'context':{'tenant':'Atlas','role':'employee','as_of':'2026-09-21'},
        'question':'certification'
    })
    assert r.status_code==200
    assert r.json()['status']=='ANSWERED'
    assert r.json()['citations'][0]['chunk_id']=='atlas-cert-current'
