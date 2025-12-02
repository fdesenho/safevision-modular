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
rabbit_client = RabbitMQClient()
weapon_detector = WeaponDetector()
gaze_detector = GazeDetector()

def generate_frames():
    print(f"🎥 Conectando à câmera: {config.VIDEO_SOURCE}")
    cap = cv2.VideoCapture(config.VIDEO_SOURCE)
    detection_id = "TRACK_ID_LIVE"

    while True:
        success, frame = cap.read()
        if not success:
            if not config.CAMERA_URL:
                cap.set(cv2.CAP_PROP_POS_FRAMES, 0) # Loop arquivo
                continue
            else:
                cap.release()
                time.sleep(1)
                cap = cv2.VideoCapture(config.VIDEO_SOURCE) # Reconnect IP Cam
                continue

        # Prepara imagem
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        img_h, img_w, _ = frame.shape

        # --- EXECUTA OS DETECTORES ---
        weapon_data = weapon_detector.process(frame, rgb_frame)
        gaze_data = gaze_detector.process(frame, rgb_frame)

        # --- DESENHA OVERLAY NA TELA ---
        status_text = f"Olhar: {gaze_data['direction']} | D: {gaze_data['depth_score']}"
        color = (0, 255, 0) # Verde

        if weapon_data['hasWeapon']:
            status_text = f"PERIGO: {weapon_data['weaponType']} NA {weapon_data['weaponLocation']}!"
            color = (0, 0, 255) # Vermelho
            cv2.rectangle(frame, (0, 0), (img_w, 50), color, -1)
        elif gaze_data['facing']:
            status_text = "ALERTA: ENCARANDO!"
            color = (0, 165, 255) # Laranja
            cv2.rectangle(frame, (0, 0), (img_w, 50), color, -1)
        else:
             cv2.rectangle(frame, (0, 0), (img_w, 40), (0, 0, 0), -1)

        cv2.putText(frame, status_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        # --- ENVIA PARA RABBITMQ ---
        # Só envia se houver algo relevante
        if weapon_data['hasWeapon'] or gaze_data['facing'] or gaze_data['depth_score'] > 0:
            event_payload = {
                "detectionId": detection_id,
                "timestamp": int(time.time()),
                "isFacingCamera": gaze_data['facing'],
                "depthPosition": gaze_data['depth_score'],
                "gazeDirection": gaze_data['direction'],
                "cameraId": "VISION-AGENT-CLEAN",
                "userId": "superadmin",
                "hasWeapon": weapon_data['hasWeapon'],
                "weaponType": weapon_data['weaponType'],
                "weaponLocation": weapon_data['weaponLocation']
            }
            rabbit_client.send_event(event_payload)

        # Encode para Web
        ret, buffer = cv2.imencode('.jpg', frame)
        frame_bytes = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)