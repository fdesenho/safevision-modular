import { Component, inject, OnInit, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http'; // <--- IMPORTANTE
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { switchMap, of, throwError } from 'rxjs'; // <--- IMPORTANTE PARA O FLUXO

// --- SERVIÇOS ---
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { VisionService } from '../../core/services/vision.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { NotificationService } from '../../core/services/notification.service';
import { Alert } from '../../core/models/app.models';
import { environment } from '../../../environments/environment';

// --- MATERIAL UI ---
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { error } from 'console';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  providers: [WebSocketService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  // Injeção de Dependências
  public auth = inject(AuthService); // Public para usar no HTML
  private alertService = inject(AlertService);
  private visionService = inject(VisionService);
  private webSocketService = inject(WebSocketService);
  private notifier = inject(NotificationService);
  private sanitizer = inject(DomSanitizer);
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient); // <--- INJEÇÃO NECESSÁRIA

  // --- SIGNALS (ESTADO) ---
  alerts = signal<Alert[]>([]);

  // Controle de UI
  isLoading = signal(false);      // Trava os botões
  isCameraActive = signal(false); // Indica se o monitoramento está ON/OFF

  // Vídeo
  videoStreamUrl = signal<SafeUrl | null>(null);

  ngOnInit() {
    this.loadAlerts();
    this.initWebSocket();
  }

  // --- WEBSOCKET ---
  private initWebSocket() {
    const user = this.auth.currentUser();

    if (!user?.username) {
      console.warn('Usuário não logado, WebSocket não iniciado.');
      return;
    }

    // Tópico corrigido para o plural (baseado nos logs anteriores)
    const userTopic = `/topic/alert/${user.username}`;
    console.log(`🔌 Conectando WebSocket no tópico: ${userTopic}`);

    this.webSocketService.watchAlerts(userTopic)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (newAlert: Alert) => {
          console.log('🔔 Alerta Recebido:', newAlert);
          this.alerts.update(list => [newAlert, ...list]);
          this.notifier.showError(`Ameaça Detectada: ${newAlert.alertType}`);
        },
        error: (err: any) => console.error('Erro no WebSocket:', err)
      });
  }

  // --- AÇÕES DO SISTEMA (Ligar/Desligar) ---

  toggleSystem(turnOn: boolean) {
    const user = this.auth.currentUser();
    if (!user?.username) return;

    // 1. Trava a UI
    this.isLoading.set(true);

    let request$;

    if (turnOn) {
      // --- FLUXO DE ATIVAR (Busca URL -> Ativa Python) ---
      request$ = this.http.get<{ cameraUrl: string }>(`${environment.apiUrl}/auth/camera-url`).pipe(
        switchMap(res => {
          if (!res.cameraUrl) {
            return throwError(() => new Error('NO_CAMERA_URL'));
          }
          // Agora passamos a URL correta para o serviço de visão
          return this.visionService.startDetection(user.username, res.cameraUrl);
        })
      );
    } else {
      // --- FLUXO DE DESATIVAR (Direto no Python) ---
      request$ = this.visionService.stopDetection(user.username);
    }

    // Executa a requisição preparada acima
    request$.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.isCameraActive.set(turnOn);

        if (turnOn) {
          this.generateVideoUrl(user.username);
          this.notifier.showSuccess('Monitoramento Ativado');
        } else {
          this.videoStreamUrl.set(null);
         
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        if (turnOn) this.isCameraActive.set(false);

        console.error('Erro ao alternar sistema:', err);

        // Tratamento de erro específico
        if (err.message === 'NO_CAMERA_URL') {
          this.notifier.showError('Você não tem uma câmera configurada!');
        } else {
          this.notifier.showError('Falha ao comunicar com a câmera.');
        }
      }
    });
  }

  // --- VÍDEO ---

  private generateVideoUrl(username: string) {
    // Adiciona timestamp para evitar cache do navegador
    const rawUrl = `${environment.visionAgentUrl}/video_feed/${username}?t=${Date.now()}`;
    this.videoStreamUrl.set(this.sanitizer.bypassSecurityTrustUrl(rawUrl));
  }

  onVideoError() {
    if (this.isCameraActive()) {
      // Opcional: Desligar a UI se o vídeo cair
      this.isCameraActive.set(false);
      this.notifier.showError('Sinal de vídeo perdido.');
    }
  }

  // --- ALERTAS ---

  loadAlerts() {
    const user = this.auth.currentUser();
    if (user?.username) {
      this.alertService.getRecentAlerts(user.username).subscribe({
        next: (data) => this.alerts.set(data),
        error: (err) => {
          this.notifier.showError('Erro ao carregar histórico de alertas.');
          console.error('Erro ao carregar histórico de alertas:', err);
        }
      });
    }
  }

  ack(id: string) {
    this.alertService.acknowledge(id).subscribe(() => {
      this.alerts.update(list => list.map(a =>
        a.id === id ? { ...a, acknowledged: true } : a
      ));
    });
  }
}
