import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-image-preview-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="preview-container">
      <div class="header-overlay">
        <span class="title">{{ data.title }}</span>
        <button mat-icon-button (click)="close()" class="close-btn">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <div class="image-wrapper">
        <img [src]="data.imageUrl" [alt]="data.title">
      </div>

      <div class="footer-overlay">
        {{ data.date | date:'dd/MM/yyyy HH:mm:ss' }}
      </div>
    </div>
  `,
  styles: [`
    .preview-container {
      position: relative;
      background: #000;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      border-radius: 8px; /* Combina com o Glassmorphism */
    }

    .header-overlay {
      position: absolute;
      top: 0; left: 0; right: 0;
      padding: 12px 16px;
      background: linear-gradient(to bottom, rgba(0,0,0,0.8), transparent);
      display: flex;
      justify-content: space-between;
      align-items: center;
      z-index: 10;
      color: #fff;

      .title { font-weight: 500; letter-spacing: 0.5px; text-shadow: 0 2px 4px rgba(0,0,0,0.8); }
      .close-btn { color: #fff; background: rgba(255,255,255,0.1); }
    }

    .image-wrapper {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px; /* Margem para a imagem não colar na borda */

      img {
        max-width: 100%;
        max-height: 85vh; /* Garante que caiba na tela sem scroll */
        object-fit: contain; /* MANTÉM PROPORÇÃO E RESOLUÇÃO */
        box-shadow: 0 10px 40px rgba(0,0,0,0.5);
        border-radius: 4px;
      }
    }

    .footer-overlay {
      position: absolute;
      bottom: 0; left: 0; right: 0;
      padding: 12px;
      text-align: center;
      color: rgba(255,255,255,0.7);
      background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
      font-size: 0.85rem;
    }
  `]
})
export class ImagePreviewDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ImagePreviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { imageUrl: string; title: string; date: string }
  ) {}

  close(): void {
    this.dialogRef.close();
  }
}
