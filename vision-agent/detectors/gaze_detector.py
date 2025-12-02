import cv2
import numpy as np
import mediapipe as mp

class GazeDetector:
    def __init__(self):
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(
            min_detection_confidence=0.5, 
            min_tracking_confidence=0.5, 
            max_num_faces=1
        )

    def process(self, frame, rgb_frame):
        img_h, img_w, _ = frame.shape
        results = self.face_mesh.process(rgb_frame)
        
        data = {
            "facing": False,
            "direction": "UNKNOWN",
            "depth_score": 0
        }

        if results.multi_face_landmarks:
            for face_landmarks in results.multi_face_landmarks:
                
                # 1. Calcular Profundidade (Largura do rosto)
                left_x = face_landmarks.landmark[234].x
                right_x = face_landmarks.landmark[454].x
                data["depth_score"] = int(abs(left_x - right_x) * 200)

                # 2. Calcular Direção do Olhar (PnP)
                face_3d = []
                face_2d = []
                # Nariz, Queixo, Olhos, Boca
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
                
                # Correção de versão do OpenCV
                retval = cv2.RQDecomp3x3(rmat)
                angles = retval[0]

                x_angle = angles[0] * 360
                y_angle = angles[1] * 360

                if y_angle < -10: data["direction"] = "ESQUERDA"
                elif y_angle > 10: data["direction"] = "DIREITA"
                elif x_angle < -10: data["direction"] = "BAIXO"
                elif x_angle > 10: data["direction"] = "CIMA"
                else: 
                    data["direction"] = "CENTER"
                    data["facing"] = True

                # Desenha caixa simples no rosto
                center_x = int(face_landmarks.landmark[1].x * img_w)
                center_y = int(face_landmarks.landmark[1].y * img_h)
                color = (0, 0, 255) if data["facing"] else (0, 255, 0)
                cv2.circle(frame, (center_x, center_y), 5, color, -1)

        return data