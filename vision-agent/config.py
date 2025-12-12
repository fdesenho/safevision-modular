import os

# --- RabbitMQ Connection ---
# O segundo parametro 'rabbitmq' é o valor padrão se a variavel de ambiente falhar
RABBITMQ_HOST = os.environ.get('RABBITMQ_HOST', 'rabbitmq')

# Aqui corrigimos: Se não achar a env, usa o valor que estava no seu .env (usersafevision)
USERNAME = os.environ.get('RABBITMQ_USERNAME', 'usersafevision')
PASSWORD = os.environ.get('RABBITMQ_PASSWORD', 'passwordsafevision')

# --- Queues ---
QUEUE_NAME = os.environ.get('VISION_QUEUE_NAME', 'safevision.vision.raw.tracking')


# --- MinIO (Armazenamento de Imagens) ---
MINIO_ENDPOINT = os.environ.get('MINIO_ENDPOINT', 'minio:9000')
MINIO_ACCESS_KEY = os.environ.get('MINIO_ACCESS_KEY', 'minioadmin')
MINIO_SECRET_KEY = os.environ.get('MINIO_SECRET_KEY', 'minioadmin')
MINIO_BUCKET = os.environ.get('MINIO_BUCKET_NAME', 'safevision-evidence')
MINIO_EXTERNAL_PREFIX = os.environ.get('MINIO_EXTERNAL_PREFIX', 'http://192.168.112.1:9000')