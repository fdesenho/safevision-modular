# ADR 010: Estratégia de Testes Automatizados e Qualidade

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Quality Engineer

## Contexto e Problema
Em uma arquitetura de microsserviços distribuída, garantir a integridade entre os componentes é um desafio crítico. Precisamos assegurar:
1. **Confiabilidade da Lógica:** Validar regras de reconhecimento (Drools) de forma isolada.
2. **Fidelidade de Integração:** Testar a persistência no PostgreSQL e a mensageria no RabbitMQ sem depender de mocks que não refletem o comportamento real do ambiente de produção.
3. **Reprodutibilidade:** Os testes devem ser executáveis de forma idêntica localmente e na pipeline de CI.

## Decisão
Implementar uma estratégia de testes baseada na stack **JUnit 5**.
* **Testes Unitários:** Uso de Mockito para isolamento de lógica pura de negócio.
* **Testes de Integração:** Adoção de **Testcontainers** para subir instâncias reais em Docker (PostgreSQL, RabbitMQ, MinIO) durante o ciclo de teste.
* **Métrica de Qualidade:** Cobertura mínima de 60% monitorada via **Jacoco**.

## Consequências
### Positivas
* **Ambiente Hermético:** O uso de Testcontainers elimina o problema do "funciona na minha máquina", testando contra versões exatas das dependências de infraestrutura.
* **Confiança em Refatorações:** Alta cobertura automatizada permite evoluir o código com baixo risco de quebra de contrato.

### Negativas
* **Tempo de Build:** O provisionamento de containers aumenta o tempo total de execução da pipeline de integração contínua.
* **Recursos de Hardware:** Exige que o ambiente de build suporte execução de Docker.