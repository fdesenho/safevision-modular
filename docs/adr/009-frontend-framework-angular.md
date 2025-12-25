# ADR 009: Adoção do Angular para o Frontend Dashboard

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Lead Engineer

## Contexto e Problema
O SafeVision exige um dashboard de monitoramento de alta performance para gerenciar fluxos de vídeo e alertas críticos. Os requisitos técnicos incluem:
1. **Programação Reativa:** Necessidade de lidar com múltiplos streams de dados assíncronos (WebSockets) para alertas em tempo real sem degradação de performance.
2. **Arquitetura Enterprise:** O frontend deve ser escalável, modular e capaz de manter um estado complexo de múltiplos dispositivos Edge simultaneamente.
3. **Segurança:** Integração nativa e rigorosa com fluxos de autenticação JWT/OAuth2.

## Decisão
Adotar o **Angular 21** como framework principal para a camada de apresentação.
* **Paradigma:** Uso intensivo de **RxJS** para gerenciamento de estados reativos e fluxos de eventos.
* **Comunicação:** Implementação de **RxStomp** para integração robusta com o RabbitMQ via WebSockets.
* **Componentização:** Estrutura baseada em *Standalone Components* para facilitar o lazy loading e otimizar o bundle final.

## Consequências
### Positivas
* **Escalabilidade:** Framework opinativo que impõe uma estrutura organizada e padronizada, ideal para projetos de longo prazo.
* **Segurança:** Proteção nativa contra XSS e injeção, além de interceptors robustos para gestão de tokens de segurança.
* **Tipagem Estrita:** Uso nativo de TypeScript, garantindo maior manutenibilidade e redução de erros em tempo de execução.

### Negativas
* **Verbosidade:** Requer mais código boilerplate inicial comparado a bibliotecas como React.
* **Curva de Aprendizado:** Exige domínio de conceitos complexos como Injeção de Dependência e Observables.