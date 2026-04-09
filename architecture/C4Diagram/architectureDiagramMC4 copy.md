graph TD
    %% === ESTILOS C4 MODEL ===
    classDef person fill:#08427b,stroke:#052e56,stroke-width:2px,color:#ffffff;
    classDef container fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#ffffff;
    classDef component fill:#85bbf0,stroke:#5d82a8,stroke-width:2px,color:#000000;
    classDef database fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#ffffff;
    classDef external fill:#999999,stroke:#6b6b6b,stroke-width:2px,color:#ffffff;

    %% === ATORES ===
    User(("👤 Security Guard<br/>[Person]"))
    Dev(("👨‍💻 Developer<br/>[Person]"))
    class User,Dev person

    %% === SISTEMAS EXTERNOS (NOTIFICAÇÕES) ===
    subgraph Ext [☁️ External Notification Providers]
        direction TB
        Telegram["✈️ Telegram API<br/>[System]"]
        SMS["📱 SMS Gateway<br/>[System]"]
        Email["📧 Email Service<br/>[System]"]
    end
    class Telegram,SMS,Email external

    %% === DEVOPS & INFRA (GITHUB/DOCKER) ===
    subgraph DevOps [🛠️ DevOps & CI/CD]
        direction TB
        GitHub["🐙 GitHub<br/>[System: VCS/Actions]"]
        DockerReg["🐳 Docker Registry<br/>[System: Artifacts]"]
    end
    class GitHub,DockerReg external

    %% === LIMITE DO SISTEMA SAFEVISION ===
    subgraph SafeVision [🛡️ SafeVision System - Docker Host]
        direction TB

        %% --- CAMADA DE APRESENTAÇÃO ---
        subgraph LayerFront [💻 Presentation Layer]
            Frontend["🖥️ Frontend Application<br/>[Container: Angular 21]<br/>Tech: RxStomp, Axios"]
        end
        class Frontend container

        %% --- CAMADA DE BORDA / EDGE ---
        subgraph LayerEdge [📍 Edge / IoT Layer]
            direction TB
            Camera["📹 Camera Device<br/>[Hardware]"]
            
            subgraph VisionAgent ["🐍 Vision Agent<br/>[Container: Python 3.11]"]
                VA_Flask["🌐 Flask Server<br/>[Component: API/MJPEG]"]
                VA_Core["🧠 Surveillance Engine<br/>[Component: OpenCV/YOLOv8]"]
                VA_Queue["🔄 Async Queue<br/>[Component]"]
            end
        end
        class Camera component
        class VisionAgent container
        class VA_Flask,VA_Core,VA_Queue component

        %% --- CAMADA DE BACKEND ---
        subgraph LayerBack [⚙️ Business Logic Layer]
            direction TB
            Eureka["🔍 Eureka Server<br/>[Container: Spring Boot]<br/>Tech: Service Discovery"]
            
            Gateway["⛩️ API Gateway<br/>[Container: Spring Boot 3]<br/>Tech: Spring Security, Spring Cloud Circuit Breaker,<br/>Resilience4j"]

            Auth["🔐 Auth Service<br/>[Container: Spring Boot 3]<br/>Tech: Spring Security, Spring Web,<br/>Spring Data JPA"]
            
            Recog["🧠 Recognition Service<br/>[Container: Java 21]<br/>Tech: Drools, Spring AMQP,<br/>Slf4j, Spring Cloud Sleuth"]
            
            Alert["🚨 Alert Service<br/>[Container: Spring Boot 3]<br/>Tech: Spring Web, Java Mail, Spring AMQP,<br/>Spring Data JPA, Slf4j, Spring Cloud Sleuth"]
        end
        class Gateway,Recog,Alert,Auth,Eureka container

        %% --- CAMADA DE DADOS E INFRA ---
        subgraph LayerData [💾 Data & Infra Layer]
            MinIO[("🗄️ MinIO<br/>[Container: Object Storage]")]
            RabbitMQ("🐇 RabbitMQ<br/>[Container: Message Broker]")
            Postgres[("🐘 PostgreSQL<br/>[Container: Database]")]
            ZipKin("📉 ZipKin<br/>[Container: Tracing]")
        end
        class MinIO,RabbitMQ,Postgres,ZipKin database
    end

    %% === REALCE DAS LINHAS DAS CAMADAS (Bordas Grossas) ===
    style SafeVision fill:none,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
    style LayerFront fill:#e8f4fa,stroke:#0d6efd,stroke-width:4px
    style LayerEdge fill:#fff0f5,stroke:#d63384,stroke-width:4px
    style LayerBack fill:#e9f7ef,stroke:#198754,stroke-width:4px
    style LayerData fill:#fff9e6,stroke:#ffc107,stroke-width:4px
    style Ext fill:#f0f0f0,stroke:#999,stroke-width:2px
    style DevOps fill:#e6e6fa,stroke:#663399,stroke-width:2px

    %% === FLUXO DEVOPS (Deploy) ===
    Dev ==>|"Commit/Push Code"| GitHub
    GitHub -->|"Trigger Build & Test"| GitHub
    GitHub -->|"Push Docker Image"| DockerReg
    DockerReg -.->|"Pull Images (Deploy)"| LayerBack
    DockerReg -.->|"Pull Images (Deploy)"| LayerFront
    DockerReg -.->|"Pull Images (Deploy)"| VisionAgent

    %% === SERVICE DISCOVERY (REGISTRO) ===
    Gateway & Auth & Recog & Alert -.->|"Register/Heartbeat"| Eureka

    %% === FLUXO 1: ATIVAÇÃO & AUTENTICAÇÃO ===
    User ==>|"1. Login / Ativar"| Frontend
    Frontend -->|"2. HTTPS Request"| Gateway
    Gateway -->|"2a. Auth/Validate JWT"| Auth
    Auth -->|"2b. User Data"| Postgres
    
    Gateway -->|"3. POST /start (Proxy)"| VA_Flask
    VA_Flask -->|"3a. Spawn Thread"| VA_Core
    VA_Core -->|"4. Power On"| Camera

    %% === FLUXO 2: VÍDEO STREAM ===
    Camera -->|"Raw Frames"| VA_Core
    VA_Core -->|"MJPEG Buffer"| VA_Flask
    VA_Flask -.->|"HTTP Stream (Display)"| Frontend

    %% === FLUXO 3: DETECÇÃO E EVIDÊNCIA ===
    VA_Core -- "Detect" --> VA_Core
    VA_Core -->|"5. Upload Image"| MinIO
    MinIO -- "Signed URL" --> VA_Core

    %% === FLUXO 4: MENSAGERIA E REGRAS ===
    VA_Core -->|"6. Enqueue Data"| VA_Queue
    VA_Queue -->|"7. Publica: vision_events"| RabbitMQ
    
    RabbitMQ -->|"8. Consume Event"| Recog
    Recog -->|"9. Apply Rules (Drools)"| Recog
    Recog -->|"10. Publica: Alerts"| RabbitMQ
    Recog -.->|"Trace Logs"| ZipKin

    %% === FLUXO 5: ALERTA E NOTIFICAÇÃO ===
    RabbitMQ -->|"11. Consume Alert"| Alert
    Alert -->|"12. Persist"| Postgres
    Alert -.->|"Trace Logs"| ZipKin
    
    Alert -->|"13. Send API"| Telegram
    Alert -->|"13. Send Gateway"| SMS
    Alert -->|"13. Send SMTP"| Email
    
    Telegram & SMS & Email -.->|"🚨 NOTIFICAÇÃO"| User
    
    Alert -->|"14. WebSocket Pub"| Gateway
    Gateway -->|"15. Push Pop-up"| Frontend

    %% === AJUSTE DE LINKS (Layout) ===
    linkStyle 7,8,9 stroke:#663399,stroke-width:1px,stroke-dasharray: 4 2;