# SafeVision

![SafeVision Banner](safevision-ui/public/logo3.jpeg)

> **Sistema de Vigilância Inteligente Híbrido para Câmeras Corporais (Body-Worn Cameras) com Processamento na Borda.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-blue?style=flat-square&logo=python)](https://www.python.org/)
[![Angular](https://img.shields.io/badge/Angular-21-dd0031?style=flat-square&logo=angular)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![Testing Strategy](https://img.shields.io/badge/Testing_Strategy-Documentation-2ea44f?style=flat-square&logo=junit5)](TESTING.md)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

---

## 🎯 Sobre o Projeto

O **SafeVision** aborda o problema crítico da latência e do consumo de banda em sistemas de vigilância policial e de segurança privada. Diferente das soluções tradicionais que enviam streams de vídeo brutos para a nuvem, o SafeVision utiliza uma arquitetura **Edge AI**.

O **Vision Agent** (baseado em YOLOv8 e MediaPipe) roda localmente no dispositivo de captura, processando frames em tempo real para detectar ameaças (armas) e comportamentos suspeitos (olhar fixo/staring). Apenas metadados, alertas e snapshots de evidência são transmitidos para o servidor, garantindo eficiência e rapidez na resposta.

### Principais Funcionalidades
* 🔫 **Detecção de Armas:** Identificação em tempo real de armas de fogo e armas brancas.
* 👁️ **Análise Comportamental:** Detecção de "Stare" (olhar fixo persistente) e Loitering.
* 📍 **Geolocalização:** Rastreamento GPS sincronizado com o evento de alerta.
* ⚡ **Alertas Instantâneos:** Notificações via WebSocket (Dashboard), Telegram, SMS (Twilio) e E-mail.
* 🛡️ **Evidência Segura:** Armazenamento de snapshots criptografados via Object Storage (MinIO).

---

## 🏗️ Arquitetura

O sistema segue uma arquitetura de microsserviços orientada a eventos, utilizando a abordagem **C4 Model** (Nível de Container). Abaixo, o diagrama ilustra a interação entre os componentes de borda (Edge), o backend (Spring Cloud) e a infraestrutura de dados.

```mermaid
graph TD
    %% === ESTILOS C4 MODEL ===
    classDef person fill:#08427b,stroke:#052e56,stroke-width:2px,color:#ffffff;
    classDef container fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#ffffff;
    classDef component fill:#85bbf0,stroke:#5d82a8,stroke-width:2px,color:#000000;
    classDef database fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#ffffff;
    classDef external fill:#999999,stroke:#6b6b6b,stroke-width:2px,color:#ffffff;

    %% === ATORES ===
    User(("👤 Segurança<br/>[Pessoa]"))
    Dev(("👨‍💻 Desenvolvedor<br/>[Pessoa]"))
    class User,Dev person

    %% === SISTEMAS EXTERNOS (NOTIFICAÇÕES) ===
    subgraph Ext [☁️ Provedores Externos de Notificação]
        direction TB
        Telegram["✈️ API Telegram<br/>[Sistema]"]
        SMS["📱 Gateway SMS<br/>[Sistema]"]
        Email["📧 Serviço de Email<br/>[Sistema]"]
    end
    class Telegram,SMS,Email external

    %% === DEVOPS & INFRA (GITHUB/DOCKER) ===
    subgraph DevOps [🛠️ DevOps e CI/CD]
        direction TB
        GitHub["🐙 GitHub<br/>[Sistema: VCS/Actions]"]
        DockerReg["🐳 Docker Registry<br/>[Sistema: Artefatos]"]
    end
    class GitHub,DockerReg external

    %% === LIMITE DO SISTEMA SAFEVISION ===
    subgraph SafeVision [🛡️ Sistema SafeVision - Docker Host]
        direction TB

        %% --- CAMADA DE APRESENTAÇÃO ---
        subgraph LayerFront [💻 Camada de Apresentação]
            Frontend["🖥️ Aplicação Frontend<br/>[Container: Angular 21]<br/>Tech: RxStomp, Axios"]
        end
        class Frontend container

        %% --- CAMADA DE BORDA / EDGE ---
        subgraph LayerEdge [📍 Camada Edge / IoT]
            direction TB
            Camera["📹 Dispositivo de Câmera<br/>[Hardware]"]
            
            subgraph VisionAgent ["🐍 Agente de Visão<br/>[Container: Python 3.11]"]
                VA_Flask["🌐 Servidor Flask<br/>[Componente: API/MJPEG]"]
                VA_Core["🧠 Motor de Vigilância<br/>[Componente: OpenCV/YOLOv8]"]
                VA_Queue["🔄 Fila Assíncrona<br/>[Componente]"]
            end
        end
        class Camera component
        class VisionAgent container
        class VA_Flask,VA_Core,VA_Queue component

        %% --- CAMADA DE BACKEND ---
        subgraph LayerBack [⚙️ Camada de Lógica de Negócio]
            direction TB
            Eureka["🔍 Servidor Eureka<br/>[Container: Spring Boot]<br/>Tech: Descoberta de Serviço"]
            
            Gateway["⛩️ API Gateway<br/>[Container: Spring Boot 3]<br/>Tech: Spring Security, Spring Cloud Circuit Breaker,<br/>Resilience4j"]

            Auth["🔐 Serviço de Autenticação<br/>[Container: Spring Boot 3]<br/>Tech: Spring Security, Spring Web,<br/>Spring Data JPA"]
            
            Recog["🧠 Serviço de Reconhecimento<br/>[Container: Java 21]<br/>Tech: Drools, Spring AMQP,<br/>Slf4j, Spring Cloud Sleuth"]
            
            Alert["🚨 Serviço de Alerta<br/>[Container: Spring Boot 3]<br/>Tech: Spring Web, Java Mail, Spring AMQP,<br/>Spring Data JPA, Slf4j, Spring Cloud Sleuth"]
        end
        class Gateway,Recog,Alert,Auth,Eureka container

        %% --- CAMADA DE DADOS E INFRA ---
        subgraph LayerData [💾 Camada de Dados e Infra]
            MinIO[("🗄️ MinIO<br/>[Container: Object Storage]")]
            RabbitMQ("🐇 RabbitMQ<br/>[Container: Broker de Mensagens]")
            Postgres[("🐘 PostgreSQL<br/>[Container: Banco de Dados]")]
            ZipKin("📉 ZipKin<br/>[Container: Rastreamento]")
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
    Dev ==>|"Commit/Push Código"| GitHub
    GitHub -->|"Dispara Build e Teste"| GitHub
    GitHub -->|"Push Imagem Docker"| DockerReg
    DockerReg -.->|"Pull Imagens (Deploy)"| LayerBack
    DockerReg -.->|"Pull Imagens (Deploy)"| LayerFront
    DockerReg -.->|"Pull Imagens (Deploy)"| VisionAgent

    %% === SERVICE DISCOVERY (REGISTRO) ===
    Gateway & Auth & Recog & Alert -.->|"Registro/Heartbeat"| Eureka

    %% === FLUXO 1: ATIVAÇÃO & AUTENTICAÇÃO ===
    User ==>|"1. Login / Ativar"| Frontend
    Frontend -->|"2. Requisição HTTPS"| Gateway
    Gateway -->|"2a. Autenticação/Validar JWT"| Auth
    Auth -->|"2b. Dados Usuário"| Postgres
    
    Gateway -->|"3. POST /start (Proxy)"| VA_Flask
    VA_Flask -->|"3a. Inicia Thread"| VA_Core
    VA_Core -->|"4. Ligar"| Camera

    %% === FLUXO 2: VÍDEO STREAM ===
    Camera -->|"Frames Brutos"| VA_Core
    VA_Core -->|"Buffer MJPEG"| VA_Flask
    VA_Flask -.->|"Stream HTTP (Visualização)"| Frontend

    %% === FLUXO 3: DETECÇÃO E EVIDÊNCIA ===
    VA_Core -- "Detectar" --> VA_Core
    VA_Core -->|"5. Upload Imagem"| MinIO
    MinIO -- "URL Assinada" --> VA_Core

    %% === FLUXO 4: MENSAGERIA E REGRAS ===
    VA_Core -->|"6. Enfileirar Dados"| VA_Queue
    VA_Queue -->|"7. Publica: vision_events"| RabbitMQ
    
    RabbitMQ -->|"8. Consome Evento"| Recog
    Recog -->|"9. Aplica Regras (Drools)"| Recog
    Recog -->|"10. Publica: Alertas"| RabbitMQ
    Recog -.->|"Logs de Rastreamento"| ZipKin

    %% === FLUXO 5: ALERTA E NOTIFICAÇÃO ===
    RabbitMQ -->|"11. Consome Alerta"| Alert
    Alert -->|"12. Persiste"| Postgres
    Alert -.->|"Logs de Rastreamento"| ZipKin
    
    Alert -->|"13. Envia API"| Telegram
    Alert -->|"13. Envia Gateway"| SMS
    Alert -->|"13. Envia SMTP"| Email
    
    Telegram & SMS & Email -.->|"🚨 NOTIFICAÇÃO"| User
    
    Alert -->|"14. Pub WebSocket"| Gateway
    Gateway -->|"15. Push Pop-up"| Frontend

    %% === AJUSTE DE LINKS (Layout) ===
    linkStyle 7,8,9 stroke:#663399,stroke-width:1px,stroke-dasharray: 4 2;