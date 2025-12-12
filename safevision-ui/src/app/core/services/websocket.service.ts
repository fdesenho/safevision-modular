import { Injectable, inject, PLATFORM_ID, OnDestroy } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RxStomp } from '@stomp/rx-stomp';
import { AuthService } from './auth.service';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators'; // <--- IMPORTANTE: Faltava isso
import { IMessage } from '@stomp/stompjs'; // <--- IMPORTANTE: Para tipar a mensagem
import { Alert } from '../models/app.models';
import { environment } from '../../../environments/environment';

@Injectable()
export class WebSocketService implements OnDestroy {

  private rxStomp = new RxStomp();
  private platformId = inject(PLATFORM_ID);
  private authService = inject(AuthService);

  private alertSubject = new Subject<Alert>();
  private isConnected = false;

  constructor() {
    // Evita erro no SSR (Server Side Rendering)
    if (!isPlatformBrowser(this.platformId)) return;

    this.configure();

    // 🔥 Só depois de conectar vamos registrar o watch
    this.rxStomp.connected$.subscribe(() => {
      console.log('🟢 [WebSocket] Conectado com sucesso.');
      this.isConnected = true;
      this.subscribeToAlerts();
    });

    this.rxStomp.activate();
  }

  private configure() {
    const token = this.authService.getToken();

    // Transforma http -> ws e https -> wss
    const wsUrl = environment.apiUrl.replace(/^http/, 'ws') + '/alert/ws/websocket';

    console.log(`🔌 [WebSocket] Tentando conectar em: ${wsUrl}`);

    this.rxStomp.configure({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      // Debug: mostre o log para facilitar a detecção de erros
      debug: (msg: string) => console.debug(new Date(), msg),
      reconnectDelay: 5000,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
    });
  }

  private subscribeToAlerts() {
    const username = this.authService.currentUser()?.username;

    if (!username) {
      console.warn('⚠️ [WebSocket] Usuário não identificado. Não foi possível inscrever no tópico.');
      return;
    }

    const topic = `/topic/alert/${username}`;
    console.log(`📡 [WebSocket] Inscrevendo no tópico: ${topic}`);

    this.rxStomp.watch(topic).subscribe({
      next: (msg: IMessage) => { // <--- Tipagem aqui também é boa prática
        try {
          const alert = JSON.parse(msg.body) as Alert;
          console.log('🚨 [WebSocket] ALERTA RECEBIDO:', alert);
          this.alertSubject.next(alert);
        } catch (e) {
          console.error('❌ [WebSocket] Erro ao processar mensagem JSON:', e);
        }
      },
      error: (err) => console.error('❌ [WebSocket] Erro na subscrição:', err)
    });
  }

  // --- AQUI ESTAVA O ERRO ---
  watchAlerts(topicUrl: string): Observable<Alert> {
      const destination = topicUrl || '/topic/alert';

      return this.rxStomp.watch(destination).pipe(
        // Adicionamos a tipagem ': IMessage' aqui
        map((message: IMessage) => JSON.parse(message.body) as Alert)
      );
  }

  ngOnDestroy() {
    console.log('🔌 [WebSocket] Encerrando conexão e limpando recursos.');
    this.rxStomp.deactivate(); // Fecha a conexão TCP/WS
  }
}
