import cv2
import mediapipe as mp
from ultralytics import YOLO
from utils import calculate_distance

class WeaponDetector:
    def __init__(self):
        # Inicializa Pose (MediaPipe)
        # Otimização: model_complexity=0 é mais rápido e menos preciso (ideal para CPU)
        self.mp_pose = mp.solutions.pose
        self.pose = self.mp_pose.Pose(
            static_image_mode=False,
            model_complexity=0, 
            min_detection_confidence=0.5, 
            min_tracking_confidence=0.5
        )
        
        # Inicializa YOLO
        print("🔄 Carregando modelo YOLO...")
        try:
            self.model = YOLO('yolov8n.pt')
        except:
            self.model = YOLO('yolov8n.pt') 
        
        # IDs das classes COCO: 43: Knife, 67: Cell phone (teste), 76: Scissors
        self.WEAPON_CLASSES = [43, 67, 76]
        self.WEAPON_MAPPING = {43: "FACA", 67: "ARMA", 76: "TESOURA"}

    def process(self, frame, rgb_frame):
        img_h, img_w, _ = frame.shape
        
        result = {
            "hasWeapon": False,
            "weaponType": "NONE",
            "weaponLocation": "NONE"
        }

        # ======================================================================
        # PASSO 1: YOLO PRIMEIRO (Filtro Rápido)
        # ======================================================================
        # Se não tiver arma, nem perdemos tempo procurando braços/corpo.
        # classes=self.WEAPON_CLASSES faz o YOLO ignorar pessoas, carros, etc.
        yolo_results = self.model(frame, verbose=False, conf=0.5, classes=self.WEAPON_CLASSES)
        
        # Verifica se detectou algo
        detected_boxes = []
        for r in yolo_results:
            for box in r.boxes:
                cls = int(box.cls[0])
                # Confirmação dupla (embora o filtro classes já resolva)
                if cls in self.WEAPON_CLASSES:
                    detected_boxes.append(box)

        # ⚡ OTIMIZAÇÃO CRÍTICA: Se não tem arma, retorna AGORA.
        if not detected_boxes:
            return result

        # ======================================================================
        # PASSO 2: MEDIAPIPE (Só roda se tiver arma)
        # ======================================================================
        # Só gastamos CPU calculando esqueleto se já sabemos que tem uma arma na cena
        pose_results = self.pose.process(rgb_frame)
        body_points = {}

        if pose_results.pose_landmarks:
            landmarks = pose_results.pose_landmarks.landmark
            # Mapeia apenas o necessário
            body_points['R_WRIST'] = (int(landmarks[16].x * img_w), int(landmarks[16].y * img_h))
            body_points['L_WRIST'] = (int(landmarks[15].x * img_w), int(landmarks[15].y * img_h))
            r_hip = (int(landmarks[24].x * img_w), int(landmarks[24].y * img_h))
            l_hip = (int(landmarks[23].x * img_w), int(landmarks[23].y * img_h))
            body_points['WAIST'] = (int((r_hip[0] + l_hip[0]) / 2), int((r_hip[1] + l_hip[1]) / 2))

        # ======================================================================
        # PASSO 3: DESENHO E CORRELAÇÃO
        # ======================================================================
        for box in detected_boxes:
            cls = int(box.cls[0])
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            obj_center = (int((x1 + x2) / 2), int((y1 + y2) / 2))
            label = self.WEAPON_MAPPING.get(cls, "ARMA")

            # Desenha caixa da arma
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

            # Só conseguimos correlacionar se o MediaPipe achou alguém
            if body_points:
                threshold = img_w * 0.20  # Aumentei levemente a tolerância (20%)
                dist_r = calculate_distance(obj_center, body_points['R_WRIST'])
                dist_l = calculate_distance(obj_center, body_points['L_WRIST'])
                dist_w = calculate_distance(obj_center, body_points['WAIST'])

                if dist_r < threshold or dist_l < threshold:
                    result["hasWeapon"] = True
                    result["weaponType"] = label
                    result["weaponLocation"] = "MAO"
                    # Visualização da linha
                    target = body_points['R_WRIST'] if dist_r < dist_l else body_points['L_WRIST']
                    cv2.line(frame, obj_center, target, (0, 255, 255), 2)
                
                elif dist_w < threshold:
                    result["hasWeapon"] = True
                    result["weaponType"] = label
                    result["weaponLocation"] = "CINTURA"
                    cv2.line(frame, obj_center, body_points['WAIST'], (0, 0, 255), 2)
            else:
                # Se achou arma mas não achou corpo, ainda é uma ameaça? 
                # Sim, mas sem localização corporal.
                result["hasWeapon"] = True
                result["weaponType"] = label
                result["weaponLocation"] = "UNKNOWN"

        return result