import subprocess
import time
import sys

def run_chaos_test():
    print("\n" + "="*55)
    print("🌪️  SAFEVISION CHAOS EXPERIMENT - ADR-019")
    print("Cenário: Indisponibilidade Forçada do RabbitMQ")
    print("="*55 + "\n")

    try:
        # 1. Injeção de Falha (Agressor)
        print("🛑 [FALHA] Derrubando o container 'rabbitmq' via SIGKILL...")
        # Usamos 'kill' em vez de 'stop' para simular uma queda abrupta (crash)
        subprocess.run(["docker", "kill", "rabbitmq"], check=True)
        
        start_time = time.time()
        print("⏱️  O sistema ficará sem broker por 10 segundos.")
        
        # Monitoramento visual simples
        for i in range(10, 0, -1):
            sys.stdout.write(f"\r   ... Tempo restante de indisponibilidade: {i}s ")
            sys.stdout.flush()
            time.sleep(1)

        # 2. Recuperação (Auto-healing)
        print("\n\n♻️  [RECUPERAÇÃO] Reiniciando o serviço RabbitMQ...")
        subprocess.run(["docker", "start", "rabbitmq"], check=True)
        
        print(f"\n✅ Experimento concluído com sucesso.")
        print("-" * 55)
        print("👉 PRÓXIMOS PASSOS PARA O ARQUITETO:")
        print("1. Verifique os logs do 'vision-agent' para ver o comportamento do Retry.")
        print("2. Verifique o 'recognition-service' (Java) para ver a reconexão automática.")
        print("3. Documente se houve perda de dados na ADR-019.")
        print("-" * 55 + "\n")

    except subprocess.CalledProcessError as e:
        print(f"\n❌ Erro ao interagir com o Docker: {e}")
        print("Certifique-se de que o container 'rabbitmq' está rodando antes de iniciar.")

if __name__ == "__main__":
    run_chaos_test()