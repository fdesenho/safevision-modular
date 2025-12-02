import os

# RabbitMQ
RABBITMQ_HOST = os.environ.get('RABBITMQ_HOST', 'rabbitmq')
USERNAME = os.environ.get('RABBITMQ_USERNAME', 'user')
PASSWORD = os.environ.get('RABBITMQ_PASSWORD', 'password')
QUEUE_NAME = os.environ.get('VISION_QUEUE_NAME', 'vision.raw.tracking')

# Camera
CAMERA_URL = os.environ.get('CAMERA_URL')
VIDEO_SOURCE = CAMERA_URL if CAMERA_URL else 'movement.mp4'