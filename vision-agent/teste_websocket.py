import time
import uuid
import json
from messaging import RabbitMQClient

# --- CONFIGURAÇÃO DO ALERTA SIMULADO ---
payload = {
    "detectionId": str(uuid.uuid4()),
    "timestamp": int(time.time()),
    "isFacingCamera": False, # False = Alerta de Arma
    "depthPosition": 0,
    "gazeDirection": "unknown",
    "cameraId": "TESTE_SCRIPT_PYTHON",
    "userId": "safe",  # <--- TEM QUE SER O MESMO USER DO DASHBOARD
    "hasWeapon": True, # <--- True = Gatilho para Alerta Vermelho
    "weaponType": "TESTE_WEBSOCKET",
    "weaponLocation": "Simulacao Manual",
    # URL de uma imagem que sabemos que funciona (do seu teste anterior)
    "snapshotUrl": "http://192.168.112.1:9000/safevision-evidence/safe_1765430275_fa74f8.jpg",
    
    # 📍 Coordenadas de Florianópolis, SC, Brasil
    "latitude": -27.5969, 
    "longitude": -48.5495
}

print("🚀 Enviando simulação de arma para o RabbitMQ...")

try:
    # Usa a mesma classe de conexão do main.py
    client = RabbitMQClient()
    client.send_event(payload)
    
    # Tenta fechar a conexão graciosamente
    if hasattr(client, 'close'):
        client.close()
    elif hasattr(client, 'connection') and client.connection:
        client.connection.close()
        
    print(f"✅ Mensagem enviada para a fila 'vision_events'!")
    print(f"📍 GPS enviado: Lat {payload['latitude']}, Lon {payload['longitude']}")
    print("👀 OLHE PARA O SEU DASHBOARD AGORA (Pop-up deve aparecer com endereço).")
    
except Exception as e:
    print(f"❌ Erro ao conectar no RabbitMQ: {e}")