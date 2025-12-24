# ADR 007: Stack de Observabilidade (Prometheus & Grafana)

* **Status:** Proposto (Fase 2)
* **Data:** 2025-12-24

## Contexto e Problema
Logs em arquivos locais são inacessíveis em containers efêmeros (K8s). A falta de métricas correlacionadas dificulta o diagnóstico de problemas de performance (ex: "O sistema está lento porque a CPU está alta ou o banco travou?"). A stack ELK (Elasticsearch) tradicional é pesada demais para nosso cenário atual.

## Decisão
Adotar a stack moderna "PLG" (Prometheus, Loki, Grafana).
* **Prometheus:** Coleta de métricas numéricas (CPU, RAM, Req/s).
* **Loki:** Agregação de logs otimizada (indexa apenas metadados).
* **Grafana:** Dashboard único de visualização.

## Consequências
### Positivas
* **Eficiência:** O Loki consome uma fração dos recursos do Elasticsearch.
* **Integração:** Padrão de mercado para Kubernetes e Spring Boot Actuator.
* **Correlação:** Permite ver o log exato que gerou um pico no gráfico de erro 500.

### Negativas
* **Busca Limitada:** O Loki não é ideal para buscas textuais complexas (Full Text Search) em logs históricos longos.