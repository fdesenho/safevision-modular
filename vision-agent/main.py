import cv2
import time
import json
import threading
import uuid
import queue  # [PERFORMANCE] Import necessário para a fila Thread-Safe
import numpy as np
from flask import Flask, Response, request, jsonify
from flask_cors import CORS

# Imports locais
import config
from messaging import RabbitMQClient
from detectors.weapon_detector import WeaponDetector
from detectors.gaze_detector import GazeDetector
from storage import MinioClient

app = Flask(__name__)
CORS(app)

# ==============================================================================
# CORES NEON
# ==============================================================================
COLOR_BRAND_ROSE = (132, 51, 214) 
COLOR_DANGER_RED = (82, 82, 255)
COLOR_NEON_GREY = (128, 128, 128)
COLOR_TEXT_WHITE = (255, 255, 255)

# ==============================================================================
# [PERFORMANCE] SISTEMA DE FILA ASSÍNCRONA (PRODUCER-CONSUMER)
# ==============================================================================
# Esta fila age como um buffer. As câmeras jogam dados aqui instantaneamente.
# Uma thread separada pega daqui e lida com a rede (RabbitMQ).
alert_queue = queue.Queue()

def async_alert_worker():
    """
    Worker único que mantém a conexão RabbitMQ aberta e processa a fila.
    Isso evita abrir/fechar conexões repetidamente e previne Race Conditions.
    """
    print("⚡ [System] Iniciando Worker de Mensageria Async...")
    try:
        # A conexão é criada APENAS dentro desta thread dedicada
        rabbit_client = RabbitMQClient()
    except Exception as e:
        print(f"❌ [System] Falha fatal ao conectar RabbitMQ: {e}")
        return

    while True:
        try:
            # Bloqueia aqui esperando um item. Não gasta CPU.
            payload = alert_queue.get()
            
            # Envia para o RabbitMQ
            rabbit_client.send_event(payload)
            
            # Avisa a fila que o trabalho foi feito
            alert_queue.task_done()
        except Exception as e:
            print(f"❌ [Worker] Erro ao processar mensagem: {e}")
            # Em produção, adicionar lógica de reconexão aqui

# Inicia o worker em background assim que o script roda
threading.Thread(target=async_alert_worker, daemon=True).start()

# ==============================================================================
# CAMERA THREADADA
# ==============================================================================
class ThreadedCamera:
    def __init__(self, src):
        self.capture = cv2.VideoCapture(src)
        self.capture.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        
        self.status, self.frame = self.capture.read()
        self.stopped = False
        self.lock = threading.Lock()
        
    def start(self):
        t = threading.Thread(target=self.update, args=(), daemon=True)
        t.start()
        return self

    def update(self):
        while not self.stopped:
            if not self.capture.isOpened():
                time.sleep(0.5)
                continue
                
            status, frame = self.capture.read()
            
            with self.lock:
                if status:
                    self.status = status
                    self.frame = frame
            
            time.sleep(0.01)

    def read(self):
        with self.lock:
            return self.status, self.frame.copy() if self.frame is not None else None

    def stop(self):
        self.stopped = True
        if self.capture:
            self.capture.release()

# ==============================================================================
# CONTEXTO
# ==============================================================================
def create_placeholder_frame(text="AGUARDANDO..."):
    img = np.zeros((480, 640, 3), dtype=np.uint8)
    img[:] = (30, 30, 30)
    font = cv2.FONT_HERSHEY_SIMPLEX
    text_size = cv2.getTextSize(text, font, 0.8, 1)[0]
    text_x = (640 - text_size[0]) // 2
    text_y = (480 + text_size[1]) // 2
    cv2.putText(img, text, (text_x, text_y), font, 0.8, (200, 200, 200), 1, cv2.LINE_AA)
    ret, buffer = cv2.imencode('.jpg', img)
    return buffer.tobytes()

active_sessions = {}
sessions_lock = threading.Lock()

class UserSessionContext:
    def __init__(self, user_id, camera_url):
        self.user_id = user_id
        self.camera_url = camera_url
        self.lock = threading.Lock()
        self.is_armed = True 
        self.current_frame = create_placeholder_frame("INICIANDO...")
        self.is_active = True 
        
        print(f"🔧 Carregando IA para {user_id}...")
        self.weapon_detector = WeaponDetector()
        self.gaze_detector = GazeDetector()
        
        self.snapshot_taken = False
        self.is_uploading = False
        
        self.last_threat_time = 0
        self.last_weapon_data = {'hasWeapon': False, 'weaponType': 'NONE', 'weaponLocation': 'NONE'}
        self.last_gaze_data = {'facing': False, 'direction': 'unknown', 'depth_score': 0}
        
        # [PERFORMANCE] Controle de Throttling (Debounce)
        self.last_tracking_alert = 0

    def update_frame(self, frame_bytes):
        with self.lock:
            self.current_frame = frame_bytes

    def get_frame(self):
        with self.lock:
            return self.current_frame
    
    def set_armed(self, armed: bool):
        with self.lock:
            self.is_armed = armed

    def stop(self):
        self.is_active = False

# ==============================================================================
# THREAD DE VIGILÂNCIA (CONSUMIDOR)
# ==============================================================================
class SurveillanceThread(threading.Thread):
    def __init__(self, context):
        super().__init__()
        self.context = context
        self.daemon = True
        # [PERFORMANCE] Removido RabbitMQClient daqui. Usamos a fila global agora.
        self.minio_client = MinioClient()
        
        self.ai_interval = 0.25 
        self.last_ai_time = 0
        self.EVENT_RESET_TIMEOUT = 5.0

    def run(self):
        print(f"🚀 [Thread {self.context.user_id}] Iniciando Camera Threadada...")
        
        camera_stream = ThreadedCamera(self.context.camera_url).start()
        time.sleep(1.0) 

        while self.context.is_active:
            success, frame = camera_stream.read()
            
            if not success or frame is None:
                # print(f"⚠️ Aguardando vídeo...") # Comentado para limpar log
                time.sleep(0.5)
                continue

            try:
                h, w = frame.shape[:2]
                target_w = 480 
                if w != target_w:
                    scale = target_w / w
                    new_h = int(h * scale)
                    frame = cv2.resize(frame, (target_w, new_h))
                
                frame = frame[:, :-2]
                clean_frame = frame.copy()

                now = time.time()
                should_run_ai = (now - self.last_ai_time) > self.ai_interval

                if self.context.is_armed and should_run_ai:
                    self.last_ai_time = now
                    
                    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    
                    self.context.last_weapon_data = self.context.weapon_detector.process(frame, rgb_frame)
                    self.context.last_gaze_data = self.context.gaze_detector.process(frame, rgb_frame)
                    
                    threading.Thread(target=self.check_threats, args=(clean_frame,)).start()
                    
                self.draw_overlay(frame)
                
                ret, buffer = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 60])
                if ret:
                    self.context.update_frame(buffer.tobytes())
                
                time.sleep(0.01)

            except Exception as e:
                print(f"❌ [Thread] Erro: {e}")
                time.sleep(0.1)

        camera_stream.stop()
        print(f"🛑 Thread finalizada.")

    def check_threats(self, clean_frame):
        try:
            weapon = self.context.last_weapon_data
            gaze = self.context.last_gaze_data
            
            is_threat = weapon['hasWeapon'] or gaze['facing']

            if is_threat:
                self.context.last_threat_time = time.time()
                
                if not self.context.snapshot_taken and not self.context.is_uploading:
                    self.context.is_uploading = True 
                    
                    print(f"📸 Ameaça! Iniciando Upload...")
                    filename = f"{self.context.user_id}_{int(time.time())}_{uuid.uuid4().hex[:6]}.jpg"
                    
                    # Upload para Minio (I/O pesado, idealmente mover para worker tb, mas ok por agora)
                    snapshot_url = self.minio_client.upload_frame(clean_frame, filename)
                    
                    self.context.snapshot_taken = True
                    self.context.is_uploading = False 
                    
                    # [PERFORMANCE] Agora apenas enfileiramos o alerta
                    self.queue_alert(weapon, gaze, snapshot_url)

            else:
                if self.context.snapshot_taken:
                    if (time.time() - self.context.last_threat_time) > self.EVENT_RESET_TIMEOUT:
                        self.context.snapshot_taken = False
                
                # [PERFORMANCE] THROTTLING / DEBOUNCE
                # Só envia Tracking (depth > 0) se passou 1.0 segundo desde o último
                if gaze['depth_score'] > 0:
                     now = time.time()
                     if (now - self.context.last_tracking_alert) > 1.0:
                        self.queue_alert(weapon, gaze, None)
                        self.context.last_tracking_alert = now

        except Exception as e:
            print(f"Erro check_threats: {e}")
            self.context.is_uploading = False

    def queue_alert(self, weapon, gaze, snapshot_url):
        # Esta função substitui o send_alert antigo
        try:
            payload = {
                "detectionId": str(uuid.uuid4()),
                "timestamp": int(time.time()),
                "isFacingCamera": gaze['facing'],
                "depthPosition": gaze.get('depth_score', 0),
                "gazeDirection": gaze['direction'],
                "cameraId": "VISION-AGENT",
                "userId": self.context.user_id,
                "hasWeapon": weapon['hasWeapon'],
                "weaponType": weapon['weaponType'],
                "weaponLocation": weapon['weaponLocation'],
                "snapshotUrl": snapshot_url
            }
            # [PERFORMANCE] Coloca na fila global INSTANTANEAMENTE (sem IO de rede)
            alert_queue.put(payload)
        except Exception as e:
            print(f"Erro ao enfileirar alerta: {e}")

    def draw_overlay(self, frame):
        img_h, img_w = frame.shape[:2]
        
        if not self.context.is_armed:
            bg_color = COLOR_NEON_GREY
            status_text = f"SISTEMA EM STANDBY"
        else:
            weapon = self.context.last_weapon_data
            gaze = self.context.last_gaze_data
            
            if weapon['hasWeapon']:
                bg_color = COLOR_DANGER_RED
                status_text = f" [!] AMEACA: {weapon['weaponType']}"
            elif gaze['facing']:
                bg_color = COLOR_DANGER_RED
                status_text = f"DETECCAO FACIAL"
            else:
                bg_color = COLOR_BRAND_ROSE
                status_text = f"MONITORAMENTO ATIVO"
        
        cv2.rectangle(frame, (0, 0), (img_w + 10, 35), bg_color, -1)
        cv2.line(frame, (0, 35), (img_w + 10, 35), (255, 255, 255), 1)
        font = cv2.FONT_HERSHEY_SIMPLEX
        cv2.putText(frame, status_text, (10, 25), font, 0.6, COLOR_TEXT_WHITE, 1, cv2.LINE_AA)

# ==============================================================================
# ROTAS FLASK
# ==============================================================================
@app.route('/toggle/on', methods=['POST'])
def turn_on():
    try:
        data = request.get_json()
        user_id = data.get('userId')
        camera_url = data.get('cameraUrl')
        
        if not user_id: return jsonify({"error": "userId required"}), 400

        with sessions_lock:
            if user_id in active_sessions:
                active_sessions[user_id]['context'].set_armed(True)
                if camera_url and camera_url != active_sessions[user_id]['context'].camera_url:
                    active_sessions[user_id]['context'].stop()
                    time.sleep(0.5)
                    del active_sessions[user_id]
                else:
                    return jsonify({"status": "active", "userId": user_id})

            if user_id not in active_sessions:
                if not camera_url: return jsonify({"error": "cameraUrl required"}), 400
                ctx = UserSessionContext(user_id, camera_url)
                t = SurveillanceThread(ctx)
                t.start()
                active_sessions[user_id] = {"context": ctx, "thread": t}
                return jsonify({"status": "created", "userId": user_id})
                
        return jsonify({"status": "error"}), 500

    except Exception as e:
        print(f"❌ Erro on: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/toggle/off', methods=['POST'])
def turn_off():
    try:
        data = request.get_json()
        user_id = data.get('userId')
        if not user_id: return jsonify({"error": "userId required"}), 400

        with sessions_lock:
            if user_id in active_sessions:
                active_sessions[user_id]['context'].stop()
                del active_sessions[user_id]
                return jsonify({"status": "stopped", "userId": user_id})
            else:
                return jsonify({"status": "already_stopped", "userId": user_id})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

def gen_frames(user_id):
    while True:
        session = None
        with sessions_lock:
            session = active_sessions.get(user_id)
        
        if not session:
            yield (b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + create_placeholder_frame("OFFLINE") + b'\r\n')
            time.sleep(1)
        else:
            frame = session['context'].get_frame()
            if frame:
                yield (b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
            time.sleep(0.04)

@app.route('/video_feed/<user_id>')
def video_feed(user_id):
    return Response(gen_frames(user_id), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/health')
def health():
    return jsonify({"status": "ok", "sessions": len(active_sessions)})

if __name__ == '__main__':
    # O Worker de alerta já é iniciado acima (linha 65)
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)