import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VisionService {
  private http = inject(HttpClient);


  private readonly AGENT_URL = 'http://localhost:5000';

  activateProtection(userId: string | undefined): Observable<any> {
    // Envia o userId no corpo do POST
    return this.http.post(`${this.AGENT_URL}/toggle/on`, { userId });
  }

  deactivateProtection(): Observable<any> {
    return this.http.post(`${this.AGENT_URL}/toggle/off`, {});
  }
}
