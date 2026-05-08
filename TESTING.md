# 🧪 Estratégia de Testes Automatizados - SafeVision

Este documento descreve a infraestrutura de Qualidade de Software (QA) do projeto SafeVision. A arquitetura de testes foi desenhada para garantir **isolamento**, **fidelidade ao ambiente de produção** e **feedback rápido**.

## 🏗️ Visão Geral da Arquitetura de Testes

Utilizamos uma abordagem híbrida adaptada para Microsserviços e Edge Computing:

| Camada | Tecnologia | Estratégia | Ferramentas Chave |
| :--- | :--- | :--- | :--- |
| **Backend** | Java 21 / Spring Boot | **Testes de Integração** | [Testcontainers](https://testcontainers.com/), JUnit 5, Mockito |
| **Edge AI** | Python 3.11 | **Containerized Testing** | Docker Compose, Pytest, Pytest-Mock |
| **Frontend** | Angular 21 | **Unit & Component** | Karma, Jasmine, ChromeHeadless |

---

## 🚀 Execução Rápida (All-in-One)

Para facilitar a validação completa do sistema em ambiente Windows, criamos um script automatizado que orquestra todas as camadas.

**Pré-requisitos:**
* Docker Desktop (Rodando)
* Java JDK 21+ (Opcional, pois usa Maven Wrapper)
* Node.js v20+ (Apenas para frontend)

**Comando:**
```powershell
.\run-tests.ps1
```

---

## 🌪️ Engenharia do Caos (Chaos Engineering)

Para garantir que o **SafeVision** seja um sistema de missão crítica resiliente, implementamos experimentos de injeção de falhas controladas baseados na **ADR-019**.

### 1. Experimento: Indisponibilidade do RabbitMQ

Este teste simula um crash abrupto do Message Broker para validar a capacidade de auto-recuperação (*self-healing*) dos microserviços.

#### 🛠️ Preparação (Build)

Como o script de teste requer dependências específicas (Python + Docker CLI), utilizamos um container dedicado para não poluir o ambiente host:

```powershell
docker build -t safevision-chaos -f scripts/chaos/Dockerfile.chaos .
```

#### 🚀 Execução (PowerShell)

Execute o comando abaixo na raiz do projeto. Ele compartilha o soquete do Docker para que o container de caos possa gerenciar o ciclo de vida dos outros serviços:

```powershell
docker run --rm `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -v "${PWD}:/app" `
  -w /app `
  safevision-chaos scripts/chaos/rabbitmq_outage.py
```

#### 📊 Critérios de Sucesso

* **Recognition-Service (Java):** Deve reconectar automaticamente sem intervenção humana após o restabelecimento do serviço.
* **Vision-Agent (Python):** Deve manter as tentativas de conexão e descarregar o buffer de eventos assim que o broker estiver online (requer Tuning de Resiliência ativado).

#### ⚠️ Solução de Problemas

Se o comando falhar com erro de permissão no `docker.sock`, certifique-se de que o Docker Desktop está configurado para permitir conexões via soquete padrão (*Settings > General > Expose daemon on tcp://localhost:2375 without TLS* - ou equivalente para o soquete local).
```