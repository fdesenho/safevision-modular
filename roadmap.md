# 🗺️ SafeVision Product Roadmap

Este documento delineia a visão estratégica para a evolução do **SafeVision**, transformando-o de um protótipo funcional em uma plataforma de segurança pública de nível industrial, focada em baixa latência, resiliência e governança ética.

> **Governança Técnica:** Todas as decisões marcadas com `[ADR-XXX]` possuem documentação detalhada no diretório `docs/adr/`, garantindo a rastreabilidade de cada escolha arquitetural.

---

## ✅ Fase 1: Estabilização (Atual / MVP)
**Foco:** Garantir a robustez das funcionalidades principais e a validação empírica da baixa latência.

- [x] **Core Architecture:** Implementação de microsserviços e API Gateway distribuído. **[[ADR-001](docs/adr/001-microservices-architecture.md)]**
- [x] **Edge AI Optimization:** Detecção de armas e análise comportamental local (Edge First). **[[ADR-002](docs/adr/002-edge-computing-strategy.md)]**
- [x] **Comunicação Assíncrona:** Pipeline de eventos resiliente via RabbitMQ. **[[ADR-003](docs/adr/003-async-communication-rabbitmq.md)]**
- [x] **Frontend Dashboard:** Interface reativa para monitoramento em tempo real. **[[ADR-009](docs/adr/009-frontend-framework-angular.md)]**
- [x] **Performance Benchmark:** Relatório comparativo de latência Cloud vs Edge para validação de KPI. **[[ADR-018](docs/adr/018-performance-benchmarking-strategy.md)]**
- [x] **Cobertura de Testes:** Elevar para 80% com JUnit 5 e Testcontainers. **[[ADR-010](docs/adr/010-testing-strategy-junit.md)]**
- [x] **Documentação API:** Contrato oficial via OpenAPI 3.0 / Swagger. **[[ADR-011](docs/adr/011-api-documentation-openapi.md)]**

---

## 🏗️ Fase 2: Otimização de Infraestrutura (Curto Prazo)
**Foco:** Segurança "Enterprise Ready", Resiliência ao Caos e Orquestração Híbrida.

- [ ] **Gestão de Segredos (Vault):** Migração de segredos para HashiCorp Vault. **[[ADR-004](docs/adr/004-secret-management-vault.md)]**
- [ ] **Pipeline CI/CD:** Automação completa de build/deploy via GitHub Actions. **[[ADR-012](docs/adr/012-cicd-pipeline-github-actions.md)]**
- [x] **Chaos Engineering:** Injeção de falhas controladas para validar failover e resiliência. **[[ADR-019](docs/adr/019-chaos-engineering-strategy.md)]**
- [ ] **Hardening & DevSecOps:** Scans de vulnerabilidades (Trivy) e análise estática. **[[ADR-020](docs/adr/020-devsecops-hardening-strategy.md)]**
- [ ] **Migração para Kubernetes:** Orquestração híbrida (K8s Cloud / K3s Edge). **[[ADR-005](docs/adr/005-orchestration-k8s-k3s.md)]**
- [ ] **Monitoramento Avançado:** Stack de observabilidade total (Prometheus/Loki/Grafana). **[[ADR-007](docs/adr/007-observability-stack.md)]**
- [ ] **Caching Distribuído:** Implementação de Redis para dados quentes e sessões. **[[ADR-006](docs/adr/006-distributed-caching-redis.md)]**

---

## 📱 Fase 3: Expansão de Recursos (Médio Prazo)
**Foco:** Mobilidade operacional e hardware especializado para oficiais em campo.

- [ ] **App Mobile (Officer Companion):** Aplicação Flutter para alertas geolocalizados. **[[ADR-008](docs/adr/008-mobile-framework-flutter.md)]**
- [ ] **Integração 5G & Network Slicing:** QoS prioritário para alertas críticos. **[[ADR-014](docs/adr/014-5g-integration-and-network-slicing.md)]**
- [ ] **Visão Computacional Avançada:** Suporte térmico e detecção postural (Man-down). **[[ADR-013](docs/adr/013-advanced-computer-vision-strategy.md)]**

---

## 📊 Fase 4: Intelligence & Analytics (Longo Prazo)
**Foco:** Inteligência preventiva, busca semântica e governança ética de dados.

- [ ] **SafeVision Analytics:** Dashboards de BI para análise de manchas criminais. **[[ADR-015](docs/adr/015-safevision-analytics-bi-architecture.md)]**
- [ ] **Análise Forense:** Busca vetorial semântica em vídeos históricos (pgvector). **[[ADR-016](docs/adr/016-semantic-video-forensic-search.md)]**
- [ ] **Federação de Dados:** Arquitetura multi-tenant para interligar agências. **[[ADR-017](docs/adr/017-multi-tenant-data-federation.md)]**
- [ ] **Privacy & LGPD Compliance:** Anonimização automática (blurring) e governança ética. **[[ADR-021](docs/adr/021-privacy-compliance-lgpd.md)]**

---

## 📈 Indicadores de Sucesso (KPIs)

Para validar a evolução deste roadmap, o projeto monitora:
1. **Latência de Alerta:** Tempo < 500ms entre detecção física e alerta no Dashboard.
2. **Taxa de Resiliência:** Capacidade de processamento local durante 100% da queda de link externo.
3. **Segurança:** Zero vulnerabilidades críticas em scans automatizados de imagens Docker.

---

> *Este roadmap é um documento vivo e evolui conforme as necessidades de segurança pública e os avanços em Edge AI.*
