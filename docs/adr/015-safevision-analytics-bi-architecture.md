# ADR 015: Arquitetura de SafeVision Analytics (BI)

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Data Engineer

## Contexto e Problema
O SafeVision gera um volume massivo de eventos. Para a gestão estratégica, dados isolados são menos valiosos do que tendências históricas (ex: manchas de criminalidade e horários de maior incidência).

## Decisão
Adotar uma arquitetura de **Data Lakehouse (Medallion Architecture)**.
* **Ingestão:** Exportação de eventos do RabbitMQ para uma camada *Bronze* no MinIO (Object Storage).
* **Processamento:** Transformação dos dados em tabelas *Silver* (limpas) e *Gold* (agregadas) para consumo de BI.
* **Visualização:** Integração de dashboards gerenciais no Angular consumindo estas agregações.

## Consequências
### Positivas
* **Inteligência Estratégica:** Permite o policiamento preditivo baseado em evidências históricas.
* **Performance:** O tráfego de BI e consultas pesadas não impacta a performance do sistema de alerta em tempo real.

### Negativas
* **Complexidade de Dados:** Exige a gestão de um pipeline de ETL/ELT adicional.