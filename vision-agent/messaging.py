import pika
import json
import time
import config
from telemetry import tracer             # 🟢 NEW: Import the tracer
from opentelemetry.propagate import inject # 🟢 NEW: Import context injector

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
        
        # 🟢 NEW: Wrap the publish logic in a Zipkin Span
        with tracer.start_as_current_span("rabbitmq_publish_raw_tracking") as span:
            max_retries = 3
            
            # 🟢 NEW: Inject B3 Context (Extracts Trace ID and creates header dictionary)
            headers = {}
            inject(headers)
            span.set_attribute("messaging.system", "rabbitmq")
            span.set_attribute("messaging.destination", config.QUEUE_NAME)
            
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
                            headers=headers   # 🟢 NEW: Pass B3 headers to RabbitMQ envelope
                        )
                    )
                    
                    # Se chegou aqui, enviou com sucesso
                    # 🟢 NEW: Added TraceID to the success log for easy visual validation
                    # Extrai o TraceID diretamente do Span em formato Hexadecimal (padrão Zipkin)
                    trace_id_hex = format(span.get_span_context().trace_id, '032x')
                    print(f"📤 [RabbitMQ] Evento enviado com sucesso! TraceID: {trace_id_hex}")
                    return 

                except (pika.exceptions.ConnectionClosed, pika.exceptions.StreamLostError, Exception) as e:
                    span.record_exception(e)  # 🟢 NEW: Record exception in Zipkin if network fails
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