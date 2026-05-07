import os
from dotenv import load_dotenv

load_dotenv()

# --- RabbitMQ Connection ---
RABBITMQ_HOST = os.environ.get('RABBITMQ_HOST', 'rabbitmq')
USERNAME = os.environ.get('RABBITMQ_USERNAME', 'usersafevision')
PASSWORD = os.environ.get('RABBITMQ_PASSWORD', 'passwordsafevision')

# --- Queues ---
QUEUE_NAME = os.environ.get('VISION_QUEUE_NAME', 'safevision.vision.raw.tracking')

# --- MinIO ---
MINIO_ENDPOINT = os.environ.get('MINIO_ENDPOINT', 'minio:9000')
MINIO_ACCESS_KEY = os.environ.get('MINIO_ACCESS_KEY', 'minioadmin')
MINIO_SECRET_KEY = os.environ.get('MINIO_SECRET_KEY', 'minioadmin')
MINIO_BUCKET = os.environ.get('MINIO_BUCKET_NAME', 'safevision-evidence')
MINIO_EXTERNAL_PREFIX = os.environ.get('MINIO_EXTERNAL_PREFIX', 'http://192.168.112.1:9000')

# 🟢 AJUSTADO: Roteamento Interno Docker e Segurança
ZIPKIN_URL = os.environ.get("ZIPKIN_URL", "http://zipkin:9411")

# Usamos o gateway-service pois ele gerencia a segurança e o roteamento na porta 8080
BACKEND_SIMULATE_URL = os.environ.get("BACKEND_SIMULATE_URL", "http://gateway-service:8080/recognition/simulate")

# Chave para assinar o token de benchmark
SAFEVISION_JWT_SECRET = os.environ.get("SAFEVISION_JWT_SECRET", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970")