import { Component, inject, OnInit, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { switchMap, throwError } from 'rxjs';
import { Router } from '@angular/router'; // 📍 1. IMPORT NECESSÁRIO

// Services
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { VisionService } from '../../core/services/vision.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { NotificationService } from '../../core/services/notification.service';
import { Alert } from '../../core/models/app.models';
import { environment } from '../../../environments/environment';

// Material Components
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MapDialogComponent } from '../../shared/components/map_dialog/map-dialog.component';
import { HeaderComponent } from '../../shared/components/header/header.component'; // 👈 Importar


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
    MatChipsModule,
    MatTooltipModule,
	HeaderComponent
  ],
  providers: [WebSocketService],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  // --- INJEÇÃO DE DEPENDÊNCIAS ---
  public auth = inject(AuthService);
  private alertService = inject(AlertService);
  private visionService = inject(VisionService);
  private webSocketService = inject(WebSocketService);
  private notifier = inject(NotificationService);
  private sanitizer = inject(DomSanitizer);
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient);
  private dialog = inject(MatDialog);
  private router = inject(Router); // 📍 2. INJEÇÃO QUE FALTAVA

  // --- SIGNALS (ESTADO) ---
  alerts = signal<Alert[]>([]);
  isLoading = signal(false);
  isCameraActive = signal(false);
  videoStreamUrl = signal<SafeUrl | null>(null);

  ngOnInit() {
    this.loadAlerts();
    this.initWebSocket();
  }

  private initWebSocket() {
    const user = this.auth.currentUser();
    if (!user?.username) return;

    const userTopic = `/topic/alert/${user.username}`;

    this.webSocketService.watchAlerts(userTopic)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (newAlert: Alert) => {
          this.alerts.update(list => [newAlert, ...list]);
          this.notifier.showError(`Ameaça Detectada: ${newAlert.alertType}`);
        },
        error: (err: any) => console.error('WebSocket Error:', err)
      });
  }

  toggleSystem(turnOn: boolean) {
    const user = this.auth.currentUser();
    if (!user?.username) return;

    this.isLoading.set(true);
    let request$;

    if (turnOn) {
      request$ = this.http.get<{ cameraUrl: string }>(`${environment.apiUrl}/auth/camera-url`).pipe(
        switchMap(res => {
          if (!res.cameraUrl) return throwError(() => new Error('NO_CAMERA_URL'));
          return this.visionService.startDetection(user.username, res.cameraUrl);
        })
      );
    } else {
      request$ = this.visionService.stopDetection(user.username);
    }

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
        this.notifier.showError('Erro ao comunicar com a câmera.');
      }
    });
  }

  private generateVideoUrl(username: string) {
    const rawUrl = `${environment.visionAgentUrl}/video_feed/${username}?t=${Date.now()}`;
    this.videoStreamUrl.set(this.sanitizer.bypassSecurityTrustUrl(rawUrl));
  }

  onVideoError() {
    if (this.isCameraActive()) {
      this.isCameraActive.set(false);
      this.notifier.showError('Sinal de vídeo perdido.');
    }
  }

  loadAlerts() {
    const user = this.auth.currentUser();
    if (user?.username) {
      this.alertService.getRecentAlerts(user.username).subscribe({
        next: (data) => this.alerts.set(data),
        error: (err) => this.notifier.showError('Erro ao carregar histórico.')
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

  openLocation(alert: Alert) {
    if (!alert.latitude || !alert.longitude) {
      this.notifier.showError('GPS indisponível para este evento.');
      return;
    }

    this.dialog.open(MapDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      panelClass: 'glass-dialog',
      data: {
        lat: alert.latitude,
        lng: alert.longitude,
        label: alert.alertType,
        timestamp: alert.createdAt,
        address: alert.address
      }
    });
  }

  // 📍 3. AGORA ESTA FUNÇÃO VAI FUNCIONAR
  goToHistory() {
    this.router.navigate(['/history']);
  }
}
