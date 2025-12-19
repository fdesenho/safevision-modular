import cv2
import time
import json
import threading
import uuid
import queue
import numpy as np
from flask import Flask, Response, request, jsonify
from flask_cors import CORS

# Local Modules
import config
from messaging import RabbitMQClient
from detectors.weapon_detector import WeaponDetector
from detectors.gaze_detector import GazeDetector
from storage import MinioClient
from services.gps_service import GPSService 

app = Flask(__name__)
# Configuração CORS permissiva
CORS(app, resources={r"/*": {"origins": "*"}})

# ==============================================================================
# UI CONFIGURATION
# ==============================================================================
COLOR_BRAND_ROSE = (132, 51, 214) 
COLOR_DANGER_RED = (82, 82, 255)
COLOR_NEON_GREY = (128, 128, 128)
COLOR_TEXT_WHITE = (255, 255, 255)

# ==============================================================================
# ASYNC MESSAGING
# ==============================================================================
alert_queue = queue.Queue()

def async_alert_worker():
    print("⚡ [System] Starting Async Messaging Worker...")
    try:
        rabbit_client = RabbitMQClient()
    except Exception as e:
        print(f"❌ [System] Fatal RabbitMQ Connection Error: {e}")
        return

    while True:
        try:
            payload = alert_queue.get()
            rabbit_client.send_event(payload)
            alert_queue.task_done()
        except Exception as e:
            print(f"❌ [Worker] Error processing message: {e}")

threading.Thread(target=async_alert_worker, daemon=True).start()

# ==============================================================================
# GPS SERVICE INITIALIZATION (HTTP MODE)
# ==============================================================================
gps_service = GPSService() 
gps_service.start()

# ==============================================================================
# THREADED CAMERA CAPTURE
# ==============================================================================
class ThreadedCamera:
    def __init__(self, src):
        self.src = src
        self.capture = None
        self.status = False
        self.frame = None
        self.stopped = False
        self.lock = threading.Lock()
        
        try:
            self.capture = cv2.VideoCapture(src)
            # Buffer size 1 é crucial para evitar delay acumulado
            self.capture.set(cv2.CAP_PROP_BUFFERSIZE, 1)
            self.status, self.frame = self.capture.read()
        except Exception as e:
            print(f"⚠️ Erro ao conectar na câmera {src}: {e}")

    def start(self):
        t = threading.Thread(target=self.update, args=(), daemon=True)
        t.start()
        return self

    def update(self):
        while not self.stopped:
            if not self.capture or not self.capture.isOpened():
                time.sleep(1.0)
                continue
            
            # Lê o frame mais recente
            status, frame = self.capture.read()
            
            with self.lock:
                if status:
                    self.status = status
                    self.frame = frame
                else:
                    self.status = False
            
            time.sleep(0.005)

    def read(self):
        with self.lock:
            return self.status, self.frame.copy() if self.frame is not None else None

    def stop(self):
        self.stopped = True
        if self.capture:
            self.capture.release()

# ==============================================================================
# USER SESSION CONTEXT
# ==============================================================================
def create_placeholder_frame(text="AGUARDANDO..."):
    img = np.zeros((480, 640, 3), dtype=np.uint8)
    img[:] = (30, 30, 30)
    font = cv2.FONT_HERSHEY_SIMPLEX
    text_size = cv2.getTextSize(text, font, 0.8, 1)[0]
    text_x = (640 - text_size[0]) // 2
    text_y = (480 + text_size[1]) // 2
    cv2.putText(img, text, (text_x, text_y), font, 0.8, (200, 200, 200), 1, cv2.LINE_AA)
    
    ts = time.strftime("%H:%M:%S")
    cv2.putText(img, ts, (10, 470), font, 0.5, (100, 255, 100), 1)

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
        self.last_tracking_alert = 0
        
        # ✅ CORREÇÃO CRÍTICA: ID Persistente
        # Usamos este ID para toda a sessão, permitindo que o Java conte o tempo de olhar.
        self.current_tracking_id = str(uuid.uuid4())

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
# SURVEILLANCE LOGIC (MAIN CONSUMER)
# ==============================================================================
class SurveillanceThread(threading.Thread):
    def __init__(self, context):
        super().__init__()
        self.context = context
        self.daemon = True
        self.minio_client = MinioClient()
        
        # OTIMIZAÇÃO: 0.5s para equilibrar performance e detecção
        self.ai_interval = 0.5 
        self.last_ai_time = 0
        self.EVENT_RESET_TIMEOUT = 5.0

    def run(self):
        print(f"🚀 [Thread {self.context.user_id}] Conectando na câmera: {self.context.camera_url}")
        
        camera_stream = ThreadedCamera(self.context.camera_url).start()
        time.sleep(1.0) 

        while self.context.is_active:
            success, frame = camera_stream.read()
            
            if not success or frame is None:
                self.context.update_frame(create_placeholder_frame("SEM SINAL"))
                time.sleep(0.5)
                continue

            try:
                # Resize (480p)
                h, w = frame.shape[:2]
                target_w = 480 
                if w != target_w:
                    scale = target_w / w
                    new_h = int(h * scale)
                    frame = cv2.resize(frame, (target_w, new_h))
                
                frame = frame[:, :-2] # Trim borders
                clean_frame = frame.copy()

                # IA Scheduling
                now = time.time()
                should_run_ai = (now - self.last_ai_time) > self.ai_interval

                if self.context.is_armed and should_run_ai:
                    self.last_ai_time = now
                    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    
                    self.context.last_weapon_data = self.context.weapon_detector.process(frame, rgb_frame)
                    # Gaze roda sempre que a IA roda
                    self.context.last_gaze_data = self.context.gaze_detector.process(frame, rgb_frame)
                    
                    threading.Thread(target=self.check_threats, args=(clean_frame,)).start()
                    
                self.draw_overlay(frame)
                
                # Compressão JPEG Quality 50
                ret, buffer = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 50])
                if ret:
                    self.context.update_frame(buffer.tobytes())
                
                time.sleep(0.01)

            except Exception as e:
                print(f"❌ [Thread] Error: {e}")
                time.sleep(0.1)

        camera_stream.stop()
        print(f"🛑 Thread finished for {self.context.user_id}")

    def check_threats(self, clean_frame):
        try:
            weapon = self.context.last_weapon_data
            gaze = self.context.last_gaze_data
            is_threat = weapon['hasWeapon'] or gaze['facing']

            if is_threat:
                self.context.last_threat_time = time.time()
                snapshot_url = None
                
                if not self.context.snapshot_taken and not self.context.is_uploading:
                    self.context.is_uploading = True 
                    print(f"📸 Threat Detected! Uploading Snapshot...")
                    filename = f"{self.context.user_id}_{int(time.time())}_{uuid.uuid4().hex[:6]}.jpg"
                    snapshot_url = self.minio_client.upload_frame(clean_frame, filename)
                    self.context.snapshot_taken = True
                    self.context.is_uploading = False 
                    
                self.queue_alert(weapon, gaze, snapshot_url)

            else:
                if self.context.snapshot_taken:
                    if (time.time() - self.context.last_threat_time) > self.EVENT_RESET_TIMEOUT:
                        self.context.snapshot_taken = False
                
                # Envia tracking mesmo sem ameaça explícita (para loitering)
                if gaze['depth_score'] > 0:
                     now = time.time()
                     if (now - self.context.last_tracking_alert) > 1.0:
                        self.queue_alert(weapon, gaze, None)
                        self.context.last_tracking_alert = now

        except Exception as e:
            print(f"Error in check_threats: {e}")
            self.context.is_uploading = False

    def queue_alert(self, weapon, gaze, snapshot_url):
        try:
            # 📍 Busca coordenadas (com Mock ou Real)
            lat, lon = gps_service.get_coordinates()

            payload = {
                # ✅ CORREÇÃO: Usa o ID persistente da sessão
                "detectionId": self.context.current_tracking_id,
                
                "timestamp": int(time.time()),
                "isFacingCamera": gaze['facing'],
                "depthPosition": gaze.get('depth_score', 0),
                "gazeDirection": gaze['direction'],
                "cameraId": "VISION-AGENT",
                "userId": self.context.user_id,
                "hasWeapon": weapon['hasWeapon'],
                "weaponType": weapon['weaponType'],
                "weaponLocation": weapon['weaponLocation'],
                "snapshotUrl": snapshot_url,
                "latitude": lat,
                "longitude": lon
            }
            alert_queue.put(payload)
        except Exception as e:
            print(f"Error enqueueing alert: {e}")

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
                status_text = f"MONITORANDO...."
        
        cv2.rectangle(frame, (0, 0), (img_w + 10, 35), bg_color, -1)
        cv2.line(frame, (0, 35), (img_w + 10, 35), (255, 255, 255), 1)
        font = cv2.FONT_HERSHEY_SIMPLEX
        cv2.putText(frame, status_text, (10, 25), font, 0.6, COLOR_TEXT_WHITE, 1, cv2.LINE_AA)

# ==============================================================================
# FLASK ROUTES
# ==============================================================================
@app.route('/toggle/on', methods=['POST'])
def turn_on():
    try:
        data = request.get_json()
        user_id = data.get('userId')
        camera_url = data.get('cameraUrl')
        
        if not user_id: return jsonify({"error": "userId required"}), 400

        if camera_url:
            gps_service.set_target_from_camera_url(camera_url)

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
        print(f"❌ Error turning on: {e}")
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
    lat, lon = gps_service.get_coordinates()
    gps_active = lat != 0.0
    return jsonify({
        "status": "ok", 
        "sessions": len(active_sessions), 
        "gps_active": gps_active,
        "gps_data": {"lat": lat, "lon": lon}
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)