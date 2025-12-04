import cv2
import time
import json
import threading
import os
import uuid # <--- NOVO
from flask import Flask, Response, request, jsonify
from flask_cors import CORS
import config
from messaging import RabbitMQClient
from detectors.weapon_detector import WeaponDetector
from detectors.gaze_detector import GazeDetector
from storage import MinioClient # <--- NOVO

app = Flask(__name__)
CORS(app)

# ==============================================================================
# CLASSE DE CONTEXTO (STATE PATTERN)
# ==============================================================================
class AgentContext:
    def __init__(self):
        self.lock = threading.Lock()
        self.current_frame = None
        self.is_armed = False
        self.user_id = "superadmin"
        self.camera_source = config.VIDEO_SOURCE
        self.needs_camera_restart = False

    def update_frame(self, frame_bytes):
        with self.lock:
            self.current_frame = frame_bytes

    def get_frame(self):
        with self.lock:
            return self.current_frame

    def set_armed(self, armed: bool, user_id: str = None):
        self.is_armed = armed
        if user_id:
            self.user_id = user_id

    def update_camera(self, new_url):
        if new_url and new_url != self.camera_source:
            self.camera_source = new_url
            self.needs_camera_restart = True

context = AgentContext()


# ==============================================================================
# SERVIÇO DE CONFIGURAÇÃO (LISTENER)
# ==============================================================================
class ConfigListener(threading.Thread):
    def run(self):
        try:
            import pika
            credentials = pika.PlainCredentials(config.USERNAME, config.PASSWORD)
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(host=config.RABBITMQ_HOST, port=5672, credentials=credentials)
            )
            channel = connection.channel()
            channel.queue_declare(queue='vision.configuration', durable=True)

            def callback(ch, method, properties, body):
                try:
                    data = json.loads(body)
                    new_url = data.get("cameraUrl")
                    if new_url:
                        print(f"🔄 [Config] Comando recebido. Nova câmera: {new_url}")
                        context.update_camera(new_url)
                except Exception as e:
                    print(f"❌ [Config] Erro ao processar JSON: {e}")

            channel.basic_consume(queue='vision.configuration', on_message_callback=callback, auto_ack=True)
            print("👂 [Config] Ouvindo fila de configuração...")
            channel.start_consuming()
        except Exception as e:
            print(f"❌ [Config] Falha na thread de configuração: {e}")


# ==============================================================================
# SERVIÇO DE VIGILÂNCIA (CORE LOGIC - OTIMIZADO)
# ==============================================================================
class SurveillanceService(threading.Thread):
    def __init__(self):
        super().__init__()
        self.daemon = True
        self.rabbit_client = RabbitMQClient()
        self.minio_client = MinioClient() # <--- INICIALIZA STORAGE
        self.weapon_detector = WeaponDetector()
        self.gaze_detector = GazeDetector()
        
        # --- OTIMIZAÇÃO: Variáveis de Controle ---
        self.frame_count = 0
        self.process_every_n_frames = 3 
        self.target_width = 640         
        
        self.last_weapon_data = {'hasWeapon': False, 'weaponType': 'NONE', 'weaponLocation': 'NONE'}
        self.last_gaze_data = {'facing': False, 'direction': '...', 'depth_score': 0}

    def run(self):
        print(f"🎥 [Surveillance] Iniciando na fonte: {context.camera_source}")
        cap = None

        while True:
            if context.needs_camera_restart:
                print("🔄 [Surveillance] Reiniciando captura para nova fonte...")
                if cap: cap.release()
                cap = None
                context.needs_camera_restart = False

            if cap is None or not cap.isOpened():
                try:
                    cap = cv2.VideoCapture(context.camera_source)
                    if not cap.isOpened():
                        print("❌ [Surveillance] Câmera offline. Tentando em 5s...")
                        time.sleep(5)
                        continue
                except:
                    time.sleep(5)
                    continue

            success, frame = cap.read()
            if not success:
                if not config.CAMERA_URL:
                    cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                    continue
                else:
                    print("⚠️ [Surveillance] Sinal perdido. Reconectando...")
                    cap.release()
                    cap = None
                    time.sleep(1)
                    continue

            try:
                # --- OTIMIZAÇÃO 1: RESIZE ---
                h, w = frame.shape[:2]
                scale = self.target_width / w
                new_h = int(h * scale)
                
                # frame_small é o que será processado e enviado ao navegador
                frame_small = cv2.resize(frame, (self.target_width, new_h))
                
                # Converte cor para IA
                rgb_frame = cv2.cvtColor(frame_small, cv2.COLOR_BGR2RGB)

                if context.is_armed:
                    # Passamos o frame original (small) para poder fazer o upload da imagem correta (BGR)
                    self.process_ai_optimized(frame_small, rgb_frame)
                else:
                    self.draw_standby_overlay(frame_small, self.target_width)

                # 4. Atualiza o frame para o Flask (Web)
                ret, buffer = cv2.imencode('.jpg', frame_small)
                if ret:
                    context.update_frame(buffer.tobytes())

            except Exception as e:
                print(f"❌ [Surveillance] Erro no loop: {e}")
                time.sleep(1)

            time.sleep(0.01)

    def process_ai_optimized(self, frame, rgb_frame):
        """Lógica com Frame Skipping e Upload"""
        self.frame_count += 1
        
        should_process = (self.frame_count % self.process_every_n_frames == 0)

        if should_process:
            self.last_weapon_data = self.weapon_detector.process(frame, rgb_frame)
            self.last_gaze_data = self.gaze_detector.process(frame, rgb_frame)
        
        weapon_data = self.last_weapon_data
        gaze_data = self.last_gaze_data

        # --- VISUALIZAÇÃO ---
        status_text = f"MONITORANDO | Olhar: {gaze_data['direction']} | D: {gaze_data['depth_score']}"
        color = (0, 255, 0)

        if weapon_data['hasWeapon']:
            status_text = f"PERIGO: {weapon_data['weaponType']} NA {weapon_data['weaponLocation']}!"
            color = (0, 0, 255)
            img_w = frame.shape[1]
            cv2.rectangle(frame, (0, 0), (img_w, 60), color, -1)
        elif gaze_data['facing']:
            status_text = "ALERTA: PESSOA ENCARANDO!"
            color = (0, 165, 255)
            img_w = frame.shape[1]
            cv2.rectangle(frame, (0, 0), (img_w, 60), color, -1)
        else:
            img_w = frame.shape[1]
            cv2.rectangle(frame, (0, 0), (img_w, 40), (0, 0, 0), -1)

        cv2.putText(frame, status_text, (10, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        # --- ENVIO (Só envia quando processa dados novos) ---
        if should_process:
            if weapon_data['hasWeapon'] or gaze_data['facing'] or gaze_data['depth_score'] > 0:
                
                # --- LÓGICA DE UPLOAD (MINIO) ---
                snapshot_url = None
                
                # Só faz upload se for crítico (Arma ou Olhar)
                if weapon_data['hasWeapon'] or gaze_data['facing']:
                    # Gera nome único
                    filename = f"{int(time.time())}_{uuid.uuid4().hex[:6]}.jpg"
                    # Upload do frame BGR (frame_small) para manter cores corretas
                    snapshot_url = self.minio_client.upload_frame(frame, filename)

                self.send_alert(weapon_data, gaze_data, snapshot_url)

    def send_alert(self, weapon_data, gaze_data, snapshot_url):
        """Monta o payload e envia"""
        event_payload = {
            "detectionId": "TRACK_ID_LIVE",
            "timestamp": int(time.time()),
            "isFacingCamera": gaze_data['facing'],
            "depthPosition": gaze_data['depth_score'],
            "gazeDirection": gaze_data['direction'],
            "cameraId": "VISION-AGENT-01",
            "userId": context.user_id,
            "hasWeapon": weapon_data['hasWeapon'],
            "weaponType": weapon_data['weaponType'],
            "weaponLocation": weapon_data['weaponLocation'],
            "snapshotUrl": snapshot_url # <--- URL DA EVIDÊNCIA
        }
        self.rabbit_client.send_event(event_payload)

    def draw_standby_overlay(self, frame, img_w):
        cv2.rectangle(frame, (0, 0), (img_w, 40), (50, 50, 50), -1)
        cv2.putText(frame, "STANDBY - IA DESATIVADA", (10, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (200, 200, 200), 2)


# ==============================================================================
# SERVIDOR FLASK (INTERFACE WEB)
# ==============================================================================

def frame_generator():
    while True:
        frame = context.get_frame()
        if frame:
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
        time.sleep(0.05)

@app.route('/toggle/on', methods=['POST'])
def turn_on():
    data = request.get_json()
    user_id = data.get('userId') if data else None
    context.set_armed(True, user_id)
    print(f"🛡️ SISTEMA ARMADO para usuário: {context.user_id}")
    return jsonify({"status": "active", "message": f"Sistema Armado para {context.user_id}"})

@app.route('/toggle/off', methods=['POST'])
def turn_off():
    context.set_armed(False)
    print("zzz SISTEMA DESARMADO")
    return jsonify({"status": "standby", "message": "Sistema em Standby"})

@app.route('/video_feed')
def video_feed():
    return Response(frame_generator(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/')
def index():
    return "<h1>SafeVision Agent v2.0 (MinIO Enabled)</h1><img src='/video_feed'>"

if __name__ == '__main__':
    # Inicia threads
    ConfigListener().start()
    SurveillanceService().start()
    
    app.run(host='0.0.0.0', port=5000, debug=False)