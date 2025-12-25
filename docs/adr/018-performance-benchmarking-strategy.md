# ADR 018: Estratégia de Benchmarking de Performance (Cloud vs Edge)

* **Status:** Aceito
* **Data:** 2025-08-28
* **Decisores:** Fabio Desenho (Software Architect), Performance Engineer

## Contexto e Problema
A premissa central do SafeVision é a redução de latência através do Edge AI. Sem métricas claras, essa vantagem é apenas teórica. Precisamos de um framework para medir e comparar objetivamente o tempo de resposta entre o processamento tradicional em Cloud e o processamento na borda.

## Decisão
Implementar um **Framework de Benchmarking** automatizado que meça o RTT (Round Trip Time) da detecção.
* **Métrica:** Latência fim-a-fim (Captura -> Detecção -> Alerta no Dashboard).
* **Cenários:** Comparação entre envio de stream bruto para nuvem vs. envio de metadados processados no Agente de Visão.
* **Output:** Geração de um relatório técnico (Whitepaper) anexado ao repositório.

## Consequências
### Positivas
* **Evidência de Valor:** Prova matematicamente a eficiência da arquitetura para stakeholders.
* **Otimização:** Identifica gargalos no pipeline de processamento do Python/OpenCV.

### Negativas
* **Esforço de Teste:** Exige a emulação de diferentes condições de rede (3G/4G/5G) para resultados realistas.