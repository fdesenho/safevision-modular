import { Component, inject, OnInit, DestroyRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { timer } from 'rxjs';

import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { VisionService } from '../../core/services/vision.service';
import { WebSocketService } from '../../core/services/websocket.service'; // <--- IMPORTANTE
import { Alert } from '../../core/models/app.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  // Injeções
  public auth = inject(AuthService);
  private alertService = inject(AlertService);
  private visionService = inject(VisionService);
  private webSocketService = inject(WebSocketService); // <--- INJEÇÃO DO WS
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  // Signals para Estado
  alerts = signal<Alert[]>([]);
  isSystemArmed = signal(false);

  // Estado do Vídeo
  videoSrc = signal<string | null>(null);
  isVideoLoading = signal(true);
  isVideoError = signal(false);

  // Formulário de Configuração
  configForm = this.fb.group({
    cameraUrl: [''],
    phone: ['']
  });

  ngOnInit() {
    // 1. Carga Inicial (HTTP GET) - Pega o histórico ao abrir a tela
    this.loadAlerts();

    // 2. Inscrição no WebSocket (PUSH) - Substitui o Polling
    // Fica ouvindo novos alertas em tempo real
    this.webSocketService.watchAlerts()
      .pipe(takeUntilDestroyed(this.destroyRef)) // Garante que desconecta ao sair
      .subscribe((newAlert) => {
        console.log('🔔 Alerta WebSocket Recebido:', newAlert);

        // Atualiza a lista adicionando o novo item no topo
        this.alerts.update(currentList => [newAlert, ...currentList]);
      });

    // 3. Inicia vídeo com delay
    setTimeout(() => {
      this.videoSrc.set('http://localhost:5000/video_feed');
    }, 500);
  }

  // --- AÇÕES ---

  // Busca o histórico completo (usado no init e no botão de refresh manual)
  loadAlerts() {
    this.alertService.getAlerts().subscribe({
      next: (data) => this.alerts.set(data),
      error: (err) => console.error('Erro ao carregar histórico:', err)
    });
  }

  // Método público para o botão de recarregar no HTML
  forceRefresh() {
    this.loadAlerts();
  }

  ack(id: string) {
    // Ao marcar como lido, atualizamos a lista visualmente ou recarregamos
    this.alertService.acknowledge(id).subscribe(() => {
        // Opção A: Recarregar tudo do backend
        //this.loadAlerts();

        // Opção B (Mais rápida): Atualizar localmente sem ir ao backend

        this.alerts.update(list => list.map(a =>
            a.id === id ? { ...a, acknowledged: true } : a
        ));

    });
  }

  toggleSystem() {
    const user = this.auth.currentUser()?.username;

    if (this.isSystemArmed()) {
      this.visionService.deactivateProtection().subscribe({
        next: () => this.isSystemArmed.set(false),
        error: () => alert('Erro ao desativar.')
      });
    } else {
      if (user) {
        this.visionService.activateProtection(user).subscribe({
          next: () => this.isSystemArmed.set(true),
          error: () => alert('Erro ao ativar.')
        });
      }
    }
  }

  saveConfig() {
    const { cameraUrl, phone } = this.configForm.value;
    if (!cameraUrl && !phone) return;

    this.auth.updateUser({
      cameraConnectionUrl: cameraUrl || undefined,
      phoneNumber: phone || undefined
    }).subscribe({
      next: () => {
        alert('Salvo! Reiniciando vídeo...');
        this.reloadVideo();
        this.configForm.reset();
      },
      error: () => alert('Erro ao salvar.')
    });
  }

  // --- CONTROLE DE VÍDEO ---

  onVideoLoad() {
    this.isVideoLoading.set(false);
    this.isVideoError.set(false);
  }

  onVideoError() {
    this.isVideoLoading.set(false);
    if (this.videoSrc()) {
      this.isVideoError.set(true);
    }
  }

  reloadVideo() {
    this.isVideoError.set(false);
    this.isVideoLoading.set(true);
    this.videoSrc.set(null);

    timer(200).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.videoSrc.set(`http://localhost:5000/video_feed?t=${Date.now()}`);
    });
  }

  retryVideo() {
    this.reloadVideo();
  }
}
