#!/bin/bash
# Script de Validação Unificada: Edge vs Cloud (ADR-018)

echo "====================================================="
echo "🛡️  SafeVision: Iniciando Validação de Arquitetura"
echo "====================================================="

echo -e "\n[Passo 1/3] Executando simulação de latência EDGE..."
python scripts/benchmark_edge.py

echo -e "\n[Passo 2/3] Executando simulação de latência CLOUD (Via Gateway/JWT)..."
python scripts/benchmark_cloud.py

echo -e "\n[Passo 3/3] Consolidando dados do Zipkin e gerando relatório..."
python scripts/generate_report.py

echo -e "\n====================================================="
echo "✅ Validação Concluída!"
echo "📄 Verifique os arquivos PERFORMANCE_REPORT.md e latency_chart.png"
echo "====================================================="