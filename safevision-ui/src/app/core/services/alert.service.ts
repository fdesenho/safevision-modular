import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Alert } from '../models/app.models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private http = inject(HttpClient);

  // URL do Gateway (Rota Singular /alert que criamos no GatewayRoutes)
  private readonly API_URL = 'http://localhost:8080/alert';

  getAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>(this.API_URL);
  }

  acknowledge(id: string) {
    return this.http.patch(`${this.API_URL}/${id}/ack`, {});
  }
}
