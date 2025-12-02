import pika
import json
import config

class RabbitMQClient:
    def __init__(self):
        self.connection = None
        self.channel = None
        self.connect()

    def connect(self):
        try:
            credentials = pika.PlainCredentials(config.USERNAME, config.PASSWORD)
            self.connection = pika.BlockingConnection(
                pika.ConnectionParameters(host=config.RABBITMQ_HOST, port=5672, credentials=credentials)
            )
            self.channel = self.connection.channel()
            self.channel.queue_declare(queue=config.QUEUE_NAME, durable=True)
            print(f"✅ [RabbitMQ] Conectado na fila: {config.QUEUE_NAME}")
        except Exception as e:
            print(f"❌ [RabbitMQ] Erro de conexão: {e}")

    def send_event(self, event_data):
        if not self.channel or self.channel.is_closed:
            self.connect()
        
        try:
            self.channel.basic_publish(
                exchange='',
                routing_key=config.QUEUE_NAME,
                body=json.dumps(event_data)
            )
            # Log opcional para debug (pode comentar em produção)
            if event_data.get("hasWeapon"):
                print(f"🔫 ARMA ENVIADA!")
        except Exception as e:
            print(f"❌ Erro ao enviar mensagem: {e}")