import { Component, AfterViewInit, ViewChild, inject, DestroyRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { merge, of } from 'rxjs';
import { startWith, switchMap, catchError, map, debounceTime } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

// Material Imports
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';

// Custom Imports
import { HeaderComponent } from '../../shared/components/header/header.component';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { Alert } from '../../core/models/app.models';
import { MapDialogComponent } from '../../shared/components/map_dialog/map-dialog.component';
import { ImagePreviewDialogComponent } from '../../shared/components/image-preview-dialog/image-preview-dialog.component';
 // Verifique se a pasta é map-dialog ou map_dialog

@Component({
  selector: 'app-alert-history',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './alert-history.component.html',
  styleUrls: ['./alert-history.component.scss']
})
export class AlertHistoryComponent implements AfterViewInit {

  private alertService = inject(AlertService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private cdr = inject(ChangeDetectorRef);

  // ESTA ORDEM DEVE SER EXATAMENTE A MESMA DO HTML
  displayedColumns: string[] = [
    'createdAt',
    'severity',
    'alertType',
    'description',
    'cameraId',
    'address',
    'snapshotUrl',
    'acknowledged'
  ];

  dataSource: Alert[] = [];
  resultsLength = 0;
  isLoading = true;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngAfterViewInit() {
      // Reseta paginação ao ordenar
      this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);

      merge(this.sort.sortChange, this.paginator.page)
        .pipe(
          startWith({}),
          debounceTime(300), // Evita o erro AbortError e múltiplas chamadas
          switchMap(() => {
            this.isLoading = true;
            this.cdr.detectChanges();

            const user = this.auth.currentUser();

            // ⚠️ CORREÇÃO CRÍTICA: Se seus alertas no banco têm userId="safe",
            // você TEM que buscar por username, não ID.
            // Se quiser usar ID, terá que apagar o banco e recriar os alertas usando o UUID.
            const identifier = user?.username; // Mudado de user?.id para user?.username para teste

            if (!identifier) return of(null);

            return this.alertService.getAlertHistory(
              identifier,
              this.paginator.pageIndex,
              this.paginator.pageSize,
              this.sort.active,
              this.sort.direction
            ).pipe(catchError((err) => {
                console.error('Erro ao buscar histórico:', err);
                return of(null);
            }));
          }),
          map(data => {
            this.isLoading = false;
            if (data === null) return [];

            // 🛡️ BLINDAGEM DE JSON: Aceita os dois formatos (Novo e Antigo)
            // Se data.page existir (Novo Spring Boot), usa ele.
            // Se não, tenta pegar totalElements da raiz (Antigo).
            if (data.page) {
                this.resultsLength = data.page.totalElements;
            } else if ('totalElements' in data) {
                // @ts-ignore
                this.resultsLength = data.totalElements;
            } else {
                this.resultsLength = 0;
            }

            return data.content || [];
          }),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe(data => {
          this.dataSource = data;
          this.cdr.detectChanges();
        });
    }

  openMap(alert: Alert) {
    if (!alert.latitude || !alert.longitude) return;

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

  goBack() {
    this.router.navigate(['/dashboard']);
  }
openImage(alert: Alert) {
    if (!alert.snapshotUrl) return;

    this.dialog.open(ImagePreviewDialogComponent, {
      maxWidth: '95vw',    // Ocupa quase toda a largura
      maxHeight: '95vh',   // Ocupa quase toda a altura
      height: 'auto',
      width: 'auto',
      panelClass: 'fullscreen-dialog', // Classe opcional para remover paddings padrões do Material
      backdropClass: 'blur-backdrop',  // Efeito de blur no fundo
      data: {
        imageUrl: alert.snapshotUrl,
        title: ` `,
        date: alert.createdAt
      }
    });
  }
  markAsRead(alert: Alert, event: Event) {
      // Evita que o clique propague (caso tenhamos clique na linha no futuro)
      event.stopPropagation();

      // Se já estiver lido, não faz nada
      if (alert.acknowledged) return;

      // Chama o backend
      this.alertService.acknowledge(alert.id).subscribe({
        next: () => {
          // Atualiza o estado LOCALMENTE para feedback instantâneo
          alert.acknowledged = true;
          
          // Força o Angular a redesenhar a tabela para mostrar o ícone verde
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Erro ao marcar como lido', err)
      });
    }

}
