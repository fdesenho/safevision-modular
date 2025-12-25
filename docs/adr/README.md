# 🏛️ Architecture Decision Records (ADR)

Este diretório contém o registro centralizado das decisões arquiteturais do projeto **SafeVision**. O uso de ADRs garante a transparência, a rastreabilidade e a governança técnica, permitindo que futuros desenvolvedores e arquitetos compreendam não apenas *o que* foi decidido, mas o *porquê*.

## 📑 Índice de Decisões

As decisões estão organizadas por domínios técnicos para facilitar a navegação.

### 🧩 1. Fundação e Arquitetura Core
| ID | Título | Data | Status |
| :--- | :--- | :---: | :---: |
| [ADR-001](001-microservices-architecture.md) | Adoção de Arquitetura de Microsserviços | 15/08/2025 | ✅ Aceito |
| [ADR-003](003-async-communication-rabbitmq.md) | Comunicação Assíncrona com RabbitMQ | 18/08/2025 | ✅ Aceito |
| [ADR-011](011-api-documentation-openapi.md) | Documentação API-First com OpenAPI/Swagger | 22/08/2025 | ✅ Aceito |
| [ADR-017](017-multi-tenant-data-federation.md) | Federação de Dados e Arquitetura Multi-tenant | 28/08/2025 | ✅ Aceito |

### 🧠 2. Edge Computing & Inteligência Artificial
| ID | Título | Data | Status |
| :--- | :--- | :---: | :---: |
| [ADR-002](002-edge-computing-strategy.md) | Estratégia de Edge Computing First | 16/08/2025 | ✅ Aceito |
| [ADR-013](013-advanced-computer-vision-strategy.md) | Estratégia de Visão Computacional Avançada | 25/08/2025 | ✅ Aceito |
| [ADR-016](016-semantic-video-forensic-search.md) | Busca Semântica Forense em Vídeo | 28/08/2025 | ✅ Aceito |

### 🖥️ 3. Frontend & Experiência do Usuário (Mobile)
| ID | Título | Data | Status |
| :--- | :--- | :---: | :---: |
| [ADR-009](009-frontend-framework-angular.md) | Adoção do Angular para o Frontend Dashboard | 20/08/2025 | ✅ Aceito |
| [ADR-008](008-mobile-framework-flutter.md) | Mobile Framework (Flutter) para Officer App | 19/08/2025 | 🔮 Futuro |

### 🏗️ 4. Infraestrutura, DevOps e Segurança
| ID | Título | Data | Status |
| :--- | :---: | :---: | :---: |
| [ADR-004](004-secret-management-vault.md) | Gestão de Segredos com HashiCorp Vault | 18/08/2025 | 📅 Planejado |
| [ADR-005](005-orchestration-k8s-k3s.md) | Orquestração Híbrida com K8s e K3s | 19/08/2025 | 📅 Planejado |
| [ADR-012](012-cicd-pipeline-github-actions.md) | Pipeline de CI/CD com GitHub Actions | 23/08/2025 | ✅ Aceito |
| [ADR-014](014-5g-integration-and-network-slicing.md) | Integração 5G e Network Slicing | 26/08/2025 | ✅ Aceito |
| [ADR-019](019-chaos-engineering-strategy.md) | Estratégia de Chaos Engineering e Resiliência | 29/08/2025 | ✅ Aceito |
| [ADR-020](020-devsecops-hardening-strategy.md) | Estratégia de DevSecOps e Hardening | 30/08/2025 | ✅ Aceito |

### 📊 5. Dados, Qualidade e Observabilidade
| ID | Título | Data | Status |
| :--- | :--- | :---: | :---: |
| [ADR-006](006-distributed-caching-redis.md) | Caching Distribuído com Redis | 19/08/2025 | 📅 Planejado |
| [ADR-007](007-observability-stack.md) | Stack de Observabilidade (Prometheus/Loki/Grafana) | 19/08/2025 | 📅 Planejado |
| [ADR-010](010-testing-strategy-junit.md) | Estratégia de Testes Automatizados e Qualidade | 21/08/2025 | ✅ Aceito |
| [ADR-015](015-safevision-analytics-bi-architecture.md) | Arquitetura de SafeVision Analytics (BI) | 27/08/2025 | ✅ Aceito |
| [ADR-018](018-performance-benchmarking-strategy.md) | Estratégia de Benchmarking de Performance | 28/08/2025 | ✅ Aceito |
| [ADR-021](021-privacy-compliance-lgpd.md) | Conformidade com Privacidade (LGPD) e Ética | 31/08/2025 | ✅ Aceito |

---

## 🛠️ Como criar uma nova ADR

Para manter o padrão, novas decisões devem seguir o template baseado no modelo de Michael Nygard:

1. **Título:** Resumido e numerado (ex: ADR-018).
2. **Status:** Proposto, Aceito, Rejeitado, Depreciado ou Substituído.
3. **Contexto:** Descrição do problema e as forças envolvidas.
4. **Decisão:** A escolha técnica feita e sua justificativa.
5. **Consequências:** Impactos positivos e negativos da decisão.

---
*Última atualização: Agosto de 2025*