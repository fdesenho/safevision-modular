# ADR 019: Estratégia de Chaos Engineering e Resiliência

* **Status:** Aceito
* **Data:** 2025-08-29
* **Decisores:** Fabio Desenho (Software Architect), Site Reliability Engineer (SRE)

## Contexto e Problema
O SafeVision é um sistema de missão crítica. Falhas de rede, queda de microsserviços ou indisponibilidade de hardware na borda não podem impedir a continuidade da vigilância. Precisamos testar a resiliência do sistema sob condições extremas.

## Decisão
Adoção de práticas de **Chaos Engineering** utilizando ferramentas como **Chaos Mesh** (pós-K8s).
* **Experimentos:** Injeção de latência sintética, terminação forçada de containers (RabbitMQ/Auth) e falhas de partição de rede.
* **Validação:** Verificar se os *Circuit Breakers* (Resilience4j) e as políticas de *Retry* assíncronas funcionam como esperado.

## Consequências
### Positivas
* **Confiabilidade:** Aumenta a confiança de que o sistema é "Self-healing".
* **MTTR Reduzido:** Melhora o tempo médio de recuperação ao entender os modos de falha.

### Negativas
* **Complexidade:** Requer um setup de monitoramento (Grafana) muito preciso para observar os efeitos do caos.