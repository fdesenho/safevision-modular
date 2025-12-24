# ADR 002: Processamento Híbrido (Edge Computing vs Cloud Streaming)

* **Status:** Aceito
* **Data:** 2025-12-24

## Contexto e Problema
O envio contínuo de vídeo bruto (streaming) de múltiplas câmeras de segurança para a nuvem centralizada apresenta três problemas críticos:
1. **Custo de Banda:** Transmitir vídeo HD 24/7 é proibitivamente caro via 4G/5G.
2. **Latência:** O tempo de viagem (RTT) do vídeo até a nuvem para análise e retorno do alerta pode exceder os segundos críticos para segurança pública.
3. **Privacidade:** O envio de imagens de cidadãos inocentes para a nuvem gera riscos desnecessários de conformidade (LGPD/GDPR).

## Decisão
Implementar uma estratégia de **Edge Computing**. O modelo de IA (YOLOv8) deve rodar localmente no dispositivo (Raspberry Pi, Jetson, Servidor Local). Apenas metadados (JSON) e recortes de evidência (snapshots) são enviados para a nuvem quando uma ameaça é detectada.

## Consequências
### Positivas
* **Redução de Custos:** Economia massiva em largura de banda e armazenamento em nuvem.
* **Baixa Latência:** Detecção e reação ocorrem em milissegundos no local.
* **Privacidade por Design:** Dados irrelevantes são descartados no local.

### Negativas
* **Hardware Dependente:** O dispositivo de borda precisa ter capacidade mínima de processamento.
* **Manutenção Distribuída:** Atualizar o modelo de IA em milhares de dispositivos desconectados é um desafio de DevOps.