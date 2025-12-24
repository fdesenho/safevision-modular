# ADR 005: Orquestração com Kubernetes (K8s) e K3s

* **Status:** Proposto (Fase 2)
* **Data:** 2025-12-24

## Contexto e Problema
Gerenciar containers manualmente ou via Docker Compose não oferece recursos nativos de *Self-healing* (reiniciar containers travados), *Rolling Updates* (atualização sem downtime) ou escalabilidade automática. Além disso, precisamos unificar a abstração de deploy entre a Nuvem e o Edge.

## Decisão
Migrar a orquestração para o ecossistema Kubernetes.
* **Nuvem:** Kubernetes padrão (Managed K8s).
* **Edge:** **K3s** (Distribuição leve certificada pela CNCF), otimizada para baixo consumo de memória (<512MB).

## Consequências
### Positivas
* **Padronização:** Mesmos manifestos YAML (Helm Charts) para Nuvem e Edge.
* **Resiliência:** O orquestrador garante que o número desejado de réplicas esteja sempre rodando.
* **Service Discovery:** DNS interno nativo elimina a necessidade de Eureka Server externo.

### Negativas
* **Curva de Aprendizado:** Kubernetes possui alta complexidade cognitiva para o time.
* **Consumo de Recursos:** Mesmo o K3s consome recursos que poderiam ser usados pela aplicação, exigindo tuning fino de limites.