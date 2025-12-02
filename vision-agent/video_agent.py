"""import cv2
import pika
import json
import time
import os
import numpy as np
import mediapipe as mp
from flask import Flask, Response
from ultralytics import YOLO

app = Flask(__name__)

# --- CONFIGURAÇÕES ---
RABBITMQ_HOST = 'rabbitmq'
USERNAME = 'user'
PASSWORD = 'password'
QUEUE_NAME = os.environ.get('VISION_QUEUE_NAME', 'vision.raw.tracking')

CAMERA_URL = os.environ.get('CAMERA_URL')
# Tenta usar URL da câmera ou webcam local (0) se não houver URL
VIDEO_SOURCE = CAMERA_URL if CAMERA_URL else 0 

# --- INICIALIZAÇÃO IA ---

# 1. MediaPipe (Corpo e Rosto)
mp_pose = mp.solutions.pose
pose = mp_pose.Pose(min_detection_confidence=0.6, min_tracking_confidence=0.6)

mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(min_detection_confidence=0.5, max_num_faces=1)

# 2. YOLOv8 (Objetos)
try:
    model = YOLO('yolov8n.pt') # Modelo Padrão
except:
    print("Baixando modelo YOLO...")
    model = YOLO('yolov8n.pt')

# --- CLASSES DE INTERESSE (COCO DATASET) ---
# 43: Knife (FACA REAL)
# 67: Cell Phone (SIMULAÇÃO DE ARMA DE FOGO - Para testes seguros)
# Se você tiver um modelo customizado de armas, altere aqui.
WEAPON_MAPPING = {
    43: "FACA",
    67: "ARMA (SIMULADA)" 
}
TARGET_CLASSES = list(WEAPON_MAPPING.keys())

# --- CONEXÃO RABBITMQ ---
try:
    credentials = pika.PlainCredentials(USERNAME, PASSWORD)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RABBITMQ_HOST, port=5672, credentials=credentials)
    )
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    print(f"✅ Conectado ao RabbitMQ.")
except Exception as e:
    print(f"❌ ERRO RabbitMQ: {e}")

# --- FUNÇÕES AUXILIARES ---

def calculate_distance(p1, p2):
    return np.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

# (Sua função de olhar is_facing_camera continua aqui - omitida para brevidade, mantenha a anterior)
def is_facing_camera(face_landmarks, img_w, img_h):
    face_3d = []
    face_2d = []
    key_points = [1, 152, 263, 33, 287, 57]
    for idx, lm in enumerate(face_landmarks.landmark):
        if idx in key_points:
            x, y = int(lm.x * img_w), int(lm.y * img_h)
            face_2d.append([x, y])
            face_3d.append([x, y, lm.z])
    face_2d = np.array(face_2d, dtype=np.float64)
    face_3d = np.array(face_3d, dtype=np.float64)
    focal_length = 1 * img_w
    cam_matrix = np.array([[focal_length, 0, img_h / 2], [0, focal_length, img_w / 2], [0, 0, 1]])
    dist_matrix = np.zeros((4, 1), dtype=np.float64)
    success, rot_vec, trans_vec = cv2.solvePnP(face_3d, face_2d, cam_matrix, dist_matrix)
    rmat, jac = cv2.Rodrigues(rot_vec)
    
    # Correção do erro de unpack
    retval = cv2.RQDecomp3x3(rmat)
    angles = retval[0]

    x_angle = angles[0] * 360
    y_angle = angles[1] * 360
    if y_angle < -10: text = "ESQUERDA"
    elif y_angle > 10: text = "DIREITA"
    elif x_angle < -10: text = "BAIXO"
    elif x_angle > 10: text = "CIMA"
    else: return True, "CENTER"
    return False, text

def generate_frames():
    cap = cv2.VideoCapture(VIDEO_SOURCE)
    detection_id = "TRACK_ID_LIVE"
    
    while True:
        success, frame = cap.read()
        if not success:
            if not CAMERA_URL: 
                cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                continue
            else:
                cap.release()
                time.sleep(1)
                cap = cv2.VideoCapture(VIDEO_SOURCE)
                continue

        img_h, img_w, _ = frame.shape
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        
        # 1. Detectar Corpo (Pose)
        pose_results = pose.process(rgb_frame)
        body_points = {}

        if pose_results.pose_landmarks:
            landmarks = pose_results.pose_landmarks.landmark
            # Punhos
            body_points['R_WRIST'] = (int(landmarks[16].x * img_w), int(landmarks[16].y * img_h))
            body_points['L_WRIST'] = (int(landmarks[15].x * img_w), int(landmarks[15].y * img_h))
            # Cintura (Média dos quadris)
            r_hip = (int(landmarks[24].x * img_w), int(landmarks[24].y * img_h))
            l_hip = (int(landmarks[23].x * img_w), int(landmarks[23].y * img_h))
            body_points['WAIST'] = (int((r_hip[0] + l_hip[0]) / 2), int((r_hip[1] + l_hip[1]) / 2))

        # 2. Detectar Objetos (YOLO)
        # conf=0.5 garante que só pegue se tiver certeza
        yolo_results = model(frame, verbose=False, conf=0.5) 
        
        has_weapon = False
        weapon_data_type = "NONE"
        weapon_data_loc = "NONE"

        for r in yolo_results:
            for box in r.boxes:
                cls = int(box.cls[0])
                
                if cls in TARGET_CLASSES:
                    # Dados do Objeto
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    obj_center = (int((x1 + x2) / 2), int((y1 + y2) / 2))
                    label = WEAPON_MAPPING[cls]

                    # Desenha o objeto
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)
                    cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

                    # 3. Validar Posição (Está na mão ou cintura?)
                    if body_points:
                        # Distâncias
                        dist_r = calculate_distance(obj_center, body_points['R_WRIST'])
                        dist_l = calculate_distance(obj_center, body_points['L_WRIST'])
                        dist_w = calculate_distance(obj_center, body_points['WAIST'])
                        
                        # Limite de pixels para considerar "segurando" ou "na cintura"
                        # Ajuste: 15% da largura da imagem
                        threshold = img_w * 0.15 

                        is_in_hand = dist_r < threshold or dist_l < threshold
                        is_in_waist = dist_w < threshold

                        if is_in_hand:
                            has_weapon = True
                            weapon_data_type = label
                            weapon_data_loc = "MAO"
                            # Linha Visual
                            closest_hand = body_points['R_WRIST'] if dist_r < dist_l else body_points['L_WRIST']
                            cv2.line(frame, obj_center, closest_hand, (0, 0, 255), 3)
                        
                        elif is_in_waist:
                            has_weapon = True
                            weapon_data_type = label
                            weapon_data_loc = "CINTURA"
                            cv2.line(frame, obj_center, body_points['WAIST'], (0, 0, 255), 3)

        # 4. Detectar Rosto (Olhar) - Opcional se arma for detectada
        face_results = face_mesh.process(rgb_frame)
        facing = False
        direction = "UNKNOWN"
        depth_score = 0

        if face_results.multi_face_landmarks:
            for face_landmarks in face_results.multi_face_landmarks:
                facing, direction = is_facing_camera(face_landmarks, img_w, img_h)
                # Profundidade simples
                depth_score = int(abs(face_landmarks.landmark[234].x - face_landmarks.landmark[454].x) * 200)

        # 5. Status Visual e Envio
        if has_weapon:
            status_text = f"PERIGO: {weapon_data_type} NA {weapon_data_loc}!"
            cv2.rectangle(frame, (0, 0), (img_w, 50), (0, 0, 255), -1) # Faixa Vermelha
            cv2.putText(frame, status_text, (20, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
        elif facing:
            status_text = "ALERTA: ENCARANDO!"
            cv2.rectangle(frame, (0, 0), (img_w, 50), (0, 165, 255), -1) # Faixa Laranja
            cv2.putText(frame, status_text, (20, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
        else:
            cv2.rectangle(frame, (0, 0), (img_w, 40), (0, 0, 0), -1)
            cv2.putText(frame, f"Status: {direction}", (20, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

        # --- ENVIO RABBITMQ ---
        # Só envia se tiver arma ou estiver encarando para economizar rede
        if has_weapon or facing or depth_score > 0:
            event_data = {
                "detectionId": detection_id,
                "timestamp": int(time.time()),
                "isFacingCamera": facing,
                "depthPosition": depth_score,
                "gazeDirection": direction,
                "cameraId": "VISION-AGENT-REAL",
                "userId": "superadmin",
                "hasWeapon": has_weapon,
                "weaponType": weapon_data_type,
                "weaponLocation": weapon_data_loc
            }
            try:
                if channel.is_open:
                    channel.basic_publish(exchange='', routing_key=QUEUE_NAME, body=json.dumps(event_data))
            except Exception as e:
                print(f"Erro Rabbit: {e}")

        ret, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)"""