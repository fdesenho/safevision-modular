import sys
import os
import time
import requests
import jwt  # 🟢 Necessário: pip install PyJWT
from datetime import datetime, timedelta

# Garante que o script enxergue a raiz para importar o config
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import config 
from telemetry import tracer
from opentelemetry.propagate import inject

def generate_benchmark_token():
    """Gera um JWT assinado para atravessar o Gateway/Auth-Service"""
    payload = {
        "sub": "safe_benchmark_user",
        "role": "ADMIN",
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(minutes=60)
    }
    return jwt.encode(payload, config.SAFEVISION_JWT_SECRET, algorithm="HS256")

def send_http_benchmark(payload, attempt, token):
    """Envia o payload via HTTP POST usando Bearer Token"""
    with tracer.start_as_current_span("http_post_benchmark_cloud") as span:
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}" # 🚀 Autenticação JWT
        }
        
        inject(headers) 
        span.set_attribute("benchmark.attempt", attempt)

        try:
            url = config.BACKEND_SIMULATE_URL
            response = requests.post(url, json=payload, headers=headers, timeout=30)
            
            span.set_attribute("http.status_code", response.status_code)
            return response.status_code, headers.get("x-b3-traceid")
            
        except Exception as e:
            span.record_exception(e)
            return f"ERR_{type(e).__name__}", headers.get("x-b3-traceid")

def run_benchmark():
    print("🚀 Iniciando Benchmarking ADR-018 com JWT (Cenário CLOUD)...")
    
    # Gera o token uma vez para o lote de 100 requisições
    token = generate_benchmark_token()
    total_requests = 100
    
    for i in range(1, total_requests + 1):
        # Payload de 2MB imitando frame bruto
        payload = {
            "cameraId": "cam-bench-cloud-01",
            "timestamp": int(time.time()),
            "frameData": "A" * (1024 * 1024 * 2) 
        }
        
        start_time = time.time()
        status, trace_id = send_http_benchmark(payload, i, token)
        elapsed_ms = (time.time() - start_time) * 1000
        
        print(f"[{i:03d}/100] HTTP {status} | {elapsed_ms:.2f}ms | TraceID: {trace_id}")
        
        if status == 401:
            print("❌ Erro de Autenticação: Verifique se o SAFEVISION_JWT_SECRET coincide com o do Java.")
            break
            
        time.sleep(1) 

    print("\n✅ Processo concluído.")

if __name__ == "__main__":
    run_benchmark()