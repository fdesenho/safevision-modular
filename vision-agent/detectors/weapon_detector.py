import cv2
import mediapipe as mp
from ultralytics import YOLO
from utils import calculate_distance
from telemetry import tracer # 🟢 NEW: Import the tracer

class WeaponDetector:
    def __init__(self):
        # Initialize Pose (MediaPipe)
        # Optimization: model_complexity=0 is faster and less accurate (ideal for CPU)
        self.mp_pose = mp.solutions.pose
        self.pose = self.mp_pose.Pose(
            static_image_mode=False,
            model_complexity=0, 
            min_detection_confidence=0.5, 
            min_tracking_confidence=0.5
        )
        
        # Initialize YOLO
        print("🔄 Loading YOLO model...")
        try:
            self.model = YOLO('yolov8n.pt')
        except:
            self.model = YOLO('yolov8n.pt') 
        
        # COCO class IDs: 43: Knife, 67: Cell phone (test), 76: Scissors
        self.WEAPON_CLASSES = [43, 67, 76]
        self.WEAPON_MAPPING = {43: "FACA", 67: "ARMA", 76: "TESOURA"}

    def process(self, frame, rgb_frame):
        # 🟢 NEW: Start Zipkin timer (Span)
        with tracer.start_as_current_span("yolov8_weapon_detection") as span:
            
            img_h, img_w, _ = frame.shape
            span.set_attribute("cv2.resolution", f"{img_w}x{img_h}") # 🟢 NEW: Save resolution
            
            result = {
                "hasWeapon": False,
                "weaponType": "NONE",
                "weaponLocation": "NONE"
            }

            # ======================================================================
            # STEP 1: YOLO FIRST (Fast Filter)
            # ======================================================================
            yolo_results = self.model(frame, verbose=False, conf=0.5, classes=self.WEAPON_CLASSES)
            
            detected_boxes = []
            for r in yolo_results:
                for box in r.boxes:
                    cls = int(box.cls[0])
                    if cls in self.WEAPON_CLASSES:
                        detected_boxes.append(box)

            # ⚡ CRITICAL OPTIMIZATION: If no weapon is found, return NOW.
            if not detected_boxes:
                span.set_attribute("threat.detected", False) # 🟢 NEW: Register in trace before exiting
                return result

            # ======================================================================
            # STEP 2: MEDIAPIPE (Only runs if a weapon is found)
            # ======================================================================
            pose_results = self.pose.process(rgb_frame)
            body_points = {}

            if pose_results.pose_landmarks:
                landmarks = pose_results.pose_landmarks.landmark
                body_points['R_WRIST'] = (int(landmarks[16].x * img_w), int(landmarks[16].y * img_h))
                body_points['L_WRIST'] = (int(landmarks[15].x * img_w), int(landmarks[15].y * img_h))
                r_hip = (int(landmarks[24].x * img_w), int(landmarks[24].y * img_h))
                l_hip = (int(landmarks[23].x * img_w), int(landmarks[23].y * img_h))
                body_points['WAIST'] = (int((r_hip[0] + l_hip[0]) / 2), int((r_hip[1] + l_hip[1]) / 2))

            # ======================================================================
            # STEP 3: DRAWING AND CORRELATION
            # ======================================================================
            for box in detected_boxes:
                cls = int(box.cls[0])
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                obj_center = (int((x1 + x2) / 2), int((y1 + y2) / 2))
                label = self.WEAPON_MAPPING.get(cls, "ARMA")

                cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)
                cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

                if body_points:
                    threshold = img_w * 0.20 
                    dist_r = calculate_distance(obj_center, body_points['R_WRIST'])
                    dist_l = calculate_distance(obj_center, body_points['L_WRIST'])
                    dist_w = calculate_distance(obj_center, body_points['WAIST'])

                    if dist_r < threshold or dist_l < threshold:
                        result["hasWeapon"] = True
                        result["weaponType"] = label
                        result["weaponLocation"] = "MAO"
                        target = body_points['R_WRIST'] if dist_r < dist_l else body_points['L_WRIST']
                        cv2.line(frame, obj_center, target, (0, 255, 255), 2)
                    
                    elif dist_w < threshold:
                        result["hasWeapon"] = True
                        result["weaponType"] = label
                        result["weaponLocation"] = "CINTURA"
                        cv2.line(frame, obj_center, body_points['WAIST'], (0, 0, 255), 2)
                else:
                    result["hasWeapon"] = True
                    result["weaponType"] = label
                    result["weaponLocation"] = ""

            # 🟢 NEW: Save threat data to Zipkin if the flow reaches the end
            span.set_attribute("threat.detected", result["hasWeapon"])
            if result["hasWeapon"]:
                span.set_attribute("threat.type", result["weaponType"])
                span.set_attribute("threat.location", result["weaponLocation"])

            return result