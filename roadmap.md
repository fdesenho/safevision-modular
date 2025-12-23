---

### ARQUIVO 2: `ROADMAP.md`

```markdown
# 🗺️ SafeVision Product Roadmap

Este documento delineia a visão estratégica para a evolução do SafeVision, transformando-o de um protótipo funcional em uma plataforma de segurança pública de nível industrial.

---

## ✅ Fase 1: Estabilização (Atual / MVP)
**Foco:** Garantir a robustez das funcionalidades principais e a confiabilidade da detecção.

- [x] **Core Architecture:** Implementação dos microsserviços (Auth, Alert, Recognition) e Gateway.
- [x] **Edge AI:** Integração do YOLOv8 e MediaPipe para detecção de armas e análise de olhar.
- [x] **Comunicação Assíncrona:** Pipeline completo via RabbitMQ.
- [x] **Frontend Dashboard:** Visualização em tempo real, mapas e histórico de alertas.
- [ ] **Cobertura de Testes:** Aumentar cobertura de testes unitários (JUnit/Mockito) para 80%.
- [ ] **Documentação API:** Finalizar documentação Swagger/OpenAPI para todos os serviços.

---

## 🏗️ Fase 2: Otimização de Infraestrutura (Curto Prazo)
**Foco:** Escalabilidade, CI/CD e preparação para deploy em ambientes hostis (Edge real).

- [ ] **Migração para Kubernetes:**
    - Criar manifestos Helm Charts para deploy em clusters K8s.
    - Adaptar o Vision Agent para rodar em **K3s** ou **MicroK8s** (Edge Computing).
- [ ] **Pipeline CI/CD:**
    - Implementar GitHub Actions para build, teste e push automático de imagens Docker.
    - Análise estática de código (SonarQube).
- [ ] **Caching Distribuído:**
    - Implementar **Redis** para cache de tokens JWT no Gateway e estados temporários no Recognition Service.
- [ ] **Monitoramento Avançado:**
    - Substituir logs básicos por stack ELK (Elasticsearch, Logstash, Kibana) ou Prometheus + Grafana.

---

## 📱 Fase 3: Expansão de Recursos (Médio Prazo)
**Foco:** Melhorar a experiência do oficial em campo e a capacidade de detecção.

- [ ] **App Mobile (Officer Companion):**
    - Aplicativo (Flutter/React Native) para que oficiais próximos recebam alertas geolocalizados.
    - Botão de pânico físico integrado ao hardware.
- [ ] **Integração 5G & Network Slicing:**
    - Otimização do protocolo de transmissão para redes 5G, garantindo QoS prioritário para alertas críticos.
- [ ] **Visão Computacional Avançada:**
    - Suporte a câmeras térmicas/infravermelho para operação noturna.
    - Detecção de quedas (Man Down) usando análise de pose.
    - Reconhecimento facial (opcional/configurável para listas de procurados).

---

## 📊 Fase 4: Intelligence & Analytics (Longo Prazo)
**Foco:** Transformar dados históricos em inteligência preventiva.

- [ ] **SafeVision Analytics:**
    - Módulo de Business Intelligence para análise de tendências de criminalidade.
    - Mapas de calor (Heatmaps) baseados em ocorrências históricas.
- [ ] **Análise Forense:**
    - Ferramentas para busca inteligente em vídeos armazenados (ex: "buscar pessoa com camisa vermelha").
- [ ] **Federação de Dados:**
    - Capacidade de interligar múltiplas instâncias do SafeVision (bairros/cidades diferentes).

---

> *Este roadmap é um documento vivo e pode evoluir com base no feedback da comunidade e avanços tecnológicos.*