import { Injectable, inject } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Alert } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private rxStomp: RxStomp;
  private authService = inject(AuthService);

  constructor() {
    this.rxStomp = new RxStomp();
    this.configure();
    this.rxStomp.activate();
  }

  private configure() {
    // Pega o token atual
    const token = this.authService.getToken();

    this.rxStomp.configure({
      brokerURL: 'ws://localhost:8080/alert/ws/websocket',

      // --- AJUSTE CRÍTICO: Enviar Token no Header STOMP ---
      connectHeaders: {
        // Isso autentica a sessão STOMP, mesmo que o handshake HTTP seja público
        Authorization: token ? `Bearer ${token}` : ''
      },
      // ----------------------------------------------------

      debug: (msg: string) => {
        console.log(new Date(), msg);
      },

      reconnectDelay: 5000, // Aumentei para 5s para não flodar em erro

      // Ajuste para evitar problemas de heartbeat
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
    });
  }

  public watchAlerts(): Observable<Alert> {
    const username = this.authService.currentUser()?.username;

    if (!username) {
        console.warn("Tentando ouvir alertas sem usuário logado.");
        return new Observable(); // Retorna vazio se não tiver user
    }

    console.log(`🔌 Ouvindo tópico: /topic/alerts/${username}`);

    return this.rxStomp.watch(`/topic/alerts/${username}`).pipe(
      map(message => JSON.parse(message.body) as Alert)
    );
  }
}
