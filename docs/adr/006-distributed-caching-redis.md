# ADR 006: Estratégia de Cache Distribuído com Redis

* **Status:** Proposto (Fase 2)
* **Data:** 2025-12-24

## Contexto e Problema
O Gateway valida tokens JWT (verificação de assinatura e revogação) a cada requisição. O Recognition Service consulta metadados de câmeras (localização, configuração) repetidamente. Isso gera I/O excessivo e desnecessário no banco de dados relacional (PostgreSQL).

## Decisão
Implementar **Redis** como camada de cache distribuído.
* Cache de Sessão/Tokens no Gateway.
* Cache de Dados de Referência no Recognition Service.

## Consequências
### Positivas
* **Performance:** Leituras de memória em microssegundos reduzem drasticamente a latência.
* **Alívio do Banco:** Reduz a carga no PostgreSQL, permitindo que ele foque em escritas de eventos.

### Negativas
* **Consistência:** Risco de dados obsoletos (Stale Data). Necessário definir TTLs (Time-to-live) adequados.
* **Custo:** Memória RAM é o recurso mais caro na nuvem.