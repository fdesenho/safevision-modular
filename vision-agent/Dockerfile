# Usa a imagem oficial do YOLO (Versão CPU para ser leve e rápida)
FROM ultralytics/ultralytics:latest-cpu

# Define o diretório de trabalho
WORKDIR /app

# Evita que o Python faça buffer de logs
ENV PYTHONUNBUFFERED=1

# 1. Dependências do Sistema
# Instala bibliotecas gráficas necessárias para o MediaPipe e OpenCV
RUN apt-get update && apt-get install -y \
    libgl1 \
    libsm6 \
    libxext6 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 2. Dependências Python
# ADICIONADO: 'minio' (Para upload de fotos)
RUN pip install --no-cache-dir pika mediapipe flask flask-cors minio

# 3. Cópia dos Arquivos
# Copia todo o conteúdo da pasta vision-agent (main.py, config.py, storage.py, etc.)
COPY . .

# 4. Comando de Execução
ENTRYPOINT ["python", "main.py"]