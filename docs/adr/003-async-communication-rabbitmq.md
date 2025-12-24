# ADR 003: Comunicação Assíncrona via RabbitMQ

* **Status:** Aceito
* **Data:** 2025-12-24

## Contexto e Problema
A comunicação síncrona (HTTP REST) entre o Edge (Python) e a Nuvem (Java) cria um acoplamento temporal forte. Se a nuvem estiver instável ou lenta, o dispositivo Edge pode travar ou perder alertas. Além disso, picos de detecção simultânea podem causar DDoS não intencional no backend.

## Decisão
Utilizar **RabbitMQ (AMQP)** como backbone de mensageria assíncrona.
* O Edge publica eventos na fila.
* O Backend consome no seu próprio ritmo (Padrão Competing Consumers).

## Consequências
### Positivas
* **Desacoplamento Temporal:** O Edge continua funcionando mesmo se o Backend cair.
* **Load Leveling:** Picos de tráfego são absorvidos pela fila, protegendo o banco de dados.
* **Confiabilidade:** O RabbitMQ garante a entrega da mensagem (Acknowledgements).

### Negativas
* **Complexidade de Infra:** Introduz um componente crítico (Broker) para gerenciar e manter.
* **Debug:** Rastrear o fluxo de uma requisição torna-se mais complexo do que em chamadas HTTP diretas.