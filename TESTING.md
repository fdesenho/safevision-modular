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