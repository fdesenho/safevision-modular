import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { isPlatformBrowser } from '@angular/common'; // <--- Importante
import { AuthResponse, LoginRequest, RegisterRequest, User, UserUpdateRequest } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID); // <--- Identifica se é Navegador ou Servidor

  private readonly API_URL = 'http://localhost:8080/auth';
  private readonly TOKEN_KEY = 'safevision_token';

  private userSignal = signal<User | null>(null);

  public currentUser = this.userSignal.asReadonly();
  public isLoggedIn = computed(() => !!this.userSignal());

  constructor() {
    // CORREÇÃO: Só carrega do storage se estiver no navegador
    if (isPlatformBrowser(this.platformId)) {
      this.loadUserFromStorage();
    }
  }

  login(credentials: LoginRequest) {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(res => this.saveSession(res.token))
    );
  }

  register(data: RegisterRequest) {
    return this.http.post(`${this.API_URL}/register`, data);
  }

  updateUser(data: UserUpdateRequest) {
    return this.http.put<User>(`${this.API_URL}/update`, data).pipe(
      tap(updatedUser => {
        this.userSignal.update(current => current ? { ...current, ...updatedUser } : null);
      })
    );
  }

  logout() {
    // Proteção no Logout
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }

  // Método público seguro para pegar o token
  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  private saveSession(token: string) {
    // Proteção ao Salvar
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
    this.decodeToken(token);
  }

  private loadUserFromStorage() {
    // Reutiliza o método getToken que já é protegido
    const token = this.getToken();
    if (token) this.decodeToken(token);
  }

  private decodeToken(token: string) {
    try {
      const decoded: any = jwtDecode(token);
      this.userSignal.set({
        id: decoded.id,
        username: decoded.sub,
        roles: decoded.roles
      } as User);
    } catch {
      this.logout();
    }
  }
}
