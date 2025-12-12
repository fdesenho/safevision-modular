import cv2
import mediapipe as mp
from ultralytics import YOLO
from utils import calculate_distance

class WeaponDetector:
    def __init__(self):
        # Inicializa Pose
        self.mp_pose = mp.solutions.pose
        self.pose = self.mp_pose.Pose(min_detection_confidence=0.6, min_tracking_confidence=0.6)
        
        # Inicializa YOLO
        print("🔄 Carregando modelo YOLO...")
        try:
            self.model = YOLO('yolov8n.pt')
        except:
            self.model = YOLO('yolov8n.pt') # Retry ou download
        
        # 43: Knife, 67: Cell phone, 76: Scissors
        self.WEAPON_CLASSES = [43, 67, 76]
        self.WEAPON_MAPPING = {43: "FACA", 67: "ARMA", 76: "TESOURA"}

    def process(self, frame, rgb_frame):
        img_h, img_w, _ = frame.shape
        
        # 1. Detectar Corpo
        pose_results = self.pose.process(rgb_frame)
        body_points = {}

        if pose_results.pose_landmarks:
            landmarks = pose_results.pose_landmarks.landmark
            body_points['R_WRIST'] = (int(landmarks[16].x * img_w), int(landmarks[16].y * img_h))
            body_points['L_WRIST'] = (int(landmarks[15].x * img_w), int(landmarks[15].y * img_h))
            r_hip = (int(landmarks[24].x * img_w), int(landmarks[24].y * img_h))
            l_hip = (int(landmarks[23].x * img_w), int(landmarks[23].y * img_h))
            body_points['WAIST'] = (int((r_hip[0] + l_hip[0]) / 2), int((r_hip[1] + l_hip[1]) / 2))

        # 2. Detectar Armas
        yolo_results = self.model(frame, verbose=False, conf=0.5)
        
        result = {
            "hasWeapon": False,
            "weaponType": "NONE",
            "weaponLocation": "NONE"
        }

        for r in yolo_results:
            for box in r.boxes:
                cls = int(box.cls[0])
                if cls in self.WEAPON_CLASSES:
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    obj_center = (int((x1 + x2) / 2), int((y1 + y2) / 2))
                    label = self.WEAPON_MAPPING.get(cls, "ARMA")

                    # Desenha caixa
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)
                    cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

                    # 3. Correlacionar
                    if body_points:
                        threshold = img_w * 0.15 
                        dist_r = calculate_distance(obj_center, body_points['R_WRIST'])
                        dist_l = calculate_distance(obj_center, body_points['L_WRIST'])
                        dist_w = calculate_distance(obj_center, body_points['WAIST'])

                        if dist_r < threshold or dist_l < threshold:
                            result["hasWeapon"] = True
                            result["weaponType"] = label
                            result["weaponLocation"] = "MAO"
                            target = body_points['R_WRIST'] if dist_r < dist_l else body_points['L_WRIST']
                            cv2.line(frame, obj_center, target, (0, 255, 255), 3)
                        
                        elif dist_w < threshold:
                            result["hasWeapon"] = True
                            result["weaponType"] = label
                            result["weaponLocation"] = "CINTURA"
                            cv2.line(frame, obj_center, body_points['WAIST'], (0, 0, 255), 3)

        return result