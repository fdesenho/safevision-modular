import threading
import time
import requests
from urllib.parse import urlparse

class GPSService:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super(GPSService, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        if getattr(self, '_initialized', False): return
        
        # 👇 MUDANÇA: Inicia com coordenadas de Floripa (Fallback) ao invés de 0.0
        # Ponte Hercílio Luz
        self.current_latitude = -27.5954
        self.current_longitude = -48.5640
        
        self.running = False
        self.gps_thread = None
        self.sensor_url = None 
        self.enable_url = None
        
        self._initialized = True
        print(f"🛰️ [GPS Service] Initialized. Default Location: Florianópolis (Fallback active)")

    def start(self):
        if not self.running:
            self.running = True
            self.gps_thread = threading.Thread(target=self._poll_gps_api, daemon=True)
            self.gps_thread.start()

    def stop(self):
        self.running = False
        if self.gps_thread:
            self.gps_thread.join(timeout=1.0)

    def set_target_from_camera_url(self, camera_url: str):
        try:
            parsed = urlparse(camera_url)
            base_url = f"{parsed.scheme}://{parsed.netloc}"
            self.sensor_url = f"{base_url}/sensors.json"
            self.enable_url = f"{base_url}/enable_sensor?id=gps" 
            self._force_enable_gps()
        except Exception as e:
            print(f"❌ [GPS] URL Error: {e}")

    def _force_enable_gps(self):
        if self.enable_url:
            try: requests.get(self.enable_url, timeout=1.0)
            except: pass

    def get_coordinates(self):
        return self.current_latitude, self.current_longitude

    def _poll_gps_api(self):
        while self.running:
            if not self.sensor_url:
                time.sleep(2.0)
                continue

            try:
                response = requests.get(self.sensor_url, timeout=2.0)
                if response.status_code == 200:
                    data = response.json()
                    
                    # Se não tem GPS, tenta ativar
                    if 'gps' not in data:
                        self._force_enable_gps()
                    else:
                        # Se tem GPS, atualiza as coordenadas reais
                        self._parse_ipwebcam_data(data)
            except:
                pass # Mantém as coordenadas anteriores (ou o default de Floripa)
            
            time.sleep(1.0)

    def _parse_ipwebcam_data(self, data):
        try:
            gps_node = data.get('gps')
            if gps_node and 'data' in gps_node and len(gps_node['data']) > 0:
                latest = gps_node['data'][-1]
                coords = latest[1]
                if len(coords) >= 2:
                    lat, lon = float(coords[0]), float(coords[1])
                    if lat != 0.0:
                        self.current_latitude = lat
                        self.current_longitude = lon
                        # print(f"📍 GPS REAL DETECTADO: {lat}, {lon}")
        except:
            pass