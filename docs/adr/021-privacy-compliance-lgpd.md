# ADR 021: Conformidade com Privacidade (LGPD) e Ética em IA

* **Status:** Aceito
* **Data:** 2025-08-31
* **Decisores:** Fabio Desenho (Software Architect), Data Privacy Officer (DPO)

## Contexto e Problema
A captura de imagens em locais públicos exige conformidade com a LGPD. O armazenamento indiscriminado de rostos de cidadãos não envolvidos em alertas representa um risco jurídico e ético.

## Decisão
Implementar mecanismos de **Privacidade na Origem (Privacy by Design)**.
* **Anonimização:** Aplicação de *Gaussian Blur* automático em rostos que não geraram alertas de ameaça.
* **Data Retention:** Políticas de expurgo automático de snapshots após 30 dias (exceto se marcados como evidência judicial).
* **Auditoria:** Log imutável de quem acessou quais imagens e por quê.

## Consequências
### Positivas
* **Proteção Jurídica:** Reduz drasticamente a responsabilidade civil sobre vazamento de dados sensíveis.
* **Ética:** Alinha o projeto com as melhores práticas mundiais de IA responsável.

### Negativas
* **Overhead de Processamento:** O blurring em tempo real exige mais CPU no Agente de Visão.