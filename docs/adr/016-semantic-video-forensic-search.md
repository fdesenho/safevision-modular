# ADR 016: Busca Semântica Forense em Vídeo

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), AI Specialist

## Contexto e Problema
Investigadores perdem horas a rever gravações para encontrar evidências. O sistema precisa de permitir buscas semânticas (ex: "Oficial perseguindo suspeito com jaqueta azul").

## Decisão
Implementar **VSS (Vector Similarity Search)** utilizando bancos de dados vetoriais.
* **Embeddings:** Gerar representações vetoriais das detecções e frames utilizando modelos como **CLIP**.
* **Persistência:** Utilizar a extensão **pgvector** no PostgreSQL ou um banco vetorial dedicado (ex: Milvus/Weaviate).
* **Interface:** Busca em linguagem natural traduzida para consultas de similaridade vetorial.

## Consequências
### Positivas
* **Agilidade Forense:** Reduz o tempo de localização de provas de horas para segundos.
* **Escalabilidade:** Permite buscas eficientes em milhões de eventos históricos.

### Negativas
* **Custo Computacional:** A geração de embeddings exige processamento adicional no momento da ingestão.