# ADR 017: Federação de Dados e Arquitetura Multi-tenant

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Cloud Architect

## Contexto e Problema
O SafeVision será utilizado por múltiplas agências ou cidades. O isolamento de dados é um requisito legal e de segurança: os dados da Agência A não podem ser visíveis pela Agência B.

## Decisão
Implementar uma arquitetura **Multi-tenant com Isolamento Lógico**.
* **Isolamento:** Uso de `tenant_id` em todas as entidades e políticas de **RLS (Row Level Security)** no PostgreSQL.
* **Federação:** Camada de API segura que permite consultas cruzadas (Cross-Tenant) apenas sob acordos explícitos de cooperação e tokens federados.

## Consequências
### Positivas
* **Segurança Jurídica:** Garante a privacidade e o isolamento total dos dados sensíveis entre diferentes órgãos.
* **Escala SaaS:** Permite adicionar novos clientes (cidades/agências) sem a necessidade de provisionar infraestruturas de banco de dados isoladas para cada um.

### Negativas
* **Risco de Configuração:** Erros na implementação do RLS podem causar vazamento de dados; exige testes de segurança automatizados rigorosos.