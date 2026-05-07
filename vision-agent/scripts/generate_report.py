import requests
import pandas as pd
import matplotlib.pyplot as plt
import os

# Configuração para comunicação interna entre containers Docker
ZIPKIN_URL = "http://zipkin:9411/api/v2/traces?limit=1000"

def fetch_traces():
    """Procura os rastreamentos reais diretamente na API do Zipkin"""
    print(f"📥 A aceder ao Zipkin em: {ZIPKIN_URL}")
    try:
        response = requests.get(ZIPKIN_URL)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"❌ Erro ao ligar ao Zipkin: {e}")
        return []

def process_real_data(traces):
    """Filtra e separa os dados entre Edge (Real) e Cloud (Simulado)"""
    data = []
    
    for trace in traces:
        # Extrai os nomes de todos os spans do rastro
        span_names = [span.get('name', '').lower() for span in trace]
        
        # Identifica o Span Raiz para calcular a latência total (RTT) em ms
        root_span = next((s for s in trace if s.get('parentId') is None), trace[0])
        duration_ms = root_span.get('duration', 0) / 1000.0

        # --- FILTROS OTIMIZADOS PARA OS SEUS LOGS ---
        
        # Cenário EDGE: Busca por detecções da câmera
        if any("yolov8" in name or "weapon" in name for name in span_names):
            data.append({"scenario": "EDGE", "latency_ms": duration_ms})
            
        # Cenário CLOUD: Busca pelos nomes exatos que apareceram no seu Zipkin
        elif any("http_post_benchmark_cloud" in name or "/recognition/simulate" in name for name in span_names):
            data.append({"scenario": "CLOUD", "latency_ms": duration_ms})
            
    return pd.DataFrame(data)

def generate_chart(df):
    """Gera o gráfico Boxplot comparativo"""
    print("📊 A gerar gráfico de performance...")
    plt.figure(figsize=(10, 7))
    
    edge_data = df[df['scenario'] == 'EDGE']['latency_ms']
    cloud_data = df[df['scenario'] == 'CLOUD']['latency_ms']
    
    plt.boxplot([edge_data, cloud_data], 
                ticket_labels=['Edge AI (SafeVision)', 'Cloud AI (Simulado)'], 
                patch_artist=True,
                boxprops=dict(facecolor='#17a2b8', color='#333333'),
                medianprops=dict(color='#ffffff', linewidth=2))
    
    plt.title('Análise de Latência: Edge Computing vs Cloud Streaming')
    plt.ylabel('Tempo de Resposta Total (ms)')
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    
    chart_filename = "latency_chart.png"
    plt.savefig(chart_filename, dpi=300, bbox_inches='tight')
    plt.close()
    return chart_filename

def generate_markdown(df, chart_filename):
    """Calcula as métricas P95 e gera o relatório final"""
    print("📝 A escrever o PERFORMANCE_REPORT.md...")
    
    metrics = df.groupby('scenario')['latency_ms'].agg(['count', 'mean', lambda x: x.quantile(0.95), 'max']).reset_index()
    metrics.columns = ['Cenário', 'Amostras', 'Média (ms)', 'P95 (ms)', 'Pico (ms)']
    
    edge_p95 = metrics[metrics['Cenário'] == 'EDGE']['P95 (ms)'].values[0] if 'EDGE' in metrics['Cenário'].values else 0
    cloud_p95 = metrics[metrics['Cenário'] == 'CLOUD']['P95 (ms)'].values[0] if 'CLOUD' in metrics['Cenário'].values else 0
    
    reduction = ((cloud_p95 - edge_p95) / cloud_p95 * 100) if cloud_p95 > 0 else 0

    md_content = f"""# 📊 Relatório de Performance: SafeVision (ADR-018)

## 1. Resumo Executivo
Relatório técnico comparando o processamento na borda vs streaming para nuvem.

* **Redução de Latência (P95):** {reduction:.2f}%
* **Dados:** Amostras reais extraídas do Zipkin via OpenTelemetry.

## 2. Métricas Consolidadas

| Cenário | Amostras | Latência Média | P95 (ms) | Pico (ms) |
| :--- | :---: | :---: | :---: | :---: |
| **Edge AI (Local)** | {int(metrics[metrics['Cenário'] == 'EDGE']['Amostras'].values[0]) if not edge_p95 == 0 else 0} | {metrics[metrics['Cenário'] == 'EDGE']['Média (ms)'].values[0]:.2f} | **{edge_p95:.2f}** | {metrics[metrics['Cenário'] == 'EDGE']['Pico (ms)'].values[0]:.2f} |
| **Cloud AI (Stream)** | {int(metrics[metrics['Cenário'] == 'CLOUD']['Amostras'].values[0]) if not cloud_p95 == 0 else 0} | {metrics[metrics['Cenário'] == 'CLOUD']['Média (ms)'].values[0]:.2f} | **{cloud_p95:.2f}** | {metrics[metrics['Cenário'] == 'CLOUD']['Pico (ms)'].values[0]:.2f} |

## 3. Visualização
![Gráfico Comparativo]({chart_filename})
"""
    with open("PERFORMANCE_REPORT.md", "w", encoding="utf-8") as f:
        f.write(md_content)
    print("✅ Relatório gerado com sucesso!")

if __name__ == "__main__":
    traces = fetch_traces()
    df = process_real_data(traces)
    
    if df.empty or len(df['scenario'].unique()) < 2:
        print(f"⚠️ Dados insuficientes. Encontrados: {df['scenario'].unique() if not df.empty else 'Nenhum'}")
    else:
        path = generate_chart(df)
        generate_markdown(df, path)