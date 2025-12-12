from minio import Minio
import io
import config
import json  # Import necessário para criar a política

class MinioClient:
    def __init__(self):
        self.client = None
        try:
            # Acessando direto de config.VARIAVEL
            self.client = Minio(
                config.MINIO_ENDPOINT,
                access_key=config.MINIO_ACCESS_KEY,
                secret_key=config.MINIO_SECRET_KEY,
                secure=False
            )
            
            # Garante que o bucket existe
            bucket = config.MINIO_BUCKET
            if not self.client.bucket_exists(bucket):
                self.client.make_bucket(bucket)
                print(f"✅ [MinIO] Bucket '{bucket}' criado.")
                # Se acabou de criar, aplica a política
                self._set_public_policy(bucket)
            else:
                # Mesmo se já existe, reaplica a política para corrigir buckets antigos
                self._set_public_policy(bucket)
            
        except Exception as e:
            print(f"❌ [MinIO] Erro ao conectar: {e}")

    def _set_public_policy(self, bucket_name):
        """
        Define o bucket como READ-ONLY para o mundo (Público).
        Isso permite acessar a imagem direto pela URL sem login/assinatura.
        """
        try:
            policy = {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]}, # "*" significa qualquer um (público)
                        "Action": ["s3:GetObject"],   # Apenas permissão de LEITURA
                        "Resource": [f"arn:aws:s3:::{bucket_name}/*"] # Em todos os arquivos
                    }
                ]
            }
            self.client.set_bucket_policy(bucket_name, json.dumps(policy))
            print(f"🔓 [MinIO] Política pública aplicada ao bucket '{bucket_name}'")
        except Exception as e:
            print(f"⚠️ [MinIO] Falha ao aplicar política pública: {e}")

    def upload_frame(self, frame_bytes, filename):
        if not self.client:
            return None
            
        try:
            # Se frame_bytes for numpy array (imagem OpenCV), converte para bytes
            import numpy as np
            import cv2
            if isinstance(frame_bytes, np.ndarray):
                ret, buffer = cv2.imencode('.jpg', frame_bytes)
                if ret:
                    frame_bytes = buffer.tobytes()

            # Prepara o stream
            data_stream = io.BytesIO(frame_bytes)
            file_size = len(frame_bytes)
            
            # Upload
            self.client.put_object(
                config.MINIO_BUCKET,
                filename,
                data_stream,
                file_size,
                content_type='image/jpeg'
            )
            
            # Retorna a URL pública
            return f"{config.MINIO_EXTERNAL_PREFIX}/{config.MINIO_BUCKET}/{filename}"
            
        except Exception as e:
            print(f"❌ [MinIO] Erro upload: {e}")
            return None