import cv2
import time
import json
import threading
import uuid
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
# CORES NEON (Visual Leve)
# ==============================================================================
COLOR_BRAND_ROSE = (132, 51, 214) 
COLOR_DANGER_RED = (82, 82, 255)
COLOR_NEON_GREY = (128, 128, 128)
COLOR_TEXT_WHITE = (255, 255, 255)

# ==============================================================================
# CLASSE NOVA: CAMERA THREADADA (O SEGREDO DA VELOCIDADE 🚀)
# ==============================================================================
class ThreadedCamera:
    def __init__(self, src):
        self.capture = cv2.VideoCapture(src)
        # Otimiza o buffer do OpenCV para 1 frame (apenas o mais recente)
        self.capture.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        
        self.status, self.frame = self.capture.read()
        self.stopped = False
        self.lock = threading.Lock()
        
    def start(self):
        # Inicia a thread que lê a câmera sem parar
        t = threading.Thread(target=self.update, args=(), daemon=True)
        t.start()
        return self

    def update(self):
        while not self.stopped:
            if not self.capture.isOpened():
                continue
                
            # Lê o frame. Se demorar, não trava o programa principal.
            status, frame = self.capture.read()
            
            with self.lock:
                if status:
                    self.status = status
                    self.frame = frame
            
            # Pequena pausa para não fritar a CPU nessa thread
            time.sleep(0.005)

    def read(self):
        with self.lock:
            # Retorna uma cópia do último frame lido (frame fresco)
            return self.status, self.frame.copy() if self.frame is not None else None

    def stop(self):
        self.stopped = True
        if self.capture:
            self.capture.release()

# ==============================================================================
# HELPER: IMAGEM DE STATUS
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

# ==============================================================================
# CONTEXTO & GESTÃO
# ==============================================================================
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
        
        # Detectores
        self.weapon_detector = WeaponDetector()
        self.gaze_detector = GazeDetector()
        
        # Estado
        self.snapshot_taken = False
        self.last_threat_time = 0
        self.last_weapon_data = {'hasWeapon': False, 'weaponType': 'NONE', 'weaponLocation': 'NONE'}
        self.last_gaze_data = {'facing': False, 'direction': 'unknown', 'depth_score': 0}

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
# THREAD DE PROCESSAMENTO (IA + CONTROLE)
# ==============================================================================
class SurveillanceThread(threading.Thread):
    def __init__(self, context):
        super().__init__()
        self.context = context
        self.daemon = True
        self.rabbit_client = RabbitMQClient()
        self.minio_client = MinioClient()
        
        # ⚡ CONFIGURAÇÃO DE VELOCIDADE
        self.ai_interval = 0.2  # Roda IA apenas a cada 200ms (5 FPS de detecção)
        self.last_ai_time = 0
        self.EVENT_RESET_TIMEOUT = 5.0 # Reseta alerta mais rápido

    def run(self):
        print(f"🚀 [Thread {self.context.user_id}] Iniciando Câmera Threadada...")
        
        # Usa a nossa classe otimizada para ler a câmera
        camera_stream = ThreadedCamera(self.context.camera_url).start()
        
        # Aguarda aquecimento da câmera
        time.sleep(1.0)

        while self.context.is_active:
            # 1. Pega o frame mais recente instantaneamente (sem buffer lag)
            success, frame = camera_stream.read()
            
            if not success or frame is None:
                print(f"⚠️ [Thread {self.context.user_id}] Aguardando frame...")
                time.sleep(0.5)
                continue

            try:
                # 2. Redimensionamento Otimizado (Mantendo 640px para visualização)
                # Dica: Se ainda estiver lento, mude target_w para 320 ou 480
                h, w = frame.shape[:2]
                target_w = 480 # BAIXEI PARA 480px PARA TESTAR VELOCIDADE
                if w > target_w:
                    scale = target_w / w
                    new_h = int(h * scale)
                    frame = cv2.resize(frame, (target_w, new_h))
                
                # Corte da borda direita (seu ajuste visual)
                frame = frame[:, :-2]
                clean_frame = frame.copy()

                # 3. Lógica de Tempo para IA (Melhor que contar frames)
                now = time.time()
                should_run_ai = (now - self.last_ai_time) > self.ai_interval

                if self.context.is_armed and should_run_ai:
                    self.last_ai_time = now
                    
                    # ⚡ DICA DE OURO: IA roda em imagem menor (320px)
                    # Isso triplica a velocidade da detecção sem perder muita precisão
                    ai_frame = cv2.resize(frame, (320, 240))
                    rgb_frame = cv2.cvtColor(ai_frame, cv2.COLOR_BGR2RGB)
                    
                    # Processa
                    self.context.last_weapon_data = self.context.weapon_detector.process(ai_frame, rgb_frame)
                    # Gaze detector talvez precise do frame original se for baseado em mesh preciso, 
                    # mas vamos tentar com o reduzido para velocidade.
                    self.context.last_gaze_data = self.context.gaze_detector.process(ai_frame, rgb_frame)
                    
                    # Verifica ameaças numa thread separada para não travar o vídeo
                    threading.Thread(target=self.check_threats, args=(clean_frame,)).start()
                    
                # 4. Desenha Overlay e Envia (Sempre fluido)
                self.draw_overlay(frame)
                
                # Compressão JPEG mais rápida (Qualidade 60%)
                encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 60]
                ret, buffer = cv2.imencode('.jpg', frame, encode_param)
                if ret:
                    self.context.update_frame(buffer.tobytes())
                
                # Pausa mínima para não consumir 100% da CPU em loop infinito
                time.sleep(0.01)

            except Exception as e:
                print(f"❌ [Thread {self.context.user_id}] Erro: {e}")
                time.sleep(0.1)

        camera_stream.stop()
        print(f"🛑 [Thread {self.context.user_id}] Encerrada.")

    def check_threats(self, clean_frame):
        # Essa função agora roda em paralelo, sem travar o vídeo
        try:
            weapon = self.context.last_weapon_data
            gaze = self.context.last_gaze_data
            
            is_threat = weapon['hasWeapon'] or gaze['facing']

            if is_threat:
                self.context.last_threat_time = time.time()
                
                # Tira foto apenas na primeira detecção
                snapshot_url = None
                if not self.context.snapshot_taken:
                    print(f"📸 [Thread {self.context.user_id}] Ameaça! Uploading...")
                    filename = f"{self.context.user_id}_{int(time.time())}_{uuid.uuid4().hex[:6]}.jpg"
                    snapshot_url = self.minio_client.upload_frame(clean_frame, filename)
                    self.context.snapshot_taken = True
                
                self.send_alert(weapon, gaze, snapshot_url)

            else:
                # Reset do estado de foto
                time_since = time.time() - self.context.last_threat_time
                if self.context.snapshot_taken and time_since > self.EVENT_RESET_TIMEOUT:
                    self.context.snapshot_taken = False
                
                if gaze['depth_score'] > 0:
                     self.send_alert(weapon, gaze, None)
        except Exception as e:
            print(f"Erro check_threats: {e}")

    def send_alert(self, weapon, gaze, snapshot_url):
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
            self.rabbit_client.send_event(payload)
        except Exception as e:
            print(f"Erro RabbitMQ: {e}")

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
        
        # Desenha Overlay
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
                # Se mudou a câmera, reinicia
                if camera_url and camera_url != active_sessions[user_id]['context'].camera_url:
                    active_sessions[user_id]['context'].stop()
                    time.sleep(0.5)
                    del active_sessions[user_id]
                else:
                    return jsonify({"status": "active", "userId": user_id})

            if user_id not in active_sessions:
                if not camera_url: return jsonify({"error": "cameraUrl required"}), 400
                
                print(f"🆕 Nova sessão: {user_id} | Cam: {camera_url}")
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
                print(f"💤 Encerrado: {user_id}")
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
            # 25 FPS para o navegador (suave)
            time.sleep(0.04)

@app.route('/video_feed/<user_id>')
def video_feed(user_id):
    return Response(gen_frames(user_id), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/health')
def health():
    return jsonify({"status": "ok", "sessions": len(active_sessions)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)