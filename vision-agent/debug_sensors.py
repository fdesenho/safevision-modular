import requests
import time

# SEU IP
IP = "192.168.0.132:4747"
URL_SENSORS = f"http://{IP}/sensors.json"
URL_ENABLE = f"http://{IP}/enable_sensor?id=gps"

def diagnostico():
    print(f"🔍 Investigando {IP}...")

    # 1. Tenta ativar o GPS via comando
    try:
        requests.get(URL_ENABLE, timeout=2)
        print("➡️ Comando de ativação enviado.")
    except:
        print("❌ Falha ao conectar para ativar sensor.")

    time.sleep(1)

    # 2. Lê o JSON cru
    try:
        response = requests.get(URL_SENSORS, timeout=2)
        data = response.json()
        
        print("\n📋 SENSORES DISPONÍVEIS NO JSON:")
        print("-" * 30)
        
        # Lista todas as chaves (sensores) encontrados
        chaves = list(data.keys())
        if not chaves:
            print("⚠️ O JSON está vazio! Verifique 'Enable data logging' no App.")
        
        for sensor in chaves:
            print(f"  • {sensor}")
            
        print("-" * 30)

        # 3. Verifica especificamente o GPS
        if 'gps' in data:
            print("✅ GPS ENCONTRADO!")
            gps_data = data['gps'].get('data', [])
            if gps_data:
                print(f"📊 Dados brutos: {gps_data[-1]}")
            else:
                print("⚠️ Campo 'gps' existe, mas a lista de dados está vazia (Aguardando satélite).")
        else:
            print("❌ GPS NÃO ESTÁ NO JSON.")
            print("   Solução: Vá no App -> Data logging -> Enable data logging.")

    except Exception as e:
        print(f"❌ Erro fatal: {e}")

if __name__ == "__main__":
    diagnostico()