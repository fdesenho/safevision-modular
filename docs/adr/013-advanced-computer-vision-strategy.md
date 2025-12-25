# ADR 013: Estratégia de Visão Computacional Avançada (Térmica e Man-down)

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Lead AI Engineer

## Contexto e Problema
Para cenários de segurança pública, a detecção de objetos simples (RGB) é insuficiente em condições de baixa luminosidade ou fumaça. Além disso, a segurança do oficial exige a detecção de estados críticos como "Man-down" (oficial caído). 
Os desafios são:
1. **Hardware:** Necessidade de suporte a sensores térmicos integrados.
2. **Algoritmos:** Transição de detecção de objetos simples para estimativa de pose (Pose Estimation).

## Decisão
Adotar uma arquitetura de **Fusão Multimodal** e **Estimativa de Pose**.
* **Sensores:** Suporte a streams RTSP térmicos processados em paralelo ao stream RGB.
* **Modelo:** Implementação de **YOLOv8-Pose** para detecção de anomalias posturais (queda/inatividade).
* **Processamento:** Ingerir metadados térmicos para filtragem de falsos positivos em ambientes de visibilidade zero.

## Consequências
### Positivas
* **Confiabilidade:** Aumenta a precisão da vigilância em ambientes hostis e noturnos.
* **Proteção à Vida:** Transforma a câmera de um simples gravador num dispositivo de segurança ativa para o oficial.

### Negativas
* **Carga de Processamento:** Aumenta significativamente o consumo de GPU/NPU no dispositivo Edge.
* **Custo:** Exige hardware de captura mais sofisticado e dispendioso.