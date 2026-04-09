flowchart TD 
Start((Início)) --> Capture["Leitura da Câmera"] 
Capture --> Detection{"Objeto Detectado?"} 
Detection -- Não --> Buffer["Limpar Buffer"] 
Buffer --> Capture 
Detection -- Sim --> Classify{"Classificação YOLO"} 
Classify -- "Faca / Tesoura / Celular" --> WeaponAction["Análise de Risco"] 
WeaponAction --> Snapshot["         
REGISTRO DE EVIDÊNCIA (Snapshot)"] 
Snapshot --> Upload["Upload para MinIO"] 
Upload --> PublishCrit["Publicar Alerta de OBJETO"] 
Classify -- Rosto --> FaceAnalysis["Análise MediaPipe"] 
FaceAnalysis --> StareCheck{"Olhando p/ Usuário?"} 
StareCheck -- Não --> ResetCounter["Zerar Contador"] 
ResetCounter --> Buffer 
StareCheck -- Sim --> PublishTelemetry["Publicar Telemetria"] 
PublishCrit --> EndNode((Notificar)) 
PublishTelemetry --> EndNode