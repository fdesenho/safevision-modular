import os
import io
import cv2
from minio import Minio
import json

class MinioClient:
    def __init__(self):
        self.client = Minio(
            os.environ.get("MINIO_ENDPOINT", "minio:9000"),
            access_key=os.environ.get("MINIO_ACCESS_KEY", "minioadmin"),
            secret_key=os.environ.get("MINIO_SECRET_KEY", "minioadmin"),
            secure=False # HTTP interno
        )
        self.bucket_name = os.environ.get("MINIO_BUCKET", "safevision-evidence")
        self.external_url = os.environ.get("MINIO_EXTERNAL_PREFIX", "http://localhost:9000")
        
        self._setup_bucket()

    def _setup_bucket(self):
        """Cria o bucket e define como público (Read-Only)"""
        try:
            if not self.client.bucket_exists(self.bucket_name):
                self.client.make_bucket(self.bucket_name)
                print(f"🗄️ Bucket '{self.bucket_name}' criado.")
                
                # Política pública (permite que o Angular leia as fotos)
                policy = {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": [f"arn:aws:s3:::{self.bucket_name}/*"]
                        }
                    ]
                }
                self.client.set_bucket_policy(self.bucket_name, json.dumps(policy))
                print("🔓 Política de acesso público aplicada.")
        except Exception as e:
            print(f"❌ Erro ao configurar MinIO: {e}")

    def upload_frame(self, frame, filename):
        """Sobe o frame (imagem numpy) e retorna a URL pública"""
        try:
            # Converte OpenCV Frame -> Bytes (JPEG)
            _, buffer = cv2.imencode(".jpg", frame)
            io_buf = io.BytesIO(buffer)
            length = io_buf.getbuffer().nbytes

            self.client.put_object(
                self.bucket_name,
                filename,
                io_buf,
                length,
                content_type="image/jpeg"
            )
            
            # Retorna a URL que o Angular vai usar
            return f"{self.external_url}/{self.bucket_name}/{filename}"
            
        except Exception as e:
            print(f"❌ Erro no upload: {e}")
            return None