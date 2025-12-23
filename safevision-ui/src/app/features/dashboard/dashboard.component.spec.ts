import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Alert } from '../../core/models/app.models';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let alertServiceMock: jasmine.SpyObj<AlertService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  const mockAlerts: Alert[] = [
    {
      id: '1',
      alertType: 'WEAPON_DETECTED',
      description: 'Test Alert',
      severity: 'CRITICAL',
      cameraId: 'CAM-01',
      acknowledged: false,
      createdAt: new Date().toISOString()
    }
  ];

  beforeEach(async () => {
    alertServiceMock = jasmine.createSpyObj('AlertService', ['getRecentAlerts', 'acknowledge']);
    authServiceMock = jasmine.createSpyObj('AuthService', ['currentUser'], {
        currentUser: () => ({ username: 'admin', roles: ['ADMIN'] })
    });

    alertServiceMock.getRecentAlerts.and.returnValue(of(mockAlerts));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AlertService, useValue: alertServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load alerts into Signal on init', () => {
    expect(component.alerts().length).toBe(1);
    expect(component.alerts()[0].alertType).toBe('WEAPON_DETECTED');
  });

  it('should render critical alert with correct CSS class', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const alertCard = compiled.querySelector('.alert-item');
    expect(alertCard?.classList).toContain('critical');
  });
});