import cv2
import time
import json
from flask import Flask, Response
import config
from messaging import RabbitMQClient
from detectors.weapon_detector import WeaponDetector
from detectors.gaze_detector import GazeDetector

app = Flask(__name__)

# 1. Inicializar Clientes e Detectores
# O RabbitMQClient já gerencia a conexão e reconexão automática se cair
rabbit_client = RabbitMQClient()
weapon_detector = WeaponDetector()
gaze_detector = GazeDetector()

def generate_frames():
    print(f"🎥 [Vision Agent] Iniciando captura: {config.VIDEO_SOURCE}")
    cap = cv2.VideoCapture(config.VIDEO_SOURCE)
    
    # ID da sessão de detecção (pode ser dinâmico se quiser rastrear sessões diferentes)
    detection_id = "TRACK_ID_LIVE"

    while True:
        success, frame = cap.read()
        
        # --- LÓGICA DE RECONEXÃO ROBUSTA ---
        if not success:
            print("⚠️ [Vision Agent] Falha ao ler frame. Tentando reconectar...")
            cap.release()
            time.sleep(2) # Espera 2 segundos antes de tentar
            
            # Recarrega a configuração (caso mude via env var no futuro)
            # Por enquanto usa a mesma fonte
            cap = cv2.VideoCapture(config.VIDEO_SOURCE)
            
            if not cap.isOpened():
                 print("❌ [Vision Agent] Não foi possível abrir a câmera.")
                 time.sleep(3) # Espera mais se falhar de novo
            continue

        # Prepara imagem
        try:
            rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            img_h, img_w, _ = frame.shape

            # --- EXECUTA OS DETECTORES (Strategy) ---
            weapon_data = weapon_detector.process(frame, rgb_frame)
            gaze_data = gaze_detector.process(frame, rgb_frame)

            # --- LÓGICA DE EXIBIÇÃO (Overlay) ---
            status_text = f"Olhar: {gaze_data['direction']} | D: {gaze_data['depth_score']}"
            color = (0, 255, 0) # Verde (Padrão)

            # Prioridade visual: Arma > Olhar > Normal
            if weapon_data['hasWeapon']:
                status_text = f"PERIGO: {weapon_data['weaponType']} NA {weapon_data['weaponLocation']}!"
                color = (0, 0, 255) # Vermelho
                cv2.rectangle(frame, (0, 0), (img_w, 60), color, -1)
            elif gaze_data['facing']:
                status_text = "ALERTA: Pessoa te olhando!"
                color = (0, 165, 255) # Laranja
                cv2.rectangle(frame, (0, 0), (img_w, 60), color, -1)
            else:
                # Barra preta padrão para legibilidade
                cv2.rectangle(frame, (0, 0), (img_w, 40), (0, 0, 0), -1)

            cv2.putText(frame, status_text, (10, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

            # --- ENVIO PARA RABBITMQ (Assíncrono) ---
            # Envia se houver detecção relevante OU movimento (profundidade > 0)
            # Isso garante que o Java receba dados para calcular o histórico
            if weapon_data['hasWeapon'] or gaze_data['facing'] or gaze_data['depth_score'] > 0:
                event_payload = {
                    "detectionId": detection_id,
                    "timestamp": int(time.time()),
                    
                    # Dados de Olhar e Aproximação
                    "isFacingCamera": gaze_data['facing'],
                    "depthPosition": gaze_data['depth_score'],
                    "gazeDirection": gaze_data['direction'],
                    
                    # Dados de Arma
                    "hasWeapon": weapon_data['hasWeapon'],
                    "weaponType": weapon_data['weaponType'],
                    "weaponLocation": weapon_data['weaponLocation'],
                    
                    # Metadados
                    "cameraId": "VISION-AGENT-CLEAN",
                    "userId": "superadmin"
                }
                # O cliente RabbitMQ lida com reconexão interna
                rabbit_client.send_event(event_payload)

            # Encode para Web (MJPEG)
            ret, buffer = cv2.imencode('.jpg', frame)
            if ret:
                frame_bytes = buffer.tobytes()
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')
        
        except Exception as e:
            print(f"❌ Erro no loop de processamento: {e}")
            time.sleep(1)

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/')
def index():
    return """
    <html>
        <head><title>SafeVision Monitor</title></head>
        <body style="background: #111; color: white; text-align: center;">
            <h1>👁️ SafeVision Edge Agent</h1>
            <p>Monitoramento em Tempo Real com IA</p>
            <div style="border: 2px solid #333; display: inline-block;">
                <img src="/video_feed" width="100%">
            </div>
        </body>
    </html>
    """

if __name__ == '__main__':
    # Roda o servidor Flask
    app.run(host='0.0.0.0', port=5000, debug=False)