import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {  Alert,Page } from '../models/app.models'; // Importe Alert se ainda usar no dashboard
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/alert`; // Base: /alert

  /**
   * Busca histórico paginado e ordenado usando o ID do usuário.
   * URL gerada: /alert/{userId}/history?page=0&size=10&sort=createdAt,desc
   */
  getAlertHistory(
    userId: string,
    page: number,
    size: number,
    activeSort: string = 'createdAt',
    direction: string = 'desc'
  ): Observable<Page<Alert>> {

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${activeSort},${direction}`);
    console.log(`Fetching alert history for user ${userId} with params:`, params);
    return this.http.get<Page<Alert>>(`${this.API_URL}/history/${userId}`, { params });
  }

  // --- Métodos Legados (Dashboard) ---

  getRecentAlerts(username: string): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${this.API_URL}/user/${username}`);
  }

  acknowledge(id: string): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/ack`, {});
  }
}
