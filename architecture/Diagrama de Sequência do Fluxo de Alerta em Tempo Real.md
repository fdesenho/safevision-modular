sequenceDiagram 
autonumber 
participant User as Usuário (Dashboard) 
participant GW as API Gateway 
participant Edge as Vision Agent (Python) 
participant MinIO as Object Storage 
participant RMQ as RabbitMQ 
participant Recog as Recognition Service 
participant Alert as Alert Service 
participant Ext as Notificadores 
Note over User, Edge: 1. Ativação do Sistema 
User->>GW: POST /start (Ativar Vigilância) 
GW->>Edge: Inicia Leitura da Câmera 
Note over Edge, Ext: 2. Cenário: Objeto de Risco (Faca/Celular) 
Edge->>Edge: Detecta Objeto (YOLOv8) 
Edge->>MinIO: Upload do Snapshot 
MinIO-->>Edge: Retorna URL Pública 
Edge->>RMQ: Publica Evento {type: WEAPON, url: ...} 
RMQ->>Recog: Consome Evento 
Recog->>RMQ: Confirma Alerta Crítico 
Note over Edge, Ext: 3. Cenário: Comportamento (Olhar/Aproximação) 
loop Telemetria Contínua 
Edge->>RMQ: Publica Telemetria {gaze: true, depth: 200} 
end 
RMQ->>Recog: Consome Telemetria 
Recog->>Recog: Analisa Histórico (Janela 5s) 
opt Limiar Atingido 
Recog->>RMQ: Publica Alerta Comportamental 
end 
Note over RMQ, Ext: 4. Disparo de Notificações 
RMQ->>Alert: Consome Alerta Confirmado 
par Notificação Simultânea 
Alert->>GW: Envia Frame STOMP (WebSocket) 
GW->>User: Push Visual (Pop-up/Mapa) 
and Canais Externos 
Alert->>Ext: Envia SMS/MMS (Twilio) 
Alert->>Ext: Envia Foto (Telegram) 
Alert->>Ext: Envia Relatório (Email SMTP) 
end