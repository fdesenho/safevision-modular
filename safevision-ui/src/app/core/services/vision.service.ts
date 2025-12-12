import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class VisionService {
  private http = inject(HttpClient);

  private readonly AGENT_BASE = `${environment.visionAgentUrl}`;



  startDetection(userId: string, cameraUrl: string): Observable<any> {
     console.log(`🔌 Ativando proteção para: ${userId}`);
    return this.http.post(`${this.AGENT_BASE}/toggle/on`, { userId, cameraUrl });
  }

  // ⚠️ CORREÇÃO AQUI: Agora precisamos passar o userId também
  stopDetection(userId: string | undefined): Observable<any> {
    console.log(`💤 Desativando proteção para: ${userId}`);

    return this.http.post(`${this.AGENT_BASE}/toggle/off`, { userId });
  }
}
