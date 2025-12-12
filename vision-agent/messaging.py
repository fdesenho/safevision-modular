import pika
import json
import time
import config

class RabbitMQClient:
    def __init__(self):
        self.connection = None
        self.channel = None
        # Tenta conectar imediatamente ao instanciar
        self.connect()

    def connect(self):
        """
        Estabelece a conexão com retry infinito.
        Configurado com heartbeat=0 para suportar picos de CPU da IA.
        """
        while True:
            try:
                credentials = pika.PlainCredentials(config.USERNAME, config.PASSWORD)
                
                parameters = pika.ConnectionParameters(
                    host=config.RABBITMQ_HOST,
                    port=5672,
                    credentials=credentials,
                    # 🔥 FIX CRÍTICO: heartbeat=0 desativa a verificação de pulso.
                    # Isso impede que o RabbitMQ derrube a conexão quando o Python
                    # congela momentaneamente processando a imagem pesada ou fazendo upload.
                    heartbeat=0, 
                    blocked_connection_timeout=300
                )
                
                self.connection = pika.BlockingConnection(parameters)
                self.channel = self.connection.channel()
                
                # Garante que a fila existe (durable=True para persistência)
                self.channel.queue_declare(queue=config.QUEUE_NAME, durable=True)
                
                print(f"✅ [RabbitMQ] Conectado em {config.RABBITMQ_HOST}")
                return # Sai do loop se conectar com sucesso
                
            except Exception as e:
                print(f"❌ [RabbitMQ] Erro de conexão: {e}. Tentando em 5s...")
                time.sleep(5)

    def send_event(self, payload):
        """Envia mensagem com resiliência: se falhar, reconecta e tenta de novo"""
        max_retries = 3
        
        for attempt in range(max_retries):
            try:
                # Verifica se precisa reconectar antes de tentar enviar
                if self.connection is None or self.connection.is_closed:
                    print("⚠️ [RabbitMQ] Conexão fechada/perdida. Reconectando...")
                    self.connect()

                self.channel.basic_publish(
                    exchange='',
                    routing_key=config.QUEUE_NAME,
                    body=json.dumps(payload),
                    properties=pika.BasicProperties(
                        delivery_mode=2,  # Mensagem persistente (salva em disco)
                    )
                )
                
                # Se chegou aqui, enviou com sucesso
                print(f"📤 [RabbitMQ] Evento enviado com sucesso!")
                return 

            except (pika.exceptions.ConnectionClosed, pika.exceptions.StreamLostError, Exception) as e:
                print(f"⚠️ [RabbitMQ] Falha ao enviar (Tentativa {attempt+1}/{max_retries}): {e}")
                # Força reset da conexão para recriar na próxima volta do loop
                self.connection = None 
                time.sleep(1)
        
        print("❌ [RabbitMQ] ERRO CRÍTICO: Mensagem descartada após várias tentativas.")

    def close(self):
        """Encerra a conexão de forma limpa (útil para o toggle/off)"""
        try:
            if self.connection and not self.connection.is_closed:
                self.connection.close()
                print("🔌 [RabbitMQ] Conexão encerrada.")
        except Exception:
            pass