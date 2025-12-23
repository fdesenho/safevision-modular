# Usa a imagem oficial do YOLO (Versão CPU)
FROM ultralytics/ultralytics:latest-cpu

# Define o diretório de trabalho
WORKDIR /app

# Evita buffer de logs e problemas de encoding
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

# 1. Dependências do Sistema
RUN apt-get update && apt-get install -y \
    libgl1 \
    libsm6 \
    libxext6 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 2. Dependências Python
# Removemos conflito do protobuf (hack para MediaPipe + YOLO conviverem)
RUN pip uninstall -y protobuf && \
    pip install --no-cache-dir protobuf==3.20.3

# Instala o MediaPipe, libs do sistema e DE TESTE
RUN pip install --no-cache-dir \
    mediapipe==0.10.9 \
    pika \
    flask \
    flask-cors \
    minio \
    requests \
    pyserial \
    pynmea2 \
    pytest \
    pytest-mock

# 3. Cópia dos Arquivos
COPY . .

# 4. Comando de Execução (MUDANÇA AQUI)
# Usamos CMD em vez de ENTRYPOINT para permitir sobrescrita
# Ex: docker-compose run --rm vision-tests
CMD ["python", "main.py"]