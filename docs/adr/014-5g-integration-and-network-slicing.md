# ADR 014: Integração 5G e Estratégia de Network Slicing

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Network Specialist

## Contexto e Problema
Em operações policiais de larga escala, o congestionamento de redes móveis (4G/LTE) pode atrasar ou impedir o envio de alertas críticos. O SafeVision precisa de garantia de entrega e latência mínima para eventos de alta prioridade (armas ou oficial caído).

## Decisão
Implementar suporte nativo a **5G Standalone (SA)** com foco em **Network Slicing**.
* **Priorização:** Configurar o uso de fatias de rede (slices) dedicadas para tráfego de missão crítica (URLLC - Ultra-Reliable Low Latency Communications).
* **MEC (Multi-access Edge Computing):** Implementar instâncias do Recognition Service em servidores de borda da operadora para reduzir o RTT (Round Trip Time).

## Consequências
### Positivas
* **Determinismo:** Garante que alertas prioritários cheguem ao comando mesmo em áreas de altíssima densidade de conexões.
* **Eficiência:** Redução drástica da latência entre a detecção na borda e o alerta no dashboard.

### Negativas
* **Dependência de Infra:** O sucesso depende da disponibilidade de infraestrutura 5G SA e do suporte da operadora ao provisionamento de fatias de rede.