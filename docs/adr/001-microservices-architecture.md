# ADR 001: Adoção de Arquitetura de Microsserviços

* **Status:** Aceito
* **Data:** 2025-12-24
* **Decisores:** Arquiteto de Software, Lead Engineer

## Contexto e Problema
O SafeVision opera em um domínio complexo que envolve processamento de vídeo pesado (Edge/IA), gestão de alta concorrência de alertas e requisitos estritos de segurança (Auth). Uma arquitetura monolítica apresentaria desafios significativos:
1. **Escalabilidade:** O módulo de IA consome muita CPU/GPU, enquanto o Core é I/O bound. Escalar um monólito desperdiçaria recursos.
2. **Stack Tecnológico:** A melhor ferramenta para IA é Python, enquanto para o Backend robusto é Java (Spring Boot). Um monólito forçaria uma escolha subótima.
3. **Resiliência:** Uma falha no módulo de reconhecimento não deve derrubar o sistema de autenticação ou o dashboard administrativo.

## Decisão
Adotar uma arquitetura de **Microsserviços Distribuídos**.
* **Core Services:** Java 21 com Spring Boot 3 (Auth, Alert, Gateway).
* **Edge Services:** Python com YOLOv8 e OpenCV (Vision Agent).
* **Comunicação:** API Gateway para entrada externa e Mensageria para interna.

## Consequências
### Positivas
* **Escalabilidade Granular:** Permite escalar apenas os serviços que estão sob carga.
* **Poliglota:** Permite o uso da melhor tecnologia para cada problema (Python para IA, Java para Enterprise).
* **Isolamento de Falhas:** Um serviço fora do ar não compromete todo o sistema.

### Negativas
* **Complexidade Operacional:** Requer infraestrutura robusta de orquestração e monitoramento.
* **Consistência de Dados:** Exige gestão de transações distribuídas (Eventual Consistency).