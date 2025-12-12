import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alert } from '../models/app.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private http = inject(HttpClient);


  private readonly API_URL = `${environment.apiUrl}/alert`;

  /**
   * Busca o histórico de alertas de um usuário específico.
   * Usado no Dashboard para carregar a lista inicial.
   */
  getRecentAlerts(username: string): Observable<Alert[]> {

    return this.http.get<Alert[]>(`${this.API_URL}/user/${username}`);
  }

  /**
   * Marca um alerta como lido (Acknowledged).
   */
  acknowledge(id: string): Observable<void> {

    return this.http.patch<void>(`${this.API_URL}/${id}/ack`, {});
  }

  /**
   * (Opcional) Busca todos os alertas (apenas para Admin)
   */
  getAllAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>(this.API_URL);
  }
}
