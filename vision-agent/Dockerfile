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
# Remove conflito do protobuf (MediaPipe + YOLO)
RUN pip uninstall -y protobuf && \
    pip install --no-cache-dir protobuf==3.20.3

# Instala libs principais
RUN pip install --no-cache-dir \
    python-dotenv \
    py_eureka_client \
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

# Instala OpenTelemetry (Observabilidade)
RUN pip install --no-cache-dir \
    opentelemetry-api \
    opentelemetry-sdk \
    opentelemetry-exporter-zipkin \
    opentelemetry-propagator-b3

# 🟢 NOVO: Dependências para Benchmarking, Segurança e Relatórios (ADR-018)
RUN pip install --no-cache-dir \
    PyJWT \
    pandas \
    matplotlib

# 3. Cópia dos Arquivos
COPY . .

# 4. Comando de Execução
CMD ["python", "main.py"]