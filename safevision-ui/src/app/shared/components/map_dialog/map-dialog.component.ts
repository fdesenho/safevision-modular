import { Component, Inject, AfterViewInit, ChangeDetectionStrategy, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common'; // 👈 Importante para SSR
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';



/**
 * Interface defining the data passed to the Map Modal.
 */
export interface MapDialogData {
  lat: number;
  lng: number;
  label: string;
  timestamp: string;
  address?: string;
}

@Component({
  selector: 'app-map-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './map-dialog.component.html',
  styleUrls: ['./map-dialog.component.scss']
})
export class MapDialogComponent implements AfterViewInit {
  private map: any;

  constructor(
    public dialogRef: MatDialogRef<MapDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MapDialogData,
    @Inject(PLATFORM_ID) private platformId: Object // 👈 Injeção para verificar a plataforma
  ) {}

  ngAfterViewInit(): void {
    // 🛡️ GUARDA SSR: Só executa se estiver no Navegador
    if (isPlatformBrowser(this.platformId)) {
      // Pequeno timeout para garantir que o DOM do modal renderizou
      setTimeout(() => this.initMap(), 100);
    }
  }

  private async initMap(): Promise<void> {
    if (!this.data.lat || !this.data.lng) return;

    // 🚀 IMPORTAÇÃO DINÂMICA
    // O Leaflet só será carregado aqui, dentro do navegador, evitando o erro "window is not defined".
    const L = await import('leaflet');

    // Nota: Como estamos dentro de um modal, precisamos garantir que o container existe
    const mapContainer = document.getElementById('leaflet-map');
    if (!mapContainer) return;

    this.map = L.map('leaflet-map', {
      center: [this.data.lat, this.data.lng],
      zoom: 15
    });

    // Dark Mode Tiles (CartoDB)
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap &copy; CARTO'
    }).addTo(this.map);

    // Visual Circle
    L.circle([this.data.lat, this.data.lng], {
        color: '#d63384',
        fillColor: '#d63384',
        fillOpacity: 0.2,
        radius: 100
    }).addTo(this.map);

    // Precise Marker
    L.circleMarker([this.data.lat, this.data.lng], {
      radius: 8,
      fillColor: '#8282FF',
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 1
    }).addTo(this.map)
    .bindPopup(`<b>${this.data.label}</b><br>${this.data.address || 'Endereço desconhecido'}`)
    .openPopup();
  }

  close() {
    this.dialogRef.close();
  }
}
