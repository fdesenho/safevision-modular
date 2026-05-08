# ADR 019: Estratégia de Chaos Engineering e Resiliência

* **Status:** Aceito
* **Data:** 2026-05-07
* **Decisores:** Fabio Desenho (Software Architect)

## 1. Contexto e Problema
O SafeVision lida com segurança física. A queda de um broker de mensagens não pode resultar em perda de evidências de detecção.

## 2. Decisão
Adotar testes de resiliência controlados. No ambiente atual (Docker Compose), utilizaremos injeção de falhas via comandos de interrupção de contêineres e monitoramento de logs de retry.

## 3. Consequências
### Positivas
* **Auto-recuperação:** Garantia de que os serviços restabelecem conexões AMQP sem reinicialização manual.
* **Integridade de Dados:** O Agent Python mantém mensagens em buffer durante a indisponibilidade momentânea.

### Trade-offs (Custos Assumidos)
* **Uso de Memória:** O buffer de retry no Python consome memória RAM enquanto o broker está fora do ar.

## 4. Resultados do Experimento
* **Cenário:** Queda forçada do RabbitMQ por 10s.
* **Observação Java:** Recuperação automática em < 1s após o serviço subir.
* **Observação Python:** Falta de visibilidade de retentativas durante o outage. Risco de perda de eventos confirmado.
* **Ação:** Implementar buffer de retentativa no Agent Python (Fase 2.1).

## 5. Evidências Visuais

Abaixo, o diagrama técnico que resume a execução do experimento de caos e o diagnóstico arquitetural resultante.

![Diagrama técnico mostrando o fluxo do teste de caos, os comportamentos do Agent Python (falha e perda de dados) e do Recognition Java (recuperação automática) e o veredito de resiliência.](assets/adr019/chaos_test_rabbitmq_results.png)

*Infográfico 1: Resumo do Experimento de Caos no RabbitMQ.*

## 6. Referências e Execução
Para instruções técnicas detalhadas e o passo a passo de como reproduzir os experimentos de caos mencionados nesta decisão, consulte o guia oficial de testes do projeto:

👉 [**Guia de Testes de Resiliência (TESTING.md)**](../../TESTING.md#engenharia-do-caos)