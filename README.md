# SafeVision

![SafeVision Banner](safevision-ui/public/logo3.jpeg)

> **Sistema de Vigilância Inteligente Híbrido para Câmeras Corporais (Body-Worn Cameras) com Processamento na Borda.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-blue?style=flat-square&logo=python)](https://www.python.org/)
[![Angular](https://img.shields.io/badge/Angular-21-dd0031?style=flat-square&logo=angular)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Docs-blueviolet?style=flat-square)](docs/adr/README.md)
[![Project Status](https://img.shields.io/badge/Status-Phase_1:_Stabilization-2ea44f?style=flat-square)](https://github.com/users/fdesenho/projects/1)
[![Testing Strategy](https://img.shields.io/badge/Testing_Strategy-Documentation-2ea44f?style=flat-square&logo=junit5)](TESTING.md)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

---

## 🎯 Executive Summary

O **SafeVision** é uma resposta de engenharia ao problema de latência em segurança pública. Em vez de transmitir streams pesados para a nuvem, movemos a inteligência para a borda (**Edge AI**).

O sistema processa vídeo localmente (YOLOv8/MediaPipe), detecta ameaças em milissegundos e transmite apenas metadados e evidências criptografadas, garantindo eficiência de banda e resposta em tempo real.

### Principais Funcionalidades
* 🔫 **Detecção de Armas:** Identificação em tempo real de armas de fogo e armas brancas.
* 👁️ **Análise Comportamental:** Detecção de "Stare" (olhar fixo persistente) e Loitering.
* 📍 **Geolocalização:** Rastreamento GPS sincronizado com o evento de alerta.
* ⚡ **Alertas Instantâneos:** Notificações via WebSocket (Dashboard), Telegram, SMS (Twilio) e E-mail.
* 🛡️ **Evidência Segura:** Armazenamento de snapshots criptografados via Object Storage (MinIO).

---

## 🚀 Como Rodar o Projeto (Quick Start)

Este projeto utiliza **Docker Compose** para orquestrar todos os microsserviços, banco de dados e frontend.

### Pré-requisitos
* [Docker](https://www.docker.com/products/docker-desktop) & Docker Compose instalados.
* [Java JDK 21](https://adoptium.net/) (Opcional, apenas para desenvolvimento local fora do Docker).
* 4GB de RAM livre (mínimo recomendado para subir a stack completa).

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/fdesenho/safevision-modular.git](https://github.com/fdesenho/safevision-modular.git)
    cd safevision-modular
    ```

2.  **Configuração de Ambiente:**
    Crie o arquivo `.env` na raiz baseado no exemplo (segredos não são versionados):
    ```bash
    cp .env.example .env
    ```

3.  **Build e Execução (Modo Infra + Apps):**
    O comando abaixo irá compilar os JARs (via multi-stage build), construir as imagens e subir os containers.
    ```bash
    docker-compose up -d --build
    ```
    > *Nota: A primeira execução pode demorar alguns minutos para baixar as dependências do Maven e as imagens do YOLO.*

4.  **Verifique os Serviços:**
    Aguarde até que todos os containers estejam com status `Healthy`. Acesse os serviços principais:

    | Serviço | URL de Acesso | Credenciais Padrão (Dev) |
    | :--- | :--- | :--- |
    | **Frontend Dashboard** | [http://localhost:4200](http://localhost:4200) | `admin` / `admin` |
    | **API Gateway** | [http://localhost:8080](http://localhost:8080) | - |
    | **Eureka Discovery** | [http://localhost:8761](http://localhost:8761) | - |
    | **RabbitMQ Admin** | [http://localhost:15672](http://localhost:15672) | `guest` / `guest` |
    | **MinIO Console** | [http://localhost:9001](http://localhost:9001) | `minioadmin` / `minioadmin` |
    | **Zipkin Tracing** | [http://localhost:9411](http://localhost:9411) | - |

---

## 📐 Architecture Decision Records (ADR)

Seguindo as boas práticas de arquitetura de software, todas as decisões estruturantes do projeto estão documentadas. Isso garante o histórico, o contexto e a justificativa técnica para cada escolha de engenharia.

> **[📂 Acessar Diretório de Documentação Completo](docs/adr/README.md)**

| ID | Decisão Arquitetural | Status | Contexto / Racional |
| :--- | :--- | :---: | :--- |
| [ADR-001](docs/adr/001-microservices-architecture.md) | **Microservices Architecture** | ✅ Aceito | Desacoplamento do *Core* (Java) e *Edge AI* (Python) para escala independente. |
| [ADR-002](docs/adr/002-edge-computing-strategy.md) | **Edge Computing First** | ✅ Aceito | Processamento na origem para eliminar latência e reduzir custos de banda 4G/5G. |
| [ADR-003](docs/adr/003-async-communication-rabbitmq.md) | **Async Communication** | ✅ Aceito | Garantia de entrega de alertas e resiliência via RabbitMQ. |
| [ADR-004](docs/adr/004-secret-management-vault.md) | **Secret Management (Vault)** | 📅 Plan | (Fase 2) Centralização de segredos e segurança Zero Trust via HashiCorp Vault. |
| [ADR-005](docs/adr/005-orchestration-k8s-k3s.md) | **Orchestration (K8s & K3s)** | 📅 Plan | (Fase 2) Gestão unificada de containers na Cloud (K8s) e dispositivos Edge (K3s). |
| [ADR-006](docs/adr/006-distributed-caching-redis.md) | **Distributed Caching (Redis)** | 📅 Plan | (Fase 2) Otimização de performance para dados de sessão e estados globais. |
| [ADR-007](docs/adr/007-observability-stack.md) | **Observability Stack (PLG)** | 📅 Plan | (Fase 2) Monitoramento integral com Prometheus, Loki e Grafana. |
| [ADR-008](docs/adr/008-mobile-framework-flutter.md) | **Mobile Framework (Flutter)** | 🔮 Futuro | (Fase 3) App multi-plataforma para oficiais em campo com base de código única. |
| [ADR-009](docs/adr/009-frontend-framework-angular.md) | **Frontend Framework** | ✅ Aceito | Dashboard reativo com Angular 21 + RxJS para alta densidade de eventos. |
| [ADR-010](docs/adr/010-testing-strategy-junit.md) | **Testing Strategy** | ✅ Aceito | Qualidade via JUnit 5 e Testcontainers para validação de integração real. |
| [ADR-011](docs/adr/011-api-documentation-openapi.md) | **API Documentation** | ✅ Aceito | Contrato oficial via OpenAPI 3.0 para integração consistente Front/Back/Mobile. |
| [ADR-012](docs/adr/012-cicd-pipeline-github-actions.md) | **CI/CD Pipeline** | ✅ Aceito | Automação de build, teste e publicação de imagens via GitHub Actions. |
| [ADR-013](docs/adr/013-advanced-computer-vision-strategy.md) | **Advanced AI Strategy** | ✅ Aceito | Visão térmica e Man-down utilizando modelos de Pose Estimation (YOLOv8). |
| [ADR-014](docs/adr/014-5g-integration-and-network-slicing.md) | **5G & Network Slicing** | ✅ Aceito | Garantia de banda e latência mínima para tráfego de missão crítica. |
| [ADR-015](docs/adr/015-safevision-analytics-bi-architecture.md) | **Analytics (BI)** | ✅ Aceito | Arquitetura Medallion para inteligência de dados e policiamento preditivo. |
| [ADR-016](docs/adr/016-semantic-video-forensic-search.md) | **Forensic Search** | ✅ Aceito | Busca vetorial semântica em storage (pgvector) para celeridade investigativa. |
| [ADR-017](docs/adr/017-multi-tenant-data-federation.md) | **Multi-tenancy & Federation** | ✅ Aceito | Isolamento lógico rigoroso e cooperação federada entre agências. |
| [ADR-018](docs/adr/018-performance-benchmarking-strategy.md) | **Performance Benchmarking** | ✅ Aceito | Validação empírica da redução de latência Cloud vs Edge. |
| [ADR-019](docs/adr/019-chaos-engineering-strategy.md) | **Chaos Engineering** | ✅ Aceito | Testes de resiliência e failover através de injeção de falhas controladas. |
| [ADR-020](docs/adr/020-devsecops-hardening-strategy.md) | **DevSecOps & Hardening** | ✅ Aceito | Segurança da cadeia de suprimentos de software e scan de vulnerabilidades. |
| [ADR-021](docs/adr/021-privacy-compliance-lgpd.md) | **Privacy & Ethics (LGPD)** | ✅ Aceito | Governança ética de dados e anonimização de imagens sensíveis. |

---

## 🗺️ Roadmap Estratégico & Sprint Planning

O desenvolvimento do SafeVision segue uma estratégia de evolução incremental, onde cada marco técnico é suportado por uma decisão arquitetural documentada e focado na validação de KPIs críticos.

### ✅ Fase 1: Estabilização (MVP)
> **Status:** Parcialmente Concluído. Foco na entrega do Core Value e validação empírica da baixa latência.

| Tarefa (Issue) | Prioridade | Status | Racional Técnico & ADR Link |
| :--- | :---: | :---: | :--- |
| **Core Architecture** | 🔥 Critical | ✅ Done | [ADR-001](docs/adr/001-microservices-architecture.md) - Fundação para escala independente. |
| **Edge AI (YOLOv8)** | 🔥 Critical | ✅ Done | [ADR-002](docs/adr/002-edge-computing-strategy.md) - Processamento local (Edge First). |
| **Async Comm (RabbitMQ)** | 🔥 Critical | ✅ Done | [ADR-003](docs/adr/003-async-communication-rabbitmq.md) - Desacoplamento e resiliência. |
| **Frontend Dashboard** | 🟡 Medium | ✅ Done | [ADR-009](docs/adr/009-frontend-framework-angular.md) - Dashboard reativo com Angular 21. |
| **Tests Coverage (80%)** | 🔥 Critical | 🚧 Doing | [ADR-010](docs/adr/010-testing-strategy-junit.md) - Qualidade via Testcontainers. |
| **Performance Benchmark** | 🔥 Critical | 📅 Todo | [ADR-018](docs/adr/018-performance-benchmarking-strategy.md) - Validação de latência Cloud vs Edge. |
| **API Docs (Swagger)** | 🟡 Medium | 📅 Todo | [ADR-011](docs/adr/011-api-documentation-openapi.md) - Contrato oficial OpenAPI 3.0. |

### 🏗️ Fase 2: Otimização de Infraestrutura
> **Status:** Planejado. Transformação do MVP em um sistema resiliente, seguro e orquestrado.

| Tarefa (Issue) | Deadline | Racional Técnico & ADR Link |
| :--- | :---: | :--- |
| **Gestão de Segredos** | 14/02/2026 | [ADR-004](docs/adr/004-secret-management-vault.md) - Vault para segurança Zero Trust. |
| **Pipeline CI/CD** | 28/02/2026 | [ADR-012](docs/adr/012-cicd-pipeline-github-actions.md) - Automação total via GitHub Actions. |
| **Chaos Engineering** | 15/03/2026 | [ADR-019](docs/adr/019-chaos-engineering-strategy.md) - Testes de resiliência e injeção de falhas. |
| **Hardening & DevSecOps** | 30/03/2026 | [ADR-020](docs/adr/020-devsecops-hardening-strategy.md) - Scan de imagens e análise estática. |
| **Migração Kubernetes** | 25/03/2026 | [ADR-005](docs/adr/005-orchestration-k8s-k3s.md) - Orquestração Híbrida (K8s/K3s). |
| **Monitoramento (PLG)** | 10/04/2026 | [ADR-007](docs/adr/007-observability-stack.md) - Observabilidade total (Grafana Stack). |
| **Caching (Redis)** | 20/04/2026 | [ADR-006](docs/adr/006-distributed-caching-redis.md) - Alta performance para dados quentes. |

### 📱 Fase 3: Expansão de Recursos
> **Status:** Backlog. Foco na mobilidade operacional e hardware de visão avançada.

| Tarefa (Issue) | Previsão | Escopo & ADR Link |
| :--- | :---: | :--- |
| **App Mobile** | Jun/2026 | [ADR-008](docs/adr/008-mobile-framework-flutter.md) - Companion App para oficiais. |
| **Visão Avançada** | Jun/2026 | [ADR-013](docs/adr/013-advanced-computer-vision-strategy.md) - Térmica e Pose Estimation. |
| **Integração 5G** | Jul/2026 | [ADR-014](docs/adr/014-5g-integration-and-network-slicing.md) - QoS via Network Slicing. |

### 📊 Fase 4: Intelligence & Analytics
> **Status:** Visão de Longo Prazo. Foco em inteligência preditiva e governança ética de dados.

| Tarefa (Issue) | Previsão | Racional Estratégico & ADR Link |
| :--- | :---: | :--- |
| **SafeVision Analytics** | Ago/2026 | [ADR-015](docs/adr/015-safevision-analytics-bi-architecture.md) - Dashboards BI (Medallion Arch). |
| **Análise Forense** | Set/2026 | [ADR-016](docs/adr/016-semantic-video-forensic-search.md) - Busca vetorial semântica. |
| **Federação de Dados** | Out/2026 | [ADR-017](docs/adr/017-multi-tenant-data-federation.md) - Arquitetura multi-tenant federada. |
| **Privacy & LGPD** | Nov/2026 | [ADR-021](docs/adr/021-privacy-compliance-lgpd.md) - Anonimização ética e proteção de dados. |

---

## 🏗️ Diagrama de Arquitetura (C4 Container)

Abaixo, a visão lógica dos microsserviços e suas interações com o mundo externo e dispositivos de borda.

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
 ```

---

## Camadas da Arquitetura

### 1. Presentation Layer (Apresentação)
*   **Frontend Application**: Single Page Application (SPA) desenvolvida em **Angular 21**. Responsável pela interação com o usuário, exibição do stream de vídeo e alertas em tempo real.

### 2. Edge / IoT Layer (Borda)
*   **Vision Agent**: Container **Python 3.11** rodando próximo à câmera. Responsável pelo processamento pesado de visão computacional (**OpenCV**, **YOLOv8**).
*   **Camera Device**: Hardware físico de captura.

### 3. Business Logic Layer (Backend - Spring Cloud)
A camada de backend foi enriquecida com componentes da stack Spring Cloud para resiliência e observabilidade.

*   **Eureka Server**: Servidor de descoberta de serviços (Service Discovery). Permite que os microserviços se encontrem dinamicamente.
*   **API Gateway**: Ponto de entrada seguro (**Spring Boot 3**, **Spring Security**). Implementa **Resilience4j Circuit Breaker** para falhar graciosamente em caso de sobrecarga.
*   **Auth Service**: Serviço dedicado para autenticação e autorização (**OAuth2**, **JWT**), utilizando **Spring Data JPA** para persistência de usuários.
*   **Recognition Service**: Serviço de regras de negócio (**Drools**). Utiliza **Spring AMQP** para mensageria, **Slf4j** para logs estruturados e **Spring Cloud Sleuth** para rastreamento distribuído.
*   **Alert Service**: Gerenciador de notificações. Utiliza **Spring Data JPA** para persistência de alertas, **Java Mail** para envios e **Sleuth/Slf4j** para monitoramento.

### 4. Data & Infra Layer (Dados)
*   **MinIO**: Armazenamento de objetos para evidências (imagens/vídeos das detecções).
*   **RabbitMQ**: Message Broker para desacoplamento assíncrono.
*   **PostgreSQL**: Persistência de logs de auditoria, usuários (Auth) e alertas.
*   **ZipKin**: Coleta e visualização de traços distribuídos gerados pelo Sleuth.

### 5. DevOps & Infrastructure (Novo)
*   **GitHub**: Repositório de código fonte e plataforma de CI/CD (GitHub Actions) que dispara os builds.
*   **Docker Registry**: Armazena as imagens de container geradas.
*   **Docker Host**: Ambiente de execução onde todos os containers da aplicação SafeVision são implantados.