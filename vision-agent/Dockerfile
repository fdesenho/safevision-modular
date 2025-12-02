FROM ultralytics/ultralytics:latest-cpu
WORKDIR /app
RUN apt-get update && apt-get install -y libgl1 libsm6 libxext6 ffmpeg && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir pika mediapipe flask

# Copia TUDO da pasta atual (incluindo subpastas 'detectors/')
COPY . .

# Executa o novo arquivo principal
ENTRYPOINT ["python", "main.py"]