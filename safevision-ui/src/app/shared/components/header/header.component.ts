import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

// Material Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

// Components & Services
import { AuthService } from '../../../core/services/auth.service';
import { UserProfileDialogComponent } from '../../../features/user-profile-dialog/user-profile-dialog.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  public auth = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  goHome() {
    this.router.navigate(['/dashboard']);
  }

  openProfileSettings() {
    // 1. Guarda a referência do Dialog aberto
    const dialogRef = this.dialog.open(UserProfileDialogComponent, {
      width: '500px',
      panelClass: 'glass-dialog',
      disableClose: true, // Obriga usar botão de ação
      autoFocus: false
    });


    dialogRef.afterClosed().subscribe(result => {
      window.location.reload();

    });
  }
}
